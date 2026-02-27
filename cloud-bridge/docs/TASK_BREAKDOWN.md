# 云转桥 MVP 任务拆分（Atomize）

本清单聚焦可在近端迭代落地的原子任务，每个任务≤3个函数/模块范围，明确输入/输出。

## 阶段一：核心服务真实化
1) 重构 AI 匹配链路
   - 输入：需求/摘要文本（POST /api/matching/match）
   - 处理：AIService 抽取 {keywords[], field}; MatchingService 基于关键词 + Neo4j 多跳查询合并打分
   - 输出：匹配结果列表 + 推荐理由（含图谱路径摘要）
   - 产物：DTO/VO、错误处理、超时与降级、内存缓存
2) 区块链 SDK 接入（可降级）
   - 输入：文件哈希、元信息
   - 处理：FISCO BCOS Java SDK 签名与上链；若未配置则走 Mock 实现
   - 输出：交易哈希与核验接口一致返回
   - 产物：Evidence.sol Java Wrapper、配置模板、环境变量说明

## 阶段二：数据填充与图谱构建
3) 数据清洗脚本
   - 输入：非结构化文本（申报书提及的生物医药/新材料示例）
   - 处理：Python/SQL 将文本抽取为 CSV/JSON，字段含 title、summary、field、keywords
   - 输出：raw_achievements.json、raw_technologies.json 扩充版
4) 知识图谱导入
   - 输入：清洗后的 CSV/JSON
   - 处理：编写/完善 Cypher 批量导入脚本，建立 (Technology)-[:BELONGS_TO]->(Field)、(Achievement)-[:USES]->(Technology)
   - 输出：Neo4j 数据库更新，提供导入与校验日志

## 阶段三：界面优化与集成测试
5) 匹配结果可视化与 UX
   - 输入：后端返回的匹配结果与推荐理由
   - 处理：接入 ECharts 展示关联路径；完善 Loading/Toast/错误态
   - 输出：更友好与可解释的匹配页面
6) 全链路 E2E 演示流
   - 输入：用户旅程（注册→发布需求→AI 匹配→存证→核验）
   - 处理：编写真机脚本或手册；补充测试数据；记录耗时
   - 输出：演示手册与基础 E2E 用例

## 阶段四：部署与运维
7) 容器化与反向代理
   - 输入：后端 Jar、前端构建产物
   - 处理：新增后端/前端 Dockerfile；Compose 增加服务；Nginx 反代与 HTTPS 模板
   - 输出：一键启动/部署脚本与文档

## 依赖清单与接口契约
- AIService 契约
  - 输入：{ text: string }
  - 输出：{ keywords: string[], field?: string, raw?: any }
  - 超时：2.5s；重试：1 次；降级：返回空关键词，走纯图谱
- BlockchainService 契约
  - store(hash: String, meta: EvidenceMeta) → { txHash: String, status: String }
  - verify(hash: String) → { exists: boolean, txHash?: String, blockNumber?: Long }

## 任务看板（Mermaid）
```mermaid
gantt
    title MVP 冲刺任务
    section 核心服务
    AI 匹配链路          :a1, 2026-02-18, 3d
    FISCO 接入/降级       :a2, after a1, 3d
    section 数据与图谱
    数据清洗脚本          :b1, 2d
    图谱导入              :b2, after b1, 2d
    section 界面与测试
    匹配可视化            :c1, 2d
    全链路 E2E           :c2, after c1, 2d
    section 部署
    容器化与反代          :d1, 2d
```

## 验收与降级策略
- 优先保证接口稳定与演示流畅；区块链可在“模拟网络”模式运行但接口不变。
- 对高频 Query 进行缓存；AI/图谱不可用时返回可理解的错误提示与演示数据。

