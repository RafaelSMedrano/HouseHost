# Notifier Module Backend Plan

## Governing Specs

- `SDD/specs/backendSpecs/notifierModuleSpec.md`
- `SDD/specs/publicBookingNotificationSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/supplierManagementSpec.md`

## Objective

Build a reusable provider-neutral notifier hexagon that persists self-contained
email intents, dispatches eligible messages automatically through AWS SES,
receives SES feedback directly through SNS, records minimized provider events
and remains independent from every consumer domain.

## Module Structure

```text
notifier/
├── domain/
│   ├── exception/
│   └── model/
├── application/
│   ├── records/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   └── service/
└── adapter/
    ├── in/
    │   ├── http/
    │   └── scheduling/
    └── out/
        ├── integration/
        └── persistence/
            └── entity/
```

No notifier package imports booking, guest, payment, public API or another
consumer module. Architecture tests enforce that rule and also prevent AWS SDK
types from entering domain, application records or persistence contracts.

## Application Contracts

`NotificationRequestUseCase` (application/port/in; interface) accepts one
provider-neutral `NotificationRequestRecord` per intended recipient.

`NotificationFeedbackUseCase` (application/port/in; interface) accepts a
validated and normalized `NotificationFeedbackRecord` produced by an inbound
provider adapter.

`NotificationIntentPersistencePort` (application/port/out; interface) supports
idempotent creation, bounded claiming, state transitions, provider-message
correlation, abandoned-lease recovery and retention selection.

`NotificationProviderEventPersistencePort` (application/port/out; interface)
persists append-only normalized provider feedback and enforces transport and
provider-event idempotency.

`EmailDeliveryPort` (application/port/out; interface) sends one immutable email
snapshot and returns a provider-neutral acceptance or failure result.

Consumer modules do not call notifier services directly. Each consumer declares
its own outbound application port, implemented by a consumer-owned
`adapter/out/integration` class that translates the business event into
`NotificationRequestUseCase` calls. This preserves the direction
`consumer application → consumer outbound port ← consumer adapter → notifier
inbound port`.

## Domain And Records

`NotificationIntent` (domain/model; class) owns state transitions and retry
eligibility. It contains no consumer entity or consumer foreign identifier.

`NotificationProviderEvent` (domain/model; class) represents one minimized,
append-only provider outcome associated only with a notifier intent.

Application records include:

- `NotificationRequestRecord`;
- `EmailMessageRecord`;
- `EmailDeliveryResultRecord`;
- `NotificationFeedbackRecord`;
- `NotificationClaimRecord`;
- `NotificationRetryDecisionRecord`.

Every variable, parameter and field typed as a record follows the mandatory
`Record` suffix convention.

## Notification Intent Persistence

The `notification_intents` table stores:

```text
id
source_system
external_event_id
idempotency_key
correlation_key
notification_type
channel
delivery_profile_key
recipient
subject
text_body
html_body
status
attempt_count
next_attempt_at
lease_until
provider_message_id
last_error_category
created_at
updated_at
accepted_at
delivered_at
failed_at
retention_until
version
```

`id` is notifier-owned and independent from consumer IDs. The database has no
foreign key to any consumer table. A unique constraint over `source_system` and
`idempotency_key` makes request creation idempotent. `provider_message_id` is
indexed and becomes unique when present because one intent sends to one
recipient.

Subject and bodies are the immutable message snapshot used for every pre-
acceptance retry. `version` provides optimistic concurrency in addition to the
atomic claim query.

## Provider Event Persistence

The `notification_provider_events` table stores:

```text
id
notification_intent_id
transport_event_id
provider_event_id
provider_message_id
event_type
bounce_type
bounce_subtype
provider_status_code
failure_category
occurred_at
received_at
processed_at
raw_event_storage_key
```

Its only foreign key points to `notification_intents`. Unique constraints cover
the SNS transport event and the provider feedback identity when available.
Complete SNS envelopes, signatures, certificate URLs, email headers and raw SES
payloads are not stored in the database.

## Dispatch And Retry

`NotificationIntentService` (application/service; class) creates intents and
owns domain transition orchestration.

`NotificationDispatchService` (application/service; class) claims a bounded
batch, invokes `EmailDeliveryPort` outside the claim transaction and records
the result in a new short transaction.

`NotificationDispatchScheduler` (adapter/in/scheduling; class) uses Spring's
native `@Scheduled` support with externally configured initial delay and fixed
delay. `@EnableScheduling` is enabled in application startup configuration, but
all scheduling behavior remains notifier-owned.

The initial single-EC2 implementation uses database claims:

```text
PENDING or due RETRYABLE_FAILURE
    → atomic claim
    → PROCESSING + leaseUntil
    → SES call outside database transaction
    → ACCEPTED, RETRYABLE_FAILURE or EXHAUSTED
```

Retry uses configurable bounded exponential backoff with jitter. Only failures
before provider acceptance are retried automatically. An expired processing
lease restores eligibility after a crash. Batch size, delays, attempt limit and
lease duration are external properties.

## AWS SES Outbound Adapter

`AwsSesEmailDeliveryAdapter` (adapter/out/integration; class) implements
`EmailDeliveryPort` with AWS SDK v2.

`NotifierDeliveryProfileProperties` (adapter configuration; class) maps a
trusted `deliveryProfileKey` to:

- enabled state;
- AWS Region;
- verified sender;
- optional reply-to;
- SES Configuration Set;
- permitted source systems;
- retry and retention policy references.

The adapter uses the AWS default credential chain and EC2 workload identity. It
never reads credentials from a notification request. Every send applies the
configured SES Configuration Set so feedback publishing remains active.

SES acceptance returns the provider message identifier. SDK exceptions are
mapped to bounded provider-neutral categories without logging recipient or
message content.

## SNS Feedback Inbound Adapter

`SnsSesFeedbackController` (adapter/in/http; class) exposes one dedicated HTTPS
endpoint outside `/public/**`. Spring Security permits that route without user
JWT only because the adapter performs SNS message authentication.

The adapter:

1. enforces a small request-size bound and supported content type;
2. parses the SNS envelope without binding it to domain objects;
3. validates the exact expected `TopicArn` and Region;
4. validates `SigningCertURL` before any network retrieval;
5. verifies the SNS signature and certificate chain;
6. handles subscription confirmation through a controlled flow;
7. parses the nested SES event only after SNS authentication;
8. maps the event to `NotificationFeedbackRecord`;
9. invokes `NotificationFeedbackUseCase` idempotently;
10. returns a bounded response without exposing processing data.

The endpoint does not trust recipient address as correlation. It locates the
intent through `mail.messageId` mapped to `providerMessageId`. Unexpected or
unknown events are handled by a bounded operational policy and never query a
consumer module.

`NotificationFeedbackService` (application/service; class) stores the provider
event and applies deterministic transitions:

```text
ACCEPTED → DELIVERED
ACCEPTED → BOUNCED
ACCEPTED or DELIVERED → COMPLAINT
```

Permanent bounce and complaint are terminal. A transient bounce after SES has
finished its own attempts is recorded for controlled reprocessing and does not
silently create a new send. Duplicate or out-of-order events preserve one
append-only history and one deterministic current state.

## Configuration

External configuration covers:

- module enablement;
- scheduler initial delay and fixed delay;
- batch size, lease duration and maximum attempts;
- retry backoff and jitter bounds;
- delivery-profile Region, sender and Configuration Set;
- expected SNS topic ARN and Region;
- supported SNS signature versions;
- request-size limit;
- intent and provider-event retention periods.

Production startup fails closed for delivery or SNS processing when an enabled
profile lacks required configuration. Disabling delivery does not disable
consumer business operations or erase pending intents.

## Privacy And Observability

Logs use intent ID, source system, notification type, state, attempt count,
provider message ID and bounded error category. Recipient, subject, bodies,
complete consumer correlation, SNS envelope and SES event payload are excluded.

Intent content is deleted or anonymized at `retentionUntil`. Provider events
have an independent bounded retention policy. Optional S3 raw archival remains
disabled and out of scope unless separately specified with encryption, private
access and lifecycle controls.

## Verification

- domain transition and retry-decision tests;
- persistence mapping, uniqueness, locking and concurrency tests;
- scheduler, lease-expiry and restart-recovery tests;
- SES mapping, Configuration Set and error-classification tests;
- SNS subscription, signature, certificate URL, topic, malformed input,
  duplicate and unknown-message tests;
- feedback correlation and deterministic state-transition tests;
- architecture tests proving no consumer-domain or misplaced AWS imports;
- privacy scans proving logs and provider-event storage are minimized;
- full Maven suite and `git diff --check`.

## Implementation Sequence

1. `037b` — provider-neutral contracts and domain.
2. `038b` — intent and provider-event persistence foundation.
3. `039b` — automatic dispatch, claims and retry.
4. `040b` — AWS SES outbound adapter and delivery profiles.
5. `041b` — secure SNS feedback ingestion and provider-event processing.
6. `042b` — public booking integration and complete backend verification.
