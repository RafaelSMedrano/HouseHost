# Data-Subject Request Experience Spec

## Specification

The Data-Subject Request Experience provides a clear public formalization page
and an accountable administrative work queue without making the browser the
authority for identity, deadlines, status or execution.

## Capabilities

### Explain The Process In The Privacy Policy

The public policy renders the server-governed channel, free-request statement,
proportional verification, official-link continuation, possible lawful
retention and available escalation channels. The wording distinguishes initial
WhatsApp contact from the official tracked workflow.

### Formalize A Request Safely

The public page validates the invitation with the backend, explains expiry,
collects only necessary request information and returns the official protocol.
It provides explicit loading, expired, invalid, submitted and retry states.

Tokens remain in memory for the active flow, are removed from visible browser
history where technically possible and never enter analytics, console output or
persistent browser storage. The UI never infers identity or record existence
from error differences.

### Provide Accessible Follow-Up

The data subject can use a safe link to see a minimized status and retrieve a
final response after required authentication. Status labels use plain language,
and keyboard, focus, contrast and live feedback remain accessible.

### Operate An Administrative Queue

The authenticated administration provides filters, overdue indicators, detail,
history, assignment, verification recording, analysis, action confirmation and
response controls. Irreversible actions require a clear confirmation showing
the exact scope. The UI displays backend decisions and does not mark an action
complete optimistically.

Sensitive narratives and documents are not placed in list screens, URLs,
browser storage or console logs. Errors preserve safe unsent input when useful
but never display another subject's data.

## Prerequisite Specs

- `SDD/specs/dataSubjectRequestWorkflowSpec.md`
- `SDD/specs/backendSpecs/dataSubjectRequestSubmoduleSpec.md`
- `SDD/specs/frontendSpecs/publicPrivacyPolicyExperienceSpec.md`

## Spec Degree

6.
