# Tasks

- [ ] Task 1: Clean up `RAGDataSeeder.java`
  - [ ] Remove `seedPolicies`, `seedFunds`, `seedEquipments`, `seedExperts`, `seedPatents`, `seedEnterprises`.
  - [ ] Remove `loadMockData` and `hasData` helper methods.
  - [ ] Ensure `run` method only calls `seedAchievements` and `seedPublicPlatforms`.

- [ ] Task 2: Implement Real Achievement Seeding
  - [ ] Rename `seedAchievementsFromExpertCSV` to `seedAchievementsFromProjectCSV`.
  - [ ] Update path to `广州市白云区省、市级科技项目立项名单.csv`.
  - [ ] Implement parsing logic for headers: `承担单位,级别,序号,项目名称,项目批次,支持方向`.
  - [ ] Map: `title`=`项目名称`, `institution`=`承担单位`, `field`=`支持方向`, `description`=`项目批次 + 级别`.

- [ ] Task 3: Implement Derived Test Demands
  - [ ] Create `seedTestDemands` method in `RAGDataSeeder`.
  - [ ] Logic: Query `AchievementRepository` for 3 random achievements.
  - [ ] Create 3 Demands:
    - 1. **Perfect Match**: Title includes Achievement Title keywords.
    - 2. **Field Match**: Title includes Achievement Field.
    - 3. **Cross-Domain**: Mix of two fields.
  - [ ] Save these Demands to `DemandRepository`.

- [ ] Task 4: Verify Public Platform Seeding
  - [ ] Ensure `seedPublicPlatformsFromCSV` uses the correct path resolution strategy (Docker/Local/Relative).
