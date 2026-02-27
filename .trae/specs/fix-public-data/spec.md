# Fix Public Data Issues Spec

## Why
The user reports two issues:
1. "Public Data Open Platform" (公共平台) only shows 2 items despite the CSV having many entries. This indicates `RAGDataSeeder` failed to load the CSV, likely due to encoding or path issues, causing the backend to fallback to hardcoded mocks.
2. The detail page for these platforms needs to provide "Application/Query" channels, not just display data. Since the CSV lacks URLs, we need to add a "Apply for Access" button that simulates this action.

## What Changes
- **Backend**:
    - **RAGDataSeeder.java**:
        - Fix `seedPublicPlatformsFromCSV` to handle potential encoding issues (try GBK/UTF-8).
        - Add better error logging for CSV parsing.
        - Ensure it doesn't fail silently.
- **Frontend**:
    - **ResourceDetail.vue**:
        - Update the `handleAction` for `public_platforms` to show a more specific message or modal about "Applying for access".
        - Ensure the button text says "申请数据访问" (Apply for Data Access) or "前往数据门户" (Go to Data Portal).

## Impact
- **RAGDataSeeder.java**: More robust CSV loading.
- **ResourceDetail.vue**: Better UX for public platforms.

## ADDED Requirements
### Requirement: Robust CSV Loading
The system SHALL attempt to read `广州市白云区公共数据开放计划.csv` using correct encoding (likely GBK or UTF-8 with BOM) to ensure all 50+ records are loaded.

### Requirement: Public Platform Detail Action
For "Public Data Open Platform" items, the detail page SHALL provide an "Apply for Access" button. Clicking it SHALL display a message guiding the user (e.g., "Redirecting to government portal..." or "Application submitted").
