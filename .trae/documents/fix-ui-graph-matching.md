# Fix UI, Graph, and Matching Spec

## Why
The user identified three critical issues:
1.  **UI**: Pagination is unreadable (grey on white/yellow).
2.  **Missing Features**: No way to upload achievements or publish needs.
3.  **Core Logic**: The knowledge graph is confusing and matching is inaccurate (e.g., "Heart Disease" matches "Artificial Bone").

## What Changes

### 1. UI Fixes
- **Frontend**: Override Element Plus pagination styles in `src/assets/main.css` or specific views to use high-contrast colors (e.g., white text on dark background, distinct active state).

### 2. Feature Implementation (Publishing)
- **Frontend**:
    - Create/Update `PublishAchievement.vue` and `PublishNeed.vue`.
    - Add file upload support (mock or real if backend supports).
    - Add fields for "Maturity", "Price", "Patent Association" (optional).
- **Backend**:
    - Ensure `AchievementController` and `DemandController` have `create` endpoints handling these fields.

### 3. Core Logic Optimization (Graph & Matching)
- **Graph Logic (`AIService.java`)**:
    - **Current**: Generates a loose graph based on keyword search.
    - **New**: Generate an **Explanatory Graph**.
        - Nodes: `User Demand` -> `Key Technology` -> `Matched Achievement`.
        - Edges: `REQUIRES`, `MATCHES`.
        - **Strict filtering**: Only include nodes that are part of the *high-score* matching path.
- **Matching Logic (`AIService.java` & `SmartMatchController.java`)**:
    - **Current**: Likely keyword-based or low-threshold semantic search.
    - **New**: **Strict Semantic Matching**.
        - Use AI to evaluate "Technical Goal" consistency.
        - **Filter**: If AI score < 60 (Strict), do NOT show in main list.
        - **"Guess You Like"**: Show domain-consistent but goal-mismatched items in a separate section.

## Impact
- **Frontend**: New pages, style overrides.
- **Backend**: Significant logic change in `AIService` (graph generation, matching evaluation).

## ADDED Requirements
### Requirement: Strict Matching & "Guess You Like"
- **Main List**: Only show results where `Technical Goal` matches (AI Score >= 60).
- **Secondary List**: Show results where `Field` matches but `Technical Goal` differs (AI Score < 60 but Field matches).

### Requirement: Explanatory Graph
- The graph SHALL visualizes *why* the top result matches.
- Structure: `Demand` --(needs)--> `Tech Goal` --(provided by)--> `Achievement`.

### Requirement: Pagination Visibility
- Pagination text color SHALL be white/light grey.
- Active page background SHALL be distinct (e.g., primary blue) with white text.
