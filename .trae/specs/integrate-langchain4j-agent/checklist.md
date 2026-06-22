# Checklist

- [ ] `pom.xml` 包含 `langchain4j` 和 `langchain4j-open-ai` 依赖，`mvn compile` 通过
- [ ] `application.yml` 包含 Agent 相关配置项（provider、model、max-tokens、temperature、max-iterations）
- [ ] `MatchAgentTools` 类包含 7 个 `@Tool` 方法（searchAchievements、searchPolicies、searchFunds、searchExperts、searchEquipments、extractMatchingProfile、evaluateMatches）
- [ ] 每个 `@Tool` 方法包含清晰的 `value` 描述字符串
- [ ] `MatchAgent` 接口包含 `@SystemMessage` 角色定义，返回 `TokenStream` 或 `String`
- [ ] `MatchAgentService` 正确初始化 ChatLanguageModel + AiServices + ChatMemoryProvider
- [ ] 会话记忆按 sessionId 隔离，不同 session 互不干扰
- [ ] `POST /api/ai/agent/chat` 端点可接受 `{ "sessionId": "...", "message": "..." }` 并返回 SSE 流
- [ ] 模糊输入"我想要一个新材料"触发 Agent 追问而不是直接返回匹配结果
- [ ] 明确输入"耐高温陶瓷基复合材料用于航空发动机叶片"触发 Agent 调用 searchAchievements 等工具
- [ ] Agent 返回结果包含成果+政策+资金+专家+设备的多模块信息（如果相关）
- [ ] 工具调用失败时 Agent 跳过失败工具并继续其余编排
- [ ] 前端 SSE 流式渲染正常，中间状态指示器工作
- [ ] 前端支持 Markdown 消息渲染
- [ ] 前端支持结构化卡片展示（匹配成果、政策、资金等分区）
- [ ] 前端"重新开始"按钮可重置会话
- [ ] 用户修正需求（如从"耐高温"切换到"低温超导"）后 Agent 重新匹配
