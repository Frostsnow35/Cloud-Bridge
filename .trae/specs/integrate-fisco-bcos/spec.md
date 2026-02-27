# FISCO BCOS Integration Spec

## Why
The current system uses `MockBlockchainService` which stores evidence in memory. The project requirement (per "云转桥" declaration) explicitly mandates using **FISCO BCOS** alliance chain for judicial-grade evidence storage. We need to replace the mock implementation with a real blockchain integration.

## What Changes
- **Dependency**: Add `fisco-bcos-java-sdk` to `pom.xml`.
- **Smart Contract**: Enhance `Evidence.sol` to support judicial requirements (Signer ID, Category, Extended Metadata).
- **Configuration**: Add FISCO BCOS connection settings (peers, keys) to `application.yml`.
- **Service Layer**: Implement `FiscoBcosService` replacing `MockBlockchainService`.
- **Tooling**: Generate Java Contract Wrapper for the updated Solidity contract.

## Impact
- **Affected Specs**: Blockchain interface completeness.
- **Affected Code**:
  - `backend/pom.xml`
  - `backend/src/main/resources/application.yml`
  - `deploy/contracts/Evidence.sol`
  - `backend/src/main/java/com/cloudbridge/service/impl/FiscoBcosService.java` (New)
  - `backend/src/main/java/com/cloudbridge/service/impl/MockBlockchainService.java` (Deprecated/Removed)

## ADDED Requirements
### Requirement: Real Blockchain Integration
The system SHALL connect to an external FISCO BCOS node using the Java SDK.

#### Scenario: Store Evidence
- **WHEN** user calls `/api/evidence/store`
- **THEN** the system sends a transaction to the FISCO BCOS network.
- **AND** returns the real Transaction Hash from the chain.

### Requirement: Judicial-Grade Evidence
The Smart Contract SHALL store:
- `evidenceHash`: The file/data hash.
- `evidenceType`: Category of evidence (e.g., "CONTRACT", "TECH_DOC").
- `signerId`: Identity of the uploader (User ID).
- `timestamp`: Blockchain block timestamp.
- `signature`: Digital signature of the uploader (optional for now, but field needed).

## MODIFIED Requirements
### Requirement: Evidence Verification
- **Old**: Checked in-memory map.
- **New**: Query the Smart Contract state on the blockchain to verify existence and integrity.
