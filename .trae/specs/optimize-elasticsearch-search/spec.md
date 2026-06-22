# Elasticsearch 搜索能力全面优化

## Why

当前 Elasticsearch 存在以下问题影响搜索效果：
1. **中文分词差**：使用 standard 分词器，中文被逐字切分，搜索"人工智能"变成搜索"人"、"工"、"智"、"能"
2. **搜索融合不优**：向量搜索和关键词搜索各自独立，未有效融合
3. **数据不稳定**：ES 数据存储在临时目录，服务重启后数据丢失
4. **性能待优化**：缺少缓存和查询优化

## What Changes

### 1. 中文分词优化
- 安装配置 IK 分词器插件
- 创建支持中文的 analyzer 和 search_analyzer
- 重新索引现有数据

### 2. 搜索效果优化
- 优化 multi_match 查询的 fields 权重
- 实现 hybrid search（向量+关键词）融合策略
- 添加 query expansion（同义词扩展）

### 3. ES 稳定性优化
- 配置 Docker Compose 持久化卷
- 添加健康检查和自动重启
- 完善启动依赖顺序

### 4. ES 性能优化
- 优化 query cache 配置
- 添加热门查询结果缓存（可选）
- 优化分片和副本配置

## Impact

- Affected specs: cloud-bridge-mvp（搜索功能）
- Affected code: 
  - SearchService.java
  - MatchingService.java
  - docker-compose.yml
  - elasticsearch.yml

## ADDED Requirements

### Requirement: 中文智能分词
系统 SHALL 使用 IK 分词器进行中文文本分析，支持：
- IK_MAX_WORD：细粒度分词（穷尽词库所有组合）
- IK_SMART：智能分词（按语义划分）

#### Scenario: 中文搜索准确性验证
- **WHEN** 用户搜索"人工智能技术"
- **THEN** ES 正确识别为一个完整词组，返回相关成果
- **AND** 不会返回仅包含"人工"或"智能"的无关结果

### Requirement: 混合搜索融合
系统 SHALL 实现向量搜索与关键词搜索的智能融合：
- 向量搜索权重：0.6
- 关键词搜索权重：0.4
- 使用 RRF（Reciprocal Rank Fusion）算法融合

#### Scenario: 混合搜索效果
- **WHEN** 用户输入"医疗影像AI"
- **THEN** 返回结果同时包含语义相似和关键词匹配的内容
- **AND** 整体相关性评分 = RRF(vector_scores) + RRF(keyword_scores)

### Requirement: 数据持久化
系统 SHALL 保证 ES 数据在服务重启后不丢失：
- 使用 Docker 持久化卷存储数据
- 配置健康检查确保服务就绪后其他服务再连接

#### Scenario: 服务重启后数据完整
- **WHEN** 执行 `docker-compose restart elasticsearch`
- **THEN** 所有索引和数据完整保留
- **AND** 服务自动恢复可用状态

### Requirement: 查询性能优化
系统 SHALL 优化搜索响应时间：
- 平均查询响应时间 < 500ms
- 向量搜索响应时间 < 1s

#### Scenario: 性能基准测试
- **WHEN** 执行100次并发搜索请求
- **THEN** P95 响应时间 < 800ms

## MODIFIED Requirements

### Requirement: 索引 Mapping 配置
将 `elasticsearch.yml` 和 `SearchService.createIndex()` 中的索引配置修改为：
```json
{
  "settings": {
    "analysis": {
      "analyzer": {
        "ik_analyzer": {
          "type": "custom",
          "tokenizer": "ik_max_word"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {"type": "text", "analyzer": "ik_max_word", "search_analyzer": "ik_smart"},
      "description": {"type": "text", "analyzer": "ik_max_word", "search_analyzer": "ik_smart"}
    }
  }
}
```

## Implementation Notes

### 关于 IK 分词器
- 插件地址：https://github.com/medcl/elasticsearch-analysis-ik
- 版本需与 ES 8.11.0 匹配
- 需要修改 ES 启动配置以加载插件

### 关于持久化卷
```yaml
volumes:
  - es-data:/usr/share/elasticsearch/data
```

### 关于混合搜索 RRF 融合
```json
{
  "query": {
    "rrf": {
      "rank": {
        "window_size": 50,
        "rank_constant": 60
      }
    }
  }
}
```
