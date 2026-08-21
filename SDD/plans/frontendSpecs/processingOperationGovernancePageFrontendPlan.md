# Processing Operation Governance Page Frontend Plan

## Governing Specs

- `SDD/specs/frontendSpecs/processingOperationGovernancePageSpec.md`
- `SDD/specs/backendSpecs/legalBasisAssessmentSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Add the administrator-only menu entry, processing-operation list, governance
filters and operation profile that expose lawful-basis assessment summaries and
version history through the completed backend contract from task `006b`.

This plan does not authorize implementation. Its frontend task depends on the
completed backend task and must be approved and executed separately.

## Current Frontend Context

Relevant components are:

- `api.js` (frontend/admin API adapter; module);
- `permissions.js` (frontend/admin access rules; module);
- `sidebarWidget.js` (frontend/admin navigation widget; module);
- `UICOntroller.js` (frontend/admin controller; module);
- `home.css` (frontend/admin shared styles);
- supplier list/profile views (frontend/admin views; governance presentation references).

The implementation follows the current JavaScript-module structure and does not
introduce a frontend framework.

## API Adapter

Extend `frontend/admin/js/api.js` with read operations:

```text
findAllDataProcessingOperations
findDataProcessingOperationById
findLegalBasisAssessmentsByOperation
findLegalBasisAssessmentById
```

Filters use `URLSearchParams` or equivalent controlled encoding. IDs are encoded
safely. The API adapter does not cache governance responses in browser storage.

If task `006b` returns operation and current assessment summaries in one detail
response, avoid duplicate network calls while keeping API functions aligned with
the authoritative backend contract.

## Permissions, Menu And Routing

Extend `permissions.js` with a `processingOperations` view available only to
`CEO`, `CTO` and `ADMIN`.

Add “Tratamentos e bases legais” to the existing privacy menu group in
`sidebarWidget.js`, alongside suppliers. Do not create a second competing
privacy group.

Extend `UICOntroller.js` to route between the operation list and operation
profile. Unauthorized route attempts return to an allowed administrative view.
Backend `403` remains independently handled.

## Processing Operation List View

Create:

```text
dataProcessingOperationsView.js (frontend/admin view; module)
```

The view renders concise operation cards or rows with name, code, status,
responsible area, lawful-basis readiness, assessment count, pending/rejected
indicator and relevant review/approval dates.

Provide controlled search and filters for operation status, readiness and
pending work. Loading, failure, empty inventory and no-filter-result states are
different visible states.

The list escapes all backend-provided text and never renders full assessment or
security narratives.

## Processing Operation Profile View

Create:

```text
dataProcessingOperationProfileView.js (frontend/admin view; module)
```

Group operation details into identity/purpose, data and titular categories,
sources/actions, access/recipients, transfer, retention/deletion,
security/responsibility and review/readiness.

Render assessment summaries grouped by purpose. Each summary displays basis,
status, version, current/superseded indication and available lifecycle dates.
Provide a callback/route for opening the full assessment profile implemented by
the subsequent workflow task.

Visually and textually label the old `legalBasis` value as a legacy summary when
the backend still returns it. Never present it as approved evidence.

## State And Navigation

Keep only ephemeral list filters and currently loaded responses in memory.
Operation IDs can be carried in controlled route state, but complete operation
or assessment objects never enter browser persistence.

Back navigation returns to the list without exposing stale detail as a different
user. Session changes clear the normal in-memory administrative state through
the existing application lifecycle.

## Security And Accessibility

- Render backend strings with `textContent` or an existing escaping helper.
- Do not use backend strings in raw `innerHTML` without escaping.
- Do not log responses or narratives.
- Use associated labels for search and filters.
- Make rows/cards and back navigation keyboard operable.
- Express statuses in text and not only color or icons.
- Provide visible/live loading and error feedback.
- Preserve focus when moving between list and profile when practical.

## Cache Busting

Update query versions for changed API, permission, sidebar, controller and view
imports so deployed browsers cannot mix incompatible module versions.

## Verification Strategy

- administrator sees the privacy menu item and opens the operation list;
- unauthorized roles see no menu or route and direct backend access remains
  `403`;
- all returned active/inactive operations can be represented;
- search and filters distinguish empty from failed loading;
- readiness and pending/rejected state are accurate;
- marketing renders as inactive;
- operation profile renders every permitted inventory group;
- assessment summaries group by purpose and preserve version/status meaning;
- legacy basis is visibly distinct from approved assessment evidence;
- backend text cannot inject HTML;
- no governance detail is stored in browser persistence or console logs;
- changed modules evaluate and existing frontend/backend tests continue to pass.

## Out Of Scope

- assessment creation, update, submission, approval, rejection or revision;
- automatic legal advice or readiness decisions in the browser;
- public privacy inventory;
- attachments, policy generation or marketing activation;
- dashboards, charts or automatic reminders.
