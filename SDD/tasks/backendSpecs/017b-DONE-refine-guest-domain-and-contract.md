# Task 017b DONE — Refine Guest Domain And Registration Contract

## Status

Completed on 12 August 2026 after explicit user authorization.

## Implementation Area

Backend (`b`).

## Objective

Make guest registration assign `INACTIVE` on the server, accept the two new
care text fields and prevent ordinary guest writes from changing lifecycle or
operational history.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/guestRegistrationPolishBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- current guest domain, DTO, application service, validation, security,
  persistence mapping and audit tests.

## Dependencies

- `SDD/tasks/backendSpecs/016b-DONE-migrate-guest-status-and-care-fields.md`

## Scope

- Replace `GuestStatus` with the exact four new values.
- Default new domain guests and JPA entities to `INACTIVE`.
- Replace structured preference request properties with
  `preferencesAndRestrictions` and `accessibilityNeeds` strings.
- Remove status and history/assessment fields from the active guest write
  contract and ignore legacy attempts to control them.
- Separate profile updates from lifecycle and operational-history mutations.
- Map, validate, respond and mask the two new text fields consistently.
- Keep internal notes and current identity/contact/origin behavior compatible.
- Ensure audits never include care-field or notes content.
- Remove every guest-oriented pet field and metric; pets remain a reservation
  attribute only.
- Add domain, service, DTO, mapper, validation and security regression tests.

## Out Of Scope

- Booking-driven lifecycle recomputation.
- Checkout history application.
- Database DDL beyond task `016b`.
- Frontend form changes.

## Expected Files

Expected changes include:

```text
src/main/java/com/househost/guest/domain/model/GuestStatus.java
src/main/java/com/househost/guest/domain/model/Guest.java
src/main/java/com/househost/guest/application/dto/GuestRegisterRequestDTO.java
src/main/java/com/househost/guest/application/dto/GuestRegisterResponseDTO.java
src/main/java/com/househost/guest/application/service/GuestService.java
src/main/java/com/househost/guest/application/service/GuestValidationService.java
src/main/java/com/househost/guest/application/service/GuestDataSecurityService.java
src/main/java/com/househost/guest/adapter/out/persistence/entity/GuestJpaEntity.java
src/main/java/com/househost/guest/adapter/out/persistence/entity/GuestPersistenceMapper.java
src/test/java/com/househost/guest/...
```

## Acceptance Criteria

- Every newly registered guest is `INACTIVE`, including when a legacy client
  sends a different status.
- An ordinary edit preserves status, stay count, total spent, last-stay date
  and rating.
- The request contract uses two optional strings and no structured preference,
  pet, favorite-room or boolean accessibility members.
- No guest response or guest-oriented metric relates guests to pets.
- Response and persistence mapping round-trip both new text values.
- Validation rejects values beyond documented bounds without altering content.
- Masked/restricted responses protect the new personal data consistently with
  current guest privacy rules.
- Audit metadata contains no free-text care or internal-notes value.
- Existing identity, contact, origin and authorization behavior remains valid.
- Focused tests, the full Maven suite and `git diff --check` pass.

## Verification Commands

```text
./mvnw test
git diff --check
```

## Required Report

```text
SDD/ImplementationReport/YYYY-MM-DD-017b-refine-guest-domain-and-contract.md
```
