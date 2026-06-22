# Tasks

- [ ] Task 1: 后端工具调用链日志记录
  - [ ] SubTask 1.1: 创建 `ToolChainLogger.java`，提供 `logToolStart(name, params)` 和 `logToolEnd(name, durationMs, result)` 方法
  - [ ] SubTask 1.2: 在 `MatchAgentCoreTools` 的三个 `@Tool` 方法中嵌入日志调用
  - [ ] SubTask 1.3: 在 `AIController` 的 `agentChat` 方法中记录 Agent 整体耗时
  - **验证**: 发送测试请求，后端日志输出完整 `[AGENT_TOOLCHAIN]` 链路

- [ ] Task 2: 设计三层推导测试用例并执行
  - [ ] SubTask 2.1: 编写测试脚本 `test_toolchain.ps1`，输入为"耐高温1300℃镍基单晶合金，用于航空发动机单晶叶片，要求抗氧化抗蠕变"
  - [ ] SubTask 2.2: 执行测试，读取后端日志确认工具调用链
  - **验证**: 日志中至少包含 `extractMatchingProfile` + `searchResources` + `searchCandidateAchievements` 三个工具调用记录

- [ ] Task 3: 前端加载状态组件
  - [ ] SubTask 3.1: 创建 `WaitingBubble` 组件，根据等待时长展示分阶段提示文案
  - [ ] SubTask 3.2: 在 `ChatPanel.jsx` 中集成 `WaitingBubble`，Agent 响应中显示
  - [ ] SubTask 3.3: Agent 响应开始输出 token 时自动隐藏 `WaitingBubble`
  - **验证**: 发送消息后看到加载提示，token 开始后提示消失

# Task Dependencies
- Task 1 和 Task 3 可并行执行
- Task 2 依赖 Task 1（需要日志记录就绪后才能验证调用链）
