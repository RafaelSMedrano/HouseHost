# Guest Registration Polish Spec

## Specification

Guest Registration Polish is the administrative guest-management capability
that keeps initial registration focused on identity, contact, free-text care
information and internal notes, while moving stay history and guest assessment
to the operational checkout moment.

A newly registered guest has no reservation and no stay. The system therefore
assigns `INACTIVE` automatically instead of asking the operator to choose a
status that the registration flow cannot truthfully establish.

The complete guest-status vocabulary is English and contains only:

- `WITH_UNCONFIRMED_BOOKING` — the guest has an unconfirmed reservation;
- `WITH_CONFIRMED_BOOKING` — the guest has a confirmed reservation;
- `IN_STAY` — the guest is currently staying at the property;
- `INACTIVE` — the guest has no active reservation and is not in a stay.

## Scope

This spec governs the authenticated administrative experience and backend
contracts for creating and editing guests, persisting guest preferences,
restrictions, accessibility needs and origin channel, synchronizing guest
operational status, and completing checkout history and assessment.

The change covers the current Spring Boot guest and checkout modules, MySQL
schema compatibility and the static administrative frontend. Existing guest
identity, contact, origin/channel, internal notes, authorization, privacy and
audit behavior remains in force unless explicitly changed below.

The two new free-text fields replace, rather than consolidate, the existing
structured preference, pet, favorite-room and boolean accessibility data.
Legacy values belonging only to fields that cease to exist may be discarded,
and their obsolete storage is removed.

Guest origin contains only the selected origin channel. The guest does not
store, expose or request the name of a person who referred them. Legacy
referrer-name values may be discarded and their obsolete storage is removed.

Pet presence belongs exclusively to a reservation. Guest domain objects,
contracts, profiles and metrics do not classify or relate a guest as traveling
with pets.

## Capabilities

### Assign A Truthful Initial Status

The guest registration form does not display or submit a status control. The
backend treats status as server-owned lifecycle data and assigns `INACTIVE` to
every newly created guest, regardless of any unknown or legacy `status` member
sent by a client.

Editing a guest profile through the ordinary guest form does not manually
change status. Reservation, check-in and checkout operations are responsible
for status transitions.

The effective guest status is derived from all current reservations using this
priority:

```text
IN_STAY
  > WITH_CONFIRMED_BOOKING
  > WITH_UNCONFIRMED_BOOKING
  > INACTIVE
```

Creating, updating, changing the status of or deleting a reservation
recomputes every affected guest. When an update transfers a reservation to a
different guest, both the previous and current guest are recomputed.

Completing check-in changes its reservation to `IN_STAY`; completing checkout
changes its reservation to `FINISHED`. The booking operation then recomputes
the guest, allowing another active reservation to preserve a truthful booking
status. A check-in explicitly created without a reservation assigns `IN_STAY`
to its guest through the check-in module's own notification flow.

Cross-module status effects follow the module architecture flow
`MainService → ParticipantNotifier → Resolver`. Principal services may call
another module's service directly for simple, side-effect-free queries, but do
not use that direct dependency for status mutation. This replaces a generic
lifecycle layer while preserving its status guarantees.

### Use Only The New English Status Vocabulary

The domain, API and database persist only `WITH_UNCONFIRMED_BOOKING`,
`WITH_CONFIRMED_BOOKING`, `IN_STAY` and `INACTIVE` after compatibility
migration.

Legacy values are migrated from reservation truth whenever current booking
data is available. Without a matching active reservation, old checked-out,
booking or unknown states become `INACTIVE`. The interface presents friendly
Portuguese labels but exchanges the exact English enum values with the backend.

### Capture Preferences And Restrictions As Free Text

The registration form contains a block named `Preferências e restrições`. Its
content consists of exactly two multiline writing fields and no suggestions,
chips, selects, switches or quick options:

- `Preferências e restrições`;
- `Necessidades de acessibilidade`.

Both fields are optional free text. The backend exposes them as textual
properties and the database stores each in its own text column on the guest.
The previous list of preferences, pet switch/type, favorite-room option and
boolean accessibility choice are no longer part of the guest write contract.
No guest response or guest-oriented aggregate exposes a pet association.

### Keep Only The Guest Origin Channel

The registration form displays the `Origem & Canal` block before
`Preferências e restrições`. That origin block contains only the `Canal de
origem` selection and no `Indicado por` writing field.

Guest write and read contracts expose `originChannel` but do not expose
`referredBy` or an equivalent referrer-name property. The database removes the
obsolete `referred_by` guest column and may discard its legacy values. The
reservation module remains free to represent referral as a reservation origin;
this rule removes only the person-referrer attribute from the guest.

### Discard Obsolete Structured Care Data

The database compatibility process is idempotent and removes the obsolete
structured care storage. It does not copy or transform legacy preference rows,
pet information, favorite-room information or the boolean accessibility flag
into the two new text fields.

Values already stored directly in `Preferências e restrições` or
`Necessidades de acessibilidade` are retained. For a legacy guest with no value
in the new fields, those fields begin empty. The obsolete preference collection
and structured columns may be dropped together with the legacy data they hold.

### Keep History And Assessment Out Of Registration

The guest creation and ordinary edit form do not display or submit number of
stays, total spent or last-stay date. Supplying those properties to the
ordinary guest write endpoint does not allow the client to alter operational
history.

Existing history remains readable in the guest profile according to current
permissions. Historical counters and amounts are derived or maintained by the
operational checkout flow, not manually edited during registration.

### Complete Stay History At Checkout

Number of stays, total spent and last-stay date can be shown as operational
consequences or a preview and are not arbitrary registration inputs. This spec
defines no generic guest or checkout rating; booking-service evaluations belong
exclusively to the independent ratings capability.

On a completed checkout, the backend atomically:

- saves the checkout;
- increments the guest stay count exactly once;
- updates the last-stay date from the actual checkout timestamp;
- incorporates the finalized stay amount available from authoritative booking
  and checkout financial data into total spent without trusting a free client
  total;
- recomputes the guest lifecycle status after the booking is finalized.

A pending or cancelled checkout does not update guest history.
Updating an already completed checkout must be idempotent and must not count
the same stay twice. If the existing data model cannot prove that history was
already applied, the implementation must add explicit persistent evidence
rather than infer from UI behavior.

### Align Internal Notes With Their Section

The guest form keeps the `Observações internas` block and its team-only
description. The text area has no redundant `Anotações` title. Its container,
padding, width, border, focus state and responsive behavior align symmetrically
with the block header and the other full-width form fields.

### Preserve Privacy, Audit And Access Control

The new free-text fields are guest personal data. Existing
authorization, masking, audit and processing-purpose rules continue to apply.
Audit metadata must not copy free-text preferences, restrictions,
accessibility needs or internal notes.

## Prerequisite Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/bookingStayHistoryRetentionSpec.md`

## Spec Degree

3.
