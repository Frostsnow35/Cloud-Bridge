# Tasks

- [ ] Task 1: 引入 LangChain4j 依赖与配置
  - [ ] 在 `pom.xml` 中添加 `langchain4j`、`langchain4j-open-ai` 依赖
  - [ ] 在 `application.yml` 中添加 Agent 相关配置（LLM provider、超时、最大迭代轮数）
  - [ ] 验证项目编译通过

- [ ] Task 2: 实现 Agent 工具集 MatchAgentTools
  - [ ] 新建 `com.cloudbridge.service.agent.MatchAgentTools` 类
  - [ ] 实现 `@Tool("searchAchievements")` 方法，复用 SearchService 的混合检索
  - [ ] 实现 `@Tool("searchPolicies")` 方法，检索政策资源
  - [ ] 实现 `@Tool("searchFunds")` 方法，检索科技金融资源
  - [ ] 实现 `@Tool("searchExperts")` 方法，检索专家资源
  - [ ] 实现 `@Tool("searchEquipments")` 方法，检索设备资源
  - [ ] 实现 `@Tool("extractMatchingProfile")` 方法，复用 AIService 的画像提取
  - [ ] 每个 @Tool 方法添加描述字符串，指导 LLM 何时调用

- [ ] Task 3: 实现 Agent 编排服务 MatchAgent
  - [ ] 新建 `com.cloudbridge.service.agent.MatchAgent` 接口（LangChain4j AiServices）
  - [ ] 使用 `@SystemMessage` 定义 Agent 系统角色与行为约束
  - [ ] 新建 `com.cloudbridge.service.agent.MatchAgentService` 类
  - [ ] 实现 Agent 初始化逻辑（构建 ChatLanguageModel + AiServices）
  - [ ] 实现会话记忆管理（ChatMemory），按 sessionId 隔离
  - [ ] 实现 `chat(sessionId, userMessage)` 方法

- [ ] Task 4: 新增 Agent 对话 API（流式 SSE）
  - [ ] 在 `AIController.java` 中新增 `POST /api/ai/agent/chat` 端点
  - [ ] 传递 sessionId 用于记忆关联
  - [ ] 实现 `SseEmitter` 流式响应，支持逐 token 推送

- [ ] Task 5: 升级前端 AICopilot.vue 为对话式 Agent
  - [ ] 重构消息展示：支持 Markdown 渲染 + 结构化卡片（匹配成果、政策、资金等）
  - [ ] 实现 SSE 流式接收，解析 `application/x-ndjson` 或 SSE 事件流
  - [ ] 增加状态指示器（"思考中…""正在查询匹配成果…""正在检索政策…"）
  - [ ] 增加对话结束后的解决方案卡片展示
  - [ ] 增加"重新开始"按钮，重置会话记忆

# Task Dependencies
- [Task 2] 依赖 [Task 1]
- [Task 3] 依赖 [Task 2]
- [Task 4] 依赖 [Task 3]
- [Task 5] 依赖 [Task 4]
