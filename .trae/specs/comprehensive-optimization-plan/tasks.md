# Tasks

- [x] Task 1: AI Engine Integration & Enhancement: Implement LLM-powered matching and keyword extraction.
  - [x] SubTask 1.1: Integrate ChatGLM-6B API client in `AIService.java` for keyword extraction and intent analysis.
  - [x] SubTask 1.2: Refactor `MatchingService.java` to use AI-extracted keywords for graph queries.
  - [x] SubTask 1.3: Update `MatchingService` to perform multi-hop Cypher queries in Neo4j for related technologies.
  - [x] SubTask 1.4: Validate AI matching results with test cases from the competition domain (e.g., Bio-medicine, New Materials).

- [x] Task 2: Blockchain Evidence System Upgrade: Deploy real blockchain network and extend evidence scenarios.
  - [x] SubTask 2.1: Set up a local/cloud FISCO BCOS test network (2 nodes) and configure `fisco-bcos-java-sdk`.
  - [x] SubTask 2.2: Implement `BlockchainService.java` to replace `MockBlockchainService` with real SDK calls.
  - [x] SubTask 2.3: Extend `EvidenceController` to handle evidence for technical disclosures, contract signing, and project delivery.
  - [x] SubTask 2.4: Create a simple block explorer interface or link to an external explorer for verification.

- [x] Task 3: Data Quality & Standardization: Import real-world industry data and build comprehensive graph.
  - [x] SubTask 3.1: Collect and clean data for 9 strategic emerging industries (20-50 high-quality records per industry).
  - [x] SubTask 3.2: Create data import scripts (`generate_real_data.py` or similar) to populate Neo4j and MySQL.
  - [x] SubTask 3.3: Define and implement cross-domain relationships in the knowledge graph (e.g., Technology -> Industry -> Application).

- [x] Task 4: User Experience & Visualization: Enhance frontend with visual components.
  - [x] SubTask 4.1: Integrate ECharts into `MatchingDashboard.vue` to visualize matching paths and technology clusters.
  - [x] SubTask 4.2: Update `Evidence.vue` to display real-time blockchain transaction details (hash, block height).
  - [x] SubTask 4.3: Add loading states and error handling feedback throughout the application.

# Task Dependencies
- [Task 1] depends on [Task 3] (Data is needed for AI matching validation)
- [Task 2] is independent but critical for evidence features.
- [Task 4] depends on [Task 1] and [Task 2] for data to visualize.
