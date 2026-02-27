# 修复数据持久化问题 (Fix Data Persistence) Spec

## Why
当前系统在每次启动时，`DataInitializer` 会强制执行 `deleteAll()` 清空数据库，导致用户在运行期间产生的数据无法持久保存。这严重影响了系统的可用性和数据完整性。

## What Changes
### Backend (Spring Boot)
1.  **修改数据初始化逻辑**：
    -   移除 `DataInitializer.java` 中的 `achievementRepository.deleteAll()` 和 `demandRepository.deleteAll()`。
    -   改为仅在数据库为空（`count() == 0`）时才加载初始 JSON 数据。
    -   保留 `initializeGraph()` 中的图谱初始化逻辑，但同样需要确保不覆盖现有数据（如果适用）。

## Impact
-   **数据保留**：重启应用后，之前创建的 `Achievement` 和 `Demand` 数据将得以保留。
-   **初始化行为**：只有首次启动（或手动清空数据库后）才会加载 `smart_achievements.json` 和 `smart_demands.json`。

## MODIFIED Requirements
### Requirement: Data Initialization
-   **Logic**: The system MUST check if data exists before loading seed data.
-   **Constraint**: The system MUST NOT delete existing data on startup.
