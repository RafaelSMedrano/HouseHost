# Legal Basis Assessment Frontend Plan

## Governing Specs

- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`
- `SDD/specs/frontendSpecs/processingOperationGovernancePageSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Add the protected assessment workflow inside the processing-operation governance
experience: create structured drafts, inspect complete evidence, submit for
review, record approval or rejection and create immutable revisions through the
backend contract defined by the backend plan.

This plan does not authorize implementation. Its frontend task depends on the
completed backend task and must be approved and ordered separately.

## Current Frontend Context

Relevant components are:

- `api.js` (frontend/admin API adapter; module);
- `permissions.js` (frontend/admin access rules; module);
- `sidebarWidget.js` (frontend/admin navigation widget; module);
- `UICOntroller.js` (frontend/admin controller; module);
- `home.css` (frontend/admin shared styles);
- `dataProcessingOperationsView.js` (frontend/admin view; created by task `004f`);
- `dataProcessingOperationProfileView.js` (frontend/admin view; created by task `004f`);
- supplier form/profile views as governance interaction references.

The experience follows the existing JavaScript-module architecture and does not
introduce a frontend framework.

## API Adapter

Extend `frontend/admin/js/api.js` with explicit operations for:

```text
findLegalBasisAssessmentById
createLegalBasisAssessmentDraft
updateLegalBasisAssessmentDraft
submitLegalBasisAssessment
approveLegalBasisAssessment
rejectLegalBasisAssessment
createLegalBasisAssessmentRevision
```

The adapter sends exact backend enum values. IDs are encoded safely, and no
assessment narrative is cached in browser storage.

## Assessment Detail

Create:

```text
legalBasisAssessmentProfileView.js (frontend/admin view; module)
```

The assessment detail opens from the operation profile created by task `004f`.
It displays purpose, basis-specific evidence, status, version, approval evidence
and version relationship.

Approved and superseded versions are read-only. Rejected versions display the
rejection reason. Actions shown depend on lifecycle state.

## Conditional Assessment Form

Create:

```text
legalBasisAssessmentFormView.js (frontend/admin view; module)
```

The form always requests purpose, basis, justification, data categories and
necessity. It conditionally requests:

- legal reference and obligation explanation for legal obligation;
- contractual context for contract/pre-contract;
- consent collection, evidence and withdrawal mechanisms for consent;
- interest, expectation, impact, safeguards and conclusion for legitimate
  interest;
- sensitive-data basis and indispensability when sensitive data are marked.

The form explains requirements in plain Portuguese, uses backend enum values,
associates labels/errors with controls, preserves draft input after controlled
validation failures and prevents duplicate submission.

The approval action states that it records an accountable governance decision
and is not automatic legal certification.

## Version And Review Interaction

- Drafts can be edited and submitted.
- Under-review assessments can be approved or rejected.
- Rejection requires a reason.
- Approved assessments are read-only and offer “Criar nova versão”.
- Version history remains visible and distinguishes current, superseded and
  rejected records with text, not color alone.
- The interface never edits an approved record in place.

## Privacy, Security And Accessibility

- Do not persist operations, assessment narratives or legal reasoning in
  `localStorage` or `sessionStorage`.
- Render backend text safely without treating it as HTML.
- Do not put sensitive examples or full assessment content into console logs.
- Require keyboard-operable actions and visible focus.
- Associate every conditional field with label, help and validation feedback.
- Use live/visible status feedback for loading, saving, submitting and review.
- Confirmation dialogs name the assessment and action without reproducing the
  complete narrative.

## Cache Busting

Update query versions on changed administrative imports so deployed browsers do
not mix old navigation, API or routing code with the new views.

## Verification Strategy

- administrators can navigate from the completed operation profile to
  assessment detail and history;
- non-administrators see no route/menu and backend access remains `403`;
- each basis displays exactly its conditional evidence fields;
- request payloads use exact enum values;
- incomplete evidence is explained and backend rejection remains authoritative;
- drafts submit, under-review records approve/reject and approved versions are
  immutable;
- creating a revision preserves the previous version;
- readiness and version statuses render through text as well as color;
- backend text is safely rendered;
- no governance narrative enters browser persistence or console logging;
- failures preserve safe draft input and re-enable actions;
- changed modules evaluate successfully and existing frontend/backend suites
  continue to pass.

## Out Of Scope

- public access to lawful-basis assessments;
- menu, operation list and operation profile already governed by the page plan;
- automatic legal recommendations or approval;
- rich-text editor, attachments or legal-opinion uploads;
- privacy-policy generation;
- marketing consent UI;
- dashboards or automated review reminders beyond status indicators.
