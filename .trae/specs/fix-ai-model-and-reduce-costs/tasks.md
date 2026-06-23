# Tasks

- [x] Task 1: 修正默认模型名 & 增强错误日志
  - [x] SubTask 1.1: `application.yml` 中 `ai.api.model` 默认值从 `deepseek-v4-pro` 改为 `deepseek-chat`
  - [x] SubTask 1.2: `AIService.callAI()` 中响应体为空或 choices 为空时，打印完整 HTTP 响应原始内容到日志（使用 `System.err.println`），并抛出带上下文的异常
  - [x] SubTask 1.3: 同样修改 `deploy/docker-compose.yml` 中 `AI_API_MODEL` 默认值（如有）

- [x] Task 2: 合并画像提取和图谱生成为单次 AI 调用
  - [x] SubTask 2.1: `AIService` 新增 `extractProfileAndGraph(String userDescription)` 方法，prompt 要求 AI 同时返回 `profile` (MatchingProfile 字段) 和 `graph` (图谱 JSON)，temperature 0.2
  - [x] SubTask 2.2: 方法返回内部类/记录 `ProfileAndGraph { MatchingProfile profile; JsonNode graph; }`
  - [x] SubTask 2.3: 修改 `MatchingService.match()` 第 171-189 行，用 `extractProfileAndGraph()` 替代 `extractMatchingProfile()` + `extractGraphData()`
  - [x] SubTask 2.4: 保留原 `extractMatchingProfile()` 和 `extractGraphData()` 方法不动（`MatchAgentOrchestrator` 仍使用前者），避免破坏其他调用方

- [x] Task 3: 图谱生成降级为规则引擎
  - [x] SubTask 3.1: 在 `MatchingService` 中新增 `buildRuleBasedGraph(MatchingProfile profile, String keyword)` 方法，基于 `DomainHierarchyUtil.DOMAIN_HIERARCHY` 和候选成果构建图谱节点和关系
  - [x] SubTask 3.2: `augmentGraphWithAchievements()` 不再依赖 AI 生成的 `graphJson`，改为接收 `MatchingProfile` 参数自行构建基础图谱
  - [x] SubTask 3.3: `match()` 方法流程调整为：先调合并后的 AI 获取 profile → 用规则构建图谱 → 搜索 → 评分

- [x] Task 4: Agent 编排支持独立模型配置
  - [x] SubTask 4.1: `application.yml` 的 `ai.agent` 节点下新增 `model` 配置项，默认值 `deepseek-chat`（Agent 编排专用）
  - [x] SubTask 4.2: `AIService` 中注入 `@Value("${ai.agent.model:#{null}}")`，在 `extractMatchingProfile()` 中如果 agentModel 不为空则覆写 request.model
  - [x] SubTask 4.3: 更新测试配置 `backend/src/test/resources/application.yml` 同步新增 `ai.agent.model`

# Task Dependencies
- Task 1 无依赖，可优先执行
- Task 2 依赖 Task 1（需要 AI 先能正常调用才能验证合并效果）
- Task 3 依赖 Task 2（图谱改规则引擎基于合并后的架构调整）
- Task 4 无依赖，可与 Task 1 并行
