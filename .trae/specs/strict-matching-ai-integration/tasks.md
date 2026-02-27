# Tasks

- [x] Task 1: Configure Backend for Real AI
  - [x] Update `application.yml` with the correct API Key (`sk-my-secret-key-888`) and URL.
  - [x] Verify `AIService.java` request format matches the Qwen OpenAI-compatible API.

- [x] Task 2: Implement Strict Matching Logic in Backend
  - [x] Modify `MatchingService.java` to enforce strict field filtering.
  - [x] Remove the "Fallback to Latest Achievements" logic.
  - [x] Improve scoring algorithm to be more granular (0-100) and realistic.
  - [x] Ensure `matches` list is only populated with high-confidence hits.

- [x] Task 3: Update Frontend to Show Real Data
  - [x] Modify `MatchingDashboard.vue` to remove `Math.random()` score generation.
  - [x] Bind `item.score` directly to the backend response.
  - [x] Ensure the "No Results" state is displayed correctly if the strict filter returns nothing.

- [ ] Task 4: Verification
  - [ ] Test with "Carbon Capture" -> Should NOT see "mRNA Vaccine".
  - [ ] Test with "Medical" -> Should see "mRNA Vaccine".
  - [ ] Verify scores are consistent (not changing on refresh).
