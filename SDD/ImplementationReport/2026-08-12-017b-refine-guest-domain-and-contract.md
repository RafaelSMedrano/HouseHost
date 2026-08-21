# Implementation Report — Task 017b

## Task And Execution

- Task: `017b DONE — Refine Guest Domain And Registration Contract`.
- Executed file:
  `SDD/tasks/backendSpecs/017b-DONE-refine-guest-domain-and-contract.md`.
- Execution completed: 12 August 2026.
- Authorization: the user explicitly requested execution of task `017b`.

## Documents Read

- `AGENTS.md`.
- `SDD/specs/sddSpec.md`.
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`.
- `SDD/specs/moduleArchitectureSpec.md`.
- `SDD/specs/guestRegistrationPolishSpec.md`.
- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`.
- `SDD/implementation/task-bootstrap.md`.
- `SDD/implementation/implementation-order.md`.
- `SDD/tasks/backendSpecs/016b-DONE-migrate-guest-status-and-care-fields.md`.
- `SDD/tasks/backendSpecs/017b-DONE-refine-guest-domain-and-contract.md`.
- Current guest domain, DTO, application service, validation, security,
  persistence mapping, audit and metrics files.

## Files Created

- `src/test/java/com/househost/guest/domain/model/GuestProfileTest.java`.
- `src/test/java/com/househost/guest/adapter/out/persistence/entity/GuestPersistenceMapperTest.java`.
- `src/test/java/com/househost/guest/service/GuestRegistrationContractTest.java`.
- `src/test/java/com/househost/metrics/application/dto/MetricsSummaryDTOTest.java`.
- `SDD/ImplementationReport/2026-08-12-017b-refine-guest-domain-and-contract.md`.

## Files Changed

- `SDD/implementation/task-bootstrap.md`.
- `SDD/implementation/implementation-order.md`.
- `SDD/tasks/backendSpecs/017b-DONE-refine-guest-domain-and-contract.md`.
- `SDD/tasks/backendSpecs/018b-DONE-synchronize-guest-lifecycle-status.md`.
- `SDD/tasks/frontendSpecs/026f-DONE-simplify-guest-identification-and-status.md`.
- `SDD/tasks/frontendSpecs/027f-DONE-rebuild-guest-care-fields.md`.
- `SDD/ImplementationReport/2026-08-12-016b-migrate-guest-status-and-care-fields.md`.
- `src/main/java/com/househost/guest/domain/model/GuestStatus.java`.
- `src/main/java/com/househost/guest/domain/model/Guest.java`.
- `src/main/java/com/househost/guest/application/dto/GuestRegisterRequestDTO.java`.
- `src/main/java/com/househost/guest/application/dto/GuestRegisterResponseDTO.java`.
- `src/main/java/com/househost/guest/application/service/GuestService.java`.
- `src/main/java/com/househost/guest/application/service/GuestValidationService.java`.
- `src/main/java/com/househost/guest/application/service/GuestDataSecurityService.java`.
- `src/main/java/com/househost/guest/adapter/out/persistence/entity/GuestJpaEntity.java`.
- `src/main/java/com/househost/guest/adapter/out/persistence/entity/GuestPersistenceMapper.java`.
- `src/main/java/com/househost/metrics/application/service/MetricsCalculationService.java`.
- `src/main/java/com/househost/metrics/application/dto/MetricsSummaryDTO.java`.
- `src/test/java/com/househost/guest/service/GuestDataSecurityServiceTest.java`.

## Flows Implemented

New guest instances now start with `INACTIVE`, and the active registration DTO
does not expose status, stay count, total spent, last-stay date or rating.
Legacy JSON members for those values and for removed pet, favorite-room,
boolean accessibility and structured-preference properties are explicitly
ignored. They therefore cannot control a registration or ordinary profile
edit, while older clients do not fail solely because they still send them.

Profile editing now updates only identity, contact, classification, origin,
internal notes and the two care strings. Lifecycle and operational history are
restored through a separate domain operation used by persistence and security
mapping, so ordinary edits preserve them.

`preferencesAndRestrictions` and `accessibilityNeeds` now round-trip through
the request, domain, persistence entity/mapper, response and masking flow. Both
are optional and limited to 4,000 characters. Validation rejects oversized
input before normalization and never truncates its content.

Audit events retain only identifiers and controlled metadata; no care text or
internal note is copied into audit metadata. Pet presence remains exclusively
on reservations; the guest contract and metrics expose no guest-with-pets
association.

## Technical And MVP Decisions

- A 4,000-character maximum was selected for each care field to provide a
  documented application bound compatible with the existing MySQL `TEXT`
  storage introduced by task `016b`.
- Peripheral whitespace is normalized consistently with the existing optional
  guest strings. Line breaks and all internal text are preserved; values are
  never truncated or structurally transformed.
- Legacy request members are ignored with an explicit closed list rather than
  remaining writable DTO fields. This preserves tolerant transition behavior
  without retaining them in the active contract.
- Operational state restoration is distinct from profile mutation. Booking
  lifecycle recomputation and checkout history mutation remain assigned to
  tasks `018b` and `019b`.
- The obsolete `guestsWithPets` metric was removed. Even when derived from
  reservations, that label incorrectly classifies the guest; pet quantity is a
  reservation attribute only.

## Difficulties, Problems And Resolutions

Removing `travelsWithPets` from the guest domain exposed a dashboard aggregate
that still classified guests by pet presence. An initial reservation-derived
implementation was rejected after product clarification because it retained
the incorrect guest association. The aggregate was removed from calculation
and response instead.

The focused JSON compatibility test initially represented a multiline value as
a literal line break, which is invalid inside a JSON string. The fixture was
corrected to use JSON's escaped newline and now verifies that the decoded line
break survives normalization, persistence mapping and response mapping.

## Tests And Verification

- `./mvnw -q -DskipTests compile`: passed during implementation.
- Focused guest and metrics tests: 13 tests passed across domain, contract,
  mapper, validation, masking, audit and metric-contract coverage.
- `./mvnw test`: 185 tests passed, zero failures, zero errors and zero skipped.
- `git diff --check`: passed before task completion; repeated after report and
  reference updates.

Focused coverage verifies the inactive default, ignored legacy writes,
preserved operational state, exact editable request fields, care-text mapping,
the 4,000-character boundary, privacy masking and audit metadata exclusion.

## Acceptance Criteria Review

- Every registration starts `INACTIVE`, including legacy status attempts:
  satisfied.
- Ordinary edits preserve status and operational history: satisfied.
- The write contract has only the two new optional care strings and no removed
  structured or operational members: satisfied.
- Guest contracts and metrics contain no pet association: satisfied; pets
  remain a reservation attribute.
- Response and persistence mapping round-trip both strings: satisfied.
- Oversized values are rejected without truncation: satisfied.
- Masked responses protect both new personal-data fields: satisfied.
- Audit metadata excludes care text and internal notes: satisfied.
- Identity, contact, origin and authorization behavior remains compatible:
  satisfied by focused and full regression suites.
- Focused tests, full Maven suite and formatting check pass: satisfied.

## Prerequisite Review

The result was reviewed against the SDD process, main product spec, module
architecture, guest registration polish spec, backend plan, dependency task and
active implementation rules. DTOs remain in `application/dto`, persistence
annotations remain in the adapter, domain state mutation is separated by
business purpose, service instance names follow their concrete types and new
list identifiers use the required suffix.

No new database DDL, frontend form change, booking lifecycle synchronization or
checkout-history application was introduced. Tasks `018b`, `019b` and frontend
tasks remain proposed and unauthorized. No contradiction remains, and all
criteria of task `017b` are satisfied.
