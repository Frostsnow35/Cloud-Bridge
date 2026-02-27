# Fix Backend & Phase 2 Spec

## Why
目前后端项目在编译时出现 `无效的目标发行版: 17` 错误，导致服务无法启动。为了继续推进项目，首先需要修复后端构建环境配置。
修复后，我们将进入 **Phase 2: 数据填充与图谱构建**，通过导入真实的演示数据（生物医药/新材料领域），让 AI 匹配功能具备实际演示价值。

## What Changes
### 1. 后端构建修复
- **配置调整**: 修正 `pom.xml` 中的 `maven-compiler-plugin` 配置，强制指定编译器版本与 `JAVA_HOME` 环境变量对齐。
- **环境隔离**: 确保构建脚本不受到宿主机（如 `C:\ProgramData\Oracle\Java`）环境的干扰。

### 2. 数据清洗与导入 (Phase 2)
- **数据脚本**: 编写 Python 脚本 (`data_cleaner.py`)，处理原始文本数据为 CSV/JSON。
- **图谱导入**: 编写 Neo4j Cypher 脚本 (`import_graph.cypher`)，批量创建 `Technology`, `Field`, `Expert` 等节点及关系。
- **数据初始化**: 在后端启动时通过 `CommandLineRunner` 自动加载基础数据（如果图数据库为空）。

## Impact
- **Backend**: `pom.xml`, `application.yml`, 新增 `DataInitializer.java`。
- **Database**: Neo4j 将被填充真实数据。
- **Frontend**: 匹配结果页将展示真实的图谱关联信息。

## ADDED Requirements
### Requirement: 真实数据演示
- **WHEN** 用户搜索 "锂离子电池" **THEN** 系统应返回不少于 3 条包含具体技术参数（如能量密度、正极材料）的成果。
- **WHEN** 查看成果详情 **THEN** 能够展示该成果关联的上游技术（如 "电解液"）和下游应用（如 "新能源汽车"）。

## MODIFIED Requirements
### Requirement: 后端启动流程
- **Before**: 依赖手动导入 SQL。
- **After**: 系统启动时自动检查并初始化演示数据（幂等操作）。
