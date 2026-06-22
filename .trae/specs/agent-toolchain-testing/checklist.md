# Checklist

- [ ] `ToolChainLogger.java` 存在且包含 `logToolStart` 和 `logToolEnd` 方法
- [ ] `MatchAgentCoreTools` 三个 `@Tool` 方法均已嵌入日志
- [ ] `AIController.agentChat` 记录 Agent 整体耗时
- [ ] 后端日志输出 `[AGENT_TOOLCHAIN]` 格式完整（工具名、参数、耗时、结果摘要）
- [ ] 测试脚本 `test_toolchain.ps1` 存在且可执行
- [ ] 执行测试后日志包含 `extractMatchingProfile` 调用记录
- [ ] 执行测试后日志包含 `searchResources` 调用记录
- [ ] 执行测试后日志包含 `searchCandidateAchievements` 调用记录
- [ ] `WaitingBubble` 组件存在，包含分阶段文案
- [ ] `ChatPanel.jsx` 集成 `WaitingBubble`
- [ ] 发送消息后前端展示加载提示
- [ ] Token 开始后加载提示消失
- [ ] 前端不暴露任何工具调用细节（tool_call 等原始数据不可见）
