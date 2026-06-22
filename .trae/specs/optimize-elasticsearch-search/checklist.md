# Checklist: Elasticsearch 搜索能力全面优化

## 阶段一：ES 稳定性优化

- [ ] **Docker Compose 配置正确**
  - [ ] docker-compose.yml 包含 elasticsearch 服务
  - [ ] 配置了 named volume 映射到 /usr/share/elasticsearch/data
  - [ ] 配置了 path.logs 持久化

- [ ] **健康检查配置**
  - [ ] healthcheck 配置了 curl 检查 9200 端口
  - [ ] 后端服务配置了 depends_on elasticsearch: condition: service_healthy

- [ ] **数据持久化验证**
  - [ ] 重启 ES 容器后索引存在
  - [ ] 重启后文档数量不变
  - [ ] 向量数据完整

## 阶段二：中文分词优化

- [ ] **IK 分词器安装**
  - [ ] 插件目录存在：ES_HOME/plugins/analysis-ik
  - [ ] 插件 JAR 文件完整
  - [ ] ES 启动日志显示 IK 插件加载成功

- [ ] **ES 配置更新**
  - [ ] elasticsearch.yml 包含 analysis.analyzer 配置
  - [ ] index template 使用 ik_max_word 作为 analyzer

- [ ] **SearchService 代码更新**
  - [ ] createIndex() 方法使用 IK analyzer
  - [ ] mapping 中 title/description 使用 IK 分词
  - [ ] 代码无编译错误

- [ ] **中文分词效果验证**
  - [ ] 搜索"人工智能"返回包含该词组的成果
  - [ ] 不返回仅包含"人工"或"智能"的无关结果
  - [ ] 搜索"深度学习"正确识别为完整词组

## 阶段三：搜索效果优化

- [ ] **multi_match 查询优化**
  - [ ] fields 权重配置正确（title^2, name^2）
  - [ ] fuzziness 配置合理
  - [ ] minimum_should_match 配置正确

- [ ] **RRF 融合实现**
  - [ ] SearchService.search() 方法整合向量搜索
  - [ ] RRF 算法正确实现
  - [ ] 融合权重配置合理

- [ ] **MatchingService 更新**
  - [ ] 向量搜索和关键词搜索融合工作正常
  - [ ] 评分机制正确
  - [ ] 返回结果质量提升

- [ ] **搜索效果测试**
  - [ ] 测试用例：搜索"医疗影像AI"返回相关成果
  - [ ] 测试用例：搜索"新能源汽车"返回正确领域成果
  - [ ] 测试用例：搜索"智能制造"领域匹配准确

## 阶段四：性能优化

- [ ] **JVM 配置优化**
  - [ ] ES_JAVA_OPTS 配置合理的 heap size
  - [ ] 配置了 -XX:+UseG1GC
  - [ ] 配置了 HeapDumpOnOutOfMemoryError

- [ ] **查询缓存配置**
  - [ ] indices.queries.cache.size 配置启用
  - [ ] indices.request.cache.enabled 配置启用

- [ ] **索引配置优化**
  - [ ] refresh_interval 设置合理（默认1s或-1禁用）
  - [ ] number_of_replicas 根据环境配置
  - [ ] translog durability 配置合理

- [ ] **性能基准测试**
  - [ ] 单次搜索响应时间 < 500ms
  - [ ] 向量搜索响应时间 < 1s
  - [ ] 100次并发请求无超时错误

## 最终验收

- [ ] **全部功能测试通过**（需要 Docker 环境验证）
  - [ ] 中文分词搜索正常工作
  - [ ] 混合搜索融合效果提升
  - [ ] 数据持久化正常工作
  - [ ] 性能指标达标

- [x] **代码质量**
  - [x] SearchService.java 代码更新完成
  - [x] MatchingService.java 代码更新完成
  - [x] docker-compose.yml 格式正确
  - [x] 配置了健康检查和启动依赖

- [x] **文档更新**
  - [x] DEPLOYMENT_GUIDE.md 更新 IK 安装步骤
  - [x] 创建 install-ik-analyzer.sh 安装脚本
  - [x] 相关配置说明完整
