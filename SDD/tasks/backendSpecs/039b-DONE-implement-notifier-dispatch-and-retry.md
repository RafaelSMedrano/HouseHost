# Task 039b DONE — Implement Notifier Dispatch And Retry

## Status

Complete after explicit implementation approval, verification and report on
2026-08-21.

## Implementation Area

Backend (`b`).

## Objective

Implement provider-neutral automatic dispatch, durable claims, bounded retry
and restart recovery using Spring's native scheduler and a fakeable delivery
port.

## Required Specs

- `SDD/specs/sddSpec.md`
- `SDD/specs/backendSpecs/notifierModuleSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`
- `SDD/specs/operationalLoggingSpec.md`

## Required Plans

- `SDD/plans/backendSpecs/notifierModuleBackendPlan.md`

## Required Implementation Files

- `SDD/implementation/task-bootstrap.md`
- `SDD/implementation/implementation-order.md`
- `SDD/tasks/backendSpecs/038b-DONE-persist-notifier-intents-and-events.md`

## Scope

- Implement intent creation orchestration through `NotificationRequestUseCase`.
- Implement bounded batch claiming and dispatch through `EmailDeliveryPort`.
- Enable Spring scheduling and create the notifier-owned scheduled adapter.
- Configure initial delay, fixed delay, batch size, lease duration, maximum
  attempts, exponential backoff and jitter.
- Keep provider calls outside long database transactions.
- Recover expired processing leases after simulated application restart.
- Record provider-neutral accepted, retryable and exhausted outcomes.
- Add privacy-safe operational events without recipient or message content.
- Test the dispatcher with a fake delivery adapter, not AWS SES.

## Out Of Scope

- AWS SDK dependencies.
- SES Configuration Set or IAM configuration.
- SNS endpoint and provider feedback.
- Consumer integration.

## Acceptance Criteria

- The scheduler automatically processes due persisted intents.
- Fixed delay begins after a cycle completes and does not overlap one worker's
  own cycle.
- Retry survives restart because all timing and attempt state is persisted.
- Provider calls do not retain claim transactions during network execution.
- Attempt exhaustion becomes visible and reprocessable.
- Disabled dispatch preserves pending intents and consumer operations.
- Scheduler, retry, concurrency, restart and privacy tests pass with
  `git diff --check`.

## Required Report

Create after implementation:

`SDD/ImplementationReport/YYYY-MM-DD-039b-implement-notifier-dispatch-and-retry.md`
