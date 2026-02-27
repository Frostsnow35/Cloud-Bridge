# 云转桥 MVP 对齐说明（ALIGNMENT）

## 目标与范围
- 目标：将现有 Skeleton 演进为可演示的 MVP，覆盖“AI 智能匹配 + 区块链存证 + 基础数据与图谱 + 可视化前端 + 可运行编排”闭环。
- 范围（MVP 内）：
  - AI 匹配后端接口：调用 AI 服务提取关键词/领域 + 结合 Neo4j 多跳查询 + MySQL 基础数据检索，返回可解释匹配结果。
  - 区块链存证：提供文件哈希上链与核验能力；若 FISCO BCOS 未就绪，保留 Mock 并在 UI 标注“模拟网络”。
  - 数据与图谱：导入 20–50 条高质量演示数据与基础技术图谱，支持 2–3 跳关联查询。
  - 前端：智能匹配页与存证页可用，提供 Loading、Toast、错误态；匹配结果包含“推荐理由”，具备基本可视化。
  - 编排与部署：提供本地一键启动脚本或 Compose 服务，生产部署预案文档。
- 范围（MVP 外）：
  - 完整用户与权限体系、账单/付费、工作流全量看板、复杂资源导航与长链路自动化。

## 当前实现快照
- 后端（Spring Boot 3, Maven, Java 17）
  - 匹配：基于字符串包含与 Technology 相关查询的简化逻辑，已有 AIService 通过 RestTemplate 调用外部聊天补全接口。
    - 关键文件：MatchingService、AIService、TechnologyRepository、MatchingController
  - 区块链：MockBlockchainService 以内存 Map 模拟上链与核验，EvidenceController 提供 API；合约样例 Evidence.sol 已在 deploy/contracts 中。
  - 数据：JPA 实体与仓库齐备；Neo4j 节点/仓库定义存在；DataInitializer 启动导入基础数据与图谱脚本。
- 前端（Vue 3 + Vite + Element Plus + Pinia）
  - 页面：智能匹配、存证、需求/成果列表、登录等；Vite 代理 /api → 8080。
  - 可视化：未见 ECharts 集成代码。
- 编排：deploy/docker-compose.yml 含 MySQL、Redis、Neo4j 基础服务；缺少后端/前端 Dockerfile。

代码参考：
- 后端入口与配置：[BackendApplication.java](file:///e:/数据要素大赛作品/cloud-bridge/backend/src/main/java/com/cloudbridge/BackendApplication.java)，[application.yml](file:///e:/数据要素大赛作品/cloud-bridge/backend/src/main/resources/application.yml)
- 核心控制器：[MatchingController](file:///e:/数据要素大赛作品/cloud-bridge/backend/src/main/java/com/cloudbridge/controller/MatchingController.java)，[EvidenceController](file:///e:/数据要素大赛作品/cloud-bridge/backend/src/main/java/com/cloudbridge/controller/EvidenceController.java)
- 服务层：[MatchingService](file:///e:/数据要素大赛作品/cloud-bridge/backend/src/main/java/com/cloudbridge/service/MatchingService.java)，[AIService](file:///e:/数据要素大赛作品/cloud-bridge/backend/src/main/java/com/cloudbridge/service/ai/AIService.java)，[MockBlockchainService](file:///e:/数据要素大赛作品/cloud-bridge/backend/src/main/java/com/cloudbridge/service/impl/MockBlockchainService.java)
- 图谱与数据：[TechnologyRepository](file:///e:/数据要素大赛作品/cloud-bridge/backend/src/main/java/com/cloudbridge/repository/graph/TechnologyRepository.java)，[import.cypher](file:///e:/数据要素大赛作品/cloud-bridge/backend/src/main/resources/cypher/import.cypher)
- 前端入口与页面：[main.ts](file:///e:/数据要素大赛作品/cloud-bridge/frontend/src/main.ts)，[SmartMatch.vue](file:///e:/数据要素大赛作品/cloud-bridge/frontend/src/views/SmartMatch.vue)，[Evidence.vue](file:///e:/数据要素大赛作品/cloud-bridge/frontend/src/views/Evidence.vue)

## 差距清单（Skeleton → MVP）
1) AI 引擎
   - 缺：稳定的 AI 提取与解析流程、Prompt 与错误处理、领域/关键词结构化输出
   - 补：完善 AIService 契约，定义 DTO；MatchingService 合并 Neo4j 多跳查询与关键词相似度；添加缓存与降级
2) 区块链
   - 缺：真实 FISCO BCOS SDK 集成、证书私钥配置、合约 Java Wrapper 调用路径
   - 补：引入 fisco-bcos-sdk，编译 Evidence.sol 生成 Java Wrapper，封装 BlockchainService 实现与配置
3) 数据与图谱
   - 缺：20–50 条高质量演示数据与对应图谱关系
   - 补：数据清洗脚本与批量导入；完善 import.cypher；回归校验
4) 前端
   - 缺：匹配理由可视化、加载/错误/空态、ECharts 结果图
   - 补：统一交互反馈与错误处理，接入 ECharts，标注“模拟网络”降级态
5) 编排与部署
   - 缺：后端/前端 Dockerfile，Nginx 反代与 HTTPS，生产配置
   - 补：新增 Dockerfile 与 Compose 服务，部署手册与环境变量清单

## 验收标准（KPI）
- 功能：识别 ≥5 种技术关键词；存证返回真实 TxHash 或标注模拟网络；图谱多跳解释路径
- 性能：AI 匹配 < 3s；存证 < 2s（或模拟）
- 数据：≥20 条演示数据；图谱至少 3 层关系
- 稳定：全链路演示无崩溃与 500 错

## 风险与降级
- FISCO BCOS 不可用：回退 MockBlockchainService，并在前端显著标注“模拟网络”；保留相同接口
- AI 响应慢/不稳定：对热门 Query 进行内存/Redis 缓存；提供预置 Demo 用例
- 资源不足：限制 JVM/Neo4j 内存，必要时配置 Swap

## 体系结构（Mermaid）
```mermaid
flowchart LR
    UI[Vue3 + Element Plus] --> GW[Spring REST API]
    GW --> MATCH[MatchingService]
    MATCH --> AI[AIService (ChatGLM)]
    MATCH --> NEO4J[(Neo4j 知识图谱)]
    MATCH --> DB[(MySQL 数据库)]
    UI --> EVD[Evidence API]
    EVD --> BC[BlockchainService]
    BC --> FISCO[FISCO BCOS SDK/Mock]
```

