# Tasks

- [x] **Task 1: Define and Implement Data Schemas**
  - [x] SubTask 1.1: Create JSON schema files for Policy, Fund, Equipment, Expert, and KG Entity in `backend/src/main/resources/schemas/`.
  - [x] SubTask 1.2: Create Java POJOs (DTOs) for these schemas in `com.cloudbridge.dto`.

- [x] **Task 2: RAG Implementation (Elasticsearch)**
  - [x] SubTask 2.1: Create Elasticsearch indices for `policies`, `funds`, `equipments`, `experts`.
  - [x] SubTask 2.2: Implement `SearchService` to handle multi-index retrieval (keyword + vector if possible, but keyword first for MVP).
  - [x] SubTask 2.3: Create a `DataSeeder` to load sample data (mock data based on report examples) into ES.

- [x] **Task 3: Prompt Engineering & AIService Upgrade**
  - [x] SubTask 3.1: Refactor `AIService` to accept "Context" (retrieved documents).
  - [x] SubTask 3.2: Rewrite `extractGraphData` prompt to use Chain-of-Thought (CoT) and explicit schema constraints.
  - [x] SubTask 3.3: Rewrite `matchSupplyDemand` prompt (or similar analysis prompt) to include RAG context (Policies, Funds).
  - [x] SubTask 3.4: Implement "Few-Shot" examples in prompts for "New Materials" and "Bio-medicine" domains.

- [x] **Task 4: Synthetic Data Generation Tool**
  - [x] SubTask 4.1: Create a script/service `DataSynthesisService` that takes a list of "Seed Questions/Inputs".
  - [x] SubTask 4.2: Implement logic to run the full RAG+CoT pipeline for each seed.
  - [x] SubTask 4.3: Save the `(Input, Generated_CoT_Output)` pairs to a `training_data.jsonl` file.

- [x] **Task 5: Verification**
  - [x] SubTask 5.1: Verify KG extraction on a sample "Carbon Fiber" text (check for correct categorization).
  - [x] SubTask 5.2: Verify RAG analysis on a sample "Funding" query (check for inclusion of specific policy names).

- [x] **Task 6: Scale Up Mock Data**
  - [x] SubTask 6.1: Develop `backend/scripts/generate_mock_data.js` (Node.js) for offline batch generation via LLM (migrated from Python due to env issues).
  - [x] SubTask 6.2: Update `RAGDataSeeder.java` to bulk-import `backend/data/*.jsonl` files on startup.
