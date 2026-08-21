# Task 002f DONE — Implement Supplier Management Frontend

## Status

Completed on 26 July 2026. See
`SDD/ImplementationReport/2026-07-26-002f-supplier-management.md`.

## Implementation Area

Frontend (`f`).

## Objective

Create the protected administrative list, filters, form, detail and review
experience for the supplier inventory using the completed backend contract.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/backendSpecs/supplierManagementSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/supplierManagementBackendPlan.md`
- `SDD/plans/frontendSpecs/supplierManagementFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- implementation report produced by task `003b`.

## Dependencies

- Task `003b DONE — Implement Supplier Management Backend` completed and reviewed.

## Scope

- Extend the administrative API adapter with all supplier operations.
- Add administrator-only `suppliers` frontend permission and sidebar entry.
- Route supplier list, form and detail through `UICOntroller`.
- Create a concise supplier list with required filters and review indicators.
- Create supplier identity and dynamic relationship form sections.
- Display role explanations and prevent contradictory `NO_PERSONAL_DATA` input.
- Create protected detail grouped by governance subject.
- Add review, approval, blocking and deactivation interactions.
- Require end date and disposition outcome for deactivation.
- Escape backend-provided text and keep supplier details out of browser storage.
- Add accessible feedback, focus behavior and keyboard-operable dynamic fields.
- Update cache-busting query versions.
- Add available automated checks and a manual verification matrix.

## Out Of Scope

- Backend changes except a documented blocking contract correction performed in
  SDD order.
- Public supplier disclosure.
- File upload, preview or contract storage.
- Automatic role classification or provider discovery.
- Supplier-authenticated access.
- Dashboard charts and automated reminders.

## Expected Files

Expected additions or changes include:

```text
frontend/admin/js/api.js
frontend/admin/js/permissions.js
frontend/admin/js/widgets/sidebarWidget.js
frontend/admin/js/controllers/UICOntroller.js
frontend/admin/js/views/suppliersView.js
frontend/admin/js/views/supplierFormView.js
frontend/admin/js/views/supplierProfileView.js
frontend/admin/css/home.css or a supplier-specific stylesheet
frontend/admin/index.html or the applicable cache-bust owner
frontend tests, if a test harness exists
```

Do not introduce a frontend framework or persist supplier inventory in browser
storage.

## Acceptance Criteria

- Only `CEO`, `CTO` and `ADMIN` see and can navigate to the supplier experience.
- Direct unauthorized backend access still returns `403` independently of the
  frontend permission.
- Authorized users can list, filter, create, view, update and review suppliers.
- Multiple processing relationships can be added and edited.
- Role, risk, governance, contract and disposition values use exact backend
  enums while displaying clear Portuguese labels.
- `NO_PERSONAL_DATA` does not submit contradictory personal-data fields.
- The list remains concise and the detail displays complete permitted evidence.
- Approval requires applicable supporting fields.
- Deactivation requires end date and disposition outcome.
- Dynamic relationship sections are keyboard accessible and have associated
  labels and errors.
- Backend text is rendered safely without HTML injection.
- No supplier detail, contract narrative or security narrative is stored in
  `localStorage` or `sessionStorage`.
- Duplicate submit is prevented while saving.
- Existing administrative views continue working.
- Changed imports receive new cache-busting versions.
- Available automated checks and the manual matrix pass.

## Verification Matrix

Manually verify at minimum:

| Scenario | Expected result |
|---|---|
| Administrator opens suppliers | List loads and filters are available. |
| Non-administrator session | No menu/route; direct API remains `403`. |
| Create supplier with two roles | Both relationships persist and render. |
| Contradictory no-data relationship | UI warns and backend rejects if forced. |
| Approve incomplete relationship | Approval is refused with clear feedback. |
| Deactivate without disposition | Deactivation is refused. |
| Backend validation error | Input remains available where safe; no duplicate save. |
| Reload after viewing detail | No supplier detail is recovered from browser storage. |

## Required Report

Create:

```text
SDD/ImplementationReport/YYYY-MM-DD-002f-supplier-management.md
```

The report must include screenshots or equivalent UI evidence when practical,
all checks run and the prerequisite review required by the SDD process.
