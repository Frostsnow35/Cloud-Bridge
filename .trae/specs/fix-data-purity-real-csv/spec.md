# Data Purity and Real Data Loading Spec

## Why
The user explicitly requires the removal of all simulated/mock data and the exclusive use of provided real-world CSV datasets for "Achievements" and "Public Data Platforms". Additionally, the user wants "Test Demands" to be generated *derived* from the real achievements to ensure realistic matching scenarios, rather than random mock strings.

## What Changes

### 1. Backend (`RAGDataSeeder.java`)
- **REMOVE** all legacy mock seeding methods:
    - `seedPolicies()`
    - `seedFunds()`
    - `seedEquipments()`
    - `seedExperts()`
    - `seedPatents()`
    - `seedEnterprises()`
    - `loadMockData()`
- **UPDATE** `seedAchievementsFromExpertCSV()`:
    - **Source**: Change to `e:\数据要素大赛作品\数据集\广州市白云区省、市级科技项目立项名单.csv` (and Docker equivalent).
    - **Mapping**:
        - `项目名称` -> `Achievement.title`
        - `承担单位` -> `Achievement.owner/institution`
        - `支持方向` + `项目批次` -> `Achievement.description`
        - `级别` -> Tags/Labels
- **VERIFY** `seedPublicPlatformsFromCSV()`:
    - Ensure it correctly reads `e:\数据要素大赛作品\数据集\广州市白云区公共数据开放计划.csv`.
- **ADD** `seedTestDemands()`:
    - Logic: After seeding achievements, fetch 3-5 distinct achievements.
    - Generate `Demand` entities based on them to simulate High/Medium/Low matching scenarios.
    - **High Match**: Demand Title = "Seeking [Achievement Title] Technology"; Description = "Need [Achievement Field] solution..."
    - **Medium Match**: Demand Title = "Project in [Achievement Field]"; Description = "Looking for general [Achievement Field] support."
    - **Low Match**: Random keywords from other fields.

### 2. Verification
- Ensure `AchievementController` and `DemandController` support file/data upload (already confirmed, but will double-check frontend links if needed).

## Impact
- **Database**: Will be wiped clean (`deleteAll`) on startup and repopulated *only* with CSV data and derived test demands.
- **Search/Matching**: Will now return results based on real project names and units.

## ADDED Requirements
### Requirement: Zero Mock Policy
The system SHALL NOT load any hardcoded mock data (e.g., "Zhang San", "Virtual Digital Human") if it does not exist in the provided CSVs.

### Requirement: Real Data Mapping
The system SHALL map the "Project List" CSV columns to the Achievement entity fields accurately.

### Requirement: Derived Test Data
The system SHALL generate at least 3 Demand entries that are semantically derived from the loaded Achievements to facilitate demonstration of the matching algorithm.
