# Booking Stay History Retention Spec

## Specification

Booking Stay History Retention is the operational capability that allows an
authorized user to remove a reservation without erasing check-in or checkout
records that document events which already occurred.

A reservation is the planning and commercial record for a stay. Check-in and
checkout are historical operational records. Once created, those operational
records remain meaningful even when the reservation that originally initiated
them is later removed. Removing the reservation therefore ends their active
association with it but does not remove the historical events themselves.

## Scope

This spec governs deletion of reservations in the authenticated administrative
experience when a check-in, checkout, financial transaction or any combination
of those records refers to the reservation.

It covers:

- preservation of existing check-in and checkout records;
- removal of their association with the deleted reservation;
- continued access to the preserved operational history;
- preservation of financial transactions after removal of obsolete relational
  booking constraints;
- stable API representation of a historical record without an active
  reservation;
- atomic deletion and detachment behavior;
- correct authentication, authorization and error semantics for the operation;
- auditability of the reservation deletion.

The check-in and checkout continue to identify their guest, room, operational
facts, timestamps, responsible user, status and notes according to their
existing contracts. This capability does not delete or anonymize the guest,
room, financial history or audit history and does not redefine their separate
retention rules.

This capability does not introduce soft deletion for reservations, restore a
deleted reservation, or allow an unauthorized role to delete operational data.
Frontend presentation changes beyond consuming the existing nullable
reservation identifier contract require a separate frontend task.

## Capabilities

### Delete A Reservation Without Erasing Stay Events

An authorized user can delete a reservation even when a check-in, checkout or
both were created from it. The reservation ceases to exist, while each existing
check-in and checkout remains stored.

The operation removes only the association from the historical event to the
reservation. It must not cascade deletion from the reservation into check-in or
checkout.

### Preserve The Independent Operational Context

After detachment, the check-in and checkout retain all information they own,
including:

- guest;
- room;
- occupancy information;
- verification and inspection results;
- arrival or departure facts;
- responsible user;
- notes;
- status;
- creation and update timestamps where applicable.

The preserved event is not silently reassigned to another reservation.

### Preserve Financial History Without Blocking Deletion

A financial transaction created from a reservation remains stored after that
reservation is deleted. Its source type and original source identifier remain
historical evidence; they do not constitute an active foreign-key association
that can block removal of the reservation.

Existing installations must migrate values from a legacy financial
`booking_id` association to the current source representation before removing
the obsolete association. The migration must neither delete financial records
nor fabricate a different source identifier.

### Represent A Detached Reservation Explicitly

When a preserved check-in or checkout no longer has an associated reservation,
its administrative API representation returns a null reservation identifier.
The response must remain otherwise readable and must not fail while mapping or
serializing the historical record.

New check-in and checkout creation rules remain unchanged. A detached state is
a valid historical outcome of reservation deletion, not a way to bypass the
normal creation workflow.

### Apply The Change Atomically

Reservation deletion and detachment of every related check-in and checkout
form one atomic outcome. The system must not leave a deleted reservation
referenced by a historical record, partially detach related records, or report
success while the reservation remains stored.

Database referential integrity must enforce the same preservation rule as the
application. Existing installations are migrated without deleting or
fabricating operational history.

### Preserve Authorization And Session Integrity

Only roles already authorized to delete reservations may perform the operation.
A permission failure returns the established forbidden outcome and does not
delete or detach data.

A valid authenticated session must not be cleared because reservation deletion
encountered a persistence, mapping or application failure. HTTP 401 remains
reserved for missing, invalid or expired authentication. Permission failures,
business conflicts and internal failures retain their appropriate non-401
semantics.

### Maintain Auditability

A successful reservation deletion remains auditable with the deleted
reservation identifier and minimal evidence that related stay history was
preserved. Audit metadata must not copy full guest, check-in, checkout or
reservation payloads.

Failed deletion does not produce a successful deletion audit event.

## Prerequisite Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/moduleArchitectureSpec.md`

## Spec Degree

2.
