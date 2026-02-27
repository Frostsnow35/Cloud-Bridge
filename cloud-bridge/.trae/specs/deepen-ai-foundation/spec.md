# Deepen AI Technology Foundation Spec

## Why
The current AI capabilities are limited to basic retrieval and single-turn analysis. With the recent expansion of data dimensions (Patents, Enterprises, Funds, etc.), the platform now holds rich, interconnected data. However, the AI does not yet fully leverage this interconnectedness. To realize the "Deep Matching" and "One-Stop Service" vision, we need to upgrade the AI foundation to support **Cross-Domain RAG** and **simulated Graph Reasoning**.

## What Changes
- **Unified Cross-Domain RAG**: Extend the RAG engine to retrieve context from ALL 6 libraries (Policies, Experts, Funds, Equipments, Patents, Enterprises) simultaneously.
- **"Deep Analysis" Capability**: New AI workflow that not only matches supply/demand but provides a holistic "Feasibility Report" connecting technology (Patents) to resources (Funds/Policies).
- **Simulated Graph-RAG**: A new "Resource Graph" generator that dynamically links entities (e.g., Expert -> Patent -> Enterprise) based on search results, providing a "Knowledge Map" for any given resource.

## Impact
- **Backend**:
  - `SearchService`: Update to support multi-index parallel search.
  - `AIService`: Refactor `analyzeDemandWithRAG` to consume broader context.
  - `AIService`: Add `generateResourceGraph` method.
  - `AIController`: Add new endpoints for deep analysis and graph data.
- **Frontend**:
  - No major UI changes in this spec (backend focus), but data will be ready for visualization.

## ADDED Requirements
### Requirement: Full-Spectrum RAG Analysis
The system SHALL be able to accept a user query (e.g., "I want to build a carbon fiber plant") and retrieve relevant records from **Patents**, **Enterprises**, **Experts**, **Policies**, **Funds**, and **Equipments** to generate a comprehensive advisory report.

#### Scenario: Investment Advice
- **WHEN** user asks "What support is available for Carbon Fiber startups?"
- **THEN** system retrieves:
  - **Policies**: Tax breaks for new materials.
  - **Funds**: "New Material Industry Fund".
  - **Patents**: Recent expiry/open patents.
  - **Experts**: Top researchers in the field.
- **AND** system generates a report structured by "Policy Support", "Financial Aid", and "Technical Partners".

### Requirement: Dynamic Resource Graph
The system SHALL provide an API to generate a node-link structure (JSON) centered around a specific entity.
- **Input**: Entity Type (e.g., "Patent"), Entity ID.
- **Output**: JSON with `nodes` and `links`.
- **Logic**:
  - If input is Patent P1:
    - Find Inventor E1 (Expert) -> Link P1-E1.
    - Find Assignee C1 (Enterprise) -> Link P1-C1.
    - Find Similar Patents P2, P3 -> Link P1-P2.

## MODIFIED Requirements
### Requirement: Enhanced Intent Recognition
Update `chatWithIntent` to support navigation to new resource libraries (Patents, Enterprises, etc.).
