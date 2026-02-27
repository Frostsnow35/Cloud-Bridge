# Optimize Matching Logic for Brain-Computer Interface (BCI) Spec

## Why
The current matching logic produces irrelevant results for specific queries like "Brain-Computer Interface" (脑机接口).
- **Issue 1 (Broad Domains)**: BCI is classified under "Artificial Intelligence" or "Bio-medicine", causing it to match loosely related projects like "Industrial Defect Detection" (also AI) or generic medical devices.
- **Issue 2 (Weak Goal Alignment)**: The system matches based on broad keywords/fields without verifying if the *technical goal* (e.g., rehabilitation vs. manufacturing) aligns.
- **Issue 3 (Noisy Graph)**: The knowledge graph extraction generates "irrelevant or bizarre" connections, polluting the matching score with low-quality paths.

## What Changes
- **Refine `AIService` Prompts (Simulated Fine-tuning)**:
  - Update `extractKeywords` prompt to extract **Sub-Field** (二级领域) and **Application Scenario** (应用场景).
  - Update `extractGraphData` prompt to enforce stricter relationship types and filter out weak associations (e.g., limit to `BELONGS_TO`, `APPLIED_IN`, `REQUIRES`).
  - Add Few-Shot examples specifically for "Brain-Computer Interface" to guide the model.
- **Enhance `MatchingService` Logic**:
  - Introduce **Sub-Field Matching**: If a sub-field is identified, require it to match or give a massive score penalty if mismatched.
  - Introduce **Scenario Matching**: Compare extracted "Application Scenario" of demand vs. achievement.
  - **Graph Relevance Filter**: Only count graph paths that are semantically relevant to the core technical goal.
- **Data Enhancement (if applicable)**:
  - Ensure `generate_data.py` (or existing data) has `subField` or `application` attributes populated (or extract them on the fly). *Note: For this task, we focus on the matching logic and extraction.*

## Impact
- **Affected Specs**: Search & Matching.
- **Affected Code**:
  - `backend/src/main/java/com/cloudbridge/service/ai/AIService.java`: Prompt updates.
  - `backend/src/main/java/com/cloudbridge/service/MatchingService.java`: Scoring logic updates.
  - `backend/src/main/java/com/cloudbridge/dto/AIResponse.java` (or related DTOs): Add `subField`, `application` fields.

## ADDED Requirements
### Requirement: Granular Domain Matching
The system SHALL extract a `subField` (e.g., "脑机接口", "计算机视觉") and `applicationScenario` (e.g., "医疗康复", "工业检测") from the user demand.
#### Scenario: BCI Search
- **WHEN** user searches for "脑机接口"
- **THEN** the system identifies `subField="脑机接口"` and `application="医疗/康复"`.
- **AND** the system filters out achievements with `subField="工业视觉"` or `application="工业检测"`.

### Requirement: Strict Graph Connection
The system SHALL only build graph connections that represent **direct technical dependencies** or **explicit application relationships**.
#### Scenario: Graph Construction
- **WHEN** extracting graph for "脑机接口"
- **THEN** it should NOT connect to "Industrial Safety" or "Traffic Control" unless explicitly mentioned.

## MODIFIED Requirements
### Requirement: Matching Scoring Algorithm
The scoring algorithm SHALL be updated to prioritize **Sub-Field** and **Application Scenario** matches over broad **Field** matches.
- **Old**: Field Match (+100) -> Keyword Match (+50)
- **New**: Sub-Field Match (+200) -> Application Match (+150) -> Field Match (+50) -> Keyword Match (+50)

## REMOVED Requirements
### Requirement: Broad Field-Only Matching
**Reason**: Causes too many false positives for specialized technologies.
**Migration**: Downgrade broad field match score; rely on sub-field/keyword intersection.
