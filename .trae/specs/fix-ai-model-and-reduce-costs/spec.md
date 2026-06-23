# 修复 AI 模型配置 & 减少调用开销 Spec

## Why
`deepseek-v4-pro` 不是 DeepSeek API 的有效模型名，导致所有 AI 调用返回 `choices` 为空，`callAI()` 抛出 "Empty AI response"。同时每次智能匹配（`MatchingService.match()`）触发多达 3 次 AI 调用（画像提取、图谱生成、AI 重排序），token 消耗过高。

## What Changes
- 修复模型名：`deepseek-v4-pro` → `deepseek-chat`（根因修复）
- 合并 AI 调用：`extractMatchingProfile()` + `extractGraphData()` → 单次调用同时返回画像和图谱
- 用规则替代部分 AI 调用：图谱生成改用确定性规则（非关键路径，不需要 LLM）
- 增强错误处理：`callAI()` 响应为空时记录完整的 HTTP 响应体到日志，便于排查
- Agent 编排支持快速模型：`MatchAgentOrchestrator` 的画像提取可用 flash/lite 模型（通过可选配置切换）

## Impact
- Affected specs: cloud-bridge-mvp, integrate-langchain4j-agent, strict-matching-ai-integration
- Affected code: `AIService.java`, `MatchingService.java`, `MatchAgentOrchestrator.java`, `application.yml`

## ADDED Requirements

### Requirement: AI 模型名修正
系统 SHALL 使用 DeepSeek API 的有效模型名 `deepseek-chat` 作为默认值。

#### Scenario: AI 调用正常返回
- **WHEN** 用户发起智能匹配或 AI 对话
- **THEN** AI API 返回正常的 `choices` 数组，不再出现 "Empty AI response"

### Requirement: 合并画像+图谱为单次 AI 调用
系统 SHALL 在一次 AI 调用中同时返回匹配画像（MatchingProfile）和知识图谱（graph JSON），替代原来分两次调用的方式。

#### Scenario: 智能匹配触发 AI
- **WHEN** `MatchingService.match()` 被调用
- **THEN** 仅发起 1 次 AI 调用获取画像+图谱，而非原来的 2 次
- **AND** 返回的 JSON 包含 `profile` 和 `graph` 两个字段

### Requirement: 图谱生成降级为规则引擎
系统 SHALL 使用 `MatchAgentOrchestrator` 内置的确定性规则替代 `extractGraphData()` 的 AI 调用。图谱节点由领域层级（`DomainHierarchyUtil`）和关键词匹配结果构成，不依赖 LLM。

#### Scenario: 生成知识图谱
- **WHEN** 需要构建图谱展示
- **THEN** 使用领域层级规则 + 候选成果构建节点和关系
- **AND** 不触发额外的 AI API 调用

### Requirement: callAI 增强错误日志
系统 SHALL 在 `callAI()` 返回空响应时（`choices` 为空或响应体为空），将完整的 HTTP 响应原始内容打印到日志。

#### Scenario: AI 返回异常响应
- **WHEN** API 返回 HTTP 200 但 `choices` 为空
- **THEN** 日志输出完整响应体内容
- **AND** 抛出包含更多上下文的异常信息

### Requirement: Agent 编排支持快速模型
系统 SHALL 允许通过配置 `ai.agent.model` 为 Agent 编排指定独立的模型（如 flash/lite 模型），与通用 AI 调用使用的 `ai.api.model` 解耦。如果不配置则回退使用 `ai.api.model`。

#### Scenario: 配置 Agent 专用模型
- **WHEN** `application.yml` 中配置了 `ai.agent.model: deepseek-chat`
- **THEN** `MatchAgentOrchestrator` 的画像提取使用该模型
- **AND** 其他通用 AI 调用（如 `chatWithIntent`）仍使用 `ai.api.model`

## MODIFIED Requirements

### Requirement: MatchingService.match() AI 调用次数
匹配流程的 AI 调用从原来的 3 次（`extractMatchingProfile` + `extractGraphData` + `evaluateMatches`）减少到 2 次（合并后的画像+图谱调用 + `evaluateMatches`）。其中图谱生成不再依赖 AI。

## REMOVED Requirements
（无）
