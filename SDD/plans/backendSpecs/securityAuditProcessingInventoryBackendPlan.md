# Security And Audit Processing Inventory Backend Plan

## Governing Specs

- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- prerequisite: `SDD/specs/cantinhoDasLavandasMainSpec.md`

## Objective

Make the initial processing inventory represent security and audit
accountability explicitly, keep business audit events attached to their business
purposes and keep legacy WhatsApp marketing inactive without deleting historical
evidence.

## Catalog Changes

Add `SECURITY_AUDIT_MANAGEMENT` as an active initial operation named
`Seguranca, auditoria e resposta a incidentes`. It records the purpose, lawful
basis context, titular and data categories, sources, actions, access roles,
recipients, retention criterion, deletion method and safeguards for security
events and audit data.

Keep `WHATSAPP_MARKETING` in the catalog only as an inactive legacy operation.
New databases persist it as inactive. Startup also changes an existing active
marketing operation to inactive, without deleting it or changing historical
audit-event relationships.

## Audit Classification

Business events remain attached to their existing processing operation. Login
failure, login blocking, login rate limiting and login-protection-unavailable
events use `SECURITY_AUDIT_MANAGEMENT`, because their primary purpose is abuse
detection and system protection. Successful login and ordinary user-management
events remain under `USER_ACCESS_MANAGEMENT`.

The change preserves module boundaries: authentication continues using its own
audit port, and its integration adapter selects the processing-operation code.

## Verification

Add catalog tests for the new operation, inactive marketing creation and
deactivation of an existing active marketing operation. Add an adapter test that
proves security login outcomes use the new code while ordinary user events keep
the user-access code. Run the focused tests, full Maven suite and
`git diff --check`.

