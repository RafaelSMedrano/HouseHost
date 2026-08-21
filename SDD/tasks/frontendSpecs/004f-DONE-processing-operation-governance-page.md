# Task 004f DONE — Implement Processing Operation Governance Page

## Status

Completed on 26 July 2026 after acceptance and prerequisite review.

## Implementation Area

Frontend (`f`).

## Objective

Create the protected menu entry, processing-operation list and operation profile
that list lawful-basis assessment entities and their versions using the
completed backend contract from task `006b`.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`
- `SDD/specs/frontendSpecs/processingOperationGovernancePageSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/legalBasisAssessmentBackendPlan.md`
- `SDD/plans/frontendSpecs/processingOperationGovernancePageFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- implementation report produced by task `006b`.

## Dependencies

- Task `006b DONE — Implement Legal Basis Assessments Backend` completed and
  reviewed.

## Scope

- Extend the administrative API adapter with operation and assessment read
  operations.
- Add administrator-only permission, privacy navigation and routing.
- Create a concise processing-operation list with status/readiness filters.
- Create operation detail grouped by purpose and assessment version.
- Display assessment summaries and immutable version history in the operation
  profile.
- Distinguish legacy basis text from approved assessment evidence.
- Render backend narratives safely and keep them out of browser persistence and
  console logs.
- Add accessible filters, keyboard navigation and visible/live feedback.
- Update cache-busting versions and add available automated checks plus a
  manual verification matrix.

## Out Of Scope

- Backend changes except a documented blocking contract correction performed in
  SDD order.
- Public disclosure of internal assessments.
- Automatic legal advice, recommendation or approval.
- Assessment creation, update, submission, approval, rejection or revision.
- Full assessment form/detail, rich text, attachments, legal opinions or policy
  generation.
- Marketing consent or reactivation.
- Automated reminder dashboards.

## Expected Files

Expected additions or changes include:

```text
frontend/admin/js/api.js
frontend/admin/js/permissions.js
frontend/admin/js/widgets/sidebarWidget.js
frontend/admin/js/controllers/UICOntroller.js
frontend/admin/js/views/dataProcessingOperationsView.js
frontend/admin/js/views/dataProcessingOperationProfileView.js
frontend/admin/css/home.css or a privacy-specific stylesheet
frontend/admin/index.html or the applicable cache-bust owner
frontend/admin/tests/...
```

Do not introduce a frontend framework or persist assessment content in browser
storage.

## Acceptance Criteria

- Only `CEO`, `CTO` and `ADMIN` see and navigate to the experience.
- Direct unauthorized backend access remains `403` independently of frontend
  permissions.
- Operation list displays concise lawful-basis readiness and filters.
- Operation detail groups assessment versions by explicit purpose.
- All returned operation inventory groups are represented in the profile.
- Marketing is visibly inactive and legacy basis text is not represented as
  approved evidence.
- Version history distinguishes current, superseded and rejected records using
  text in addition to color.
- Backend text is rendered safely without HTML injection.
- No operation or assessment narrative is stored in `localStorage`,
  `sessionStorage` or console logs.
- Search, filters and list/profile navigation are keyboard accessible with
  associated labels and errors.
- Loading failure is distinct from an empty inventory or no filter results.
- Existing administrative views continue working and changed imports receive
  new cache-busting versions.
- Available automated checks and the manual verification matrix pass.

## Verification Matrix

| Scenario | Expected result |
|---|---|
| Administrator opens processing operations | List and readiness filters load. |
| Non-administrator session | No menu/route; direct API remains `403`. |
| Search/filter operations | Matching records render; empty and failure remain distinct. |
| Open operation | Full inventory groups and assessment summaries load. |
| Inactive marketing | Clearly inactive; no approved-readiness inference. |
| Historical versions | Current, rejected and superseded meanings remain clear. |
| Reload after viewing detail | No narrative is restored from browser storage. |

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-004f-processing-operation-governance-page.md
```

The report must include UI evidence when practical, all checks run and the
prerequisite review required by `SDD/specs/sddSpec.md`.
