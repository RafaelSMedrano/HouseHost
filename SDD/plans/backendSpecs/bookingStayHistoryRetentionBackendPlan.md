# Booking Stay History Retention Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/bookingStayHistoryRetentionSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Objective

Make reservation deletion preserve existing check-in, checkout and financial
history, detach obsolete relational associations and prevent
non-authentication failures from being converted into a session-ending HTTP
401 response.

Task `012b` executes this plan only after explicit user approval. Creation of
this plan does not authorize implementation.

## Current Backend Condition

The current booking deletion removes only the reservation. Check-in and
checkout persistence entities reference `bookings` through `booking_id`, and
the schema compatibility runner can make the checkout association non-null.
Existing foreign keys can therefore reject deletion of a reservation that has
operational history.

Legacy installations can also retain `financial_transactions.booking_id` and
its restrictive foreign key after financial source data has moved to
`source_type` and `source_id`. The current financial JPA entity no longer maps
that column, so the obsolete constraint can reject an otherwise valid booking
deletion even though the transaction already has an independent historical
source representation.

The check-in response already represents an absent booking identifier safely,
but both persistence mappers assume a non-null booking and the checkout response
dereferences the booking unconditionally. Those paths would fail after a valid
detachment unless they are updated together.

The security configuration also protects the generic error dispatch. In the
stateless authentication model, an internal error dispatch can arrive without
the authentication established for the original request and replace the real
failure with HTTP 401. The frontend then correctly interprets that 401 as an
expired session. The backend must preserve the original error category instead.

## Persistence Relationship

Update:

```text
CheckInJpaEntity.java
  (booking.checking adapter/out/persistence/entity; CheckInJpaEntity)
CheckOutJpaEntity.java
  (booking.checkout adapter/out/persistence/entity; CheckOutJpaEntity)
```

Declare the `booking` one-to-one associations explicitly optional and their
`booking_id` join columns nullable. Guest and room associations remain
unchanged and continue carrying the independent operational context.

Do not add JPA cascade removal from `BookingJpaEntity` to either historical
entity. Reservation deletion must never call check-in or checkout deletion as
a side effect.

## Idempotent MySQL Compatibility

Extend:

```text
DatabaseSchemaCompatibilityRunner.java
  (config/startup compatibility; DatabaseSchemaCompatibilityRunner)
```

Replace the legacy checkout rule that restores `booking_id` to `NOT NULL` with
an idempotent compatibility operation for both `check_ins` and `check_outs`.
For each table, the runner must:

1. verify that the table and `booking_id` column exist;
2. inspect the current nullability and foreign-key delete rule through
   `information_schema`;
3. rebuild the existing booking foreign key only when its behavior differs;
4. make `booking_id` nullable;
5. reference `bookings(id)` with `ON DELETE SET NULL`;
6. preserve the existing values and indexes.

Constraint names obtained from database metadata are identifiers, not user
input, and must be quoted safely. Table and replacement-constraint names remain
closed constants. Repeated startup must reach the same schema without repeated
destructive DDL once compatibility has been established.

`ON DELETE SET NULL` is the authoritative referential-integrity rule. Because
`BookingService.delete` is transactional, the database detaches related
history as part of the same reservation deletion. Do not perform independent
commits or disable foreign-key checks for this operation.

## Legacy Financial Booking Link Migration

Extend:

```text
DatabaseSchemaCompatibilityRunner.java
  (config/startup compatibility; DatabaseSchemaCompatibilityRunner)
```

After ensuring `financial_transactions.source_type` and `source_id` exist, the
runner must complete migration of a legacy `booking_id` column by:

1. copying non-null legacy identifiers into the current source fields only
   when the current source is absent;
2. discovering every foreign key attached to the legacy column through
   `information_schema`;
3. dropping those constraints using validated SQL identifiers;
4. dropping the obsolete `booking_id` column;
5. doing nothing on later startups when that column no longer exists.

The transaction row, monetary state, parties, dates and source identifier must
remain stored. Do not cascade-delete financial data, clear source fields,
disable foreign-key checks or introduce a JPA association from finance back to
booking.

## Null-Safe Persistence Mapping

Update:

```text
CheckInPersistenceMapper.java
  (booking.checking adapter/out/persistence/entity; CheckInPersistenceMapper)
CheckOutPersistenceMapper.java
  (booking.checkout adapter/out/persistence/entity; CheckOutPersistenceMapper)
```

Both `toDomain` methods map a null JPA booking association to a null domain
booking. Both `toEntity` methods preserve a null domain booking instead of
calling `BookingPersistenceMapper` with it. Guest and room mapping remains
mandatory and unchanged.

The `CheckIn` and `CheckOut` domain models already represent their booking as a
reference that can be absent during party resolution. Keep Spring and JPA
annotations out of those domain classes. Add a small semantic detach method
only if tests or application behavior require an explicit domain transition;
do not add infrastructure behavior to the domain.

## Stable Response Contract

Update:

```text
CheckOutResponseDTO.java
  (booking.checkout application/dto; CheckOutResponseDTO)
```

Mirror the existing `CheckInResponseDTO` behavior: obtain the booking once and
return `bookingId = null` when it is absent. Preserve every other response
field. Add regression coverage for both DTOs so future mapping changes cannot
reintroduce an unconditional dereference.

No endpoint path or response envelope changes. `GET /check-ins` and
`GET /check-outs` continue returning preserved records; only `bookingId` can be
null after the associated reservation has been deleted.

## Reservation Deletion And Audit

Keep:

```text
delete (booking.booking application/service; BookingService)
```

as the application-level deletion command. It continues validating record
existence, deleting through `BookingPersistencePort` and recording the existing
successful `BOOKING_DELETED` audit event inside the transaction.

Extend the minimal audit metadata with booleans or counts indicating whether
check-in or checkout history was detached only if those values can be obtained
without crossing module boundaries or duplicating persistence access. The
reservation identifier and successful deletion event are mandatory; full
operational payloads are forbidden.

If a persistence failure still occurs, it must roll back deletion and audit
together. A controlled error may be introduced for an unexpected remaining
referential conflict, but the operation must not claim that the JWT is invalid.

## Authentication And Error Dispatch

Update:

```text
SecurityConfig.java
  (security adapter/in/config; SecurityConfig)
```

Permit only `DispatcherType.ERROR` through the authorization rules so the
container can return the error produced by the already-authorized original
request. This does not make an application endpoint public and does not bypass
the JWT filter for ordinary `REQUEST` dispatches.

The original `DELETE /bookings/{id}` remains restricted to the existing
management roles. A missing, invalid or expired JWT still returns 401;
insufficient role still returns 403. Database or application failures must not
clear an otherwise valid frontend session by being remapped to 401.

## Verification Strategy

Add focused tests for:

- check-in JPA-to-domain and domain-to-JPA mapping with no booking;
- checkout JPA-to-domain and domain-to-JPA mapping with no booking;
- check-in and checkout response DTOs returning null `bookingId` while
  preserving guest and room data;
- successful deletion of a booking linked to only check-in, only checkout and
  both records;
- preservation and readability of every linked historical record after
  deletion;
- successful deletion of a booking formerly referenced by a legacy financial
  transaction while preserving its `source_type` and `source_id`;
- ordered and idempotent removal of the legacy financial booking foreign key
  and column;
- rollback behavior if reservation deletion fails;
- existing management-role DELETE authorization;
- 403 for insufficient permission;
- true invalid-token 401 behavior;
- preservation of a non-401 application error through an ERROR dispatch;
- idempotent schema compatibility and live MySQL verification of nullable
  columns and `DELETE_RULE = SET NULL` when MySQL is available;
- the full Maven suite and `git diff --check`.

The deletion integration test must verify the database state, not only mocked
repository calls:

```text
bookings row       -> absent
check_ins row      -> present, booking_id null
check_outs row     -> present, booking_id null
guest/room links   -> unchanged
financial row      -> present, source_type BOOKING, source_id unchanged
```

## Out Of Scope

- deleting check-in or checkout records;
- soft deletion or restoration of reservations;
- guest, room, financial or audit retention-policy changes beyond removal of
  the obsolete financial booking association;
- frontend redesign;
- creation of detached check-ins or checkouts through ordinary create APIs;
- changing which roles may delete reservations.
