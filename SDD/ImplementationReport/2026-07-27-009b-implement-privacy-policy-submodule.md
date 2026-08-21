# Implementation Report — Task 009b Privacy Policy Submodule

## Task And Execution

- Task: `009b DONE — Implement Privacy Policy Hexagonal Submodule`.
- Execution date: 27 July 2026.
- Implementation file: `SDD/implementation/implementation-order.md`.
- Completion state: complete.

## Documents Read

- `AGENTS.md`;
- `SDD/specs/sddSpec.md`;
- `SDD/specs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`;
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`;
- `SDD/plans/backendSpecs/privacyPolicySubmoduleBackendPlan.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/009b-DONE-implement-privacy-policy-submodule.md`.

## Files Created

- `src/main/java/com/househost/privacy/policy/domain/model/PrivacyPolicy.java`;
- `src/main/java/com/househost/privacy/policy/domain/model/PrivacyPolicyStatus.java`;
- `src/main/java/com/househost/privacy/policy/domain/model/PrivacyPolicyContentHash.java`;
- `src/main/java/com/househost/privacy/policy/domain/exception/PrivacyPolicyUnavailableException.java`;
- `src/main/java/com/househost/privacy/policy/application/dto/PrivacyPolicyRequestDTO.java`;
- `src/main/java/com/househost/privacy/policy/application/dto/PrivacyPolicyResponseDTO.java`;
- `src/main/java/com/househost/privacy/policy/application/dto/PublicPrivacyPolicyResponseDTO.java`;
- `src/main/java/com/househost/privacy/policy/application/records/PublishedPrivacyPolicyRecord.java`;
- `src/main/java/com/househost/privacy/policy/application/port/in/PrivacyPolicyUseCase.java`;
- `src/main/java/com/househost/privacy/policy/application/port/in/PublicPrivacyPolicyUseCase.java`;
- `src/main/java/com/househost/privacy/policy/application/port/out/PrivacyPolicyPersistencePort.java`;
- `src/main/java/com/househost/privacy/policy/application/port/out/PrivacyPolicyPublisherPort.java`;
- `src/main/java/com/househost/privacy/policy/application/port/out/PrivacyPolicyAuditPort.java`;
- `src/main/java/com/househost/privacy/policy/application/service/PrivacyPolicyService.java`;
- `src/main/java/com/househost/privacy/policy/application/service/PrivacyPolicyValidationService.java`;
- `src/main/java/com/househost/privacy/policy/application/service/PrivacyPolicyHashService.java`;
- `src/main/java/com/househost/privacy/policy/adapter/in/rest/PrivacyPolicyController.java`;
- `src/main/java/com/househost/privacy/policy/adapter/in/rest/PublicPrivacyPolicyController.java`;
- `src/main/java/com/househost/privacy/policy/adapter/in/config/PrivacyPolicyInitialContent.java`;
- `src/main/java/com/househost/privacy/policy/adapter/in/config/PrivacyPolicyCatalogInitializer.java`;
- `src/main/java/com/househost/privacy/policy/adapter/out/integration/PrivacyPolicyAuditAdapter.java`;
- `src/main/java/com/househost/privacy/policy/adapter/out/integration/UserPrivacyPolicyPublisherAdapter.java`;
- `src/main/java/com/househost/privacy/policy/adapter/out/persistence/PrivacyPolicyJpaRepository.java`;
- `src/main/java/com/househost/privacy/policy/adapter/out/persistence/PrivacyPolicyPersistenceAdapter.java`;
- `src/main/java/com/househost/privacy/policy/adapter/out/persistence/entity/PrivacyPolicyJpaEntity.java`;
- `src/main/java/com/househost/privacy/policy/adapter/out/persistence/entity/PrivacyPolicyPersistenceMapper.java`;
- `src/test/java/com/househost/privacy/policy/domain/model/PrivacyPolicyTest.java`;
- `src/test/java/com/househost/privacy/policy/application/service/PrivacyPolicyServiceTest.java`;
- `src/test/java/com/househost/privacy/policy/application/service/PrivacyPolicyValidationHashServiceTest.java`;
- `src/test/java/com/househost/privacy/policy/adapter/in/config/PrivacyPolicyCatalogInitializerTest.java`;
- `src/test/java/com/househost/privacy/policy/adapter/in/rest/PrivacyPolicyAuthorizationTest.java`;
- `src/test/java/com/househost/privacy/policy/adapter/out/integration/PrivacyPolicyAuditAdapterTest.java`;
- `src/test/java/com/househost/privacy/policy/adapter/out/persistence/PrivacyPolicyConcurrencyGuardTest.java`;
- `src/test/java/com/househost/privacy/policy/adapter/out/persistence/entity/PrivacyPolicyPersistenceMapperTest.java`;
- `src/test/java/com/househost/privacy/policy/architecture/PrivacyPolicyArchitectureTest.java`;
- `SDD/ImplementationReport/2026-07-27-009b-implement-privacy-policy-submodule.md`.

## Files Changed

- `src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java`;
- `src/main/java/com/househost/security/adapter/in/config/SecurityConfig.java`;
- `src/main/java/com/househost/shared/exception/GlobalExceptionHandler.java`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/backendSpecs/009b-DONE-implement-privacy-policy-submodule.md`.

## Flows Implemented

The new `privacy.policy` hexagon owns policy identity, lifecycle, validation,
canonicalization, hashing, persistence, publication, publisher resolution,
audit and public delivery. Drafts can be created and edited. Publication
validates and canonicalizes the document, calculates SHA-256, locks the current
publication, supersedes it when necessary and persists the selected draft as
the only current version.

Administrative endpoints support draft creation, update, publication, history
and detail. `SecurityConfig` restricts them to `CEO`, `CTO` and `ADMIN`.
`GET /public/privacy-policy` is unauthenticated and returns only ID, version,
title, canonical content, hash and effective time. A controlled HTTP 503 is
returned if no current publication exists.

The startup catalog converts the trustworthy static policy into the restricted
document format and seeds version 2 with effective time 26 July 2026. It does
not create version 1, touch old bookings or overwrite conflicting evidence.

## Technical And MVP Decisions

Canonical content is structured JSON schema version 1. Supported nodes are
paragraphs, lists and HTTP(S) links. Unknown fields and node types, raw markup,
script-like text and links without a valid HTTP(S) host are rejected. Object
keys are recursively sorted while array order remains meaningful; SHA-256 is
calculated over the resulting UTF-8 string as lowercase `sha256:<hex>`.

MySQL enforces one current publication with nullable unique `current_slot`.
Publication also uses a pessimistic write lock and reloads the draft after that
lookup to avoid stale concurrent republication. The initial publisher is the
lowest existing administrative user ID, not an invented identity.

Audit metadata contains version, status and content hash. Full content and
publisher contact data are excluded. The hash is an integrity fingerprint, not
a digital signature.

## Difficulties, Problems And Resolutions

Concurrent publication required an application transaction and a durable
database invariant. A nullable unique slot was combined with pessimistic
locking; a structural test protects both parts of this strategy.

The database stores timestamps in UTC while the application exposes its local
representation. Live verification confirmed the specified API effective time
`2026-07-26T00:00:00`.

## Tests And Verification

- focused `com.househost.privacy.policy` tests: passed;
- final full Maven suite: 111 tests passed, zero failures, zero errors and zero
  skipped tests;
- forbidden domain-import search: no matches;
- `git diff --check`: passed;
- live MySQL startup: successful twice, followed by graceful shutdown;
- public endpoint: HTTP 200 with version 2 and expected hash;
- unauthenticated administrative endpoint: HTTP 401;
- both startups left exactly one policy, version 2, one `CURRENT` row,
  publisher user ID 1 and hash
  `sha256:7bcd23b5832cfc11a95ef6c3e48e5e36e0ce9402811c86de5c8f45eb3a983f10`;
- version-1 count remained zero;
- booking snapshot remained 3 rows with aggregate checksum `2578253035`;
- legal-basis snapshot remained 12 rows with checksum `24691422691`;
- two policy audit events exist for draft creation and publication;
- port 8080 was released after verification.

## Prerequisite And Acceptance Review

The implementation was compared with every required spec, plan, task criterion
and active implementation rule. It is a complete hexagonal submodule with a
framework-free domain, immutable published history, safe canonical content,
transaction and database concurrency protection, protected administration,
minimized public delivery and idempotent migration. No booking evidence or
version 1 was manufactured.

No contradiction remains. Every acceptance criterion is satisfied.
