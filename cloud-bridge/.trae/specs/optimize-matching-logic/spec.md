# Optimize Matching Logic Spec

## Why
Currently, the matching system produces irrelevant results (e.g., matching "Medical" demands with "Transportation" achievements) because it relies heavily on broad keyword matching without strict domain constraints. The user explicitly requested a fix for this behavior and approved a weighted scoring approach.

## What Changes
- Implement a **Weighted Scoring System** in `MatchingService`:
  - **Field Match (+100)**: Strict domain alignment.
  - **Keyword Match (+50)**: Core entity match.
  - **Graph/Related Match (+10/20)**: Contextual relevance.
- Implement **Strict Domain Filtering**:
  - If a clear domain is identified (Score >= 100), filter out low-score results from unrelated domains.
- Enhance **Knowledge Graph Data**:
  - Update data seeding to include missing fields (AI, Big Data, etc.) and relationships to support the scoring logic.

## Impact
- **Affected Specs**: `ai-capability-enhancement` (related but distinct)
- **Affected Code**: 
  - `backend/src/main/java/com/cloudbridge/service/MatchingService.java`
  - `backend/data/generate_data.py` (or equivalent data seeder)

## MODIFIED Requirements

### Requirement: Intelligent Matching
The system SHALL rank achievements based on a weighted score of:
1. **Field Compatibility**: Highest priority.
2. **Keyword Relevance**: High priority.
3. **Graph Connectivity**: Medium/Low priority.

#### Scenario: Cross-Domain Filtering
- **WHEN** a user inputs a demand clearly in the "Medical" field
- **THEN** the system MUST NOT recommend "Transportation" achievements unless they have a strong cross-domain application link (high graph score).
- **AND** the primary results should be dominated by "Medical" achievements.

## ADDED Requirements

### Requirement: Graph Data Coverage
The system SHALL contain Knowledge Graph nodes and relationships for key domains (AI, Big Data, IoT) to ensure the matching logic has sufficient data to traverse.
