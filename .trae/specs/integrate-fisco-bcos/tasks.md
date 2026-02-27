# Tasks

- [x] Task 1: Add FISCO BCOS Dependencies
  - [x] SubTask 1.1: Update `backend/pom.xml` to include `org.fisco-bcos.java-sdk:fisco-bcos-java-sdk:2.9.1` (or latest stable).
  - [x] SubTask 1.2: Add BCOS configuration section to `backend/src/main/resources/application.yml`.

- [x] Task 2: Enhance Smart Contract
  - [x] SubTask 2.1: Modify `deploy/contracts/Evidence.sol` to add fields: `evidenceType`, `signerId`, `signature`.
  - [x] SubTask 2.2: (Manual/Script) Compile `Evidence.sol` to Java Wrapper `com.cloudbridge.contract.Evidence`. *Note: Since we lack the sol2java tool environment, we will generate a placeholder Wrapper class that mimics the SDK generated code structure for compilation, or ask user to provide it.* -> **Decision**: We will write a "Mock-but-Real-Structure" Java Wrapper manually if generation fails, or use a simplified `RawTransaction` approach if Wrapper generation is too complex for the agent environment. **Better**: We will implement the `FiscoBcosService` to use `BcosSDK`'s dynamic contract handling or write a standard Java Wrapper manually.

- [x] Task 3: Implement FiscoBcosService
  - [x] SubTask 3.1: Create `backend/src/main/java/com/cloudbridge/config/BcosConfig.java` to load SDK.
  - [x] SubTask 3.2: Create `backend/src/main/java/com/cloudbridge/service/impl/FiscoBcosService.java` implementing `BlockchainService`.
  - [x] SubTask 3.3: Implement `storeEvidence` using SDK to send transaction.
  - [x] SubTask 3.4: Implement `verifyEvidence` and `getEvidence` using SDK call.

- [x] Task 4: Switch Implementation
  - [x] SubTask 4.1: Annotate `MockBlockchainService` with `@Profile("mock")` or remove `@Service`.
  - [x] SubTask 4.2: Annotate `FiscoBcosService` with `@Service` and `@Primary` (or `@Profile("prod")`).

# Task Dependencies
- Task 3 depends on Task 1 and Task 2.
