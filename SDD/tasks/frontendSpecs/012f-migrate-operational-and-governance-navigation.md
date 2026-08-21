# Task 012f — Migrate Operational And Governance Navigation

## Status

Superseded by task `018f`. Not authorized for implementation; retained as the
former route-registry task for historical traceability.

## Implementation Area

Frontend (`f`).

## Objective

Apply the navigation-history contract to the existing room, operations,
supplier and privacy-governance controller factories.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/administrativeNavigationHistoryFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `frontend/admin/js/controllers/roomController.js`
- `frontend/admin/js/controllers/operationsController.js`
- `frontend/admin/js/controllers/supplierController.js`
- `frontend/admin/js/controllers/privacyController.js`
- related operational and governance views under `frontend/admin/js/views/`

## Dependencies

- Task `011f` completed and reviewed.

## Scope

Migrate these paths:

```text
Quartos -> editar quarto
Check-in -> formulário de check-in
Check-out -> formulário de check-out
Fornecedores -> perfil -> editar
Operações -> perfil da operação -> avaliação
Avaliações -> perfil da avaliação -> operação relacionada
```

Preserve operation and assessment origin context when the product already
distinguishes the operation profile from the assessment list. Convert that
origin into actual route history rather than a second competing back system.

Forms must cancel with `back` and save with the documented `replace` or
`navigate` behavior. Permission failures must resolve to an authorized route
without adding the rejected route to history.

## Acceptance Criteria

- Every operational form returns to its initiating screen.
- Supplier profile returns to the supplier list when opened from the sidebar.
- Supplier edit returns to the supplier profile on cancel.
- Assessment opened from an operation returns to that operation.
- Assessment opened from the assessment list returns to that list.
- Navigating to a related operation does not discard the assessment predecessor.
- Unauthorized privacy/supplier routes do not remain in the stack.
- Existing governance version and lifecycle behavior is preserved.

## Expected Files

```text
frontend/admin/js/controllers/roomController.js
frontend/admin/js/controllers/operationsController.js
frontend/admin/js/controllers/supplierController.js
frontend/admin/js/controllers/privacyController.js
frontend/admin/js/views/roomFormView.js
frontend/admin/js/views/supplierFormView.js
frontend/admin/js/views/supplierProfileView.js
frontend/admin/js/views/legalBasisAssessmentProfileView.js
frontend/admin/tests/operationalNavigationFlows.test.mjs
```

## Required Report

Create `SDD/ImplementationReport/YYYY-MM-DD-012f-migrate-operational-and-governance-navigation.md`
after implementation and verification.
