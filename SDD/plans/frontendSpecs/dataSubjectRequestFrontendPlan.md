# Data-Subject Request Frontend Plan

## Governing Specs

- `SDD/specs/frontendSpecs/dataSubjectRequestExperienceSpec.md`
- `SDD/specs/dataSubjectRequestWorkflowSpec.md`
- `SDD/specs/backendSpecs/dataSubjectRequestSubmoduleSpec.md`
- `SDD/specs/frontendSpecs/publicPrivacyPolicyExperienceSpec.md`
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Add the public formalization/status experience and a separate administrative
queue after the backend request contracts are implemented and verified.

This plan is executed by frontend tasks `007f` and `008f`; its creation does not
approve either task.

## Public Policy And Entry

Update the canonical backend policy through its publication workflow with the
reviewed rights-channel language. The frontend continues rendering the current
server document and does not hard-code a competing policy version.

Add public routes/views for invitation formalization, minimized status and safe
response retrieval. Extend `frontend/public/js/api.js` with dedicated request
methods. Keep link/session secrets only in memory, remove the raw token from
browser history after exchange and exclude it from logs, analytics and storage.

The form collects right type, request description and only necessary locating
information. It presents loading, invalid/expired, validation, retry, submitted
and protocol states without revealing record existence.

## Administrative Queue

Add a rights-request area to the existing authenticated administration using
its current controller/view/API patterns. Provide paginated filters for status,
right, assignee and due state; a minimized queue; detail/history; assignment;
identity decision; context analysis; action recording; delivery status; and
final response.

Dangerous execution controls show the precise action and context, require
confirmation and remain pending until the backend returns execution evidence.
The UI never directly changes guest, booking, audit or finance data.

## State And Error Handling

The backend remains authoritative for statuses, identity, deadlines, roles and
allowed transitions. Prevent duplicate submissions and commands, disable
controls while a command is pending and reconcile after conflicts. Preserve
only safe unsent UI state after recoverable errors.

Never place narratives, identity evidence, response packages or tokens in
local/session storage, URLs after exchange, console logs or list rows. Use
generic public errors and specific authenticated administrative errors.

## Accessibility And Verification

Use semantic labels, keyboard-operable controls, focus management, textual
status, live feedback and clear confirmation dialogs. Verify expired and reused
links, retry/idempotency, public error equivalence, protocol display, status
refresh, role denial, overdue filters, irreversible confirmations and backend
conflicts. Run available frontend checks and `git diff --check`.

## Task Split

- `007f`: policy wording, public invitation, submission, protocol, status and
  safe response retrieval.
- `008f`: authenticated administrative queue, history, analysis, execution and
  response controls.

## Out Of Scope

- policy authoring editor;
- browser-side identity decisions or deadline calculation;
- WhatsApp automation;
- direct modification of backend-owned personal data;
- storing sensitive request state in the browser.
