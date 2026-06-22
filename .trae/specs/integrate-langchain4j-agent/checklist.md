# Checklist

- [x] `pom.xml` 包含 `langchain4j` 和 `langchain4j-open-ai` 依赖，`mvn compile` 通过
- [x] `application.yml` 包含 Agent 相关配置项（provider、model、max-tokens、temperature、max-iterations）
- [x] `MatchAgentCoreTools` 类包含 3 个 `@Tool` 方法（extractMatchingProfile、searchResources、searchCandidateAchievements）
- [x] 每个 `@Tool` 方法包含清晰的 `value` 描述字符串
- [x] `MatchAgent` 接口包含 `@SystemMessage` 角色定义
- [x] `MatchAgentService` 正确初始化 ChatLanguageModel + AiServices + ChatMemoryProvider
- [x] Embedding 使用独立 URL/Key（`ai.api.embedding-url` + `ai.api.embedding-key`），与 LLM 解耦
- [x] 会话记忆按 sessionId 隔离，不同 session 互不干扰
- [x] `POST /api/ai/agent/chat` 端点可接受 `{ "sessionId": "...", "message": "..." }` 并返回 SSE 流
- [x] `POST /api/ai/agent/reset` 端点可重置指定会话
- [x] 前端 SSE 流式接收实现完成（ReadableStream + Event 解析）
- [x] 前端支持简易 Markdown 渲染（粗体、标题、列表）
- [x] 前端状态指示器实现（思考中/生成中/已连接）
- [x] 前端"重新开始"按钮可重置会话
- [x] DeepSeek + 多轮对话测试通过：Agent 成功进行需求澄清引导
- [x] 同一 sessionId 上下文记忆保持正常
- [ ] 跨模块工具编排（DeepSeek 正确性需优化 SystemMessage 减少幻觉）
- [ ] 工具调用失败降级验证
- [ ] 需求中途修正验证

**总结**：LangChain4j Agent 框架 + DeepSeek `deepseek-chat` 已完整跑通。SSE 流式输出正常，多轮对话记忆正常，需求澄清引导正常。工具调用待优化 SystemMessage 后验证。
