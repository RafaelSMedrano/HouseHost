# Booking Service Rating Spec

## Specification

Booking Service Rating is the hospitality capability through which a guest
evaluates the service delivered for one completed reservation. Each rating is
an independent domain entity owned by the `ratings` module and is associated
one-to-one with the evaluated booking.

The rating contains six required criteria, each scored with an integer from
`1` to `5`, plus an optional free-text observation:

- check-in procedure;
- checkout procedure;
- accommodation cleanliness;
- team communication;
- location;
- comfort;
- observations.

The interface represents each numeric criterion with five initially empty
stars. Selecting a star fills it and every preceding star. The selected star
position is the stored score: first star `1`, second `2`, third `3`, fourth `4`
and fifth `5`.

## Scope

This spec governs the new backend ratings module, rating persistence and
contracts, checkout integration, booking-derived guest rating history, the
administrative ratings list experience, the collapsible history shown during
reservation creation and the navigation from ratings to guests and bookings.

Ratings are collected during administrative checkout as the guest's feedback.
They are not entered during guest registration and do not belong to the guest
aggregate. The backend discovers a guest's rating history by following each
rating's booking and that booking's guest.

The existing generic `Guest.rating` and checkout-level overall rating are
obsolete and are removed completely from frontend state and payloads, HTTP
contracts, domain models, services, persistence entities, database columns and
tests. Their legacy values are discarded because they are not equivalent to
the six criteria and cannot be migrated truthfully. No compatibility read or
write path for the generic rating remains after this feature is implemented.

The required six-criterion rating replaces the former optional single overall
rating. Guest history counters and the remaining checkout behavior from
`guestRegistrationPolishSpec` remain in force.

## Capabilities

### Own Ratings In An Independent Module

The backend contains a `ratings` module with domain, application and adapter
layers. `Rating` is a domain model, not a property collection embedded in
`Booking`, `Guest` or `CheckOut`.

Each rating has its own identifier and records the evaluated booking, the six
scores, optional observations, evaluation timestamp, creation timestamp and
update timestamp.

The evaluation timestamp is derived by the backend from the completed
checkout's actual checkout time. The client does not submit an arbitrary
evaluation date.

The database enforces at most one rating for each booking with a unique,
non-null booking foreign key. A booking can have no rating before checkout, but
a rating cannot exist without its booking.

### Validate The Complete Rating

All six scores are mandatory integers from `1` through `5`. Zero, values above
five, fractions and missing criteria are rejected. Observations are optional,
trimmed and limited to 4,000 characters without silent truncation.

The server owns validation. Star controls provide immediate interface feedback
but cannot expand the accepted range or bypass required criteria.

### Create The Rating During Checkout

The completed checkout form contains the six star groups and the observations
textarea. Completing checkout creates the rating for the same booking as part
of the checkout transaction.

The checkout module communicates the completed checkout through its existing
`CheckOutParticipantNotifier`. A ratings-specific Resolver in the checkout
module invokes the public ratings capability. `CheckOutService` does not call
`RatingService` directly for this state-changing integration.

If rating creation fails, checkout completion and its participant effects roll
back. A pending or cancelled checkout creates no rating. Repeating or updating
a completed checkout does not create a second rating or silently replace the
existing one. Ratings are immutable through the ordinary administrative API
after checkout; correction or removal requires a separately governed retention
or data-subject workflow.

Checkout cannot be completed while any of the six criteria is unanswered.
Observations remain optional.

### Preserve The Booking Relationship

A rated booking cannot be deleted while its rating exists. The API returns a
business conflict explaining that the rating must be handled under its own
authorized retention/deletion operation first. Ratings never cascade-delete
silently with a booking.

The ordinary ratings API does not delete a rating, booking, guest, checkout or
stay history. Creation and access are auditable without copying observations or
score payloads into audit metadata.

### Query Guest History Through Bookings

The ratings module supports a paginated query by guest identifier. The query
joins rating to booking and booking to guest; it does not add a rating
collection or rating identifiers to the guest domain.

The history returns one row for each rated booking with booking identifier,
booking dates or display reference, evaluation date, all six scores and the
observation. Ordering is newest evaluation first and remains deterministic for
equal timestamps.

Queries avoid one request per row and do not expose unrelated guest, financial,
document or contact data.

### Show Collapsible History During Reservation Creation

After reservation creation identifies one existing guest by name or document,
the form shows a textual `Histórico de avaliações` link even when that guest has
no ratings. Activating the link opens an inline table when results exist or an
accessible empty-history message when they do not; activating it again hides
the content without losing the selected guest or other form values.

The table contains one row per evaluated booking. Its columns are booking,
evaluation date, the six evaluated criteria and observations. Numeric values
are presented as filled/empty stars with an accessible textual score such as
`4 de 5`. The booking cell opens the booking profile through the existing
navigation flow.

No link is shown before an exact guest selection. A changed guest invalidates
the previous request and history.

### Provide Ratings Navigation And List

The administrative sidebar includes a new `Avaliações` option for the same
operational roles that can manage reservations and checkout. Selecting it
starts a ratings root flow and opens the ratings list page.

The list is a responsive table ordered by newest evaluation first. Each rating
is represented exclusively as one non-clickable row. Its columns, in order,
are guest name, evaluated booking, evaluation date, the six criterion scores
and observations.

The guest name in the first column is a link to the related guest profile. The
evaluated booking in the second column is a link to the related booking
profile. No other cell or row area is a link or action. There is no rating
profile, rating-detail page or navigation entry for an individual rating.

Empty, loading, permission-denied and failed-request states are accessible and
do not manufacture ratings or clear unrelated navigation history.

### Protect Personal Data And Accessibility

Rating observations are guest personal data and may reveal sensitive or
complaint information. They are never copied to logs, audit metadata, URLs or
browser storage. Backend authorization is authoritative.

Every star group is keyboard operable as one named rating control, exposes the
selected value programmatically and does not rely on color alone. Tables use
real headings, retain usable horizontal overflow on narrow screens and provide
accessible link names for the related guests and bookings.

## Prerequisite Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/bookingStayHistoryRetentionSpec.md`
- `SDD/specs/guestRegistrationPolishSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Spec Degree

4.
