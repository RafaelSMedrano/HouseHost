# Implementation Report — Task 040b

## Task And Implementation File

- Task: `040b — Integrate AWS SES Outbound Adapter`.
- Completed task file:
  `SDD/tasks/backendSpecs/040b-DONE-integrate-aws-ses-outbound-adapter.md`.
- Implementation controls: `SDD/implementation/task-bootstrap.md` and
  `SDD/implementation/implementation-order.md`.
- Execution date: 2026-08-21.

## Specs, Prerequisites And Plans Read

- `SDD/specs/sddSpec.md`.
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`.
- `SDD/specs/backendSpecs/notifierModuleSpec.md`.
- `SDD/specs/publicBookingNotificationSpec.md`.
- `SDD/specs/publicBookingDataMinimizationSpec.md`.
- `SDD/specs/publicBookingOwnerEmailNotificationSpec.md`.
- `SDD/specs/moduleArchitectureSpec.md`.
- `SDD/specs/operationalLoggingSpec.md`.
- `SDD/specs/lgpdGovernanceSpec.md`.
- `SDD/specs/backendSpecs/supplierManagementSpec.md`.
- `SDD/plans/backendSpecs/notifierModuleBackendPlan.md`.
- `SDD/tasks/backendSpecs/039b-DONE-implement-notifier-dispatch-and-retry.md`.
- `SDD/ImplementationReport/2026-08-21-039b-implement-notifier-dispatch-and-retry.md`.

## Files Created

- `src/main/java/com/househost/notifier/adapter/out/integration/AwsSesClientProvider.java`.
- `src/main/java/com/househost/notifier/adapter/out/integration/DefaultAwsSesClientProvider.java`.
- `src/main/java/com/househost/notifier/adapter/out/integration/NotifierDeliveryProfileProperties.java`.
- `src/main/java/com/househost/notifier/adapter/out/integration/AwsSesConfiguration.java`.
- `src/main/java/com/househost/notifier/adapter/out/integration/AwsSesEmailDeliveryAdapter.java`.
- `src/test/java/com/househost/notifier/adapter/out/integration/AwsSesEmailDeliveryAdapterTest.java`.
- `src/test/java/com/househost/notifier/adapter/out/integration/NotifierDeliveryProfilePropertiesTest.java`.
- `src/test/java/com/househost/notifier/adapter/out/integration/AwsSesConfigurationTest.java`.
- `src/test/java/com/househost/notifier/adapter/out/integration/DefaultAwsSesClientProviderTest.java`.
- `SDD/ImplementationReport/2026-08-21-040b-integrate-aws-ses-outbound-adapter.md`.

## Files Changed

- `pom.xml` — imported the AWS SDK v2 BOM at version `2.54.1`, added `sesv2`
  and selected the synchronous URLConnection HTTP client explicitly.
- `src/main/java/com/househost/notifier/application/port/out/EmailDeliveryPort.java` —
  added the neutral source-system value required for delivery-profile
  authorization.
- `src/main/java/com/househost/notifier/application/service/NotificationDispatchService.java` —
  forwarded the claimed source system to the delivery port.
- `src/main/resources/application.properties` — added disabled-by-default SES
  activation, call timeouts and the external HouseHost transactional profile.
- `src/test/java/com/househost/notifier/adapter/out/persistence/NotificationDispatchPersistenceIntegrationTest.java` —
  aligned the fake delivery port with source-system authorization.
- `src/test/java/com/househost/notifier/adapter/in/scheduling/NotificationDispatchSchedulerIntegrationTest.java` —
  aligned the scheduled fake provider with the extended neutral port.
- `src/test/java/com/househost/notifier/architecture/NotifierCoreArchitectureTest.java` —
  enforced AWS SDK isolation, default credentials and privacy-safe adapter
  behavior.
- `SDD/tasks/backendSpecs/040b-DONE-integrate-aws-ses-outbound-adapter.md` —
  marked the fully verified task complete.
- `SDD/tasks/backendSpecs/041b-DONE-receive-ses-feedback-through-sns.md` — updated
  its completed prerequisite filename.
- `SDD/tasks/backendSpecs/042b-DONE-integrate-public-booking-with-notifier.md` —
  updated its completed prerequisite filename.
- `SDD/ImplementationReport/2026-08-21-039b-implement-notifier-dispatch-and-retry.md` —
  updated the historical reference to the renamed task file.
- `SDD/implementation/task-bootstrap.md` — recorded completion of `040b` and
  retained `041b` and `042b` as separately authorized work.
- `SDD/implementation/implementation-order.md` — updated the ordered task path
  and completion note.

## Flows Implemented

- Resolved a trusted delivery profile from the notifier-owned profile key and
  rejected disabled, unknown or source-unauthorized profiles before any AWS
  call.
- Built one-recipient SES v2 messages containing UTF-8 text and HTML content.
- Applied only externally configured Region, sender, optional reply-to and SES
  Configuration Set; notification requests cannot override these controls.
- Created and cached synchronous SES clients by Region using the AWS default
  credential chain and bounded API-call timeouts.
- Returned the exact SES message identifier through the existing neutral
  acceptance result for persistence by the dispatcher.
- Classified throttling, provider unavailability and network failures as
  retryable, and authentication, configuration, rejected-content and invalid-
  request failures as permanent.
- Failed application startup when SES is enabled without a complete enabled
  profile, while keeping all SES and dispatch activation disabled by default.

## Technical And MVP Decisions

- AWS SDK for Java v2 version `2.54.1`, released on 2026-08-20, was selected
  through its BOM so every AWS module remains version-aligned.
- `url-connection-client` is explicit. The SDK's default Apache 5 runtime
  client conflicted with the older HttpClient version managed by Spring Boot
  3.2.5; excluding Apache and Netty also keeps the synchronous SES footprint
  bounded.
- Source-system authorization was added to the provider-neutral delivery port.
  It carries no AWS concept and prevents one consumer from using another
  application's profile.
- No access key or secret property exists. Runtime authentication is delegated
  to `DefaultCredentialsProvider`, allowing EC2 workload identity.
- Dispatch and SES are separate flags and both remain false by default. This
  prevents production sends merely because the code or dependency is deployed.
- The task does not verify domain ownership, sandbox exit, Configuration Set
  resources, IAM policy or production recipients; those remain controlled AWS
  operational prerequisites.

## Difficulties, Problems And Resolutions

- The sandbox initially prevented Maven from writing downloaded AWS artifacts
  into the local repository. Explicit permission was obtained for the normal
  Maven dependency download.
- Creating a real SDK client exposed a binary incompatibility between the AWS
  SDK Apache 5 client and Spring Boot's managed Apache HttpClient version. The
  adapter now explicitly uses the SDK URLConnection client, and a test creates,
  caches and closes the resulting client without network activity.
- Mockito service-exception fixtures initially nested mock creation inside an
  unfinished stubbing expression. Exceptions are now prepared before response
  stubbing.
- The worktree contained extensive unrelated existing changes. They were
  preserved; implementation remained confined to the notifier SES adapter and
  the files listed here.

## Tests And Verification

- SES adapter tests — passed for exact request mapping, text/HTML, Configuration
  Set, reply-to, message identifier, unauthorized source and failure classes.
- Configuration tests — passed for enabled, disabled and fail-closed startup.
- Real client-construction test — passed without contacting AWS.
- Existing dispatcher persistence and scheduler tests — passed with the
  extended neutral delivery port.
- Architecture and privacy tests — passed with AWS imports restricted to the
  outbound integration package and no static credentials or message logging.
- `./mvnw -q -DskipTests test-compile` — passed.
- `./mvnw -q test` — passed: 455 tests, 0 failures, 0 errors, 0 skipped.
- Dependency tree — resolved only `sesv2` and `url-connection-client` as direct
  AWS modules at version `2.54.1`; Apache and Netty clients were excluded.
- `git diff --check` — passed.

## Acceptance And Prerequisite Review

- Requests cannot override sender, Region, credentials, reply-to or
  Configuration Set.
- Profiles reject unauthorized source systems and disabled or unknown keys
  without calling SES.
- Accepted sends return the exact provider message identifier used by existing
  dispatcher persistence and future feedback correlation.
- Enabled SES with absent or incomplete enabled-profile configuration fails
  startup; disabled SES leaves consumer operations and pending intents intact.
- SES and network failures become bounded neutral outcomes and do not delete or
  mutate consumer state.
- AWS SDK types exist only in notifier outbound integration code.
- No SNS endpoint, booking integration, AWS provisioning or real email send was
  implemented or performed.
- No authoritative prerequisite conflict remained. Task `041b` is the next
  proposed backend task and still requires explicit user authorization.
