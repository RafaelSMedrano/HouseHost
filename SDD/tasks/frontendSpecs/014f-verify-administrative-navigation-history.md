# Task 014f — Verify Administrative Navigation History

## Status

Superseded by task `020f`. Not authorized for implementation; retained as the
former route-registry task for historical traceability.

## Implementation Area

Frontend (`f`).

## Objective

Verify the complete navigation-history capability, fix only issues within the
approved preceding scopes and document the prerequisite review.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`

## Required Plans

- `SDD/plans/frontendSpecs/administrativeNavigationHistoryFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- all implementation files listed by tasks `009f` through `013f`;
- existing frontend test files relevant to controllers and views.

## Dependencies

- Tasks `009f`, `010f`, `011f`, `012f` and `013f` completed and reviewed.

## Scope

Run automated unit and integration checks where available, then execute the
manual matrix below. Fix only regressions caused by the navigation-history
implementation and record each fix in the implementation report.

Required flow matrix:

| Flow | Expected back path |
|---|---|
| Caixa -> transação -> hóspede | hóspede -> transação -> Caixa |
| Caixa -> transação -> reserva | reserva -> transação -> Caixa |
| Reservas -> reserva -> hóspede | hóspede -> reserva -> Reservas |
| Hóspedes -> hóspede | hóspede -> Hóspedes |
| Fornecedores -> fornecedor -> editar | editar cancel -> fornecedor |
| Operações -> operação -> avaliação | avaliação -> operação |
| Avaliações -> avaliação -> operação | operação -> avaliação |
| Sidebar reset after deep path | new root flow only |
| Profile -> edit -> save | profile without duplicate edit |
| Root back | safe module fallback or dashboard |

Verification must include permission-denied routes, missing identifiers,
failed profile loads, repeated back clicks, refresh behavior and keyboard
activation of back controls.

## Acceptance Criteria

- All mandatory flow rows produce the expected path.
- Stack operations have automated coverage for append, pop, replace and reset.
- Repeated back at the root is safe and deterministic.
- Failed route rendering does not corrupt the remaining stack.
- Permission checks remain unchanged.
- No unrelated backend or frontend behavior is changed.
- `git diff --check` and available project tests pass.
- Unavailable verification commands are explicitly recorded with their reason.
- Prerequisite review confirms conformity with the governing spec, plan,
  bootstrap and implementation order.

## Expected Files

```text
frontend/admin/tests/navigationController.test.mjs
frontend/admin/tests/administrativeNavigationFlows.test.mjs
```

## Required Report

Create `SDD/ImplementationReport/YYYY-MM-DD-014f-verify-administrative-navigation-history.md`
after implementation and verification. The report must list every file
created, every file changed, all commands run, unavailable commands and the
final prerequisite review.
