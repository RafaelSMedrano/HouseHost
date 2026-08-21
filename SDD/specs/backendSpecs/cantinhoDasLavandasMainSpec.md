# Cantinho das Lavandas Main Spec

## Specification

Cantinho das Lavandas is a hospitality project supported by HouseHost, a system
for presenting the property to guests and managing its operational and financial
activities.

The product combines a public experience, through which visitors learn about the
property and request reservations, with an authenticated administrative
experience for managing guests, rooms, reservations, stays, finances and the
information required to operate the lodging responsibly.

## Scope

This mother spec covers the whole product vision. Detailed behavior belongs in
specialized specs descended from this document.

The product currently includes a Spring Boot backend, an administrative frontend
and a public website. Its evolution must preserve separation between business
rules, application orchestration and external technologies, while protecting
guest data and maintaining traceability of relevant operations.

Existing behavior is not automatically a new product requirement merely because
it exists in code. When a feature is changed through SDD, its intended behavior
must be made explicit in the appropriate descendant spec.

## Capabilities

### Public Property Experience

Visitors can discover the property, accommodations, destination, experiences,
gallery, frequently asked questions and contact information through the public
website.

### Public Reservation Journey

Visitors can consult information needed for a stay and submit a reservation
request through the public experience. Specialized specs define availability,
quotation, confirmation and personal-data rules.

### Administrative Access

Authorized users can authenticate and access administrative functions according
to their permissions.

### Guest Management

Authorized users can maintain the guest information needed for hospitality
operations, subject to privacy, minimization and access-control rules.

### Accommodation And Reservation Management

Authorized users can manage rooms, reservations and their operational status.

### Stay Operations

The system supports the operational lifecycle around guest arrival, stay and
departure. Detailed check-in and checkout rules belong in descendant specs.

### Financial Management

Authorized users can manage financial transactions, installments, cashier
entries and expenses related to lodging operations.

### Privacy, Security And Auditability

The product protects access to administrative and personal data, maintains an
inventory of relevant processing operations and records auditable business
events where required.

The product and all descendant specifications must comply with Brazil's Lei
Geral de Proteção de Dados Pessoais (LGPD). Personal-data processing must have
an identified lawful basis and observe purpose limitation, necessity,
transparency, security, prevention, accountability, data-subject rights,
appropriate retention and deletion, and personal-data incident response.
Detailed rules for each processing activity belong in the applicable descendant
spec.

### Operational Metrics

Authorized users can access summaries that support day-to-day management without
bypassing the access and privacy rules of the underlying information.

## Prerequisite Specs

None.

## Spec Degree

0.
