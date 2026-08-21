# Legal Basis Assessment Governance Page Frontend Plan

## Governing Specs

- `SDD/specs/frontendSpecs/legalBasisAssessmentGovernancePageSpec.md`
- `SDD/specs/frontendSpecs/processingOperationGovernancePageSpec.md`
- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Evolve “Tratamentos e bases legais” into a two-tab governance experience, add a
line-based assessment inventory and full assessment profile, and connect every
assessment summary in an operation profile to that detail route.

This plan complements `legalBasisAssessmentFrontendPlan`: this document defines
information architecture, lists, profiles and navigation, while that plan
defines assessment lifecycle forms and commands.

## Current Frontend Context

Relevant components are:

- `api.js` (frontend/admin API adapter; module);
- `UICOntroller.js` (frontend/admin navigation controller; module);
- `dataProcessingOperationsView.js` (frontend/admin operation list view; module);
- `dataProcessingOperationProfileView.js` (frontend/admin operation profile view; module);
- `home.css` (frontend/admin shared styles);
- `processingOperationGovernance.test.mjs` (frontend/admin governance tests).

The implementation retains the existing JavaScript-module architecture and
does not introduce a frontend framework.

## Tabs And Navigation State

Create a small parent governance view or controller-owned tab shell for
“Operações” and “Bases legais”. The shell owns only ephemeral active-tab and
navigation state. It must not duplicate loaded domain objects or persist legal
content in browser storage.

Use accessible tab semantics, visible focus and an explicit active state. The
operation list remains the initial tab for compatibility. Back navigation uses
an origin descriptor in memory so an assessment profile can return either to
its related operation or to the assessment list.

## API Adapter

Reuse the existing assessment detail and operation-scoped endpoints. Add or
adapt an authorized collection query capable of providing the assessment list
with enough related-operation identity for each line.

If the current backend exposes only operation-scoped collections, first verify
whether the existing operation summary contract can assemble the authorized
inventory without narrative duplication. A missing efficient collection
contract is documented as a blocking backend dependency rather than hidden
behind unbounded browser fan-out.

Encode IDs and controlled filters safely. Do not cache responses in browser
storage and do not log assessment payloads.

## Legal Basis Assessment List View

Create:

```text
legalBasisAssessmentsView.js (frontend/admin view; module)
```

Render a line-based list analogous to `dataProcessingOperationsView.js`. Each
line contains purpose, basis, related operation, lifecycle status, version,
current/history meaning, relevant dates and an explicit profile action.

Provide controlled search and filters for status, basis, current/history and
operation when the collection contract supports them. Keep loading, error,
empty inventory and filtered-empty states distinct. Render only concise text and
escape every backend value.

## Legal Basis Assessment Profile View

Create:

```text
legalBasisAssessmentProfileView.js (frontend/admin view; module)
```

Render identity, operation relationship, purpose, common evidence,
basis-specific evidence, sensitive-data evidence, lifecycle facts, rejection
reason and version relationships in readable line-based sections. Approved and
superseded versions are read-only.

Expose navigation to the related operation. The profile also hosts the state
appropriate actions and routes defined by `legalBasisAssessmentFrontendPlan`
without duplicating lifecycle rules in the controller.

## Complete The Operation Profile

Extend `dataProcessingOperationProfileView.js` so every assessment line has an
enabled inspect action. Wire `onOpenAssessment` in `UICOntroller.js`, carrying
the originating operation ID in ephemeral navigation state.

Remove disabled “Detalhe na próxima etapa” wording. Preserve grouping by
purpose, immutable version history, textual statuses and the distinction between
legacy `legalBasis` text and approved structured evidence.

## Styling And Accessibility

Reuse the line visual language established for the operation list and operation
profile. Do not introduce cards for assessment list or profile content.

Use associated filter labels, accessible tabs, keyboard-operable row actions,
visible focus, live loading/error feedback and responsive line layouts. Text
must carry status and version meaning independently of color.

## Cache Busting

Update query versions on every changed administrative import so deployed
browsers cannot mix the tab shell, controller, API adapter and new views from
different releases.

## Verification Strategy

- authorized administrators can switch between the two tabs;
- the assessment list represents every returned record in concise lines;
- search and filters preserve distinct loading, failure and empty states;
- opening a list line displays complete safely escaped evidence;
- an assessment opened from an operation returns coherently to that operation;
- an assessment opened from its tab returns to the assessment list;
- every operation-profile assessment action is enabled and opens the correct ID;
- no “próxima etapa” placeholder remains;
- current, rejected and superseded versions remain textually distinguishable;
- no governance narrative enters browser persistence or console output;
- responsive and keyboard navigation checks pass;
- existing administrative checks remain green.

## Out Of Scope

- public access to assessments;
- automatic legal recommendations, certification or approval;
- backend contract changes unless an efficient authorized collection endpoint is
  confirmed missing and handled through a separate SDD backend change;
- attachments, legal-opinion uploads, policy generation or marketing activation.
