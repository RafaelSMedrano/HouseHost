# Public Booking Data Minimization Frontend Plan

## Governing Specs

- `SDD/specs/publicBookingDataMinimizationSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Send the reduced numeric backend contract and mirror backend text limits in the
public booking form for immediate feedback.

## Changes

Give each guest-composition option explicit numeric adult and child values,
derive the payload from the selected option and send numeric `adults`,
`children` and `pets` to quote and booking endpoints. Add HTML maximum lengths
for name, surname, city and notes while retaining server authority.

## Verification

Evaluate the public JavaScript modules, scan generated payload fields and run
the backend suite because the public static assets are served by the same
application.

