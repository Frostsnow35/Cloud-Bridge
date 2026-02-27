# Comprehensive Optimization Plan Spec

## Why
Current project review reveals gaps between the competition requirements and the actual implementation. Key areas needing improvement include AI matching depth (currently string-based), blockchain evidence completeness (mock data usage), data quality (limited test data), and user experience (basic interface). This plan aims to bridge these gaps to meet the competition standards for "Data Gathering in Baiyun · Smart Creation for the Future".

## What Changes
- **AI Engine Integration**: Replace simple string matching with ChatGLM-6B API for semantic understanding and keyword extraction.
- **Knowledge Graph Enhancement**: Implement multi-hop queries in Neo4j to find hidden relationships between technologies and industries.
- **Blockchain Deployment**: Transition from MockBlockchainService to a real FISCO BCOS test network (2 nodes).
- **Evidence Expansion**: Add evidence recording for technical disclosures, contract signing, and project delivery.
- **Data Quality Upgrade**: Import real-world data from 9 strategic emerging industries (Bio-medicine, New Materials, etc.) and standardize it.
- **UX Visualization**: Introduce ECharts for visualizing matching results and evidence chains.

## Impact
- **Backend**:
    - `MatchingService`: Refactor to use AI API and advanced Cypher queries.
    - `BlockchainService`: Integrate `fisco-bcos-java-sdk` and deploy smart contracts.
    - `DataInitializer`: Update to load high-quality industry data.
- **Frontend**:
    - `MatchingDashboard.vue`: Add ECharts components for result visualization.
    - `Evidence.vue`: Display real transaction hashes and block details.
- **Database**:
    - Neo4j: Update schema for deeper relationships.
    - MySQL: Add tables for new evidence types if needed.

## ADDED Requirements
### Requirement: AI Semantic Matching
The system SHALL use an LLM (e.g., ChatGLM-6B) to extract keywords and intent from user demands.
#### Scenario: User searches for "high-efficiency solar panels"
- **WHEN** user submits the demand
- **THEN** the system extracts "solar energy", "photovoltaic", "high efficiency" and queries the graph for related technologies, not just exact string matches.

### Requirement: Judicial-Level Blockchain Evidence
The system SHALL record critical actions (publish, match, sign) on a FISCO BCOS blockchain.
#### Scenario: Contract Signing
- **WHEN** users sign a technical cooperation contract
- **THEN** the contract hash, timestamp, and signer IDs are written to the blockchain, returning a transaction hash verifiable on a block explorer.

## MODIFIED Requirements
### Requirement: Data Quality
**Original**: Use mock data for testing.
**New**: Use curated data from 9 strategic industries.
**Reason**: To demonstrate real-world applicability in the competition.

### Requirement: User Interface
**Original**: Basic list view for results.
**New**: Visual graph view for matching paths.
**Reason**: To improve interpretability and user engagement.
