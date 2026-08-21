# Public Booking Notification Spec

## Specification

Public Booking Notification defines the transactional email capability for a
reservation request created through the public site. After the public form is
successfully completed and the reservation request is persisted, the system
sends one request-received email to the guest and one operational notification
to the management recipient through the notifier module.

The capability communicates a business event. It does not make AWS SES part of
the booking domain and it does not allow a public caller to choose recipients,
sender identity or message templates.

## Scope

This spec governs the public guest email contract, the two request-received
recipients, notification intent creation, delivery state, retry and the SES
integration boundary. It applies to the backend module that accepts public
reservation requests and to the provider adapter owned by the notifier module.

This spec does not define marketing email, bulk messaging, WhatsApp delivery,
payment authorization, cancellation messages or a frontend implementation. A
reservation request created by the public form remains `UNCONFIRMED` and is
never described as a confirmed reservation in either email.

## Capabilities

### Capture The Guest Transactional Email

The public booking request accepts a required guest email as a transactional
contact field. The backend trims and normalizes it, validates its bounded
format and persists it as part of the guest contact information. The public
caller cannot provide a management recipient, sender, reply-to address or
template identifier.

### Notify Only After A Successful Public Request

When the public booking use case successfully persists a reservation request,
it creates exactly one guest-request-received intent and exactly one
management-new-request intent for that request event. The persisted reservation
remains `UNCONFIRMED`. A rejected or rolled-back request creates none of these
intents.

The intents are created transactionally with the reservation request. Email
delivery occurs after the transaction has committed and never determines
whether the request is persisted. Later WhatsApp contact, payment negotiation
and reservation confirmation are outside this notification event.

### Send Two Distinct Request Messages

The guest message confirms receipt of the reservation request and makes clear
that the stay is not yet confirmed. The management message identifies the new
unconfirmed request and contains only the operational information needed for
WhatsApp follow-up and payment negotiation.

Both messages contain a stable event identifier and booking identifier. They
do not contain payment credentials, identity documents, privacy-policy content,
audit metadata, internal notes or complete domain objects.

### Keep The Provider And Notifier Independent

The public API requests two self-contained messages through a provider-neutral
notifier application contract. It supplies a source system, stable external
event identifier, one idempotency key per message, opaque correlation key,
notification type and immutable rendered content.

The notifier stores no booking identifier field or foreign key and never loads
a booking or guest to dispatch, retry or process provider feedback. The opaque
correlation key may refer operationally to the public request, but the notifier
does not interpret it. Public API and booking classes do not import AWS SDK,
SES, SNS or notifier persistence types.

### Track Delivery Without Claiming Exactly Once

Each intent has a stable identifier and delivery state. The dispatch states are
`PENDING`, `PROCESSING`, `RETRYABLE_FAILURE`, `EXHAUSTED` and `ACCEPTED`.
Asynchronous SES feedback adds `DELIVERED`, `BOUNCED` and `COMPLAINT` outcomes.
Temporary provider or network failures before SES acceptance are retried using
a bounded policy. Permanent failures and exhausted retries remain visible and
reprocessable.

The system reduces duplicate sends using the stable intent identifier but does
not promise exactly-once email delivery. A duplicate must remain associated
with the same booking and public-request event.

### Receive Delivery, Bounce And Complaint Feedback

SES publishes provider feedback to a controller-approved SNS topic, and SNS
sends it to a dedicated notifier HTTPS endpoint. The endpoint validates the SNS
signature, certificate location and exact topic before parsing the nested SES
event.

The notifier correlates feedback through the SES message identifier returned at
provider acceptance. It persists a minimized append-only provider event and
updates only notifier-owned delivery state. Bounce or complaint processing does
not mutate the reservation, guest, payment or public-request result.

Permanent bounce and complaint never cause automatic resend. Transient delivery
failure after SES has ended its own attempts becomes visible for controlled
reprocessing. Duplicate SNS delivery is idempotent.

### Protect Recipients And Configuration

The sender, AWS Region, management recipient, feature flag and retry policy are
external configuration. The effective management recipient cannot be changed
by an HTTP request. Production runtime access uses an AWS workload identity
with the minimum SES permissions and no repository-stored long-lived keys.

Notification can be disabled without disabling reservation creation or later
confirmation. Missing or invalid notification configuration must fail closed
for delivery and remain observable without redirecting mail to an unintended
recipient.

### Preserve Privacy-Safe Evidence

Delivery logs may contain the intent identifier, source system, notification
type, opaque correlation key only when approved as non-sensitive, delivery
state, attempt count, provider status category and provider message identifier.
Logs and audit metadata must not contain guest email, names, phone numbers,
message bodies, internal notes, complete SNS envelopes or complete SES payloads.

Intent content and provider events have independently bounded retention. The
notifier database does not become a permanent copy of booking or guest data.
Optional raw-event archival in S3 requires separate approval, encryption,
access control and lifecycle configuration and does not replace SNS feedback
processing.

## Prerequisite Specs

- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/publicBookingOwnerEmailNotificationSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/backendSpecs/supplierManagementSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Spec Degree

4.
