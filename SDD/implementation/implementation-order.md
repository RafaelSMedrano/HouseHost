# Implementation Order

## Purpose

This file defines the active ordered SDD implementation cycle for Cantinho das
Lavandas.

Before executing any listed task, read:

- `SDD/specs/sddSpec.md`;
- `SDD/implementation/task-bootstrap.md`;
- the current task file;
- every spec and plan required by that task.

## Ordered Tasks

1. `SDD/tasks/backendSpecs/001b-DONE-login-failure-state.md`
2. `SDD/tasks/backendSpecs/002b-DONE-enforce-login-failure-limits.md`
3. `SDD/tasks/frontendSpecs/001f-DONE-login-restriction-feedback.md`
4. `SDD/tasks/backendSpecs/003b-DONE-supplier-management.md`
5. `SDD/tasks/frontendSpecs/002f-DONE-supplier-management.md`
6. `SDD/tasks/backendSpecs/004b-DONE-security-audit-processing-inventory.md`
7. `SDD/tasks/backendSpecs/005b-DONE-public-booking-data-minimization.md`
8. `SDD/tasks/frontendSpecs/003f-DONE-public-booking-data-minimization.md`
9. `SDD/tasks/backendSpecs/006b-DONE-legal-basis-assessments.md`
10. `SDD/tasks/frontendSpecs/004f-DONE-processing-operation-governance-page.md`
11. `SDD/tasks/frontendSpecs/005f-DONE-legal-basis-assessment-workflow.md`
12. `SDD/tasks/backendSpecs/007b-DONE-extract-privacy-processing-submodule.md`
13. `SDD/tasks/backendSpecs/008b-DONE-extract-privacy-legal-basis-submodule.md`
14. `SDD/tasks/backendSpecs/009b-DONE-implement-privacy-policy-submodule.md`
15. `SDD/tasks/backendSpecs/010b-DONE-record-privacy-acceptance-snapshot.md`
16. `SDD/tasks/frontendSpecs/006f-DONE-load-versioned-privacy-policy.md`
17. `SDD/tasks/backendSpecs/012b-DONE-preserve-stay-history-on-booking-deletion.md`
18. `SDD/tasks/backendSpecs/011b-implement-data-subject-request-workflow.md`
19. `SDD/tasks/frontendSpecs/007f-implement-public-data-subject-request-experience.md`
20. `SDD/tasks/frontendSpecs/008f-implement-data-subject-request-admin-queue.md`
21. `SDD/tasks/frontendSpecs/009f-DONE-implement-navigation-history-core.md`
22. `SDD/tasks/frontendSpecs/010f-DONE-register-navigation-routes.md`
23. `SDD/tasks/frontendSpecs/015f-DONE-refactor-navigation-history-entries.md`
24. `SDD/tasks/frontendSpecs/016f-DONE-build-lazy-navigation-facade.md`
25. `SDD/tasks/frontendSpecs/017f-DONE-undo-navigation-facade.md`
26. `SDD/tasks/frontendSpecs/018f-DONE-migrate-guest-reservation-finance-navigation.md`
27. `SDD/tasks/frontendSpecs/019f-DONE-migrate-operational-governance-navigation.md`
28. `SDD/tasks/frontendSpecs/020f-DONE-integrate-sidebar-topbar-accessibility.md`
29. `SDD/tasks/frontendSpecs/021f-DONE-verify-administrative-navigation-history.md`
30. `SDD/tasks/backendSpecs/013b-DONE-configure-operational-logging-foundation.md`
31. `SDD/tasks/backendSpecs/014b-DONE-implement-client-log-ingestion.md`
32. `SDD/tasks/frontendSpecs/022f-DONE-create-frontend-logger-core.md`
33. `SDD/tasks/frontendSpecs/023f-DONE-capture-global-frontend-errors.md`
34. `SDD/tasks/frontendSpecs/024f-DONE-integrate-logger-administrative-startup.md`
35. `SDD/tasks/frontendSpecs/025f-DONE-integrate-api-log-transport.md`
36. `SDD/tasks/backendSpecs/015b-DONE-remove-legacy-financial-booking-constraint.md`
37. `SDD/tasks/backendSpecs/016b-DONE-migrate-guest-status-and-care-fields.md`
38. `SDD/tasks/backendSpecs/017b-DONE-refine-guest-domain-and-contract.md`
39. `SDD/tasks/backendSpecs/018b-DONE-synchronize-guest-lifecycle-status.md`
40. `SDD/tasks/backendSpecs/020b-DONE-refactor-guest-status-notification.md`
41. `SDD/tasks/backendSpecs/021b-DONE-centralize-guest-status-participant-notifiers.md`
42. `SDD/tasks/backendSpecs/019b-DONE-apply-guest-history-at-checkout.md`
43. `SDD/tasks/frontendSpecs/026f-DONE-simplify-guest-identification-and-status.md`
44. `SDD/tasks/frontendSpecs/027f-DONE-rebuild-guest-care-fields.md`
45. `SDD/tasks/frontendSpecs/028f-DONE-move-assessment-to-checkout.md`
46. `SDD/tasks/frontendSpecs/029f-DONE-align-internal-notes-design-and-verify-flow.md`
47. `SDD/tasks/backendSpecs/022b-DONE-remove-guest-referrer-name.md`
48. `SDD/tasks/frontendSpecs/030f-DONE-simplify-and-reorder-guest-origin.md`
49. `SDD/tasks/backendSpecs/023b-DONE-create-ratings-domain-and-persistence.md`
50. `SDD/tasks/backendSpecs/024b-DONE-build-ratings-use-cases-and-api.md`
51. `SDD/tasks/backendSpecs/025b-DONE-integrate-ratings-with-checkout.md`
52. `SDD/tasks/backendSpecs/026b-DONE-query-ratings-by-booking-guest.md`
53. `SDD/tasks/backendSpecs/027b-DONE-secure-audit-and-verify-ratings-module.md`
54. `SDD/tasks/frontendSpecs/031f-DONE-create-ratings-navigation-and-api.md`
55. `SDD/tasks/frontendSpecs/032f-DONE-build-checkout-rating-stars.md`
56. `SDD/tasks/frontendSpecs/033f-DONE-build-ratings-list-page.md`
57. `SDD/tasks/frontendSpecs/034f-DONE-link-rating-table-participants.md`
58. `SDD/tasks/frontendSpecs/035f-DONE-add-collapsible-guest-rating-history.md`
59. `SDD/tasks/backendSpecs/028b-DONE-centralize-financial-participant-notification.md`
60. `SDD/tasks/backendSpecs/029b-DONE-remove-financial-transaction-directional-amounts.md`
61. `SDD/tasks/frontendSpecs/036f-DONE-remove-financial-transaction-directional-amounts.md`
62. `SDD/tasks/backendSpecs/030b-DONE-notify-financial-source-deletion.md`
63. `SDD/tasks/backendSpecs/031b-DONE-align-ftp-transaction-taxonomy-and-sources.md`
64. `SDD/tasks/backendSpecs/032b-DONE-correct-cashier-schedule-semantics.md`
65. `SDD/tasks/backendSpecs/033b-DONE-create-ftp-domain-and-persistence.md`
66. `SDD/tasks/backendSpecs/034b-DONE-build-ftp-use-cases-and-reservation-creation.md`
67. `SDD/tasks/backendSpecs/035b-DONE-implement-atomic-ftp-payment-replacement.md`
68. `SDD/tasks/backendSpecs/036b-DONE-integrate-ftp-checkin-checkout-and-security.md`
69. `SDD/tasks/frontendSpecs/037f-DONE-build-reservation-ftp-allocation-interface.md`
70. `SDD/tasks/frontendSpecs/038f-DONE-integrate-reservation-ftp-command-and-state.md`
71. `SDD/tasks/frontendSpecs/039f-DONE-materialize-ftp-payment-at-checkin.md`
72. `SDD/tasks/frontendSpecs/040f-DONE-materialize-ftp-payment-at-checkout-and-verify.md`
73. `SDD/tasks/backendSpecs/037b-DONE-create-notifier-contracts-and-domain.md`
74. `SDD/tasks/backendSpecs/038b-DONE-persist-notifier-intents-and-events.md`
75. `SDD/tasks/backendSpecs/039b-DONE-implement-notifier-dispatch-and-retry.md`
76. `SDD/tasks/backendSpecs/040b-DONE-integrate-aws-ses-outbound-adapter.md`
77. `SDD/tasks/backendSpecs/041b-DONE-receive-ses-feedback-through-sns.md`
78. `SDD/tasks/backendSpecs/042b-DONE-integrate-public-booking-with-notifier.md`
79. `SDD/tasks/frontendSpecs/041f-DONE-adapt-public-booking-email-contract.md`

## Execution Notes

- Execute tasks in the listed order.
- Tasks `006b`, `004f`, `005f`, `007b`, `008b`, `009b` and `010b` are complete.
- Tasks `006f`, `009f`, `010f` and `012b` are complete. Task `011b` is the next
  proposed backend task and requires explicit approval. Tasks `007f`, `008f`
  and `011f` through `014f` belong to the superseded route-registry plan and
  must not be executed. Tasks `015f` through `021f` are complete.
- Task `013b` was explicitly selected by the user as an independent
  cross-cutting logging task and is complete; that instruction changed the
  order only for `013b` and did not authorize any other proposed task.
- Task `014b` was subsequently selected explicitly by the user and is complete.
  Its execution did not authorize the remaining logging tasks.
- Task `022f` was subsequently selected explicitly by the user and is complete.
  Its execution did not authorize global error capture, startup integration or
  remote transport tasks.
- Task `023f` was subsequently selected explicitly by the user and is complete.
  Its execution did not authorize startup integration or remote transport.
- Task `024f` was subsequently selected explicitly by the user and is complete.
  Its execution did not authorize API correlation or remote transport.
- Task `025f` was subsequently selected explicitly by the user and is complete.
  The operational logging task sequence is now complete.
- Task `015b` was selected explicitly as a corrective reservation-deletion
  task after a legacy financial foreign key was observed in production-like
  logs and is complete. Its execution does not authorize another proposed task.
- Task `016b` was selected explicitly by the user as an independent guest
  schema-compatibility task and is complete. Its execution did not authorize
  tasks `017b` through `019b` or any frontend task.
- Task `017b` was subsequently selected explicitly by the user as an
  independent guest domain and contract task and is complete. Its execution did
  not authorize tasks `018b`, `019b` or any frontend task.
- Task `018b` was subsequently authorized with a simplified direct-transition
  design and is complete. It introduced no lifecycle service or competing-
  reservation scan and did not authorize task `019b` or any frontend task.
- Task `020b` was explicitly authorized as a corrective architectural task and
  is complete. It replaced the direct transitions from `018b` with
  Notifier/Resolver flows and reservation-wide status derivation. Its execution
  did not authorize task `019b` or any frontend task.
- Task `021b` was explicitly authorized to align guest-status communication
  with the module architecture and is complete. It replaced participant-
  specific Notifiers with one `ParticipantNotifier` per source module and
  restored direct service calls for simple queries. Its execution did not
  authorize task `019b` or any frontend task.
- Task `019b` was subsequently authorized and is complete. Completed checkout
  now applies guest history and optional assessment exactly once with
  persistent evidence and transactional rollback. Its execution did not
  authorize any frontend task.
- Task `026f` was subsequently authorized and is complete. Guest registration
  no longer controls or submits lifecycle status, while form previews, lists,
  profiles, filters and badges use the authoritative four-state vocabulary.
  Its execution did not authorize tasks `027f` through `029f`.
- Task `027f` was subsequently authorized and is complete. Guest registration
  and profile presentation now use only the two free-text care properties,
  with accessible feedback and no obsolete structured care controls or payload
  members. Its execution did not authorize tasks `028f` or `029f`.
- Task `028f` was subsequently authorized and is complete. Registration no
  longer edits operational history, while checkout provides the optional
  rating and a backend-read history preview with accessible, duplicate-safe
  submission. Its execution did not authorize task `029f`.
- Task `029f` was subsequently authorized and is complete. Internal notes now
  have one visible section title, a dedicated symmetric textarea wrapper and
  an accessible name. Integrated guest-flow and cache-version tests passed;
  the authenticated manual smoke test could not run because the local backend
  was unavailable.
- Tasks `022b` and `030f` were explicitly authorized by the request to remove
  the guest referrer name from the interface and database. Guest contracts and
  storage now retain only `originChannel`, and the origin section precedes the
  guest care section.
- Ratings task `023b` was explicitly authorized and is complete. It created the
  ratings domain, persistence boundary and constrained schema.
- Ratings task `024b` was subsequently authorized and is complete. It created
  the application use case, validation, bounded paginated API and
  booking-derived guest-history query. Its execution did not authorize
  checkout integration, rated-booking deletion protection, audit/security
  completion or frontend work.
- Ratings task `025b` was subsequently authorized and is complete. Completed
  checkout now creates exactly one six-criterion rating through the checkout
  ParticipantNotifier/Resolver flow in the same transaction, and obsolete
  generic Guest/CheckOut ratings were removed.
- Ratings task `026b` was subsequently authorized and is complete. Rating
  summaries now use minimized booking-derived projections with bounded query
  counts, and booking deletion returns a conflict when a rating exists.
- Ratings task `027b` was subsequently authorized and is complete. Backend
  authorization now restricts ratings to operational roles, minimized audit
  events cover creation and access, and architecture/legacy verification
  completes the backend sequence.
- Ratings frontend task `031f` was subsequently authorized and is complete.
  The administrative shell now exposes an authorized ratings root backed by
  bounded API helpers and preserves list history when profiles are opened.
- Ratings frontend task `032f` was subsequently authorized and is complete.
  Completed checkout now collects six required accessible scores and optional
  observations through the exact nested backend contract.
- Ratings frontend task `033f` was subsequently authorized and is complete.
  The ratings root now presents the complete escaped, paginated and responsive
  table with accessible stars and participant links.
- Ratings frontend task `034f` was subsequently authorized and is complete.
  Link-event, exact-destination, retained-page and non-clickable-row behavior
  are now independently verified.
- Ratings frontend task `035f` was subsequently authorized and is complete.
  Exact guest selection now loads a stale-safe collapsible inline history, and
  booking links restore the preserved reservation form after profile return.
  The booking-service ratings frontend sequence is now complete.
- Backend task `028b` was completed after scope correction, centralizing
  financial Resolvers and isolating the Cashier participant through an adapter
  and Cashier-owned inbound use case.
- Backend task `029b` and frontend task `036f` were explicitly authorized and
  are complete. Financial transactions now retain one positive amount and the
  approved structural type vocabulary, while the obsolete directional amount
  properties and columns are removed. Cashier movement direction remains
  unchanged.
- Backend task `030b` was explicitly authorized to make transaction deletion
  notify its optional source through the central financial participant flow
  and is complete.
- FTP task `031b` was explicitly authorized and completed the authoritative
  transaction taxonomy, compatibility migration and source hierarchy.
  FTP task `032b` was explicitly authorized and completed Cashier temporal,
  waiting-projection, idempotent reversal and concurrency semantics. FTP task
  `033b` was explicitly authorized and completed the aggregate domain,
  persistence, stable ordering, bounded loading and locking foundation. FTP
  task `034b` was explicitly authorized and completed authenticated use cases,
  reservation allocation, idempotence and atomic reservation/FTP creation.
  FTP task `035b` was explicitly authorized and completed atomic, idempotent
  replacement of provisional payment components, post-commit definitive audit
  and rollback/concurrency coverage. FTP task `036b` was explicitly authorized
  and completed check-in/checkout materialization, authorization and end-to-end
  backend verification. Frontend task `037f` was explicitly authorized and
  completed the reservation allocation interface, cent-precise preview and
  contract tests. Frontend task `038f` was explicitly authorized and completed
  the idempotent reservation command, timeout reconciliation and read-only FTP
  summary. Frontend task `039f` was explicitly authorized and completed the
  check-in scheduled-payment loading, confirmation, atomic materialization and
  recovery flow. Frontend task `040f` was explicitly authorized, completed the
  checkout materialization and verification flow, and is now complete.
- Do not skip a blocked task unless the user changes the order explicitly.
- Backend task `037b` was explicitly authorized and completed the reusable,
  provider-neutral notifier contracts, domain state and architecture boundary.
  Its execution did not authorize tasks `038b` through `042b`.
- Backend task `038b` was explicitly authorized and completed notifier-owned
  intent/provider-event persistence, atomic claims, lease recovery,
  idempotency and retention anonymization. Its execution did not authorize
  tasks `039b` through `042b`.
- Backend task `039b` was explicitly authorized and completed provider-neutral
  scheduled dispatch, durable retry with exponential backoff and jitter,
  restart recovery and exhausted-intent reprocessing. Its execution did not
  authorize tasks `040b` through `042b`.
- Backend task `040b` was explicitly authorized and completed trusted delivery
  profiles, AWS SDK v2 SES sending, default credential-chain client creation
  and provider-neutral failure classification. Its execution did not authorize
  tasks `041b` or `042b` and did not activate production delivery.
- Backend task `041b` was explicitly authorized and completed authenticated SNS
  ingress, normalized SES feedback, atomic idempotent provider-event handling
  and notifier-owned delivery-state transitions. Its execution did not
  authorize task `042b` or activate the production SNS subscription.
- Backend task `042b` was explicitly authorized and completed the transactional
  public-booking integration, required normalized guest email, two independent
  notifier intents, rollback and provider-isolation verification. Production
  dispatch, SES and SNS activation remain externally disabled. Its execution
  did not authorize frontend task `041f`.
- Frontend task `041f` was explicitly authorized and completed the required,
  normalized transactional email field, minimized public payload, privacy
  wording and regression coverage. It did not activate SES, SNS or production
  notification flags.
- Do not infer that legacy documentation is an approved task.
- After each task, create its report in `SDD/ImplementationReport/` and complete the
  prerequisite review before proceeding.
