# 云转桥核心链路对齐与可解释匹配 Spec

## Why
当前项目已经具备需求发布、成果展示、智能匹配、联系沟通、揭榜评审等能力雏形，但“需求方/成果方”双角色主流程和“从需求到匹配结果的解释链路”仍未稳定落地。为满足需求文档、申报材料和当前审查结论，需要先统一角色口径、做实匹配解释证据、修复关键闭环断点，并建立可复验的运行基线。

## What Changes
- 统一平台角色模型与权限口径，使“需求方/成果方”双角色与前后端实现一致
- 重构智能匹配返回结构，为每条匹配结果补充可核验的解释证据
- 修复供需对接关键链路中的权限不一致、错误路由和越权访问问题
- 建立演示与回归验证基线，确保核心链路具备可运行、可验证状态
- **BREAKING** 调整部分前端角色判断、AI 导航回退路由、匹配接口返回字段结构

## Impact
- Affected specs: `implement-user-auth-and-profile`, `implement-core-platform-features`, `optimize-matching-logic`, `fix-critical-issues`
- Affected code: `frontend/src/views/Register.vue`, `frontend/src/views/PublishAchievement.vue`, `frontend/src/views/PublishNeed.vue`, `frontend/src/views/SmartMatch.vue`, `frontend/src/views/MatchingDashboard.vue`, `frontend/src/router/index.ts`, `frontend/src/stores/user.ts`, `backend/src/main/java/com/cloudbridge/controller/AuthController.java`, `backend/src/main/java/com/cloudbridge/controller/DemandController.java`, `backend/src/main/java/com/cloudbridge/controller/MessageController.java`, `backend/src/main/java/com/cloudbridge/controller/AchievementController.java`, `backend/src/main/java/com/cloudbridge/service/MatchingService.java`, `backend/src/main/java/com/cloudbridge/service/ai/AIService.java`

## ADDED Requirements
### Requirement: 统一双角色主流程
系统 SHALL 以“需求方”和“成果方”作为核心业务口径，对注册、登录、发布、查看、联系、响应、评审等环节给出一致的权限定义与页面行为。

#### Scenario: 需求方发布需求
- **WHEN** 需求方登录后进入需求发布流程
- **THEN** 系统允许其创建、查看、管理自己的需求，并在详情页查看后续响应与对接状态

#### Scenario: 成果方发布成果
- **WHEN** 成果方登录后进入成果发布流程
- **THEN** 系统允许其创建、查看、管理自己的成果，且不会因前后端角色命名不一致而被拦截

#### Scenario: 角色口径一致
- **WHEN** 用户完成注册并进入任一业务页面
- **THEN** 前端角色判断、后端权限校验、个人中心默认视图和按钮可见性保持一致

### Requirement: 可解释匹配链路
系统 SHALL 在智能匹配结果中展示从需求输入到结果命中的关键解释证据，而不是只展示可视化外观或静态标签。

#### Scenario: 返回匹配解释证据
- **WHEN** 用户提交需求描述并触发智能匹配
- **THEN** 匹配接口返回核心关键词、领域判断、命中规则、图谱关联节点、排序依据和每个结果的解释摘要

#### Scenario: 前端展示可读链路
- **WHEN** 匹配接口返回解释信息
- **THEN** 前端在匹配页或分析页中展示“需求解析 -> 关键词/领域 -> 图谱路径 -> 命中成果 -> 推荐理由”的链路信息

#### Scenario: 无真实证据时降级
- **WHEN** 后端无法获得完整 AI 或图谱证据
- **THEN** 系统仅展示真实可确认的规则或检索依据，并明确标注降级状态，不得伪造解释分值或随机分析图

### Requirement: 供需对接闭环可用
系统 SHALL 打通需求方与成果方的关键对接路径，确保联系、揭榜、评审、采纳等关键动作权限正确且状态可追踪。

#### Scenario: 需求方查看响应结果
- **WHEN** 需求方查看自己的需求详情
- **THEN** 系统展示响应记录、揭榜状态、评审结果和后续处理入口

#### Scenario: 成果方参与需求响应
- **WHEN** 成果方查看可参与的需求详情
- **THEN** 系统允许其发起联系、提交揭榜或查看自己可访问的响应状态，不会因为接口权限错误而失败

#### Scenario: 消息访问受控
- **WHEN** 用户查看站内信、未读数量或已发送记录
- **THEN** 系统仅返回当前登录用户有权访问的数据

### Requirement: 演示与回归基线
系统 SHALL 提供能够支撑演示与回归验证的最小可运行基线，包括构建、路由、依赖口径和关键链路验证结果。

#### Scenario: AI 导航回退可用
- **WHEN** AI 助手主调用失败并进入 fallback 路径
- **THEN** 返回的页面路径与前端真实路由一致，用户能够继续完成核心操作

#### Scenario: 核心链路可复验
- **WHEN** 团队执行构建和核心链路验证
- **THEN** 前端构建通过，后端具备明确的运行前提和验证方法，核心演示路径可被复现

## MODIFIED Requirements
### Requirement: 智能匹配结果展示
系统原有智能匹配功能需从“展示匹配列表与图谱外观”升级为“展示有来源、有依据、可核验的匹配解释链路”。

#### Scenario: 匹配结果不再只显示装饰性指标
- **WHEN** 用户查看匹配结果卡片和分析面板
- **THEN** 页面优先展示真实解释字段、来源和状态标识，默认占位分数、随机扰动图和无来源标签不得作为正式分析依据

### Requirement: 角色驱动的页面权限
系统原有基于 `ENTERPRISE`、`EXPERT`、`RESEARCHER` 等角色的页面访问逻辑需调整为与“需求方/成果方”业务口径一致，并允许内部实现保留细分角色映射。

#### Scenario: 细分角色映射到业务角色
- **WHEN** 用户属于科研人员、专家或技术经理人等内部细分类型
- **THEN** 系统将其映射为成果方能力集合或明确限制范围，避免注册角色与发布权限冲突

## REMOVED Requirements
### Requirement: 无依据的默认分析分值
**Reason**: 默认分值、随机图表和无来源标签会降低平台对外演示的可信度，无法满足“匹配解释链路”目标。
**Migration**: 改为返回真实解释字段和降级标识；无法计算时显示“待分析”或“规则匹配结果”，不再伪造分析结论。

### Requirement: 与真实路由不一致的 AI fallback 路径
**Reason**: 降级路径跳转失败会直接打断核心用户主流程。
**Migration**: 所有 fallback 导航路径统一收敛到前端已注册路由，并在变更时同步校验。
