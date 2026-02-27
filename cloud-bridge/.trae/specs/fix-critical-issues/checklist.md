# Checklist

- [x] 负数价格 (`price < 0`) 创建请求被拦截，返回 400 错误。
- [x] 空标题 (`title=""` 或 `null`) 创建请求被拦截，返回 400 错误。
- [x] 价格字段类型在 Java 代码中为 `BigDecimal`。
- [x] XSS 脚本 (`<script>...`) 在存入或返回时被转义。
- [x] `GET /api/achievements` 接口支持分页参数，且不再一次性返回全量数据。
- [x] `MatchingService` 代码中不再包含 `findAll()` 调用。
- [x] `MatchingService` 代码中不再包含 `split(" ")[0]` 逻辑。
- [x] 中文长句搜索能正确调用数据库 `LIKE` 查询并返回结果。
