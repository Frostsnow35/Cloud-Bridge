# Checklist

- [x] `pom.xml` 包含 `langchain4j` 和 `langchain4j-open-ai` 依赖，`mvn compile` 通过
- [x] `application.yml` 包含 Agent 相关配置项（provider、model、max-tokens、temperature、max-iterations）
- [x] `MatchAgentTools` 类包含 7 个 `@Tool` 方法（searchAchievements、searchPolicies、searchFunds、searchExperts、searchEquipments、extractMatchingProfile、searchCandidateAchievements）
- [x] 每个 `@Tool` 方法包含清晰的 `value` 描述字符串
- [x] `MatchAgent` 接口包含 `@SystemMessage` 角色定义，返回 `TokenStream`
- [x] `MatchAgentService` 正确初始化 ChatLanguageModel + AiServices + ChatMemoryProvider
- [x] 会话记忆按 sessionId 隔离，不同 session 互不干扰
- [x] `POST /api/ai/agent/chat` 端点可接受 `{ "sessionId": "...", "message": "..." }` 并返回 SSE 流
- [x] `POST /api/ai/agent/reset` 端点可重置指定会话
- [x] 前端 SSE 流式接收实现完成（ReadableStream + Event 解析）
- [x] 前端支持简易 Markdown 渲染（粗体、标题、列表）
- [x] 前端状态指示器实现（思考中…/生成中…/已连接）
- [x] 前端"重新开始"按钮可重置会话
- [ ] 模糊输入"我想要一个新材料"触发 Agent 追问而不是直接返回匹配结果（需运行验证）
- [ ] 明确输入"耐高温陶瓷基复合材料用于航空发动机叶片"触发 Agent 调用 searchAchievements 等工具（需运行验证）
- [ ] Agent 返回结果包含成果+政策+资金+专家+设备的多模块信息（需运行验证）
- [ ] 工具调用失败时 Agent 跳过失败工具并继续其余编排（需运行验证）
- [ ] 用户修正需求（如从"耐高温"切换到"低温超导"）后 Agent 重新匹配（需运行验证）
