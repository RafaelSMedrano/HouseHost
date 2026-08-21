# Legal Basis Assessment Governance Page Spec

## Specification

Legal Basis Assessment Governance Page is the protected administrative
experience that gives processing operations and their registered lawful-basis
assessments equal, direct navigation inside “Tratamentos e bases legais”. It
allows authorized administrators to browse either governance entity, open its
profile and move between a processing operation and the assessments that belong
to it.

In this experience, “Bases legais” means the structured, versioned
`LegalBasisAssessment` records governed by `legalBasisAssessmentSpec`. It does
not mean a static catalog containing only LGPD lawful-basis names.

## Scope

The experience belongs to the authenticated administrative frontend and has two
primary tabs under the same privacy menu entry:

```text
Tratamentos e bases legais
  -> Operações
       -> lista de operações
       -> perfil da operação
            -> avaliações de base legal relacionadas
                 -> perfil da avaliação
  -> Bases legais
       -> lista de avaliações de base legal
       -> perfil da avaliação
            -> operação relacionada
```

The tabs are alternative entry points into the same governance relationships.
They do not duplicate entities or create a second source of truth. Only `CEO`,
`CTO` and `ADMIN` can access them, and backend authorization remains
authoritative.

The spec governs navigation, lists and read-only profiles. Assessment creation,
editing, submission, approval, rejection and revision continue to follow
`legalBasisAssessmentSpec` and the applicable frontend workflow plan.

## Capabilities

### Provide Two Explicit Governance Tabs

The page displays tabs named “Operações” and “Bases legais”. “Operações” opens
the processing-operation list already provided by the product. “Bases legais”
opens the lawful-basis assessment list.

The active tab is communicated through text, visual state and accessible tab
semantics. Keyboard users can activate either tab. Loading or switching tabs
does not place governance narratives in browser persistence.

### List Registered Legal Basis Assessments

The “Bases legais” tab displays one line for every assessment returned by the
authorized backend contract. Each concise line identifies:

- assessed purpose;
- lawful-basis type with a clear Portuguese label;
- related processing-operation name and stable code;
- lifecycle status;
- assessment version;
- whether the version is current or superseded;
- submission or review evidence when available;
- an action to open the assessment profile.

The presentation is analogous to the processing-operation list: it uses a
readable line-based layout rather than independent content cards, renders status
with text in addition to color and separates loading, failure, empty and
no-filter-result states.

### Search And Filter Legal Basis Assessments

Authorized users can search by purpose, basis label, operation name or stable
operation code. They can filter by lifecycle status, lawful-basis type, current
or historical version and related operation when supported by the backend
contract.

Filtering never converts a failed request into an apparently empty compliant
inventory.

### Show The Complete Legal Basis Assessment Profile

Opening a list line displays the assessment profile with:

- related processing operation and explicit purpose;
- ordinary-data lawful basis, justification, evaluated data categories and
  necessity analysis;
- basis-specific evidence required by `legalBasisAssessmentSpec`;
- sensitive-data basis, indispensability and safeguards when applicable;
- lifecycle status, version and current or superseded meaning;
- authorship, submission, review, approval or rejection evidence permitted by
  the backend;
- rejection reason when applicable;
- predecessor or successor relationship when available;
- a route back to the originating list and a route to the related operation.

Approved and superseded records are visibly read-only. The profile states that
approval records an accountable human governance decision and is not automatic
legal certification.

### Complete The Operation-To-Assessment Relationship

The reserved assessment section in every processing-operation profile lists all
assessment versions related to that operation, grouped by purpose. Each line
shows basis, status, version, current or historical meaning and available review
evidence.

The current placeholder action is replaced by a working action that opens the
selected assessment profile. The interface must not display “próxima etapa” or
another implementation placeholder after this capability is delivered.

Returning from an assessment opened through an operation preserves a coherent
route back to that operation. Opening the same assessment from the “Bases
legais” tab returns to the assessment list.

### Preserve Safe And Accessible Presentation

Backend text is escaped and never treated as executable HTML. Complete legal
reasoning appears only in profiles or controlled forms, never in concise list
lines, logs, `localStorage` or `sessionStorage`.

Tabs, list actions, filters, back navigation and assessment actions are keyboard
operable. Focus, loading and error changes receive visible or live-region
feedback. Status and version meaning never depend only on color.

## Prerequisite Specs

- `SDD/specs/frontendSpecs/processingOperationGovernancePageSpec.md`
- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`

## Spec Degree

4.
