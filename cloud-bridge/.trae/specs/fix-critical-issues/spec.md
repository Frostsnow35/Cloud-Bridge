# 修复关键问题与漏洞 (Fix Critical Issues) Spec

## Why
在最近的对抗性测试与代码审查中，发现了系统存在多个严重问题，包括：
1.  **逻辑漏洞**：允许负数价格和空数据（幽灵数据）。
2.  **性能风险**：`MatchingService` 使用全表扫描加载到内存匹配，存在 OOM 风险；列表接口无分页，存在带宽耗尽风险。
3.  **功能缺陷**：中文匹配逻辑存在缺陷（分词错误），导致搜索结果为 0。
4.  **安全风险**：存在存储型 XSS 漏洞。
5.  **精度问题**：价格字段使用 `Double` 导致浮点数精度丢失。

## What Changes
### Backend (Spring Boot)
1.  **数据校验与类型修正**：
    -   实体类 (`Achievement`, `Demand`) 引入 Jakarta Bean Validation (`@NotNull`, `@Min`, `@Size`, `@NotBlank`)。
    -   将 `price` 字段类型从 `Double` 修改为 `BigDecimal`。
    -   Controller 层添加 `@Valid` 注解触发校验。
2.  **安全性增强**：
    -   引入 HTML 转义工具（如 `HtmlUtils` 或依赖库）对 `title` 和 `description` 进行清洗，防止 XSS。
3.  **性能优化**：
    -   `AchievementRepository` 和 `DemandRepository` 继承 `PagingAndSortingRepository` 或使用 `Pageable`。
    -   Controller 列表接口 (`getAllPublished`) 修改为支持分页查询 (`page`, `size`)。
    -   **重构匹配逻辑**：废弃 `MatchingService` 中的 `findAll()` + 内存过滤模式。改用数据库层面的模糊查询 (`findByTitleContainingOrDescriptionContaining`)。
4.  **功能修复**：
    -   修复 `MatchingService` 中的降级逻辑，移除 `split(" ")[0]`，在 AI 提取失败时使用完整输入作为关键词进行数据库模糊匹配。

## Impact
-   **API 变更**：
    -   `GET /api/achievements` 和 `GET /api/demands` 将响应分页结构（`Page<T>`）而非 `List<T>`。客户端需要适配。
    -   `POST` 接口在校验失败时将返回 `400 Bad Request` 及详细错误信息。
-   **数据库变更**：
    -   `price` 字段类型变更可能需要数据库迁移（在此 MVP 阶段可接受自动更新或重建）。

## MODIFIED Requirements

### Requirement: Data Integrity
-   **Constraint**: `price` MUST be non-negative (`>= 0`).
-   **Constraint**: `title` MUST NOT be empty or blank.
-   **Constraint**: `price` MUST use `BigDecimal` for precision.

### Requirement: Performance
-   **Constraint**: All list endpoints MUST support pagination. Default page size = 20.
-   **Constraint**: Search/Matching MUST be performed at Database level, NOT Application memory level.

### Requirement: Search
-   **Logic**: If AI keyword extraction fails, the system MUST use the original user input for a database `LIKE` query (Support Chinese sentences).
