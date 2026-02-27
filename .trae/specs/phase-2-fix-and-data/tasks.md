# Tasks

## Phase 1 Fix: 修复后端构建与验证
- [x] Task 1.1: 修复 Maven 编译器配置 (pom.xml)，确保使用 E:\env\jdk-17。
- [x] Task 1.2: 验证 AI 服务调用（AIServiceIntegrationTest）在后端环境中通过。

## Phase 2: 数据清洗与图谱构建 (Week 2 Start)
- [x] Task 2.1: 准备演示数据 (生物医药/新材料)
  - [x] SubTask 2.1.1: 收集并编写 `data/raw_achievements.json` (至少 20 条)。
  - [x] SubTask 2.1.2: 编写 `data/raw_technologies.json` (图谱节点)。
- [x] Task 2.2: 实现图谱导入脚本
  - [x] SubTask 2.2.1: 编写 Neo4j Cypher 导入脚本 (`import.cypher`)。
  - [x] SubTask 2.2.2: 实现后端 `DataInitializer` 类，启动时自动执行导入。
- [x] Task 2.3: 验证智能匹配效果
  - [x] SubTask 2.3.1: 启动全栈应用，输入特定关键词进行匹配测试。
  - [x] SubTask 2.3.2: 调整 `MatchingService` 权重逻辑，确保 Top 3 结果准确。

## Phase 3: 前端展示优化
- [x] Task 3.1: 优化前端 `SmartMatch.vue` 页面
  - [x] SubTask 3.1.1: 移除无用的静态图谱，保留核心匹配功能。
  - [x] SubTask 3.1.2: 丰富结果卡片信息（增加价格、成熟度、领域标签）。
- [x] Task 3.2: 验证前端与后端的联调效果
  - [x] SubTask 3.2.1: 启动前端开发服务器 (Vite)。
  - [x] SubTask 3.2.2: 确认 API 调用成功并能正确渲染结果列表。

## Phase 4: 前端首页重构 (Tech Portal)
- [x] Task 4.1: 修复前端路由配置 (main.ts)
  - [x] SubTask 4.1.1: 引入 `router` 和 `ElementPlusIcons`。
- [x] Task 4.2: 更新顶部导航栏 (App.vue)
  - [x] SubTask 4.2.1: 增加 "智能匹配" 入口，美化 Logo 和菜单样式。
- [x] Task 4.3: 重构首页 (Home.vue) - 现代科技风门户
  - [x] SubTask 4.3.1: 实现 Hero Section (大搜索框、标语)。
  - [x] SubTask 4.3.2: 实现 Stats Dashboard (数据看板)。
  - [x] SubTask 4.3.3: 实现 Quick Actions 和 Featured Carousel。

## Phase 5: Refinement & AI Assistant
- [x] Task 5.1: 视觉升级 - 活力商务风配色调整
  - [x] SubTask 5.1.1: 创建 `theme.css`，定义现代商务蓝 (Teal/Blue) 主色调。
  - [x] SubTask 5.1.2: 在 `main.ts` 中引入全局样式。
- [x] Task 5.2: 实现全局 AI 悬浮助手 (AICopilot.vue)
  - [x] SubTask 5.2.1: 创建 `AICopilot.vue` 组件，包含悬浮按钮和对话窗口。
  - [x] SubTask 5.2.2: 集成到 `App.vue` 全局入口。
  - [x] SubTask 5.2.3: 实现 Mock 对话逻辑 (支持导航指引、成果查询)。
- [x] Task 5.3: 开发成果/需求详情页 (Detail Views)
  - [x] SubTask 5.3.1: 创建 `AchievementDetail.vue`，包含面包屑、富文本介绍、所有者信息卡片。
  - [x] SubTask 5.3.2: 配置动态路由 `/achievements/:id`。

# Task Dependencies
- [Task 2.2] depends on [Task 2.1]
- [Task 2.3] depends on [Task 1.2] and [Task 2.2]
- [Task 3.1] depends on [Task 2.3]
- [Task 4.3] depends on [Task 4.1]
- [Task 5.2] depends on [Task 4.1]
