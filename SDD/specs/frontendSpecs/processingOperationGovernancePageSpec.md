# Processing Operation Governance Page Spec

## Specification

Processing Operation Governance Page is the protected administrative experience
that makes the privacy inventory visible and navigable. It allows authorized
administrators to find every registered processing operation and inspect the
lawful-basis assessment records and versions that belong to it.

The page presents governance entities managed by the product. It is not a
generic browser for Java classes, database tables or arbitrary domain objects.

## Scope

The page belongs to the authenticated administrative frontend and appears in the
privacy section of the main menu. It consumes the processing-operation and
lawful-basis assessment contracts governed by
`legalBasisAssessmentSpec`.

The experience has two navigation levels:

```text
Processing operation list
  -> processing operation profile
       -> lawful-basis assessment summaries and version history
```

Only `CEO`, `CTO` and `ADMIN` can see or navigate to this experience. Backend
authorization remains authoritative when a route or API is invoked directly.

This spec governs listing, filtering, navigation and read-only presentation.
Creating, editing, submitting, approving, rejecting and revising an assessment
remain governed by `legalBasisAssessmentSpec` and a separate frontend task.

## Capabilities

### Place Governance In The Administrative Menu

The main administrative menu contains a visible privacy group with an item
named “Tratamentos e bases legais” or an equivalent clear Portuguese label.

Selecting the item opens the processing-operation list. The menu item is hidden
for unauthorized roles, direct unauthorized navigation is refused by the
frontend and direct backend access independently returns `403`.

### List Every Registered Processing Operation

The list displays one concise item per processing operation, including:

- operation name and stable code;
- operational status;
- responsible area;
- lawful-basis readiness;
- count of current assessments;
- indication of draft, under-review or rejected assessments;
- last inventory review and last lawful-basis approval when available.

The list includes active and inactive operations when requested. Marketing is
visibly inactive and is never presented as current merely because it has a
legacy basis or historical record.

### Support Search And Governance Filters

Authorized users can search by operation name or stable code and filter by:

- operational status;
- lawful-basis readiness;
- responsible area when supported by the backend response;
- presence of pending or rejected assessment work.

Empty, loading, error and no-result states are explicit. A failed load does not
silently appear as an empty compliant inventory.

### Show The Processing Operation Profile

Opening an operation displays its registered inventory information in readable
groups:

- description and purpose;
- titular and personal-data categories;
- sources and processing actions;
- internal access and external recipients;
- transfer indication;
- retention and deletion;
- security measures and responsible area;
- operational status and generic inventory review evidence;
- lawful-basis readiness.

The profile clearly distinguishes the legacy lawful-basis summary from approved
assessment evidence. It does not describe the legacy string or generic
`reviewedAt` timestamp as lawful-basis approval.

### List Assessments And Immutable Versions

The operation profile lists the new lawful-basis assessment entities grouped by
explicit purpose. For every record it displays:

- basis type;
- lifecycle status;
- version;
- whether it is current or superseded;
- submission, approval or rejection date when available;
- reviewer evidence when permitted;
- a clear action to inspect the complete assessment.

Historical versions remain visible. Approved and superseded versions are marked
read-only. Rejected records display that a rejection reason exists without
copying complete narratives into the concise list.

### Preserve Safe And Accessible Presentation

Long governance narratives appear only in the operation or assessment detail,
not in the list. Backend text is rendered as text, not executable HTML.

The page does not store operation details or assessment narratives in
`localStorage` or `sessionStorage` and does not write them to console logs.

Navigation, filters and expandable groups are keyboard operable. Status is
expressed through text in addition to color, and loading/error changes are
announced visibly or through an appropriate live region.

## Prerequisite Specs

- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`

## Spec Degree

3.
