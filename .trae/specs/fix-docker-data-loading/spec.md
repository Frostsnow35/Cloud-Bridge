# Fix Data Loading in Docker Spec

## Why
The user reports that "fake mock data" (e.g., "虚拟数字人") persists in the Matching Dashboard despite previous attempts to remove it. 
Root Cause Analysis: 
1. The backend `RAGDataSeeder` uses a hardcoded Windows absolute path (`e:\...`) to load the expert CSV.
2. When running in Docker (via the newly added one-click script), this path does not exist.
3. The `RAGDataSeeder` aborts before executing `achievementRepository.deleteAll()`.
4. Consequently, old/stale data (possibly from previous runs or persistent volumes) remains in the database, appearing as "fake data".

## What Changes
- **Backend (`docker-compose.yml`)**:
    - Mount the host's `数据集` (datasets) directory into the backend container at `/app/datasets`.
- **Backend (`RAGDataSeeder.java`)**:
    - Update logic to check multiple paths for the CSV (Docker path `/app/datasets/...` AND local Windows path).
    - Ensure `deleteAll()` is executed if *any* valid data source is found, or enforce it to ensure clean state.
- **Frontend (`MatchingDashboard.vue`)**:
    - (Already fixed, but verify) Ensure no hardcoded fallbacks remain.

## Impact
- **Backend**: `RAGDataSeeder` will now successfully load data in Docker environments.
- **Database**: All previous data will be WIPED on restart and replaced with CSV data.

## ADDED Requirements
### Requirement: Docker Compatibility
The system SHALL load initial data correctly when running in a Docker container.

### Requirement: Data Purity
The system SHALL delete all existing achievements before loading new ones from the CSV to ensure no "fake" data remains.

## MODIFIED Requirements
### Requirement: CSV Loading Path
The `RAGDataSeeder` SHALL look for the expert CSV in:
1. `/app/datasets/` (Docker)
2. `e:\数据要素大赛作品\数据集\` (Local Dev fallback)
3. Relative path `./数据集/` (Project root fallback)
