# Tasks

- [x] Task 1: 修复数据校验与安全漏洞 (Fix Validation & Security)
  - [x] SubTask 1.1: 修改 `Achievement` 和 `Demand` 实体类，将 `price` 类型改为 `BigDecimal`。
  - [x] SubTask 1.2: 在实体类添加 Bean Validation 注解 (`@NotNull`, `@Min(0)`, `@NotBlank`)。
  - [x] SubTask 1.3: 在 Controller (`AchievementController`, `DemandController`) 的 `create` 方法添加 `@Valid` 注解。
  - [x] SubTask 1.4: 实现全局异常处理 (`GlobalExceptionHandler`) 以优雅返回校验错误信息。
  - [x] SubTask 1.5: 在 Service 层或 Controller 层对 `title` 和 `description` 进行 XSS 清洗 (`HtmlUtils.htmlEscape`).

- [x] Task 2: 修复性能与匹配逻辑 (Fix Performance & Matching)
  - [x] SubTask 2.1: 修改 `AchievementRepository` 和 `DemandRepository`，添加支持 `Pageable` 的查询方法。
  - [x] SubTask 2.2: 更新 Controller 列表接口，支持 `page` 和 `size` 参数，返回 `Page<T>`。
  - [x] SubTask 2.3: 在 Repository 中添加 `findByTitleContainingOrDescriptionContaining` 方法。
  - [x] SubTask 2.4: 重构 `MatchingService`，移除 `findAll()` 和内存匹配逻辑，改用上述 Repository 方法。
  - [x] SubTask 2.5: 修复 `MatchingService` 的中文降级逻辑（移除 `split(" ")[0]`）。

- [x] Task 3: 验证修复结果 (Verify Fixes)
  - [x] SubTask 3.1: 运行 `vulnerability_test.py` 验证漏洞是否被拦截。
  - [x] SubTask 3.2: 编写新的分页测试脚本验证分页功能。
  - [x] SubTask 3.3: 验证中文长句匹配是否返回结果。
