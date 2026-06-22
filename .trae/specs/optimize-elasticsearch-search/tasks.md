# Tasks: Elasticsearch 搜索能力全面优化

## 阶段一：ES 稳定性优化（持久化）

- [ ] **Task 1.1**: 创建 Docker Compose 持久化配置
  - 修改 docker-compose.yml，添加 ES 数据卷映射
  - 配置 named volume 持久化
  - 验证数据目录权限

- [ ] **Task 1.2**: 配置 ES 健康检查和启动顺序
  - 添加 healthcheck 配置
  - 配置 depends_on 启动依赖
  - 测试服务重启后数据完整性

## 阶段二：中文分词优化（IK 分词器）

- [ ] **Task 2.1**: 下载安装 IK 分词器插件
  - 下载与 ES 8.11.0 匹配的 IK 插件版本
  - 解压到 ES 插件目录
  - 验证插件加载成功

- [ ] **Task 2.2**: 修改 ES 配置启用 IK 分析器
  - 更新 elasticsearch.yml 添加 analysis 配置
  - 创建 index template 使用 IK 分词器

- [ ] **Task 2.3**: 更新 SearchService 的 createIndex 方法
  - 修改 mapping 使用 ik_max_word 和 ik_smart
  - 添加动态 analyzer 配置

- [ ] **Task 2.4**: 重新索引现有数据（验证中文分词效果）
  - 删除旧索引
  - 重新导入 achievements 和 public_platforms 数据
  - 测试中文搜索准确性

## 阶段三：搜索效果优化（混合搜索）

- [x] **Task 3.1**: 优化 multi_match 查询配置
  - 调整 fields 权重配置（title^2, name^2）
  - 添加 fuzziness AUTO 容错配置

- [x] **Task 3.2**: 实现向量与关键词搜索的 RRF 融合
  - 实现 searchHybrid() 方法整合向量和关键词搜索
  - 实现 RRF 融合算法（1/(k+rank)）
  - 添加 searchVectorWithScore() 和 searchESWithScore() 辅助方法

- [x] **Task 3.3**: 更新 MatchingService 的匹配逻辑
  - 更新 match() 方法使用 searchHybrid()
  - 添加 fallbackToSeparateSearch() 降级方案
  - 优化评分机制，根据 _source_type 调整分数

## 阶段四：ES 性能优化

- [ ] **Task 4.1**: 优化 ES JVM 和查询配置
  - 配置合适的 heap size
  - 启用 query cache
  - 配置 request cache

- [ ] **Task 4.2**: 优化索引配置
  - 调整 refresh_interval
  - 配置 index.number_of_replicas
  - 优化 index.translog 配置

## 任务依赖关系

```
Task 1.1 (Docker配置) ─┬─> Task 1.2 (健康检查)
                        │
Task 2.1 (IK插件) ─────┴─> Task 2.2 (ES配置)
                              │
                              v
                        Task 2.3 (代码配置)
                              │
                              v
                        Task 2.4 (重新索引)
                              │
                              v
              ┌────────────────┴────────────────┐
              v                                 v
        Task 3.1 (查询优化)              Task 3.2 (RRF融合)
              │                                 │
              └─────────────┬───────────────────┘
                            v
                      Task 3.3 (MatchingService)
                            │
                            v
                      Task 4.1 (JVM配置)
                            │
                            v
                      Task 4.2 (索引优化)
```

## 验证方式

- 中文搜索测试：搜索"人工智能"应返回相关成果，而非逐字匹配结果
- 持久化测试：重启 ES 容器后数据完整
- 性能测试：使用 Apache Bench 或 Postman 验证响应时间
- 混合搜索测试：验证 RRF 融合后的结果质量
