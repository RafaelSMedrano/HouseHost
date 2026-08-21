# Booking Service Rating Backend Plan

## Governing Specs

- `SDD/specs/bookingServiceRatingSpec.md`;
- all prerequisite specs declared by that spec.

## Objective

Create an independent hexagonal ratings module, integrate one complete rating
atomically with completed checkout and expose authorized booking-derived query
contracts for rating lists and guest history.

Backend tasks `023b` through `027b` are complete.

## Target Architecture

```text
src/main/java/com/househost/ratings/
├── domain/model/Rating.java
├── domain/exception/RatingException.java
├── application/dto/
│   ├── RatingRequestDTO.java
│   ├── RatingResponseDTO.java
│   └── RatingSummaryDTO.java
├── application/port/in/RatingUseCase.java
├── application/port/out/
│   ├── RatingPersistencePort.java
│   └── RatingAuditPort.java
├── application/service/
│   ├── RatingService.java
│   └── RatingValidationService.java
└── adapter/
    ├── in/rest/RatingController.java
    └── out/
        ├── integration/RatingAuditAdapter.java
        └── persistence/
            ├── RatingPersistenceAdapter.java
            ├── RatingJpaRepository.java
            └── entity/
                ├── RatingJpaEntity.java
                └── RatingPersistenceMapper.java
```

`Rating` holds six `Integer` scores, observations, evaluation timestamp and a
`Booking` relationship. It contains no Spring, JPA or HTTP dependencies.

## Persistence And Migration

Create table `ratings` with:

```text
id
booking_id UNIQUE NOT NULL
check_in_procedure_score
check_out_procedure_score
accommodation_cleanliness_score
team_communication_score
location_score
comfort_score
observations TEXT NULL
evaluated_at
created_at
updated_at
```

Every score is non-null and constrained to `1..5`. The unique booking key is
enforced in database and application validation. Compatibility DDL is
idempotent and does not map the legacy overall guest/checkout rating into the
six new criteria.

After the ratings module is ready for checkout integration, remove the obsolete
generic rating completely:

- remove `rating` from Guest domain state, response DTOs, security masking,
  persistence mapping and `GuestJpaEntity`;
- remove `rating` from CheckOut domain state, request/response DTOs,
  validation, persistence mapping and `CheckOutJpaEntity`;
- drop `guests.rating` and `check_outs.rating` idempotently and discard their
  values;
- remove tests and compatibility aliases that treat the generic value as an
  active contract.

No backfill copies the old value into any new criterion.

The booking deletion flow checks ratings before deletion and returns conflict
when a rating exists. No cascade removes a rating.

## Application Contracts

`RatingUseCase` provides coherent operations:

```text
createForCompletedBooking
findAll(page, size)
findByGuestId(guestId, page, size)
existsByBookingId
```

HTTP contracts provide paginated summaries for the ratings and guest-history
tables. Responses include only identifiers/display context, evaluation date,
six scores and observations required by the specified tables. No endpoint
returns an individual rating detail for a rating-profile flow.

Validation rejects missing/out-of-range scores, oversized observations,
duplicate booking ratings and rating creation for a booking without completed
checkout. Queries use persistence-level joins/projections or bounded loading to
avoid N+1 access.

## Checkout Integration

Replace the checkout input contract's single `rating` integer with a nested
rating payload containing the six scores and observations. Compatibility with
the single-rating input is removed rather than retained or ignored.

Add:

```text
CheckOutParticipantNotifier
  -> CheckOutRatingResolver
      -> RatingUseCase.createForCompletedBooking(...)
```

`CheckOutParticipantNotifier` invokes the Resolver only for a newly completed
checkout. The ratings operation participates in the checkout transaction.
Persistent uniqueness and application idempotence prevent duplicates.

## Authorization, Audit And Privacy

Authenticated operational roles can create/read ratings. The ordinary ratings
API exposes no update or deletion operation. Backend checks remain authoritative.

Use `RatingAuditPort` for create and view/list events. Metadata
may contain rating ID, booking ID and operation outcome, but never observations
or the six-score payload.

## Tests

Cover domain invariants, persistence constraints, concurrent duplicates,
checkout statuses, rollback/idempotence, rated-booking deletion conflict,
booking-derived guest queries, pagination/order, authorization, minimized audit
metadata and module architecture.

## Out Of Scope

- public unauthenticated rating links;
- average scores, ranking or analytics;
- preservation or fabricated migration of the obsolete generic rating;
- rating a guest independently of a booking;
- multiple ratings for one booking.
