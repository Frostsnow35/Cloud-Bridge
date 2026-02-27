# Tasks

- [ ] Task 1: Fix Detail View API & Frontend
    - [ ] Debug `DemandController` and `AchievementController` to identify the cause of the "white screen/failure".
    - [ ] Fix the API implementation to return correct data structures.
    - [ ] Update `DemandDetail.vue` and `AchievementDetail.vue` to handle data loading and errors gracefully.
    - [ ] Verify that clicking "View Details" works for existing data.

- [ ] Task 2: Implement DeepSeek Data Generation Script
    - [ ] Create a Python script (`generate_smart_data.py`) or Java Service to interact with DeepSeek API.
    - [ ] Implement the "Matched Pair" logic: Generate a demand, then generate corresponding High/Medium/Low achievements.
    - [ ] Generate 50+ Demands and 300+ Achievements with professional terminology and logical fields.
    - [ ] Save the generated data to `backend/data/smart_demands.json` and `backend/data/smart_achievements.json`.

- [ ] Task 3: Load and Verify Data
    - [ ] Update `DataInitializer.java` to load the new `smart_*.json` files into the H2 database.
    - [ ] Restart backend and verify data count (300+ achievements, 50+ demands).
    - [ ] Verify that the "Matched Pairs" actually result in the expected scores (95%, 80%, 70%) in the Matching Dashboard.

- [ ] Task 4: Final Verification
    - [ ] Verify 300+ items are visible in the frontend list (pagination check).
    - [ ] Verify detail pages for new data work correctly.
