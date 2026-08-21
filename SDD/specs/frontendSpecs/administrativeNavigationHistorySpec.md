# Administrative Navigation History Spec

## Specification

Administrative Navigation History is the authenticated administrative
experience capability that preserves the user's real path through lists,
profiles, related records and forms. Its purpose is to make “Voltar” return to
the screen from which the current screen was opened, rather than to a fixed
module list.

The capability applies to the administrative SPA and remains in memory for the
active interface session. It does not change the business identity of guests,
reservations, financial transactions or other records. It only preserves the
user's presentation path between those records.

The navigation controller builds history entries when navigation occurs. It
does not require a complete centralized registry containing every possible
administrative route before the interface starts. Each entry carries the page
identity, its parameters and the renderer needed to restore that page.

The navigation controller is injected directly into domain controllers. Domain
controllers create entries for their own pages and call `goTo`, `reset`,
`replace` or `back` directly. No application-wide navigation facade is
required. Views remain unaware of the navigation controller and communicate
through semantic callbacks.

## Scope

This spec governs navigation between authenticated administrative experiences,
including the dashboard, sidebar modules, list pages, profile pages, related
profiles and operational forms.

The navigation history must support paths such as:

```text
Caixa
  -> perfil da transação financeira
       -> perfil do hóspede
```

When the user selects “Voltar” from the guest profile, the experience returns
to the financial transaction profile. A second “Voltar” returns to Caixa.

The same rule applies to other relationships:

```text
Reservas -> perfil da reserva -> perfil do hóspede
Caixa -> perfil da transação -> perfil da reserva
Tratamentos -> operação -> base legal
Fornecedores -> perfil do fornecedor -> edição
Hóspedes -> perfil do hóspede -> nova reserva
```

Selecting a primary item in the sidebar starts a new navigation flow. A new
sidebar flow does not inherit unrelated detail pages previously visited.

This spec governs user-visible navigation behavior. It does not require URL
routing, browser-history persistence, backend changes or persistence of the
navigation path after a full page reload.

## Capabilities

### Preserve The Actual Navigation Path

The administrative experience records each screen opened as part of the active
flow. A screen opened from another screen retains that predecessor, including
the predecessor's relevant record identifier and display context.

The history is an ordered in-memory array. Normal navigation appends one entry;
back removes the current entry and renders the immediately previous entry. Each
entry retains enough information to render the same page again.

The experience must not decide the return destination only from the type of the
current screen. A guest profile opened from a reservation must not assume that
the correct return destination is always the guest list.

### Return To The Immediate Predecessor

The “Voltar” action returns to the immediately previous screen in the active
flow and preserves the parameters required to display it.

For the following path:

```text
Caixa -> transação 25 -> hóspede 10
```

the successive return actions are:

```text
hóspede 10 -> transação 25 -> Caixa
```

Returning from a profile must not jump over an intermediate profile merely
because the intermediate profile belongs to another module.

### Preserve Record Context

When a previous screen is restored, its record context remains available. The
experience must preserve identifiers and state needed to reopen at least:

- guest profiles by guest identifier;
- reservation profiles by booking identifier;
- financial transaction profiles by transaction identifier;
- supplier profiles by supplier identifier;
- processing-operation profiles by operation identifier;
- legal-basis assessment profiles by assessment identifier and relationship
  context when applicable;
- edit and creation forms when the active flow requires returning to them.

The navigation capability does not require preserving unsaved form values after
the form is intentionally abandoned. Forms must clearly distinguish cancel,
back and successful save behavior according to the applicable module flow.

### Start A New Flow From The Sidebar

The sidebar is the primary entry point for module lists. Selecting a sidebar
item starts a new flow rooted at that module.

For example, after the user visits:

```text
Caixa -> transação -> hóspede
```

and selects “Hóspedes” in the sidebar, the active flow becomes:

```text
Hóspedes
```

Opening a guest profile from there returns to the Hóspedes list, not to the
previous financial transaction.

### Keep Related Navigation Reversible

Opening a related record creates a reversible step in the active flow. This
includes navigation from:

- a reservation to its guest;
- a financial transaction to its reservation or guest when available;
- a guest to a related reservation;
- a processing operation to an assessment;
- an assessment to its related operation;
- a supplier profile to its edit form.

The relationship direction must not permanently replace the screen that opened
the related record unless the action explicitly represents a replacement, such
as completing a form and returning to the resulting profile.

### Handle Forms Without Creating Misleading History

Opening an edit or creation form must preserve the screen that initiated the
form. Cancel returns to that screen.

After a successful save, the experience returns to the appropriate resulting
profile or list without creating duplicate back steps that cause the user to
re-enter the obsolete form.

The user must not see a sequence such as:

```text
perfil -> edição -> perfil -> edição
```

merely because a save operation re-rendered the profile.

### Provide A Safe Root Behavior

If there is no previous screen in the active flow, “Voltar” uses the module's
safe primary destination or the dashboard, according to the applicable
experience.

The interface must not fail silently, render an undefined screen or navigate to
a record without the identifier required to load it.

### Keep Views Independent Of Navigation Origin

Views express the user's intent to go back, open a related record or open a
form. They must not need to know whether they were opened from the sidebar,
another profile or a financial relationship.

The visible “Voltar” action must have consistent meaning across modules while
its destination is determined by the active navigation path.

Views and widgets must not access the history array directly. They receive
semantic callbacks such as `onBack`, `onOpenGuest` or `onOpenTransaction`.
Controllers or the UI composition layer convert those intents into entries.

### Preserve Access-Control Behavior

Navigation history does not grant access to a module or record. Existing role
and permission checks remain authoritative in the frontend experience, while
the backend remains authoritative for direct API access.

If a previously recorded destination is no longer available to the user, the
experience must refuse that destination safely and return to an authorized
fallback. It must not expose record data merely because a route was previously
visited.

### Provide Explicit Loading And Error Outcomes

Restoring a previous profile or list uses the same loading, empty and error
states as opening it normally. A failed restoration must not silently display a
different module and must not corrupt the remaining navigation path.

Record identifiers and navigation metadata are not written to console logs or
persistent browser storage as part of this capability.

### Maintain Accessible Back Behavior

The “Voltar” control is keyboard operable, has an accessible name and remains a
visible text action. Returning to a previous screen must place focus in a
predictable location, such as the restored page heading or its primary content
area.

The interface must not rely on color alone to communicate the current screen or
the availability of the back action.

## Prerequisite Specs

- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`

## Spec Degree

1.
