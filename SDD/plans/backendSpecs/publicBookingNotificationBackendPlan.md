# Public Booking Notification Backend Plan

## Governing Specs

- `SDD/specs/publicBookingNotificationSpec.md`
- `SDD/specs/backendSpecs/notifierModuleSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/publicBookingOwnerEmailNotificationSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/supplierManagementSpec.md`

## Objective

Connect successful public reservation requests to the reusable notifier without
placing SES, SNS, retry, notification persistence or provider state inside the
public API module.

## Existing Flow

`PublicBookingService.createBooking` (application/service; method) validates the
request, creates the guest, persists an `UNCONFIRMED` booking and returns a
request-received response. That business status and WhatsApp follow-up remain
unchanged.

The public guest request gains one required normalized transactional email. It
does not gain sender, management recipient, template, AWS Region, SNS or SES
fields.

## Consumer Boundary

The public API application declares `PublicBookingNotificationPort`
(application/port/out; interface). The principal service or its existing
participant notifier depends only on that port.

`NotifierPublicBookingAdapter` (publicapi/adapter/out/integration; class)
implements the port and calls the notifier's `NotificationRequestUseCase`. It
creates two independent requests for the same public business event:

```text
HOUSEHOST / GUEST_REQUEST_RECEIVED
HOUSEHOST / MANAGEMENT_NEW_REQUEST
```

Each message has its own stable `idempotencyKey`. Both share one
`externalEventId` and may use an opaque, non-sensitive correlation key. The
notifier never interprets that key or persists a booking foreign key.

## Transaction Boundary

The two notifier intents are created in the same local database transaction as
the persisted public reservation request. If booking creation rolls back, both
intent creations roll back. SES dispatch happens only later through the
notifier scheduler and cannot change the public HTTP result.

Repeated handling of the same public event returns the existing intents through
their idempotency keys. It does not create another booking or another message
pair.

## Message Snapshots

The public API owns the business wording and supplies final minimized snapshots.
The guest email states that the request was received and that confirmation and
payment will be discussed through WhatsApp. The management email identifies an
unconfirmed request requiring follow-up.

Snapshots may contain only the fields approved by the governing spec. Subject
and HTML values are safely constructed, and neither message contains documents,
payment credentials, internal notes, privacy-policy content or complete domain
objects.

## Provider Feedback Isolation

Delivery, bounce and complaint feedback enters the notifier through its SNS
adapter. The public API is not called back and does not change booking or guest
state based on email outcomes. Operational correlation uses notifier intent and
provider message identifiers.

## Verification

- public email normalization and validation tests;
- two-intent creation with stable independent idempotency keys;
- rollback proving no intent survives rejected booking creation;
- duplicate-event handling proving one intent pair;
- minimized guest and management snapshot tests;
- architecture tests proving public API has no SES, SNS, scheduler or notifier
  persistence dependency;
- SES unavailability proving public request durability;
- complete notifier and public-booking backend suite;
- `git diff --check`.
