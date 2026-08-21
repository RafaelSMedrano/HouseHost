# Supplier Management Frontend Plan

## Governing Specs

- `SDD/specs/backendSpecs/supplierManagementSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Add a protected administrative supplier inventory experience for listing,
filtering, registering, reviewing and updating suppliers and their personal-data
relationships using the backend contract defined by
`SDD/plans/backendSpecs/supplierManagementBackendPlan.md`.

This plan does not authorize implementation. Its frontend task depends on the
completed backend task and must be approved and ordered separately.

## Current Frontend Context

Relevant components:

- `api.js` (frontend/admin API adapter; module)
- `permissions.js` (frontend/admin access rules; module)
- `sidebarWidget.js` (frontend/admin navigation widget; module)
- `UICOntroller.js` (frontend/admin controller; module)
- `dashboardTopbarWidget.js` (frontend/admin widget; module)
- existing list, form and profile views as visual/interaction conventions.

The supplier experience follows existing static-module patterns and cache
busting. It does not introduce a frontend framework.

## API Adapter

Extend `frontend/admin/js/api.js` with explicit operations:

```text
createSupplier
findAllSuppliers
findSupplierById
updateSupplier
changeSupplierStatus
reviewSupplierRelationship
```

Filters are encoded safely through `URLSearchParams` or equivalent controlled
encoding. The API adapter sends domain enum names and does not translate display
labels into arbitrary backend strings.

## Permissions And Navigation

Extend `permissions.js` with a `suppliers` view available only to `CEO`, `CTO`
and `ADMIN`. Add a supplier/governance navigation item through
`sidebarWidget.js` only for those roles.

`UICOntroller.js` routes between supplier list, form and detail. Directly calling
the backend without the menu remains subject to backend authorization.

## Supplier List View

Create:

```text
suppliersView.js (frontend/admin view; module)
```

Display concise fields:

- official/trade name;
- principal service;
- role labels;
- highest risk;
- governance status;
- lifecycle status;
- last or next review date;
- overdue-review indicator.

Support filters for name, role, risk, governance status and lifecycle status.
The list does not display long security, contract or responsibility text.

## Supplier Form View

Create:

```text
supplierFormView.js (frontend/admin view; module)
```

The form contains supplier identity plus one or more relationship sections. It:

- starts with at least one relationship;
- permits adding/removing unsaved relationship sections;
- displays explanatory role descriptions;
- hides or disables personal-data fields for `NO_PERSONAL_DATA`;
- requires applicable governance evidence before an approval choice;
- uses enum values in requests and friendly Portuguese labels in the UI;
- enforces reasonable client-side length and required-field feedback while
  treating backend validation as authoritative;
- prevents duplicate submit while saving;
- does not accept credentials, secrets or contract files.

## Supplier Detail And Review

Create:

```text
supplierProfileView.js (frontend/admin view; module)
```

The detail groups information into identity, relationships, data categories,
location/transfer, retention/deletion, security/incidents, contract,
responsibilities and review history.

Review actions allow an authorized user to record governance status, risk,
review notes and next review date. Deactivation requires end date and data
disposition outcome. Confirmation text must explain that approval records a
review decision and is not an automatic legal certification.

## Privacy, Security And Accessibility

- Never place supplier detail or contract/security narratives in browser
  storage.
- Never render HTML supplied by the backend without escaping.
- Do not collect individual supplier contacts when a functional channel is
  sufficient.
- Do not display credentials, secrets or full contract files.
- Associate labels and errors with inputs and provide keyboard-operable dynamic
  relationship sections.
- Announce save, validation and load outcomes through visible text/live regions.
- Do not rely only on color for risk, overdue or blocked status.

## Cache Busting

Update query versions on all changed imports in the administrative frontend so
deployed browsers do not combine old routing, permissions or API functions with
new supplier views.

## Verification Strategy

- authorized administrator can open list, form and detail;
- other roles do not see or route to the supplier view;
- backend `403` remains handled if a route is invoked directly;
- create and update payloads use exact enum values;
- multiple relationships render and submit correctly;
- `NO_PERSONAL_DATA` prevents contradictory fields;
- filters are encoded and reflected accurately;
- list remains concise and detail displays complete permitted evidence;
- approval and deactivation require their supporting fields;
- no detail is persisted in browser storage;
- dynamic controls are keyboard accessible;
- failures preserve user input where safe and prevent duplicate submission;
- cache-busting versions are updated.

## Out Of Scope

- public supplier list;
- contract/evidence upload or preview;
- automatic provider discovery;
- legal advice or automatic role recommendation;
- supplier self-service portal;
- charts or executive dashboards beyond list status indicators.
