# 集成 LangChain4j Agent 强化供需对接 Spec

## Why
当前"供需对接"模块采用单次"输入→检索→返回"流水线，缺乏多轮需求澄清能力与跨模块智能编排能力。引入 LangChain4j Agent 框架，赋予平台自主推理、工具调用、多步编排的能力，将匹配从被动检索升级为主动代理。

## What Changes
- 新增 `langchain4j` 依赖到 `pom.xml`
- 新建 Agent 层：`MatchAgent` 接口（LangChain4j AiServices 声明式）
- 定义 Agent 工具集：`@Tool` 注解封装现有服务能力
- 升级 `AICopilot.vue`：支持多轮对话流程与解决方案卡片展示
- 新增 Agent 对话 API：`POST /api/ai/agent/chat`（流式 SSE）
- **BREAKING**: 无（全部为增量变更，不破坏现有接口）

## Impact
- Affected specs: 无（全新功能）
- Affected code: `pom.xml`, `AIController.java`, `AIService.java`, `SearchService.java`, `MatchingService.java`（仅添加工具注解），新增 `MatchAgent.java`, `MatchAgentService.java`, `MatchAgentTools.java`，`AICopilot.vue`
- New dependency: `dev.langchain4j:langchain4j`, `dev.langchain4j:langchain4j-open-ai`

## ADDED Requirements

### Requirement 1: Agent 需求澄清引导
系统 SHALL 在用户输入模糊需求时，通过多轮对话引导用户补充技术参数、应用场景、预算等关键信息，然后启动精准匹配。

#### Scenario: 模糊需求逐步澄清
- **GIVEN** 用户输入"我想要一个新材料"
- **WHEN** Agent 收到该消息
- **THEN** Agent 回应追问（如"请问您期望新材料应用于哪个领域？航空航天、新能源还是医疗器械？"）
- **AND** 用户补充"航空航天"
- **THEN** Agent 继续追问（如"具体需要什么性能？耐高温、轻量化还是导电性？"）
- **AND** 用户补充"耐高温300度以上"
- **THEN** Agent 调用匹配工具，返回相关成果列表

### Requirement 2: Agent 跨模块智能编排
系统 SHALL 在完成需求澄清后，自动编排多模块资源（成果匹配、政策补贴、资金渠道、共享设备、相关专家），生成综合解决方案。

#### Scenario: 全链路解决方案生成
- **GIVEN** 用户已明确需求"耐高温陶瓷基复合材料用于航空发动机叶片"
- **WHEN** Agent 完成需求澄清
- **THEN** Agent 依次调用：匹配服务（找成果）→ 搜索服务（找政策/资金/设备/专家）→ 生成综合报告
- **AND** 前端以卡片分栏形式展示"匹配成果""政策支持""资金渠道""推荐专家""共享设备"

#### Scenario: 工具调用失败降级
- **GIVEN** Agent 编排过程中某个工具调用失败（如 ES 不可用）
- **WHEN** Agent 收到工具调用异常
- **THEN** Agent 跳过该工具，继续其余编排步骤
- **AND** 在最终报告中标注"该模块暂不可用"

### Requirement 3: Agent 对话流式响应
系统 SHALL 通过 Server-Sent Events (SSE) 向前端推送 Agent 的推理过程，包括"思考中""正在查询匹配成果""正在检索政策补贴"等中间状态。

#### Scenario: 实时推理过程可见
- **GIVEN** 用户发送需求
- **WHEN** Agent 开始推理
- **THEN** 前端逐条展示中间状态（如"正在分析您的需求...""识别到核心关键词: 陶瓷基复合材料""已匹配到3项相关成果"）
- **AND** 最终结果以结构化卡片展示

### Requirement 4: Agent 对话记忆
系统 SHALL 在单次对话会话中保持上下文记忆，支持用户在中途修正需求或切换话题。

#### Scenario: 需求中途修正
- **GIVEN** 用户已告知需求"耐高温材料"
- **AND** Agent 已返回匹配结果
- **WHEN** 用户说"不对，我要的是低温超导材料"
- **THEN** Agent 识别需求变更，重新启动匹配流程
- **AND** Agent 回应"好的，已根据'低温超导材料'重新为您匹配"

### Requirement 5: 后端 Agent 编排引擎
系统 SHALL 基于 LangChain4j AiServices 实现声明式 Agent 接口，具备以下工具方法（Tool）：
- `searchAchievements`: 混合检索科技成果（复用 SearchService）
- `searchPolicies`: 检索相关产业政策
- `searchFunds`: 检索科技金融产品
- `searchExperts`: 检索领域专家
- `searchEquipments`: 检索共享设备
- `extractMatchingProfile`: AI 提取需求画像
- `evaluateMatches`: AI 对候选成果评分

#### Scenario: Agent 自主决策工具调用顺序
- **GIVEN** Agent 收到需求"生物医药领域的基因编辑技术"
- **WHEN** Agent 开始处理
- **THEN** Agent 自主决定先调用 extractMatchingProfile 提取画像，再调用 searchAchievements 检索成果，最后调用 searchPolicies/searchFunds/searchExperts 补充资源
