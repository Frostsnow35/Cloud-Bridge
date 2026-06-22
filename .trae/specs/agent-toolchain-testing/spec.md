# Agent 工具链测试与前端友好化 Spec

## Why
DeepSeek Agent 已跑通但无法验证 Agent 是否真实调用了工具。需要在后端记录完整工具调用链，同时前端黑盒化等待过程、展示友好提示。

## What Changes
- 后端新增 `ToolCallLogger`，结构化记录每次工具调用的名称、参数、耗时、结果摘要
- 优化 `MatchAgentService`，在调用前后插入日志点
- 前端新增加载状态组件：根据等待时长分阶段展示"正在分析需求…"→"正在检索成果…"→"正在生成方案…"
- 前端移除 agent_process_display 中暴露工具调用的原始输出（如有）

## Impact
- Affected specs: `integrate-langchain4j-agent`
- Affected code:
  - `cloud-bridge/backend/.../service/agent/MatchAgentService.java`
  - `cloud-bridge/backend/.../service/agent/MatchAgentCoreTools.java`
  - `cloud-bridge/backend/.../controller/AIController.java`
  - `cloud-bridge/frontend/src/components/ChatPanel.jsx`

## ADDED Requirements
### Requirement: Agent 工具调用链记录
系统 SHALL 在后端日志中以结构化格式记录每次工具调用，包含：工具名、输入参数、调用时间戳、耗时、结果摘要。

#### Scenario: 三层推导完整调用链
- **WHEN** 用户输入明确需求（如"耐高温1300℃镍基单晶合金，航空发动机单晶叶片"）
- **THEN** 日志输出包含 `[AGENT_TOOLCHAIN]` 标记的完整链路：
  1. `extractMatchingProfile` — 入参为用户原话，出参为关键词/领域/场景
  2. `searchResources(type=achievements)` — 入参为画像关键词
  3. `searchCandidateAchievements` — 入参为画像关键词，出参为候选列表

### Requirement: 前端友好加载提示
系统 SHALL 在用户发送消息后展示分阶段加载提示，不暴露内部工具调用细节。

#### Scenario: 用户等待时看到友好提示
- **WHEN** 用户发送消息等待 Agent 响应
- **THEN** 前端展示带 typing 动画的提示文字：
  - 0-3秒：显示"正在分析您的需求…"
  - 3-8秒：显示"正在搜索匹配的科技成果…"  
  - 8秒+：显示"正在生成转化方案…"
