# Admin Evidence Management Spec

## Why
The administrator currently lacks visibility into the blockchain evidence records. The requirement document specifies an "Evidence Management Module" for visualizing, querying, and exporting evidence records. To achieve this efficiently without scanning the blockchain for every query, we need to synchronize evidence metadata to a local database.

## What Changes
- **Database**: New table `evidence_records` to store `hash`, `txHash`, `owner`, `type`, `metadata`, `timestamp`.
- **Backend API**:
  - `POST /api/evidence/store`: Updated to save record to DB after blockchain success.
  - `GET /api/evidence/list`: New endpoint for admins to list all evidence.
- **Frontend**:
  - `AdminDashboard.vue`: Add "Evidence Management" tab.
  - Display list of evidence with filtering.
  - "View on Chain" button linking to external browser.

## Impact
- **Affected Code**: `EvidenceController`, `AdminDashboard.vue`.
- **New Code**: `EvidenceRecord.java`, `EvidenceRepository.java`.

## ADDED Requirements
### Requirement: Evidence Synchronization
- **WHEN** evidence is successfully stored on blockchain,
- **THEN** the system SHALL save a record in the local database with transaction details.

### Requirement: Admin List View
- **WHEN** admin accesses the dashboard,
- **THEN** they can view a paginated list of all evidence records.
