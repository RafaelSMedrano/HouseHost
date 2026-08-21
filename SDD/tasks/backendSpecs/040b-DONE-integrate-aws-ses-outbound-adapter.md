# Task 040b DONE — Integrate AWS SES Outbound Adapter

## Status

Complete after explicit implementation approval, verification and report on
2026-08-21.

## Implementation Area

Backend (`b`).

## Objective

Implement the AWS SES output adapter, trusted delivery profiles and provider
acceptance mapping behind the notifier's neutral email-delivery port.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/notifierModuleSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/backendSpecs/supplierManagementSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/notifierModuleBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/tasks/backendSpecs/039b-DONE-implement-notifier-dispatch-and-retry.md`

## Scope

- Add AWS SDK v2 SES dependencies through dependency management.
- Configure SES clients with externally selected Region and the default
  credential chain.
- Implement trusted delivery profiles for sender, reply-to, Configuration Set,
  allowed source systems and enabled state.
- Implement text and HTML sending for one recipient per intent.
- Apply the configured SES Configuration Set to every message.
- Persist provider acceptance time and returned SES message identifier.
- Classify SES and network failures into bounded retryable or permanent
  categories.
- Add adapter tests with a mocked SES client and architecture tests isolating
  AWS imports to integration/configuration code.

## Out Of Scope

- SNS feedback endpoint.
- AWS resource, DNS or IAM provisioning commands.
- Public booking integration.
- Real production guest data.

## Acceptance Criteria

- Requests cannot override sender, Region, credentials or Configuration Set.
- Delivery profiles reject unauthorized source-system use.
- Accepted sends persist the exact provider message identifier required for
  later feedback correlation.
- Missing enabled-profile configuration fails closed for delivery.
- SES failure does not remove or corrupt the intent.
- Focused adapter, configuration, architecture and privacy tests pass with
  `git diff --check`.

## Required Report

Create after implementation:

`SDD/ImplementationReport/YYYY-MM-DD-040b-integrate-aws-ses-outbound-adapter.md`
