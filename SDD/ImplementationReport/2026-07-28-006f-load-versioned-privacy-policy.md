# Implementation Report — Task 006f Versioned Privacy Policy Experience

## Task And Execution

- Task: `006f DONE — Load And Acknowledge Versioned Privacy Policy`.
- Execution completed: 28 July 2026.
- Implementation file: `SDD/implementation/implementation-order.md`.
- Completion state: complete.
- Deployment state: no deployment or EC2 change was performed, following the
  user's explicit instruction to finish the task and stop.

## Documents Read

- `AGENTS.md`;
- `SDD/specs/sddSpec.md`;
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/publicBookingDataMinimizationSpec.md`;
- `SDD/specs/backendSpecs/privacyHexagonalSubmodulesSpec.md`;
- `SDD/specs/backendSpecs/privacyPolicySubmoduleSpec.md`;
- `SDD/specs/frontendSpecs/publicPrivacyPolicyExperienceSpec.md`;
- `SDD/plans/frontendSpecs/publicPrivacyPolicyFrontendPlan.md`;
- `SDD/plans/backendSpecs/publicBookingPrivacyAcceptanceBackendPlan.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/frontendSpecs/006f-DONE-load-versioned-privacy-policy.md`;
- `SDD/ImplementationReport/2026-07-27-009b-implement-privacy-policy-submodule.md`;
- `SDD/ImplementationReport/2026-07-28-010b-record-privacy-acceptance-snapshot.md`.

## Files Created

- `frontend/public/js/privacyPolicyDocument.js`;
- `frontend/public/js/controllers/privacyPolicyController.js`;
- `frontend/public/tests/api.test.mjs`;
- `frontend/public/tests/privacyPolicyDocument.test.mjs`;
- `frontend/public/tests/privacyPolicyController.test.mjs`;
- `SDD/ImplementationReport/2026-07-28-006f-load-versioned-privacy-policy.md`.

## Files Changed

- `frontend/public/js/api.js`;
- `frontend/public/js/controllers/UICOntroller.js`;
- `frontend/public/js/controllers/main.js`;
- `frontend/public/js/controllers/publicInteractions.js`;
- `frontend/public/js/views/politicaPrivacidadeView.js`;
- `frontend/public/js/views/reservaView.js`;
- `frontend/public/css/publicSite.css`;
- `frontend/public/index.html`;
- `README.md`;
- `SDD/specs/lgpdGovernanceSpec.md`;
- `SDD/specs/moduleArchitectureSpec.md`;
- `SDD/specs/publicBookingDataMinimizationSpec.md`;
- `SDD/plans/frontendSpecs/publicPrivacyPolicyFrontendPlan.md`;
- `SDD/plans/backendSpecs/publicBookingPrivacyAcceptanceBackendPlan.md`;
- `SDD/implementation/task-bootstrap.md`;
- `SDD/implementation/implementation-order.md`;
- `SDD/tasks/frontendSpecs/006f-DONE-load-versioned-privacy-policy.md`.

## Flows Implemented

The public API adapter now loads `GET /public/privacy-policy` with browser cache
disabled for that lookup. Its typed error preserves HTTP status so the booking
flow can distinguish a policy conflict from ordinary request failures. The
adapter still unwraps the project's standard response envelope.

The static authoritative notice was removed from the public policy page. The
page now has explicit loading and unavailable states and a keyboard-operable
retry action. A strict client-side normalizer accepts schema version 1 and only
the controlled section, paragraph, list and HTTP(S) link shapes. The renderer
creates semantic DOM nodes and assigns every server string through
`textContent`; server content is never interpolated as HTML. Version and the
calendar effective date come from the current response.

One in-memory controller owns policy ID, version, title, parsed content, hash,
effective date, loading state, acknowledgement identity, conflict state and
submission state. It ignores obsolete concurrent loads and writes nothing to
browser storage. The reservation view loads the same current policy, blocks its
native checkbox and submit button until ready, exposes the document inline and
also offers a new-tab policy link that preserves the active form.

Booking payloads now contain transient `privacyPolicyId` and
`privacyAccepted`; the removed client-declared version and the content hash are
not submitted. A policy `409` clears acknowledgement, reloads the policy,
opens the updated document, focuses the live status message and preserves the
other form controls. A failed conflict reload remains unavailable and blocked.

Submission has an in-memory single-flight state. Repeated calls, rerender
attempts and SPA navigation cannot release the guard while the POST is active.
Navigation is held until the request completes, and confirmation uses a
pre-request display snapshot instead of reading form elements after the server
has created the booking. This prevents a removed DOM from turning a successful
booking into an apparent frontend failure. An intentionally new journey after
leaving a completed flow can reset the terminal state.

## Technical And MVP Decisions

- `cache: "no-store"` was selected for the current-policy lookup because the
  backend has no explicit public cache validator; server-side `409` remains the
  authoritative concurrency protection.
- Policy IDs must be positive JavaScript safe integers before being retained or
  returned. Hashes must use the published `sha256:` plus 64 lowercase
  hexadecimal digits format.
- `effectiveAt` is formatted from its ISO calendar components in UTC so the
  backend `LocalDateTime` date cannot shift with the browser timezone.
- The hash is retained only in current page memory for integrity context. It is
  neither persisted in browser storage nor sent as booking authority.
- Native checkbox, live status regions, retry buttons, visible focus and
  disabled states replace the previous hidden click-only checkbox.
- The client prevents duplicate in-page submissions and a `409` creates no
  booking by backend contract. Exactly-once recovery after a server commit whose
  response is lost would require a backend idempotency key and remains outside
  this frontend task.

## Difficulties, Problems And Resolutions

The required documents cited a mother-spec path that no longer exists. The real
file is under `SDD/specs/backendSpecs/`; the task, its required plans and the
affected prerequisite declarations were corrected without changing product
intent.

Node.js was not installed in the development `PATH`. An official Node.js
22.17.0 archive was downloaded only to `/tmp`, and its SHA-256 was matched
against the official checksum before use. The first frontend run had one test
failure because the test expected an absolute local URL even though the
existing API resolver intentionally uses a relative URL when served on port
8080. The expectation was corrected to the established behavior. The temporary
runtime and archives were removed after verification.

Independent review found that the first submission guard could be reset by a
reservation rerender and that navigating during a slow successful POST could
remove fields later read by the confirmation code. The controller now rejects
reset while submitting, same-view navigation does not rerender, navigation away
is blocked during the POST and confirmation uses an immutable display snapshot.
Tests were expanded to cover guard reset, real booking `409`, DOM states, field
preservation, focus, failed conflict reload and safe policy identifiers.

No automated real-browser visual smoke test was available in the repository.
The semantic DOM renderer, focus helper, control-state transitions and
keyboard-relevant markup are covered by Node tests and source checks, but a
future visual regression suite can complement these checks.

## Tests And Verification

- `/tmp/node-v22.17.0-darwin-x64/bin/node --check` for every changed public
  JavaScript module: passed;
- `/tmp/node-v22.17.0-darwin-x64/bin/node --test frontend/public/tests/*.test.mjs`:
  16 tests passed, zero failures, zero skipped;
- `./mvnw test`: 126 tests passed, zero failures, zero errors and zero skipped;
- the existing backend `PublicBookingPolicyConflictHttpTest` passed inside the
  full Maven suite;
- searches for hard-coded policy version/content, browser storage, console
  logging and the removed client `privacyPolicyVersion`: no production matches;
- targeted trailing-whitespace search: no matches;
- `git diff --check`: passed.

## Prerequisite And Acceptance Review

The result was compared with the mother spec, LGPD governance, module
architecture, public minimization, privacy submodule boundary, policy backend,
public policy experience, both implementation plans, task criteria and active
implementation rules.

The policy page has no authoritative hard-coded policy body or version. It
loads and safely renders server title, content, version and effective date while
retaining ID and hash only in memory. Failure is explicit and retryable and
blocks acknowledgement and submission. The booking request sends only the
transient policy ID and acknowledgement. Conflict recovery preserves form
fields, clears acknowledgement, displays the new document, requires a new
acknowledgement and remains single-flight. Loading and conflict feedback are
textual, live, focusable and keyboard operable. Cache-busting imports were
updated, existing navigation remains available outside the protected in-flight
window and all executable checks passed.

No contradiction remains. Every acceptance criterion is satisfied.
