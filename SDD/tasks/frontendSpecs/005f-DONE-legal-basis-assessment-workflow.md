# Task 005f DONE — Implement Legal Basis Assessment Workflow

## Status

Completed on 26 July 2026 after implementation, verification and prerequisite
review.

## Implementation Area

Frontend (`f`).

## Objective

Transform “Tratamentos e bases legais” into a two-tab experience, add the
line-based legal-basis assessment list and complete profile, connect operation
profiles to those assessments, and implement conditional drafts, submission,
approval, rejection and revision interactions.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`
- `SDD/specs/frontendSpecs/processingOperationGovernancePageSpec.md`
- `SDD/specs/frontendSpecs/legalBasisAssessmentGovernancePageSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/legalBasisAssessmentBackendPlan.md`
- `SDD/plans/frontendSpecs/processingOperationGovernancePageFrontendPlan.md`
- `SDD/plans/frontendSpecs/legalBasisAssessmentGovernancePageFrontendPlan.md`
- `SDD/plans/frontendSpecs/legalBasisAssessmentFrontendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- implementation reports produced by tasks `006b` and `004f`.

## Dependencies

- Task `006b DONE — Implement Legal Basis Assessments Backend` completed and
  reviewed.
- Task `004f DONE — Implement Processing Operation Governance Page` completed
  and reviewed.

## Scope

- Add accessible “Operações” and “Bases legais” tabs under the existing menu
  entry.
- Add the line-based assessment list with search, filters and explicit states.
- Extend the API adapter with assessment list and create/update/lifecycle
  operations.
- Route the assessment profile from both its list and the operation profile.
- Complete operation profiles with enabled links to every related assessment;
  remove the “próxima etapa” placeholder.
- Create complete assessment detail with lifecycle and version relationships.
- Create conditional fields for every supported ordinary and sensitive basis.
- Support draft creation/update/submission, review approval/rejection and
  revision creation.
- Require rejection reason and preserve approved versions as read-only.
- Explain that approval records an accountable human governance decision and is
  not automatic legal certification.
- Render narratives safely without browser persistence or console logging.
- Add accessible conditional help, errors, focus and status feedback.
- Update cache-busting versions and add available automated checks plus a manual
  verification matrix.

## Out Of Scope

- Backend changes except a blocking contract correction performed in SDD order.
- The existing operation-list behavior completed by task `004f`, except the tab
  integration and operation-to-assessment profile route required here.
- Public disclosure, automatic legal advice or automatic approval.
- Attachments, legal-opinion storage, policy generation or marketing activation.

## Expected Files

Expected additions or changes include:

```text
frontend/admin/js/api.js
frontend/admin/js/controllers/UICOntroller.js
frontend/admin/js/views/dataProcessingOperationProfileView.js
frontend/admin/js/views/legalBasisAssessmentsView.js
frontend/admin/js/views/legalBasisAssessmentFormView.js
frontend/admin/js/views/legalBasisAssessmentProfileView.js
frontend/admin/css/home.css or a privacy-specific stylesheet
frontend/admin/index.html or the applicable cache-bust owner
frontend/admin/tests/...
```

## Acceptance Criteria

- Authorized users can switch between “Operações” and “Bases legais” using
  accessible tabs.
- The bases tab lists registered assessments in lines with purpose, basis,
  operation, status, version and current/history meaning.
- Search, filters, loading, failure, empty and no-result states are explicit.
- Authorized users can open complete assessment evidence from either the
  assessment list or an operation profile.
- Every assessment shown in an operation profile opens the correct profile and
  no “próxima etapa” placeholder remains.
- Back navigation respects whether the profile originated from the assessment
  list or from a processing operation.
- DTO payloads use exact backend enum values with clear Portuguese labels.
- Every basis shows and submits exactly its conditional evidence fields.
- Sensitive-data purposes require separate basis and indispensability evidence.
- Drafts can be edited and submitted; under-review versions can be approved or
  rejected; approved versions are read-only.
- Rejection requires a reason and revision preserves the approved predecessor.
- Approval language does not claim automatic legal certification.
- Current, rejected and superseded versions remain distinguishable using text in
  addition to color.
- Backend text is rendered safely without HTML injection.
- No assessment narrative is stored in browser persistence or console logs.
- Conditional controls are keyboard accessible with associated labels, help and
  errors.
- Failures preserve safe draft input and prevent duplicate submission.
- Existing administrative views continue working and changed imports receive
  new cache-busting versions.
- Available automated checks and the manual verification matrix pass.

## Verification Matrix

| Scenario | Expected result |
|---|---|
| Switch governance tabs | Correct line-based list opens and active tab is accessible. |
| Search/filter assessments | Matching versions render; failure and empty remain distinct. |
| Open assessment from its list | Complete profile opens and returns to the bases tab. |
| Open assessment from an operation | Same profile opens and returns to the operation. |
| Contract assessment | Contractual context is required and submitted. |
| Legal-obligation assessment | Concrete legal reference is required. |
| Legitimate-interest assessment | Full balance sections are required. |
| Sensitive-data purpose | Separate basis and indispensability are required. |
| Submit incomplete draft | Clear feedback; backend remains authoritative. |
| Approve under-review version | Reviewer/time appear and record becomes read-only. |
| Reject without reason | Action is refused. |
| Revise approved version | New draft opens; predecessor remains readable. |
| Reload after viewing detail | No narrative is restored from browser storage. |

## Required Report

Create after implementation:

```text
SDD/ImplementationReport/YYYY-MM-DD-005f-legal-basis-assessment-workflow.md
```

The report must include UI evidence when practical, all checks run and the
prerequisite review required by `SDD/specs/sddSpec.md`.
