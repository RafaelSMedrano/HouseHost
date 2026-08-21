# Notifier Module Spec

## Specification

Notifier Module is the reusable backend capability that receives
provider-neutral notification requests from one or more applications, persists
an immutable delivery intent, sends transactional email through AWS SES,
retries eligible failures and receives asynchronous provider feedback through
Amazon SNS.

The module is independent from booking, guest, payment and every other consumer
domain. It does not import their models, query their tables, retain foreign keys
to their entities or interpret their business identifiers. A consuming
application supplies a self-contained message snapshot and opaque correlation
values; the notifier remains capable of sending, retrying and processing
provider feedback without consulting that application again.

## Scope

This spec governs:

- provider-neutral notification request contracts;
- immutable email-message snapshots;
- notification intent persistence and idempotency;
- automatic dispatch and bounded retry;
- the AWS SES outbound adapter;
- direct SNS-to-backend feedback reception;
- SES delivery, bounce and complaint event normalization;
- provider-event persistence and intent-state updates;
- provider configuration, access control, privacy and retention;
- reuse by HouseHost and other applications.

This spec does not govern the business event that causes a consumer to request
a notification. It does not define booking confirmation, payment negotiation,
marketing campaigns, frontend behavior or AWS infrastructure provisioning
commands. Optional archival of raw provider events in S3 is a separate
operational capability and does not replace direct feedback processing.

## Capabilities

### Remain Independent From Consumer Domains

The notifier stores no `bookingId`, `guestId`, payment identifier or foreign key
to a consumer-owned table. It does not import consumer domain models or invoke
consumer repositories during dispatch or feedback processing.

Every request identifies its origin through neutral values:

- `sourceSystem` identifies the consuming application;
- `externalEventId` identifies the originating business event without exposing
  its implementation;
- `idempotencyKey` uniquely identifies the requested message;
- `correlationKey` is an optional opaque support reference that the notifier
  stores but never interprets;
- `notificationType` is a validated textual purpose owned by the consumer;
- `channel` identifies the delivery channel and initially supports `EMAIL`;
- `deliveryProfileKey` selects trusted provider configuration without exposing
  AWS credentials, sender addresses or Region choices to a public caller.

An arbitrary metadata map is not part of the contract. A consumer must not use
the notifier as unbounded storage for domain objects or personal data.

### Persist A Self-Contained Notification Intent

Each requested message becomes one `NotificationIntent`. One intent has exactly
one recipient so provider acceptance and later feedback remain attributable to
one delivery target.

The intent stores:

- `id` — notifier-owned UUID;
- `sourceSystem` — application that requested the notification;
- `externalEventId` — opaque originating event identifier;
- `idempotencyKey` — stable uniqueness key for this message;
- `correlationKey` — optional opaque operational reference;
- `notificationType` — validated semantic message type;
- `channel` — delivery channel;
- `deliveryProfileKey` — trusted server-side delivery configuration key;
- `recipient` — target email address;
- `subject` — immutable rendered subject;
- `textBody` — immutable rendered text content;
- `htmlBody` — immutable rendered HTML content;
- `status` — current delivery state;
- `attemptCount` — number of provider handoff attempts;
- `nextAttemptAt` — earliest time for the next eligible attempt;
- `leaseUntil` — expiration of the current processing claim;
- `providerMessageId` — identifier returned by SES after acceptance;
- `lastErrorCategory` — minimized provider-neutral failure category;
- `createdAt` and `updatedAt` — persistence chronology;
- `acceptedAt` — time SES accepted the send request;
- `deliveredAt` — time provider feedback reported delivery;
- `failedAt` — time a terminal delivery failure was established;
- `retentionUntil` — scheduled content deletion or anonymization boundary.

The subject and bodies are a final snapshot. Retry does not reload booking,
guest or any other consumer state and does not change because a template or
consumer record changed after intent creation.

The unique `idempotencyKey`, scoped by `sourceSystem`, prevents duplicate intent
creation. Duplicate requests return or identify the original intent without
creating another message.

### Dispatch Automatically After Commit

The Spring application enables native scheduling. A notifier-owned scheduled
adapter invokes the notifier application use case using configurable initial
delay, fixed delay and bounded batch size.

Each cycle selects only:

- `PENDING` intents eligible now;
- `RETRYABLE_FAILURE` intents whose `nextAttemptAt` is due;
- abandoned `PROCESSING` intents whose `leaseUntil` expired.

Claiming is atomic and changes the intent to `PROCESSING` before provider
handoff. Network calls do not hold a long database transaction. A process
restart loses no retry schedule because `nextAttemptAt`, attempt count and claim
lease are durable.

The notifier uses bounded backoff and a maximum attempt count. Immediate AWS or
network failures before SES acceptance may become `RETRYABLE_FAILURE`.
Permanent request failures and exhausted attempts become `EXHAUSTED`.

### Adapt Outbound Email To AWS SES

The notifier application layer declares a provider-neutral email output port.
Only the SES output adapter and its infrastructure configuration know AWS SDK
request, response and exception types.

The adapter resolves Region, verified sender, SES Configuration Set and runtime
credentials from trusted external configuration selected by
`deliveryProfileKey`. It uses the AWS default credential chain and a
least-privilege workload identity. It never accepts AWS credentials, sender,
Region or Configuration Set from a public request.

When SES accepts a send request, the adapter records `ACCEPTED`, `acceptedAt`
and the returned `providerMessageId`. Provider acceptance is not represented as
delivery, reading or booking confirmation.

### Receive SES Feedback Directly Through SNS

SES publishes delivery, bounce, complaint, reject, rendering-failure and
delivery-delay events to a controller-approved SNS topic. SNS subscribes a
dedicated HTTPS endpoint owned by the notifier inbound adapter.

The SNS endpoint is not part of the public booking API. Before processing any
message, the adapter:

- enforces HTTPS and a bounded request size;
- distinguishes subscription confirmation, notification and unsubscribe
  message types;
- verifies the SNS signature using the signed message fields;
- accepts only supported signature versions;
- validates that the signing certificate URL uses HTTPS and belongs to the
  expected AWS SNS domain and Region;
- validates the certificate chain;
- requires the exact configured `TopicArn`;
- rejects malformed, unsigned, unexpected-topic or replay-abusive input;
- handles subscription confirmation through a controlled operational flow;
- returns promptly after durable idempotent processing.

After validation, the adapter parses the outer SNS envelope and then the inner
SES event JSON. It converts provider-specific data into a minimized
`NotificationFeedbackRecord` before invoking the application use case. SNS and
SES JSON types do not cross into domain or persistence contracts.

### Correlate Feedback Without Consumer Data

The notifier correlates asynchronous feedback through the SES
`providerMessageId` stored at acceptance time. It does not locate an intent by
guest email, booking identifier or another consumer domain value.

One accepted SES send targets one recipient. If provider feedback redacts a
recipient address, correlation remains possible through `providerMessageId`.
Unknown message identifiers are recorded as minimized unmatched operational
events or rejected according to a bounded policy; they never cause a consumer
record lookup.

### Persist Minimized Provider Events

Each accepted feedback message becomes an append-only
`NotificationProviderEvent` associated only with a `NotificationIntent`.

The provider-event model stores:

- `id` — notifier-owned UUID;
- `notificationIntentId` — the only foreign key, pointing inside the notifier;
- `transportEventId` — SNS message identifier used for transport deduplication;
- `providerEventId` — SES feedback identifier when supplied;
- `providerMessageId` — SES message identifier used for correlation;
- `eventType` — normalized event type;
- `bounceType` and `bounceSubType` — minimized bounce classification when
  applicable;
- `providerStatusCode` — bounded provider or SMTP status when applicable;
- `failureCategory` — provider-neutral internal category;
- `occurredAt` — provider event time;
- `receivedAt` — notifier receipt time;
- `processedAt` — time the event affected the intent;
- `rawEventStorageKey` — optional reference to separately governed raw archival
  storage, never the raw JSON itself.

The database does not persist the complete SNS envelope, SNS signature,
certificate URL, unsubscribe URL, full email headers, complete provider JSON or
duplicated message body as provider-event evidence. Raw archival is disabled
unless separately specified with encryption, access and lifecycle controls.

`transportEventId` and provider feedback identifiers enforce idempotent event
processing. SNS redelivery does not create another provider event or repeat the
same state transition.

### Maintain Provider-Aware Delivery State

The notifier supports at least:

- `PENDING` — awaiting first handoff;
- `PROCESSING` — claimed by a dispatcher;
- `RETRYABLE_FAILURE` — provider handoff failed before acceptance and may retry;
- `EXHAUSTED` — automatic handoff attempts or a permanent request failure ended;
- `ACCEPTED` — SES accepted the send request;
- `DELIVERED` — SES feedback reported delivery to the destination server;
- `BOUNCED` — SES reported that delivery ultimately failed;
- `COMPLAINT` — the provider reported a spam complaint.

Permanent bounce and complaint are terminal and never trigger automatic resend.
A transient bounce reported after SES has stopped its own delivery attempts does
not silently resend; it becomes operationally visible for controlled
reprocessing according to policy. Reprocessing preserves history and never
recreates the consumer business event.

Out-of-order and duplicate provider events follow deterministic transitions.
No provider event changes booking, payment, guest or another consumer domain
state.

### Preserve Privacy, Retention And Operational Evidence

Recipient and message content are personal data. Access is restricted to the
notifier's dispatch and authorized support paths. Ordinary logs, metrics and
alarms contain no recipient address, subject, bodies, consumer personal data,
complete SNS envelope or complete SES payload.

Operational evidence may contain intent ID, source system, notification type,
provider-neutral state, attempt count, bounded failure category and provider
message identifier. `correlationKey` is logged only when its consumer-defined
format is approved as non-sensitive.

Intent content is deleted or anonymized at `retentionUntil`. Provider-event
retention is independently bounded. Deletion preserves only the minimum
non-personal evidence required for operational accountability and does not turn
the notifier database into an indefinite parallel consumer database.

### Remain Reusable By Other Applications

Another application can use the notifier by supplying the same neutral request
contract and a configured delivery profile. Adding an application does not
require adding its domain classes, tables, repositories or identifiers to the
notifier.

Notification types are validated textual identifiers rather than a HouseHost-
specific enum. Delivery profiles are controller-approved configuration, and
source-system authorization prevents one application from impersonating or
using another application's profile.

## Prerequisite Specs

- `SDD/specs/publicBookingNotificationSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/supplierManagementSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Spec Degree

5.
