# Strict Matching & Real AI Integration Spec

## Why
Users reported severe mismatches (e.g., "Carbon Capture" matching "mRNA Vaccine") and suspicion of fake results. The current system relies on a "fallback to latest" mechanism when AI matching fails or returns low scores, and the frontend artificially inflates scores (random 75-95%). This destroys user trust.

## What Changes
- **Backend**:
  - Integrate real Qwen2.5-72B AI model for accurate keyword and field extraction.
  - Implement **Strict Domain Filtering**: If a demand has a specific field (e.g., "Carbon Neutrality"), ONLY achievements in that field (or strictly related) will be returned.
  - **Remove Fallback**: If no matches are found, return an empty list with a "No Result" status, rather than recommending unrelated latest items.
  - **Real Scoring**: Return the actual calculated match score (0-100) based on field, keyword, and semantic similarity.
- **Frontend**:
  - Remove random score generation. Display the raw score from the backend.
  - Handle "No Results" state gracefully with a clear message.

## Impact
- **Affected Specs**: Search and Matching logic.
- **Affected Code**: 
  - `backend/.../MatchingService.java`
  - `backend/.../AIService.java`
  - `backend/resources/application.yml`
  - `frontend/.../MatchingDashboard.vue`

## ADDED Requirements
### Requirement: Strict Domain Matching
The system SHALL filter achievements such that the `field` of the achievement strictly matches the `field` of the demand.
- **Scenario**: User searches for "Carbon Capture" (Field: 碳中和).
- **Result**: Only achievements with `field="碳中和"` are candidates. "生物医药" achievements are excluded.

### Requirement: Real AI Integration
The system SHALL use the Qwen2.5-72B API to extract structured `keyword` and `field` from the user's natural language description.

### Requirement: Transparent Scoring
The system SHALL return the calculated match score.
- **Formula**: Field Match (40%) + Keyword Match (40%) + Semantic Bonus (20%).
- **Constraint**: No artificial inflation (no `Math.random()`).

## MODIFIED Requirements
### Requirement: Fallback Logic
**OLD**: If < 5 matches, fill with latest achievements.
**NEW**: Do NOT fill with unrelated achievements. If 0 matches, return 0.

## Configuration
- **Model**: `Qwen2.5-72B` (or `qwen2.5-72B` as per API).
- **API Key**: `sk-my-secret-key-888`
