# 迁移至 PostgreSQL (Migrate to PostgreSQL) Spec

## Why
当前系统使用 H2 内存数据库（文件模式），仅适用于开发和演示，不具备生产环境所需的高并发、持久化和数据完整性保障。迁移至 PostgreSQL 可以提供更可靠的数据存储、更丰富的数据类型支持和更好的性能。

## What Changes
### Backend (Spring Boot)
1.  **依赖变更**：
    -   在 `pom.xml` 中移除（或注释掉）H2 依赖（可选，保留用于测试）。
    -   添加 `postgresql` 驱动依赖。
2.  **配置变更**：
    -   修改 `application.yml` 中的 `spring.datasource` 配置，指向 PostgreSQL。
    -   配置 JPA 方言为 `org.hibernate.dialect.PostgreSQLDialect`。
    -   更新 `docker-compose.yml`，添加 PostgreSQL 服务容器。

### Infrastructure (Docker)
1.  **新增服务**：
    -   在 `docker-compose.yml` 中添加 `postgres` 服务。
    -   配置数据卷挂载，确保持久化。
    -   设置初始用户名、密码和数据库名。

## Impact
-   **数据迁移**：原有 H2 数据将不再使用。系统启动时将基于现有的 `DataInitializer` 逻辑和 JSON 种子文件，在新的 PostgreSQL 数据库中重新初始化数据。
-   **环境依赖**：运行后端前必须先启动 Docker 容器（PostgreSQL）。

## ADDED Requirements
### Requirement: Database Infrastructure
-   **Component**: PostgreSQL 15+ container.
-   **Config**: Port 5432 exposed (mapped to host).
-   **Persistence**: Data mounted to host directory `./data/postgres`.

## MODIFIED Requirements
### Requirement: Database Connection
-   **Driver**: `org.postgresql.Driver`
-   **Url**: `jdbc:postgresql://localhost:5432/cloud_bridge`
