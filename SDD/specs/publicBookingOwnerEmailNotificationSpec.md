# Public Booking Owner Email Notification Spec

## Specification

Public Booking Owner Email Notification is the operational capability that
notifies the lodging owner by email after the public journey has successfully
created a reservation request.

The notification shortens the time between a visitor's request and the owner's
operational follow-up. It is an internal alert about a committed reservation
request; it is not a confirmation sent to the guest, does not change the
reservation status and does not replace the administrative reservation record.

The production capability uses approved AWS services and must be introduced
without destabilizing the HouseHost instance that is already running in
production on Amazon EC2. AWS account, identity, email-delivery and monitoring
readiness precede application integration and production activation.

## Scope

This spec governs:

- the internal owner notification triggered by a public reservation request;
- the minimum reservation and guest data permitted in the notification;
- recipient control and email-delivery status;
- reliable asynchronous delivery, retry and failed-delivery recovery;
- operational evidence and privacy-safe monitoring;
- AWS production readiness around the existing EC2 deployment;
- controlled activation, rollback and coexistence with the current production
  application;
- governance of AWS services that process notification data.

This spec does not:

- confirm a reservation automatically;
- send an email to the guest;
- define the guest transactional email or guest confirmation message; that
  capability is governed by `publicBookingNotificationSpec.md`;
- change room availability, quotation, conflict or capacity rules;
- define payment, cancellation or automatic expiration behavior;
- replace WhatsApp as the current operational guest-contact channel;
- select the detailed AWS topology, application classes, infrastructure code or
  deployment commands, which belong in a later plan;
- authorize implementation tasks or production changes merely because the spec
  has been approved.

## Capabilities

### Notify Only After A Successful Public Reservation Request

The system creates one owner-notification intent only after the public booking
flow has accepted the request and made the reservation durable.

The notification refers to the persisted reservation through a stable booking
identifier and describes it as a new reservation request. It never describes an
unconfirmed request as a confirmed stay.

If reservation creation is rejected or rolled back, no owner email is sent. A
later email-delivery failure does not roll back, delete or invalidate a
successfully created reservation and does not turn the visitor's successful
response into an error.

### Preserve The Existing Public Journey

The visitor continues to submit the reduced public booking contract and receives
the existing reservation-request result. The public contract may contain a
validated transactional guest email as defined by
`publicBookingDataMinimizationSpec.md`; this owner-notification capability does
not allow the public caller to choose the management recipient, sender or
template.

The guest's operational WhatsApp remains the contact channel through which the
owner follows up on the request. This capability sends only the owner alert.
Guest confirmation email is a separate transactional capability governed by
`publicBookingNotificationSpec.md`; neither capability is used for marketing.

### Minimize Notification Content

The owner email contains only the information needed to identify and act on the
request:

- booking identifier;
- request creation date and time;
- room or accommodation identification;
- desired check-in and check-out dates;
- numbers of adults, children and pets;
- quoted reservation total and currency;
- guest first and last name;
- operational WhatsApp number;
- current reservation-request status.

The notification does not contain identity documents, payment credentials,
privacy-policy content or hash, IP address, user agent, audit metadata, complete
domain objects or database records. Free-text stay notes are not copied into the
email; an authorized operator consults them in the administrative system when
necessary.

The email subject identifies the event and booking without including the guest
name, telephone number or free text. HTML and text rendering treat all booking
values as untrusted content and prevent markup or header injection.

### Control The Owner Recipient

The production recipient is an externally configured, controller-approved
operational address. A public caller cannot choose or override the recipient,
sender, reply-to address or template.

Changing the production recipient is a controlled operational change. The
effective recipient is testable without exposing it in public responses or
ordinary operational logs. Test and development environments use designated
non-production recipients and do not send production guest data.

### Deliver Reliably Without Claiming Exactly Once

Notification delivery is asynchronous with respect to the public HTTP response.
A temporary AWS, network or email-provider failure is retried according to a
bounded operational policy.

Every notification intent has a stable event identifier. Delivery processing
uses that identifier to reduce duplicate emails and to support investigation and
safe replay. Because asynchronous delivery can repeat work after partial
failure, the product does not promise exactly-once email delivery. A duplicate,
when it occurs, remains recognizable as referring to the same booking.

An event that exhausts automatic retries enters a recoverable failed state. It
is not silently discarded, and an authorized operator can identify, inspect and
reprocess it without recreating the reservation.

### Make Delivery State Operationally Visible

The production operation can distinguish at least:

- notification pending;
- handed off for delivery;
- email provider accepted the send request;
- delivery failed and remains eligible for retry;
- automatic retries exhausted and operator action required.

Provider acceptance does not make an unsupported claim that a person opened or
read the message. When the provider supplies delivery, bounce, complaint or
rendering-failure evidence, the operation can monitor those outcomes without
placing guest contact data in logs or alarms.

Critical failed-delivery accumulation, processing interruption and exhausted
retries produce an operational alert. Alerting failure does not corrupt booking
state, but it remains observable through an independent health or monitoring
signal.

### Keep Logs And Audit Evidence Privacy-Safe

Operational events use stable names and may contain the notification event
identifier, booking identifier, delivery stage, attempt count, provider status
category, runtime environment and correlation identifier when available.

Operational logs, metrics and alarms do not contain the guest name, telephone
number, email body, free-text notes or complete provider payload. Provider
message identifiers may be retained when needed for diagnosis, subject to access
and retention controls.

The existing public-booking audit remains the evidence that the reservation was
created. Email-delivery logs do not replace that audit and do not create a second
copy of the full booking event.

### Prepare AWS Before Application Activation

Before application integration is enabled in production, the controller
establishes and verifies the AWS capabilities needed for email delivery and its
operation. Readiness includes, as applicable:

- identifying the AWS account and Region that own each production resource;
- documenting the existing EC2 instance, runtime identity, deployment process,
  network dependencies and rollback path affected by the change;
- assigning workload permissions through a dedicated least-privilege AWS
  identity instead of storing long-lived AWS access keys in the application;
- verifying the authorized sending domain or address and the applicable DNS
  authentication records;
- satisfying the email service's production-sending requirements before sending
  to an unrestricted production recipient;
- configuring a controller-approved sender and recipient;
- establishing retry, failed-message recovery, monitoring and alert ownership;
- validating the AWS path with synthetic data and a designated test recipient;
- recording the supplier, processing, location, retention, security and incident
  facts required by the governing LGPD and supplier specs.

AWS preparation is additive around the running EC2 workload. It does not replace,
recreate, stop or expose the production instance, database, network or DNS as an
implicit consequence of enabling email notification.

Production notification remains disabled by external configuration until AWS
readiness and an end-to-end synthetic verification have succeeded. Disabling the
notification after activation stops new email handoff without disabling public
booking creation.

### Protect The Existing EC2 Production Workload

The production rollout preserves the current EC2 application's availability,
secrets, database connectivity, HTTPS behavior, logs and restart procedure.

AWS credentials are not committed to the repository, embedded in an image,
written to application logs or copied into email payloads. Runtime access is
restricted to the exact notification capabilities required by the deployed
component.

Configuration changes distinguish production from development and test. A
failed readiness check, missing required configuration or unauthorized sender
does not cause the application to send through an unintended account, Region,
recipient or fallback identity.

Activation has an explicit rollback path that restores the previous production
behavior: public reservations continue to be recorded while new owner email
handoff is disabled. Pending and failed notifications remain accountable under
the chosen retention and recovery policy rather than being erased by rollback.

### Govern AWS As A Personal-Data Processing Supplier

Before real notification data enters a new AWS service, the controller records
or updates the applicable AWS supplier relationship. Separate services or
purposes are assessed separately when their processing facts differ.

The assessment identifies at least:

- operational purpose and controller instructions;
- personal-data and data-subject categories;
- processing and storage locations;
- international-transfer status and applicable mechanism when required;
- AWS role and relevant sub-operators;
- access controls, encryption and incident channels;
- retention and deletion behavior for messages, failed messages, provider
  events, logs and backups;
- contractual status, responsibility and review evidence.

Notification data is retained in transient AWS delivery components only for the
period necessary to send, retry, investigate or recover the message. The owner's
mailbox is also a controlled operational copy: access, forwarding and retention
must not turn email into an indefinite parallel reservation database.

### Verify Readiness Before Real Guest Data

The capability is ready for production activation only when all of the following
are demonstrated:

- a committed synthetic reservation produces the expected owner email;
- a rejected or rolled-back reservation produces no email;
- email unavailability does not change successful booking creation;
- retry and exhausted-retry recovery are observable;
- replay identifies the same booking and does not create another reservation;
- recipient and sender cannot be influenced by the public request;
- logs, metrics and alarms contain no prohibited guest data;
- the sending identity and production recipient are authorized;
- least-privilege runtime access works without repository-stored credentials;
- notification can be disabled without disabling public reservations;
- EC2 deployment and rollback procedures have been verified for the production
  environment;
- supplier, privacy, retention and incident responsibilities are recorded.

Real guest data is not used to prove initial infrastructure readiness. Synthetic
verification precedes controlled production activation.

## Prerequisite Specs

- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/backendSpecs/supplierManagementSpec.md`

## Spec Degree

3.
