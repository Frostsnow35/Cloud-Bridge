# Tasks

- [x] Task 1: Update MatchingService with Weighted Scoring Logic
  - [ ] Refactor `match` method to calculate scores.
  - [ ] Implement `ScoredAchievement` inner class.
  - [ ] Add strict domain filtering (Score >= 100 threshold for field).
  - [ ] Sort results by score descending.

- [ ] Task 2: Enhance Knowledge Graph Data
  - [ ] Update `backend/data/generate_data.py` (or create a new seeder `RAGDataSeeder.java` improvement) to populate the Knowledge Graph with missing fields and relationships (AI, Big Data, etc.).
  - [ ] Ensure `Technology` nodes are linked to `Field` nodes for AI and related domains.

- [ ] Task 3: Verify the Fix
  - [ ] Run the test case: "Medical demand matching with Transport achievement".
  - [ ] Confirm no Transport achievements appear in top results.
