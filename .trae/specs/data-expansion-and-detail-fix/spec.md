# Data Expansion & Detail View Fix Spec

## Why
The user reported two critical issues:
1.  **Detail View Failure**: Clicking "View Details" results in a white screen and error message, blocking core functionality.
2.  **Missing/Low-Quality Data**: The previously promised 600+ data items are not visible. The user requires high-quality, AI-generated simulation data (300+ achievements, 50+ demands) using a specific DeepSeek model to ensure professional terminology and logical consistency.
3.  **Specific Matching Scenarios**: The user explicitly requested that some demands must have a pre-designed distribution of matching achievements (e.g., one ~95%, one ~80%, two ~70%).

## What Changes

### Backend
1.  **Fix Detail API**:
    - Debug and fix `DemandController.getDemandById` and `AchievementController.getAchievementById`.
    - Ensure robust error handling for missing IDs or data format issues.
2.  **Advanced Data Generation**:
    - Create a new generation script/service (`DataGeneratorService.java` or Python script) using the **DeepSeek-R1-0528-Qwen3-8B** API.
    - **API Key**: `sk-zzfnrqhpsxrvkutyjqbkfukflugzkurpwbhmopzpsxygukqm`
    - **Logic**:
        - Generate 50+ high-quality **Demands**.
        - For each demand, generate a specific set of **Achievements**:
            - 1x High Match (90-99% relevance).
            - 1x Medium Match (75-85% relevance).
            - 2-3x Low Match (60-75% relevance).
            - Random filler achievements to reach 300+ total.
    - **Persistence**: Ensure all generated data is correctly saved to the H2 database on startup (or via a specific admin endpoint).

### Frontend
1.  **Fix Detail Pages**:
    - Update `DemandDetail.vue` and `AchievementDetail.vue` to correctly handle API responses and loading states.
    - Fix the "white screen" issue (likely caused by unhandled nulls or API errors).
2.  **Data Visibility**:
    - Ensure the Search/List pages can display the expanded dataset (verify pagination/infinite scroll).

## Impact
- **Affected Specs**: Data Management, Detail View.
- **Affected Code**:
    - `backend/.../DataInitializer.java` (or new `DataGenerator.java`)
    - `backend/.../controller/DemandController.java`
    - `backend/.../controller/AchievementController.java`
    - `frontend/.../views/DemandDetail.vue`
    - `frontend/.../views/AchievementDetail.vue`

## ADDED Requirements
### Requirement: Targeted Data Generation
The system SHALL generate data pairs where specific demands have pre-calculated matching achievements with varying degrees of relevance (High/Medium/Low).

### Requirement: DeepSeek Integration
The data generation process MUST use the `deepseek-ai/DeepSeek-R1-0528-Qwen3-8B` model via the provided API key.

## MODIFIED Requirements
### Requirement: Detail View Reliability
The Detail View SHALL load successfully for all valid IDs and display a "Not Found" message for invalid IDs, instead of crashing or showing a white screen.
