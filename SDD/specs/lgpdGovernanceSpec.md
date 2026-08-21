# LGPD Governance Spec

## Specification

LGPD Governance is the project-wide product capability that keeps personal-data
processing in Cantinho das Lavandas lawful, purposeful, necessary, transparent,
secure and demonstrable in proportion to the real risk of the operation.

The project currently has a deliberately simple public reservation journey. It
collects ordinary identification, contact and stay-planning data so that a
visitor can request availability and receive operational contact. The public
journey does not collect a document, payment credentials or data for marketing.
This limited scope reduces risk and permits simple operational procedures, but
it does not remove the principles, lawful-basis, security, transparency,
data-subject-rights, retention and incident-response duties established by the
Lei Geral de Proteção de Dados Pessoais.

The administrative system processes a broader set of guest, reservation, stay,
financial, user-access and audit information. Its protection cannot be inferred
only from the simplicity of the public form. Every internal field and use must
remain connected to a documented operational, contractual, legal or security
need.

This spec defines required product behavior and governance evidence. It does
not certify legal compliance, replace fact-specific legal review or state that
software alone makes the controller compliant.

## Scope

This spec governs every project surface and operational process that handles
information relating to an identified or identifiable natural person,
including:

- the public website and public reservation API;
- guest and reservation management;
- check-in, stay and checkout operations;
- financial records linked to guests or reservations;
- administrative users, credentials, roles and authentication records;
- audit events, IP addresses, user agents and security alerts;
- logs, backups, exports, test data and operational support copies;
- infrastructure, communication and database providers acting on the
  controller's behalf;
- manual procedures used to answer data-subject requests or security
  incidents.

This spec treats the current operation as small and limited in scale for product
design purposes because the public collection is narrow, marketing is absent,
card credentials are absent and the system is intended to serve one lodging
operation. This is a proportionality assumption, not a legal declaration that
the controller qualifies for every small-processing-agent benefit.

The controller must verify its actual organizational and economic
classification. Even when the controller qualifies as an agent of small
processing size, simplification does not remove lawful bases, LGPD principles,
essential security measures or data-subject rights.

### Current Public-Collection Boundary

The public reservation journey may request:

- first and last name;
- WhatsApp or telephone contact;
- desired check-in and check-out dates;
- numbers or descriptions of adults, children and pets required for capacity;
- city of origin when optional and operationally useful;
- optional stay requests or observations accompanied by a minimization warning;
- transactional email required only to acknowledge and operate the requested
  reservation communication;
- acknowledgment that the visitor read the privacy notice and the applicable
  reservation terms.

The public reservation journey does not request or accept as part of its normal
contract:

- CPF or another identity document;
- card number, CVV, bank credentials or payment data;
- a payment method for the public pre-reservation step;
- health or other sensitive personal data;
- identified data about children or companions beyond the operational count;
- marketing consent or marketing preferences;
- email for marketing, profiling or any purpose unrelated to the requested
  reservation communication.

Free text does not authorize unrestricted collection. The product warns the
visitor not to send sensitive, documentary or financial information and applies
reasonable server-side rejection and minimization controls.

### Broader Administrative Boundary

Authorized administrative workflows may process additional information such as
guest document, address, birth date, stay history, financial status,
accessibility needs, preferences and operational notes only when the field is
necessary for a defined process and its lawful basis, access, retention and
deletion are documented.

Accessibility and free-text data can reveal health or other sensitive facts.
Their presence requires stricter necessity analysis, restricted access and a
specific lawful-basis assessment. Convenience, curiosity or future possible use
is not sufficient.

The financial area may process values, installment, payment method and payment
status needed to administer lodging. The system does not store complete card
credentials or CVV. Introducing direct payment processing requires a new risk
and architecture review before collection begins.

### Public Privacy Policy In Force

The current public privacy policy is version 2, dated 26 July 2026, presented by
the public website at the stable route `#politica-de-privacidade`.

It describes:

- Daniela Santos Medrano, a Brazilian individual microentrepreneur registered
  under CNPJ `67.277.911/0001-31`, as the controller operating under the trade
  name Refúgio Cantinho das Lavandas;
- the controller's public address at Rua Mercúrio, 162, Monte Verde,
  Camanducaia, Minas Gerais, CEP 37653-000;
- Daniela Santos Medrano as the encarregada and WhatsApp
  `+55 12 99252-5319` as her public contact channel;
- Daniela Santos Medrano and Rafael Moreno dos Santos Medrano as the people
  internally responsible for handling data-subject requests;
- the public reservation data and purposes;
- the absence of document, payment and marketing collection in that journey;
- procedures preliminary to lodging and contract execution as the principal
  treatment context;
- necessary infrastructure, database, backup and WhatsApp providers;
- necessity-based retention and legally permitted conservation;
- data-subject rights;
- WhatsApp `+55 12 99252-5319` as the channel for access, correction or deletion
  requests;
- general security measures and policy-version information.

The public policy governs the public reservation collection. It does not by
itself replace internal notices and governance for employees, administrative
users, suppliers or personal data obtained outside the public journey.

The controller's legal identity and contact information in the published policy
must be accurate and complete. Placeholders or fictitious legal identifiers are
not permitted.

### Normative Baseline

This spec is informed by:

- Lei nº 13.709/2018, the LGPD;
- ANPD guidance on data-subject rights;
- Resolução CD/ANPD nº 2/2022 for small processing agents;
- Resolução CD/ANPD nº 15/2024 and current ANPD guidance for personal-data
  security incidents;
- subsequent applicable regulation in force when a treatment or incident is
  evaluated.

If a later binding rule conflicts with this spec, the product must stop the
conflicting change, update this spec through SDD and only then update plans,
tasks and implementation.

## Capabilities

### Identify Controller, Operators And Responsibilities

The project identifies the natural or legal person that determines treatment
purposes and means as controller. The public-facing identity, address and
contact match the legally responsible operation.

Infrastructure, database, backup, hosting, communication, accounting, payment
or support providers that process personal data on behalf of the controller are
identified as operators or recipients according to their actual role. Their
instructions, access, security, retention, subcontracting and incident duties
are documented proportionally to the service.

The project does not label every technology provider an operator without
examining the real relationship, and it does not mark international transfer as
absent without examining where each provider processes or stores the data.

For a qualifying small processing agent, a formal dedicated data-protection
officer may be unnecessary under the applicable regulation, but a clear and
effective communication channel for data subjects remains required.

### Maintain A Simplified Processing Inventory

Every recurring processing activity has a stable entry in the project's
processing-operation inventory. At minimum, the inventory describes:

- purpose and operational description;
- data-subject categories;
- personal-data categories;
- collection source;
- treatment actions;
- lawful basis and any required balancing or legal analysis;
- internal access roles;
- operators, recipients and transfer assessment;
- retention criteria and deletion destination;
- security measures;
- responsible operational area;
- active or inactive status and last review evidence.

The inventory can be simplified in proportion to the project size, but it must
describe real behavior rather than desired future behavior.

The current minimum activities are reservation management, guest management,
stay operations, financial lodging management, internal-user access management
and audit/security accountability.

Marketing is not a current processing activity. A legacy marketing operation,
field or event does not authorize marketing and must remain inactive, rejected
or removed from active product behavior. Marketing can only return after a
product-spec change, lawful-basis analysis, transparency update and new
implementation approval.

### Connect Every Treatment To Purpose And Lawful Basis

Personal data is collected and used only for explicit, legitimate and informed
purposes.

The principal context for a visitor-requested pre-reservation is taking steps at
the data subject's request before a contract and, when confirmed, performing the
lodging relationship. Reading the privacy policy is transparency; it is not
treated as blanket consent for the operational reservation processing.

Legal obligation is used only when the controller can identify an actual
applicable obligation. Exercise of rights, security, fraud prevention and
internal access management receive their own documented analysis rather than
being silently folded into the reservation purpose.

Consent is used only when it is the appropriate lawful basis, is specific,
informed, freely given, demonstrable and as easy to revoke as to provide. The
current product has no marketing purpose and therefore requests no marketing
consent.

Sensitive personal data is not processed under an ordinary-data basis. If an
accessibility request or free-text note reveals sensitive data, the controller
limits the information to what is necessary, restricts access and documents the
applicable sensitive-data treatment basis before systematic use.

### Apply Necessity And Data Minimization

Every field, response, event, log and export must be justified by current need.
Data is not collected merely because it may be useful later.

The public API exposes purpose-built request and response contracts rather than
administrative entities. It rejects public attempts to submit documents,
payment data or a marketing flag outside the current product boundary.

Administrative list views show masked contact values by default. Full contact
or edit data is revealed only to an authorized operational role for a defined
need and the revelation is auditable.

Logs and audit metadata do not copy passwords, JWTs, authorization headers,
complete request payloads, complete observations, card data, documents or
contact data merely for debugging convenience.

Production data is not copied into development, demonstration, training or test
environments unless a documented necessity and equivalent controls exist.
Synthetic or anonymized data is preferred.

### Preserve Data Quality Without Unsafe Merging

The controller provides a way to correct inaccurate or outdated personal data.
The product avoids unnecessary duplicate guest records, but does not merge two
people based only on a weak similarity such as common name, partial phone or
unverified contact.

Corrections preserve operational and audit integrity. Historical evidence is
not silently rewritten when the correct action is to preserve the original
event and record a later correction.

### Provide Layered Access Protection

Public access is limited to the small public contract. Administrative data
requires authentication and authorization by role.

Backend authorization is authoritative. Hiding a button, route or value in the
frontend does not grant or deny access by itself.

The project applies least privilege so each role accesses only the data and
operations required for its work. Financial, audit, privacy-governance and user
administration functions receive narrower access than ordinary operational
reading.

Every person uses an individual administrative identity. Shared credentials are
not normal product operation. Access is removed or restricted when the person's
operational relationship ends or changes.

Passwords are stored with an appropriate one-way password hash. Tokens and
secrets are protected, externally configured where appropriate, limited in
lifetime and never written to ordinary logs or audit metadata.

### Apply Security Proportionate To Current Risk

The small volume and ordinary nature of the public data permit a simple
security program, but essential technical and administrative measures remain
mandatory.

At minimum, production provides:

- HTTPS for public and administrative traffic;
- restricted administrative authentication and role authorization;
- backend input validation and size limits;
- protected secrets and database credentials;
- database network isolation and least-privilege accounts;
- protected, restorable backups;
- dependency and infrastructure maintenance;
- restricted access to logs, metrics, consoles and management endpoints;
- trusted-proxy configuration before accepting forwarded client addresses;
- reasonable protection against repeated login attempts and public API abuse;
- monitoring for critical failures and suspicious high-impact behavior.

Security claims remain accurate and avoid absolute promises. The project does
not claim that encryption, CORS, JWT, audit or any single control makes all data
safe.

### Make The Public Policy Accessible And Demonstrable

The privacy policy is available without authentication, before public data is
submitted and through a stable link in both the reservation journey and public
footer.

The reservation journey opens the policy without forcing the visitor to lose
information already entered.

The policy uses clear language and states, at minimum:

- controller identity and contact;
- data collected and excluded;
- purposes and lawful-basis context;
- operators or recipient categories;
- international-transfer information when applicable;
- retention criteria;
- data-subject rights and request channel;
- material security information without exposing security secrets;
- version and effective date;
- how material updates are handled.

The authoritative policy version and immutable content accepted or acknowledged
for a reservation are demonstrable. The client cannot invent a policy version
that the system treats as valid.

Changing the policy does not retroactively rewrite the version presented in an
older reservation. A material purpose expansion requires analysis before the
new treatment begins, not only a later text update.

### Keep Marketing Outside The Current Product

The public site does not display a marketing option, send a marketing flag or
use reservation contact data for offers and campaigns.

The public API rejects or ignores an attempt to activate any legacy marketing
field. Legacy database columns or processing-operation constants are not proof
of consent and do not authorize sending a message.

Operational WhatsApp contact is limited to the requested reservation,
hospitality and rights-support context. It is not repurposed into promotional
contact.

### Support Data-Subject Rights Through WhatsApp

WhatsApp `+55 12 99252-5319` is the current public channel for requests about
confirmation, access, correction, sharing information, restriction,
anonymization or deletion when applicable.

An automated portal is not required for the current scale. A manual process is
acceptable when it is effective, free to the data subject, known by the team and
capable of producing evidence.

For each request, the controller:

1. records receipt date, request type and responsible person;
2. verifies identity proportionally without requesting excessive documents;
3. locates relevant data across guest, booking, stay, financial, audit,
   integration and backup contexts as applicable;
4. evaluates the requested action and any legally permitted retention;
5. executes the applicable correction, access, restriction, deletion,
   anonymization or explanation;
6. propagates the action to operators or derived copies when required and
   feasible;
7. responds in clear language through a safe channel;
8. records the decision, execution and response without placing unnecessary
   sensitive content in audit metadata.

The identity check must prevent one person from obtaining another guest's data.
A deletion request does not automatically erase records that must be retained
for an applicable obligation, exercise of rights, fraud prevention or audit
integrity; the reason for refusing or limiting the request is documented and
explained.

### Define And Execute Retention

No category of personal data is retained indefinitely merely because storage is
available.

The controller maintains a proportional retention matrix for public requests,
confirmed reservations, guest records, stays, financial records, internal
users, login attempts, audit events, logs, exports, test copies and backups.

Each rule identifies:

- purpose and data category;
- event that begins the retention period;
- concrete period or objective termination criterion;
- legal or operational exception;
- deletion, anonymization, blocking or archival method;
- responsible person or process;
- evidence that the action occurred.

Unconfirmed public requests receive a short defined lifecycle and do not block
availability or retain personal data indefinitely. Confirmed reservations and
financial records may follow longer contractual, accounting, tax or rights
protection requirements that must be specifically identified.

Manual periodic deletion is acceptable for the current scale if it actually
occurs, is access-controlled and is recorded. Automation becomes necessary when
manual execution can no longer reliably meet the matrix.

Backups follow a documented lifecycle. A deletion from the active database need
not promise immediate physical removal from every protected immutable backup,
but restoration procedures prevent deleted data from silently returning to
active use and the backup expires under its own rule.

### Control Sharing, Exports And External Communication

Personal data is shared only with internal roles and external parties required
for documented purposes.

Exports are exceptional, authorized, minimized, auditable, securely delivered
and removed when their purpose ends. Screenshots, personal spreadsheets and
uncontrolled internal conversations are not normal repositories for guest
data.

WhatsApp is both a communication channel and an external processing context.
The controller evaluates account access, device security, conversation access,
retention, provider terms and any applicable international transfer.

Personal data is not sold. It is not shared for third-party marketing.

### Maintain Auditability Without Creating A Second Exposure

Audit records material access and changes needed for accountability, including
authentication, viewing or revealing protected data, creation, update,
deletion, financial changes, rights handling and relevant security decisions.

An audit event answers what happened, when, to which entity and by which actor
or public context. It is not a complete copy of the affected record.

The narrowly defined FTP replacement governed by
`financialTransactionPlanSpec` treats an unsettled scheduled check-in or
checkout transaction as a provisional representation whose definitive payment
structure does not yet exist. Creation and destructive removal of that
provisional transaction, including cleanup of its participant effects, do not
produce audit events. Only creation of the definitive new transaction is
audited, without storing the removed transaction identifier or a replacement
relationship.

This exception applies only before settlement and only to that FTP replacement
cycle. It does not authorize removal or suppression of an audit event that
already exists and does not apply to deletion of settled, historical or
independently material financial transactions.

Audit access is restricted. Audit data has its own retention and protection
because actor identity, IP address, user agent and event history are also
personal or security-relevant data.

Audit failure is observable. A design that preserves business availability by
absorbing an audit failure must still generate an external operational signal
and document the accepted consistency tradeoff.

Audit is evidence, not automatic legal compliance and not incident response.

### Detect And Respond To Personal-Data Incidents

A personal-data security incident is a confirmed adverse event that compromises
confidentiality, integrity, availability or authenticity of personal data.

A vulnerability or suspicious action is not automatically an incident. The
project observes evidence, creates alerts and permits human confirmation.

The proportional response process includes:

1. detection or receipt of a report;
2. confirmation and initial classification;
3. containment of access, credential, endpoint, device or integration;
4. preservation of appropriate evidence;
5. identification of affected systems, data and data subjects;
6. assessment of likely material, moral, identity, discrimination or other
   impacts;
7. mitigation or reversal where possible;
8. documented decision about communication to the ANPD and affected data
   subjects under the regulation in force;
9. correction of the cause and follow-up review;
10. retention of the incident record for the legally required period.

Not every alert or incident requires external communication. Confirmed
incidents involving personal data that can cause relevant risk or harm receive
the communication required by the regulation then in force. The controller
does not omit a required data-subject communication merely because it contacted
the ANPD.

For the current scale, detection may use simple rules and human review. Examples
include repeated login failures, unusual contact revelation, destructive
actions, public-request abuse, audit failures and backup failures. Thresholds
are calibrated and an alert never asserts certainty that a credential was
stolen.

### Keep Privacy Governance Proportional And Demonstrable

The current operation may use concise policies, a simplified processing
inventory, WhatsApp rights handling, manual retention review and a small
incident team. Simplicity is acceptable when the controls are effective and
evidenced.

The project does not require ceremonial features with no operational use. It
does not create a cookie banner when no nonessential cookie or tracking purpose
exists merely to appear compliant. If analytics, advertising or tracking is
introduced, the project assesses it and updates transparency before activation.

Evidence of governance includes, as applicable:

- processing inventory and review history;
- current and historical public policy versions;
- access-control matrix;
- rights-request register and response evidence;
- retention matrix and deletion evidence;
- audit events and monitoring evidence;
- processor inventory and contractual review;
- backup and restoration verification;
- incident register, decisions and simulations;
- security tests and dependency-maintenance records;
- decisions explaining why a control is proportional to the current risk.

The absence of a dedicated privacy portal, enterprise SIEM or full-time data
protection officer is not by itself noncompliance for this scale. The absence of
an effective channel, lawful basis, essential security, rights handling,
retention execution or incident procedure is not justified by simplicity.

### Review Changes Before Expanding Treatment

The LGPD impact is reviewed before, not after, introducing:

- marketing or promotional messaging;
- CPF or identity-document collection in the public journey;
- direct card or bank credential processing;
- systematic sensitive or health data;
- identified data about children, adolescents or elderly people beyond current
  operational necessity;
- automated profiling or decisions that significantly affect a person;
- cameras, biometrics or public-area surveillance;
- analytics, advertising cookies or cross-site tracking;
- new exports, integrations, operators or international transfers;
- materially larger volume, frequency, geography or retention;
- reuse of existing data for a new incompatible purpose.

The change updates product spec, transparency, processing inventory, lawful
basis, security, retention, rights handling and incident planning before the
new processing is enabled.

### Production Readiness Requirements

The product must not be considered ready for production use with real personal
data until every applicable requirement below is implemented and its evidence
is documented. A requirement identified as not applicable must have a recorded
justification.

1. Identify the controller, every operator and sub-operator, their
   responsibilities and the instructions governing each processing activity.
2. Maintain an inventory of personal-data processing activities, including the
   data collected, titular category, purpose, lawful basis, source, recipients,
   storage location, retention period and disposal method.
3. Collect only data that is adequate, relevant and necessary for the stated
   purpose, and prevent reuse for incompatible purposes.
4. Define and document a valid lawful basis for every processing purpose; when
   consent is the chosen basis, make it free, informed, unambiguous,
   demonstrable and as easy to withdraw as to grant.
5. Publish a clear and accessible privacy notice describing the controller,
   processing purposes, data sharing, retention, titular rights and the
   available contact channel.
6. Implement a documented channel and workflow to authenticate, receive, track
   and answer titular requests within the applicable legal time limits,
   including access, confirmation, correction, information, portability when
   regulated and applicable, anonymization, blocking, deletion, consent
   withdrawal and review of solely automated decisions.
7. Indicate an encarregado and publish the required contact information, or
   document the legal basis for any applicable small-processing-agent exemption
   while maintaining a communication channel for titular rights.
8. Define retention schedules and implement secure deletion or anonymization in
   databases, files, logs, exports and backups after the purpose or applicable
   legal retention period ends.
9. Apply privacy and security by design and by default to every new or changed
   feature involving personal data.
10. Enforce least-privilege access, strong authentication, authorization by
    role, secrets protection, encryption in transit and at rest where
    appropriate, secure backups, vulnerability correction and auditable access
    to personal data.
11. Prevent production personal data from being used in development or test
    environments unless use is necessary, authorized and protected; prefer
    synthetic or effectively anonymized data.
12. Keep auditable evidence of notices, consent when applicable, processing
    decisions, access to sensitive operations, titular requests, deletions,
    sharing and compliance reviews without logging unnecessary personal data.
13. Assess suppliers and integrations before sharing data, establish data
    protection and security obligations contractually and control their access,
    retention, deletion, incident notification and use of sub-operators.
14. Identify international data transfers and implement an authorized LGPD
    transfer mechanism before they occur.
15. Classify and apply additional safeguards to sensitive personal data and to
    data involving children or adolescents; when such processing is
    unnecessary, do not collect it.
16. Assess privacy risk and prepare a personal-data protection impact report
    when required by the LGPD or ANPD, requested by the ANPD or justified by the
    nature and risk of the processing.
17. Maintain and test a personal-data incident response plan covering
    detection, containment, investigation, evidence preservation, risk
    assessment, recovery and responsibilities.
18. Enable the controller to notify the ANPD and affected titulars within the
    regulatory deadline when an incident can cause relevant risk or damage, and
    retain incident records for the period required by regulation.
19. Train every person with access to personal data and periodically review
    permissions, processing records, security controls, retention, suppliers
    and unresolved titular requests.
20. Complete and record a production-readiness review confirming that all
    applicable requirements in this spec are implemented, tested and assigned
    to an accountable owner.

## Prerequisite Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Spec Degree

1.
