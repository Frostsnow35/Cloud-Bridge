# Tasks

- [ ] Task 1: Update `AIService` to support granular extraction
  - [ ] SubTask 1.1: Modify `extractKeywords` prompt to include `subField` and `application` extraction with BCI few-shot examples.
  - [ ] SubTask 1.2: Modify `extractGraphData` prompt to enforce stricter relationship types (`BELONGS_TO`, `APPLIED_IN`, `REQUIRES`) and reduce hallucination.
  - [ ] SubTask 1.3: Update `AIResponse` parsing logic to handle new JSON fields.
  - [ ] SubTask 1.4: Implement `evaluateMatch` method in `AIService` for semantic comparison of Demand vs Achievement.

- [ ] Task 2: Update `MatchingService` scoring logic
  - [ ] SubTask 2.1: Integrate `evaluateMatch` (AI Reranking) into the matching pipeline for top candidates.
  - [ ] SubTask 2.2: Implement scoring rules: High reward for `subField` match, penalty for `application` mismatch.
  - [ ] SubTask 2.3: Refine graph matching to only count nodes that align with the `subField`.

- [ ] Task 3: Verification
  - [ ] SubTask 3.1: Create a test case for "脑机接口" (BCI) input.
  - [ ] SubTask 3.2: Verify that "Industrial Defect Detection" achievements are ranked low or filtered out.
  - [ ] SubTask 3.3: Verify that relevant BCI achievements (if any, or mock ones) are ranked high.
