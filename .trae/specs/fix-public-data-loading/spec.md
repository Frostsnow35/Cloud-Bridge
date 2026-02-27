# Fix Public Data Loading (Critical)

## Why
The "Public Platforms" list is currently empty because the backend service was not restarted after updating the data seeding logic (`RAGDataSeeder.java`). The `CommandLineRunner` which loads the CSV only runs on application startup. Since the mock data was removed, the fallback is empty, resulting in "No Data".

## What Changes
- **Process Management**:
    - **Restart Backend**: Stop the currently running `mvn spring-boot:run` process and start it again. This will trigger the updated `RAGDataSeeder` to execute the fixed CSV parsing logic (GBK support, relaxed column check).
- **Verification**:
    - **Log Inspection**: Verify startup logs for "Seeded X public platforms from CSV".
    - **API Check**: Call `/api/libraries/public_platforms` to confirm data is returned.

## Impact
- **Backend**: No code changes required (if previous code was correct), just a restart.
- **Frontend**: Should display ~50 items instead of 0 or 2.

## ADDED Requirements
### Requirement: Service Restart
The backend service SHALL be restarted to apply the changes to `RAGDataSeeder` and populate the in-memory search index.

### Requirement: Data Verification
The system SHALL verify that the API returns a non-empty list for "public_platforms" after restart.
