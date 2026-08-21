# Public Privacy Policy Experience Spec

## Specification

Public Privacy Policy Experience is the unauthenticated website capability that
loads the authoritative published privacy policy from the backend, presents it
clearly before personal data are submitted and binds the visitor's reservation
acknowledgement to the exact policy returned by the server.

## Scope

This spec replaces the static policy text and hard-coded policy-version string
in the public frontend. It covers the policy page, reservation acknowledgement,
loading and failure behavior, safe content rendering and the user experience
when a new policy is published while a form is open.

It does not add an administrative policy editor. Draft creation and publication
are backend administrative capabilities governed by
`privacyPolicySubmoduleSpec`.

## Capabilities

### Load The Authoritative Policy

The public site requests the current policy from `GET /public/privacy-policy`.
It presents the returned title, canonical content, version and effective date
and retains the returned policy ID and content hash only in the current in-memory
page state needed to submit the reservation.

The policy ID is only a short-lived concurrency token sent back for validation.
It does not become a booking identifier or a persistent relationship between
Booking and Privacy.

The frontend does not hard-code an authoritative policy version or treat a
cache-busting query string as policy evidence.

### Render Content Safely

Policy content is rendered through the controlled document format. Backend text
is treated as data, not arbitrary HTML. Scripts, inline event handlers, unsafe
URLs and executable markup are never inserted into the page.

The page preserves headings, paragraphs, lists and the approved rights-contact
link using accessible semantic elements. Version and effective date are visible
as text.

### Block Uninformed Submission

Until the policy loads successfully, its acknowledgement control and public
reservation submission remain unavailable. Loading failure is explicit and
offers a retry without erasing safe reservation fields already entered.

The visitor can open and read the policy before acknowledgement without losing
form state. The checkbox does not represent consent to unrelated purposes; it
records acknowledgement of the notice and applicable reservation terms.

### Submit The Policy Identifier Read

The reservation request sends transient `privacyPolicyId` and
`privacyAccepted`. It does not send a self-declared policy version or hash as
authoritative evidence, and the backend does not persist the policy ID in the
booking.

If the backend responds with HTTP `409 Conflict` because the policy changed,
the site:

1. explains that the privacy policy was updated;
2. clears the previous acknowledgement;
3. reloads and displays the new policy;
4. preserves other safe form fields;
5. requires a new acknowledgement before resubmission.

Failures are not represented as successful or empty submissions, and duplicate
reservation creation is prevented.

### Preserve Privacy And Accessibility

Policy content and reservation data are not written to `localStorage` or
`sessionStorage`. Loading, unavailable, changed and ready states are visible and
announced accessibly. The policy link, retry control, acknowledgement and
submission are keyboard operable with visible focus.

## Prerequisite Specs

- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/publicBookingDataMinimizationSpec.md`

## Spec Degree

4.
