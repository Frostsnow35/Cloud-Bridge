# Tasks

- [x] **Task 1: Enhance SearchService for Full-Spectrum RAG**
  - [x] SubTask 1.1: Add `searchAll` method to `SearchService` that executes parallel search across `policies`, `experts`, `funds`, `equipments`, `patents`, `enterprises` indices.
  - [x] SubTask 1.2: Refactor `search` method to handle multi-index query more efficiently (or just loop through indices).
  - [x] SubTask 1.3: Add `getById` method for generic retrieval of any resource type (needed for Graph generation).

- [x] **Task 2: Upgrade AIService for Deep Analysis**
  - [x] SubTask 2.1: Implement `analyzeDemandWithFullRAG(String query)`:
    - Retrieve context from all 6 libraries using `searchAll`.
    - Construct a comprehensive prompt with categorized context (Policy, Fund, Tech, Talent, Biz).
    - Generate a structured analysis report (JSON or Markdown).
  - [x] SubTask 2.2: Implement `generateResourceGraph(String type, String id)`:
    - Retrieve the target entity.
    - Search for related entities (e.g., same `field`, `industry`, or `keyword`).
    - Construct a node-link JSON structure for visualization.

- [x] **Task 3: Update Intent Recognition**
  - [x] SubTask 3.1: Modify `chatWithIntent` prompt to include new intents: `FIND_PATENT`, `FIND_ENTERPRISE`, `FIND_FUND`, `FIND_EXPERT`.
  - [x] SubTask 3.2: Add routing logic for new intents in `AIController` or frontend handling.

- [x] **Task 4: Expose AI Capabilities via API**
  - [x] SubTask 4.1: Update `AIController` to add `/api/ai/analyze-full` endpoint.
  - [x] SubTask 4.2: Update `AIController` to add `/api/ai/graph/{type}/{id}` endpoint.

- [x] **Task 5: Verification**
  - [x] SubTask 5.1: Verify `analyzeDemandWithFullRAG` returns valid report for "Carbon Fiber".
  - [x] SubTask 5.2: Verify `generateResourceGraph` returns valid JSON structure.
