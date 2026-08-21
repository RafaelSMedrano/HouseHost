# Supplier Management Spec

## Specification

Supplier Management is the internal administrative capability that records the
organizations and professionals supplying services to Cantinho das Lavandas and
documents how each relationship involves personal data.

The capability distinguishes the commercial supplier from its data-processing
relationships. A supplier can provide more than one service and can act in a
different LGPD role for each service or purpose. Classification follows actual
behavior rather than the supplier's commercial label or a contract title alone.

The inventory is governance evidence. It supports supplier assessment,
contractual follow-up, security review, international-transfer identification,
incident response and accountability. Registration in the product does not by
itself approve a supplier or certify LGPD compliance.

## Scope

This spec governs the authenticated administrative inventory of suppliers that
receive, store, access, transmit, delete or otherwise process personal data, as
well as commercial suppliers assessed and recorded as not processing personal
data.

The initial capability covers:

- supplier identity and contact information;
- one or more services or data-processing relationships per supplier;
- personal-data and data-subject categories involved;
- purpose and concrete treatment activities;
- classification as operator, sub-operator, independent controller, joint
  controller, recipient or supplier without personal-data processing;
- storage and processing locations and international-transfer indication;
- retention and deletion commitments;
- security measures and incident contact;
- contract status, reference, responsibilities and review evidence;
- operational risk, approval status and lifecycle status;
- administrative list, detail, creation and update experiences.

The module stores inventory metadata and references. It does not store access
credentials, API secrets, complete contract files, full security reports or
unnecessary personal information about supplier employees. Contract and
evidence file upload requires a later spec.

The inventory is internal and is not published in the public privacy policy.
The public policy may continue describing recipient categories while the
controller maintains nominal internal evidence.

Supplier management is an independent module boundary. Its services do not
communicate directly with services from audit, authentication, privacy,
financial or other modules. Every cross-module interaction uses an explicit
port and adapter, in either direction, as governed by
`moduleArchitectureSpec.md`.

## Capabilities

### Register A Supplier

An authorized privacy-governance user can register a supplier with, at minimum:

- official or legal name;
- trade name when applicable;
- service description;
- country of establishment;
- privacy or incident contact when available;
- internal owner responsible for the relationship;
- lifecycle status;
- review date and notes required to explain material decisions.

CNPJ, other registration identifiers, website and ordinary business contact
are optional unless required to distinguish the supplier or support governance.
The product does not collect a supplier contact person's personal information
when a functional business channel is sufficient.

Duplicate suppliers must be detected through normalized official name and,
when present, registration identifier. Similar names produce a review warning
and are not merged automatically.

### Register Data-Processing Relationships

`SupplierDataProcessingRelationship` represents one concrete service or purpose
through which a supplier processes, or is assessed as not processing, personal
data for Cantinho das Lavandas. It does not represent the supplier itself. The
`Supplier` holds the supplier's shared commercial identity, while each
`SupplierDataProcessingRelationship` holds the LGPD, security, retention,
location and contractual facts applicable to that specific service or purpose.

This separation prevents evidence from one service from being incorrectly
applied to every service offered by the same supplier. For example, AWS can be
registered once as a supplier and have separate relationships for S3 storage,
SES email delivery and RDS database hosting. Each relationship can involve
different data categories, purposes, locations, retention rules, security
measures, contracts and LGPD roles.

Every supplier has at least one service relationship. Each relationship records:

- service name and operational description;
- purpose;
- personal-data categories or an explicit declaration that no personal data is
  processed;
- data-subject categories;
- treatment actions such as access, storage, transmission, backup or deletion;
- LGPD role;
- controller instructions or autonomous purposes that justify the role;
- storage and processing countries or regions;
- international-transfer status and applicable mechanism when required;
- retention criterion and deletion or return procedure;
- security measures;
- incident-notification channel and contractual expectation;
- sub-operator information when applicable;
- contract status, reference and material responsibilities;
- risk level, assessment notes and last review evidence.

A relationship classified as supplier without personal-data processing must not
contain personal-data categories or treatment actions. A relationship involving
personal data must not omit purpose, role, location, retention, deletion,
security and responsibility information.

### Classify The Real LGPD Role

The product supports these classifications:

- `OPERATOR`: processes personal data on instructions from Cantinho das
  Lavandas;
- `SUB_OPERATOR`: is engaged through another operator to assist processing for
  Cantinho das Lavandas;
- `INDEPENDENT_CONTROLLER`: determines its own purposes or essential means for
  the received data;
- `JOINT_CONTROLLER`: jointly determines purposes and essential means with the
  controller;
- `RECIPIENT`: receives data and requires a documented case-specific role
  assessment;
- `NO_PERSONAL_DATA`: supplies a product or service without processing personal
  data for this relationship.

The classification interface provides short explanations but does not make the
decision automatically. The authorized reviewer records the factual reason for
the selected role. One supplier may have multiple relationships with different
roles.

### Assess Contract And Security Readiness

Each relationship has a governance status:

- `DRAFT`: information is incomplete or under assessment;
- `PENDING`: evidence, contract or remediation is outstanding;
- `APPROVED`: applicable required information and review are complete;
- `BLOCKED`: the relationship has an unresolved risk that prevents approval;
- `INACTIVE`: the service ended and its return or deletion outcome is recorded.

Approval requires an accountable reviewer and timestamp. The product prevents
approval while mandatory information for the relationship is absent. An
inactive relationship records the termination date and whether data was
returned, deleted, retained under a documented justification or remains pending.

An approved relationship must have an active contract or a contract status of
not applicable supported by a recorded justification. A relationship with an
absent, unreviewed, under-review or expired contract cannot be approved.

The product records contract existence, status, reference, start/end or renewal
dates and responsibility summary. It does not claim that checking a box replaces
legal or security review.

### Return Supplier Information To The Administrative Frontend

Authorized users can obtain:

- a supplier list with name, principal service, roles, risk, governance status,
  lifecycle status and last review date;
- filtered results by name, role, risk, governance status and lifecycle status;
- a supplier detail with every relationship and its governance evidence;
- the current data required to edit a supplier and its relationships.

List responses are concise and do not repeat long contract, security or
responsibility narratives. Full detail is returned only through the protected
detail operation.

### Update Without Erasing History

Authorized users can correct and update supplier information and processing
relationships. Material changes to role, purpose, personal-data categories,
international transfer, retention, security, contract or status are auditable.

Suppliers and relationships are deactivated rather than hard-deleted during the
initial capability. Deactivation preserves governance history and requires an
end date and outcome for retained or deleted personal data.

### Restrict Access And Maintain Auditability

The supplier inventory is available only to authenticated administrative users
authorized for privacy governance. The backend is authoritative for read and
write permissions; hiding a frontend view is not access control.

Creation, detail access, update, approval, blocking and deactivation produce
auditable events. Audit metadata contains identifiers and changed-field names,
not complete contract text, full request payloads, credentials or security
secrets.

### Remain Independent From Other Modules

The supplier module owns its business behavior and persistence. Audit and
reviewer identity are external capabilities accessed through supplier-owned
output ports and integration adapters. `SupplierService` does not inject or
invoke another module's service, repository, persistence port, JPA entity or
adapter.

Any future external dependency requires a purpose-specific port and adapter.
Likewise, a module that needs supplier data declares its own port and adapter;
that adapter may invoke the public `SupplierUseCase`, but its service must not
invoke `SupplierService` or supplier persistence directly.

### Review The Inventory

Every active relationship has a next-review date. The administrative experience
identifies overdue and upcoming reviews without automatically changing approval.

Review is required before introducing a new supplier that processes personal
data, after a material service or contract change, after a relevant incident and
at the periodic interval chosen by the controller.

## Prerequisite Specs

- `SDD/specs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/lgpdGovernanceSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Spec Degree

2.
