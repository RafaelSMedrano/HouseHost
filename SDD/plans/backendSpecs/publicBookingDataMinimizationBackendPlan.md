# Public Booking Data Minimization Backend Plan

## Governing Specs

- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Reduce public DTOs, replace localized count parsing with integers, normalize and
validate booking data in `PublicBookingService`, and reject oversized public
request bodies in an inbound web filter.

## Contract And Validation

Remove document, email, payment and marketing from public booking input and
payment from public booking output. Replace `guests` and `pets` strings in quote
and booking requests with `adults`, `children` and `pets` integers.

Centralize public limits and normalization in the public API application layer.
Persist valid Brazilian phone numbers as E.164. Keep known CPF/card rejection in
the remaining free-text fields.

## Request Size

Create a `OncePerRequestFilter` in `publicapi/adapter/in/web` for POST, PUT and
PATCH requests under `/public/**`. Buffer at most 16 KiB plus one byte, return
HTTP 413 with the normal JSON error shape when exceeded and replay accepted bytes
to Spring MVC.

## Verification

Add service tests for reduced numeric contracts, validation and normalization;
add filter tests for accepted and oversized bodies; run the full Maven suite and
`git diff --check`.

