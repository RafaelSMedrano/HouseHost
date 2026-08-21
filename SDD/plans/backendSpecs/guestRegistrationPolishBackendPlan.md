# Guest Registration Polish Backend Plan

## Governing Specs

- `SDD/specs/guestRegistrationPolishSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Objective

Make guest lifecycle status server-owned, replace structured guest care choices
with two text fields, update the MySQL schema idempotently and let completed
checkout apply history and assessment exactly once.

This plan does not authorize implementation. Tasks `016b` through `019b` and
corrective tasks `020b` and `021b` require explicit approval before their
backend or database changes.

## Current Backend Context

Relevant components include:

```text
GuestStatus (guest domain/model; enum)
Guest (guest domain/model; class)
GuestRegisterRequestDTO (guest application/dto; class)
GuestRegisterResponseDTO (guest application/dto; class)
GuestService (guest application/service; class)
GuestJpaEntity.java (guest adapter/out/persistence/entity; GuestJpaEntity)
GuestPersistenceMapper.java (guest adapter/out/persistence/entity; GuestPersistenceMapper)
DatabaseSchemaCompatibilityRunner.java (config/startup compatibility; DatabaseSchemaCompatibilityRunner)
BookingService (booking.booking application/service; class)
CheckInParticipantNotifier (booking.checking application/service; class)
CheckOutRequestDTO (booking.checkout application/dto; class)
CheckOutService (booking.checkout application/service; class)
CheckOutParticipantNotifier (booking.checkout application/service; class)
```

The current model defaults guests to `IN_BOOKING`, accepts status and history
from the guest request, stores preferences in `guest_preferences`, and keeps
pet/accessibility/favorite-room choices in structured columns. Checkout changes
the guest to a legacy checked-out status but does not collect rating or prove
that history was applied only once.

## Guest Domain And Contracts

Replace `GuestStatus` values with:

```text
WITH_UNCONFIRMED_BOOKING
WITH_CONFIRMED_BOOKING
IN_STAY
INACTIVE
```

The guest's construction default is `INACTIVE`. Separate registration profile
mutation from lifecycle and operational-history mutation so ordinary writes
cannot change status, stay count, total spent, last-stay date or rating.

Update `GuestRegisterRequestDTO` to accept `preferencesAndRestrictions` and
`accessibilityNeeds`. Remove status, operational history and the obsolete
structured preference members from its active contract. Unknown JSON members
remain subject to the project's current compatibility policy, but service code
must never use a client status or history value during registration/update.

Keep `originChannel` as the only guest-origin property. Remove `referredBy`
from guest request/response DTOs, domain, masking and persistence. During
compatibility rollout, an obsolete `referredBy` JSON member is ignored and
cannot affect guest state.

Update response, masking and audit-sensitive paths to expose or protect the new
text fields consistently. DTOs stay in `application/dto`; new immutable
internal carriers, if needed, stay in `application/records` and end in
`Record`.

Remove guest-oriented pet output and aggregation. Pet quantity remains an
attribute of `Booking`; metrics must not derive or expose a guest-with-pets
classification from reservations.

## Persistence And Idempotent Migration

Update:

```text
GuestJpaEntity.java (guest adapter/out/persistence/entity; GuestJpaEntity)
GuestPersistenceMapper.java (guest adapter/out/persistence/entity; GuestPersistenceMapper)
DatabaseSchemaCompatibilityRunner.java (config/startup compatibility; DatabaseSchemaCompatibilityRunner)
```

Add nullable text columns equivalent to:

```text
guests.preferences_and_restrictions TEXT
guests.accessibility_needs TEXT
```

The compatibility runner must:

1. verify the `guests` table before acting;
2. add the new columns when absent;
3. migrate guest status through a temporary superset and then narrow it to the
   four authoritative English values with default `INACTIVE`;
4. derive active status using booking priority `IN_STAY`, `CONFIRMED`, then
   `UNCONFIRMED`, otherwise `INACTIVE`;
5. preserve values already present in the two new text columns;
6. drop `guest_preferences` and the obsolete `travels_with_pets`, `pet_type`,
   `favorite_room` and `needs_accessibility` columns without copying or
   transforming their legacy values;
7. drop the obsolete `referred_by` guest column without retaining its legacy
   values;
8. tolerate any obsolete table or column that is already absent;
9. do nothing destructive on subsequent compatible startups.

Tests must cover a fresh schema, representative legacy status states, retention
of values already in the new text columns, deletion of obsolete care storage,
absent optional tables/columns and repeated execution.

## Lifecycle Synchronization

Keep `setStatus` on the guest domain and application service. Principal
services may inject another module's public service for simple, synchronous and
side-effect-free queries. Every cross-module mutation must instead follow the
module architecture flow `MainService → ParticipantNotifier → Resolver`.

Booking has one `BookingParticipantNotifier`. It coordinates
`BookingGuestResolver`, which derives the effective status from
`BookingPersistencePort.findByGuestId` and applies the priority `IN_STAY`,
`CONFIRMED`, `UNCONFIRMED`, otherwise `INACTIVE`. Booking creation, update,
status change and deletion notify it, including both guests when ownership
changes. `BookingService` and `BookingFormService` query `GuestService` and
`RoomService` directly; query-only pass-through Resolvers are forbidden.

Check-in has one `CheckInParticipantNotifier` coordinating Booking, Guest and
Room Resolvers. Checkout similarly has one `CheckOutParticipantNotifier`
coordinating Booking and Room Resolvers. Booking-backed transitions obtain the
guest consequence through `BookingService.setStatus`, avoiding a duplicate
direct guest write. A bookingless completed check-in assigns `IN_STAY` through
its guest Resolver.

Public booking queries `RoomService` directly and uses one
`PublicBookingParticipantNotifier` to coordinate guest creation and status
synchronization through `PublicBookingGuestResolver`. Startup synchronization
remains an idempotent compatibility backfill; runtime correctness does not
depend on restart.

## Checkout Stay History

Add a persistent marker to checkout, or an equivalent transactional record,
proving whether guest history has been applied. That marker is infrastructure
data and must not be inferred only from checkout status. This plan defines no
generic guest or checkout rating; structured booking-service ratings belong to
their independent module.

On the transition to `COMPLETED`, `CheckOutService` coordinates:

```text
save checkout
  -> finalize booking/room parties
  -> apply guest stay history once
  -> save applied marker
  -> audit completion
```

All steps execute in one transaction. Repeated updates of a completed checkout
must not increment count or total again. Pending/cancelled records leave guest
history untouched.

Use authoritative backend values to calculate total spent. If the current
booking/finance model has multiple candidate totals, task `019b` must document
the selected existing source in its implementation report; the client never
sends an arbitrary total-spent value.

## Verification Strategy

Add focused tests for:

- new guest always stored as `INACTIVE` despite a client status member;
- ordinary guest update preserves lifecycle and history;
- exact enum serialization and persistence;
- booking-state priority and guest recomputation after create, update, status
  change, transfer and delete;
- check-in and checkout lifecycle integration;
- fresh and legacy MySQL status migration, obsolete care-data deletion and
  idempotence;
- mapping and masking of both new text fields;
- absence of pet association from guest contracts and guest-oriented metrics;
- completed checkout applying date, count and amount atomically;
- pending/cancelled checkout applying none of them;
- repeated completed-checkout update not applying history twice;
- transaction rollback when guest-history persistence fails;
- audit metadata excluding sensitive free text;
- full Maven suite and `git diff --check`.

## Out Of Scope

- redesigning the guest list or profile beyond contract compatibility;
- changing booking-status vocabulary;
- creating a multi-checkout rating history or review-comment feature;
- changing authorization roles, LGPD retention or masking policy;
- manual guest lifecycle status editing;
- unrelated guest-module architecture refactoring.
