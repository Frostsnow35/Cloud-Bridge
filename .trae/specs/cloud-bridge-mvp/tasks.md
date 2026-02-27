# Tasks

## Phase 1: 基础设施搭建与核心业务开发 (Week 1)
- [ ] Task 1.1: 环境搭建与项目初始化
  - [ ] SubTask 1.1.1: 搭建开发环境 (Docker Compose: MySQL, Redis, Neo4j, FISCO BCOS节点)。
  - [ ] SubTask 1.1.2: 初始化后端项目 (Spring Boot 3.0, 集成MyBatis-Plus, Swagger)。
  - [ ] SubTask 1.1.3: 初始化前端项目 (Vue 3 + TypeScript + Element Plus/Ant Design Vue)。
- [ ] Task 1.2: 用户中心模块开发
  - [ ] SubTask 1.2.1: 实现用户注册、登录 (JWT鉴权) 与角色管理 (RBAC)。
  - [ ] SubTask 1.2.2: 实现企业/个人基础实名认证接口与后台审核页面。
- [ ] Task 1.3: 供需管理模块开发
  - [ ] SubTask 1.3.1: 设计成果与需求的数据库模型 (MySQL)。
  - [ ] SubTask 1.3.2: 实现成果/需求发布、编辑、详情查询接口。
  - [ ] SubTask 1.3.3: 开发后台审核流程与前端管理界面。

## Phase 2: AI智能匹配引擎集成 (Week 2)
- [ ] Task 2.1: 知识图谱构建 (Neo4j)
  - [ ] SubTask 2.1.1: 设计图谱Schema (技术实体、专家、机构、关系)。
  - [ ] SubTask 2.1.2: 导入演示用垂直领域数据 (如生物医药领域Demo数据)。
- [ ] Task 2.2: AI模型服务集成
  - [ ] SubTask 2.2.1: 部署ChatGLM-6B (API服务化或本地轻量化部署)。
  - [ ] SubTask 2.2.2: 开发后端AI服务接口，实现文本Embedding与实体提取。
- [ ] Task 2.3: 匹配算法实现
  - [ ] SubTask 2.3.1: 实现基于语义相似度与图谱关联的混合匹配算法。
  - [ ] SubTask 2.3.2: 前端展示匹配结果及推荐理由。

## Phase 3: 区块链存证与系统联调 (Week 3)
- [ ] Task 3.1: 区块链服务开发 (FISCO BCOS)
  - [ ] SubTask 3.1.1: 编写并部署存证智能合约 (Solidity)。
  - [ ] SubTask 3.1.2: 集成Java SDK，封装存证上链与查询接口。
- [ ] Task 3.2: 存证业务集成
  - [ ] SubTask 3.2.1: 在关键业务节点 (发布、合作确认) 埋点触发存证。
  - [ ] SubTask 3.2.2: 开发存证证书生成与前端核验页面。
- [ ] Task 3.3: 系统集成与测试
  - [ ] SubTask 3.3.1: 全链路联调 (前端 -> 网关 -> 业务服务 -> AI/BC服务)。
  - [ ] SubTask 3.3.2: 部署演示环境 (阿里云ECS/本地演示机)。

# Task Dependencies
- [Task 2.3] depends on [Task 2.1] and [Task 2.2]
- [Task 3.2] depends on [Task 3.1] and [Task 1.3]
