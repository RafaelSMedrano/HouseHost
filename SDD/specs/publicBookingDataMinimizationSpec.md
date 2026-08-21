# Public Booking Data Minimization Spec

## Specification

Public Booking Data Minimization defines the smallest request contract and the
authoritative validation required for public quotation and pre-reservation.
Browser controls improve usability, but the backend enforces every boundary.

## Scope

The public booking request contains only room, dates, numeric guest and pet
composition, privacy acknowledgement, the guest name, transactional email,
operational WhatsApp, optional city and short stay-related notes. The email is
accepted exclusively for the reservation communication defined by the public
booking notification capability; it is not a marketing consent or marketing
channel. The request does not accept document or payment data. The public
response does not expose payment fields.

The public quote contains only room, dates and numeric guest/pet composition.
Administrative booking contracts are outside this spec.

## Capabilities

### Use A Reduced Typed Contract

Booking input fields are `roomId`, `checkIn`, `checkOut`, `adults`, `children`,
`pets`, `privacyPolicyId`, `termsVersion`, `privacyAccepted`, `guest` and
`notes`. Guest fields are `firstName`, `lastName`, `email`, `phone` and
optional `city`. The email is validated, normalized and used only for the
transactional reservation notification; it is not exposed to unrelated public
responses or audit metadata.
The server resolves policy version and content hash from the immutable published
policy identified by `privacyPolicyId`; it does not trust a client-supplied
policy-version string. The ID is a transient request value used only for
current-policy validation. It is not persisted in the booking.

Quote input fields are `roomId`, `checkIn`, `checkOut`, `adults`, `children` and
`pets`. Guest and pet counts are JSON integers, not localized labels.

### Enforce Text And Numeric Boundaries

- first and last name are independently required, trimmed, contain 2 to 80
  characters and use human-name characters;
- phone accepts a Brazilian 10 or 11 digit national number, optionally prefixed
  by country code 55, and is persisted in E.164 form;
- email is required for the public transactional confirmation, is trimmed,
  normalized to lowercase and validated as an email address with a bounded
  length;
- optional city is trimmed and contains at most 120 characters;
- optional notes are trimmed and contain at most 500 characters;
- privacy-policy ID must identify the current published policy and the terms
  version contains at most 100 characters;
- adults are between 1 and 20, children between 0 and 20, their total is at
  most 20 and cannot exceed room capacity;
- pets are between 0 and 5;
- public POST request bodies contain at most 16 KiB.

The backend returns a controlled client error for an invalid field or oversized
public body. Database truncation and frontend attributes are not validation.

### Preserve An Independent Acceptance Snapshot

After validating the transient policy ID, a new booking stores only the
server-derived policy version, content hash and acceptance time. It stores no
policy ID and has no foreign key or JPA relationship with the Privacy module.
The snapshot remains readable without joining or loading a privacy-policy row.

### Minimize Free Text

Notes remain optional and limited to operational stay requests. The public
journey warns against document, financial and unnecessary sensitive content.
Known CPF and card patterns remain rejected, but pattern detection does not
claim to classify every possible sensitive fact.

### Preserve Business Validation

Dates, room availability, room capacity and booking conflicts continue to be
validated by the backend. Audit metadata stores numeric composition and does not
copy phone or notes.

## Prerequisite Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`

## Spec Degree

2.
