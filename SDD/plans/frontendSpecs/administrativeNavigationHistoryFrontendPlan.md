# Administrative Navigation History Frontend Plan

## Governing Specs

- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`
- `SDD/specs/backendSpecs/cantinhoDasLavandasMainSpec.md`
- `SDD/specs/sddSpec.md`

## Objective

Replace fixed module-specific back destinations with a small in-memory
navigation history for the authenticated administrative SPA. The history must
preserve the actual path through lists, profiles, related records and forms,
while sidebar entries start independent flows. Pages are constructed lazily
when the user navigates to them; there is no mandatory complete route table at
startup.

This plan defines technical direction only. It does not authorize code
implementation by itself. Implementation requires explicit approval and the
ordered tasks defined under `SDD/tasks/frontendSpecs/`.

## Current Frontend Context

The administrative frontend uses JavaScript ES Modules without a frontend
framework. All sidebar modules are already separated into controller
factories. `UICOntroller` is the composition root: it creates the controller
objects and injects shared dependencies, including the navigation controller.
An earlier facade-based integration was implemented but is treated as an
architectural error and is removed by task `017f`.

The current shell and module controllers are under:

```text
frontend/admin/js/controllers/UICOntroller.js
frontend/admin/js/controllers/dashboardController.js
frontend/admin/js/controllers/guestController.js
frontend/admin/js/controllers/reservationController.js
frontend/admin/js/controllers/roomController.js
frontend/admin/js/controllers/operationsController.js
frontend/admin/js/controllers/financeController.js
frontend/admin/js/controllers/supplierController.js
frontend/admin/js/controllers/privacyController.js
frontend/admin/js/controllers/userController.js
frontend/admin/js/controllers/sidebarController.js
```

Views currently receive callbacks such as `onBack`. The controllers are already
separated, but some callbacks still encode a fixed destination, such as always
returning a guest profile to the guest list. The refactor must keep the
factory/controller boundary and replace those fixed destinations with the
central history operation.

## Navigation Model

Create:

```text
navigationController.js (frontend/admin controller; module)
```

The module owns a private ordered array of page entries. An entry contains a
stable page name, parameters and the operation needed to render that page:

```js
{
    name: "guestProfile",
    params: { guestId: 10 },
    render: () => guestController.renderGuestProfilePanel(10),
    meta: { source: "transactionProfile" }
}
```

The `render` function is created when the page is opened, not registered for
every module in advance. The navigation controller owns the array and invokes
the current entry renderer. It does not import domain controllers or views;
the domain controller that owns the page supplies the render closure.

The public contract is:

```text
createNavigationController({ fallbackPage })
goTo(entry)
back()
replace(entry)
reset(entry)
current()
canGoBack()
```

`goTo` appends an entry and renders it. `back` removes the current entry
and renders the previous entry. `replace` changes the current entry without
adding a step. `reset` clears the stack and creates a new root flow. `current`
returns a defensive representation of the current entry, and `canGoBack`
reports whether a previous entry exists.

The navigation controller itself is the controller-facing navigation API.
Domain controllers receive it as a dependency and construct entries on demand.
They do not import one another directly and do not manipulate the private
history array.

## Page Entry Construction

Domain controllers construct entries for these page identities:

```text
dashboard
reservations
reservationProfile { bookingId }
reservationForm { bookingId? }
guests
guestProfile { guestId }
guestForm { guestId? }
rooms
roomForm { roomId? }
checkin
checkinForm { bookingId? }
checkout
checkoutForm { bookingId? }
timeline
finance
transactionProfile { transactionId }
suppliers
supplierProfile { supplierId }
supplierForm { supplierId? }
processingOperations
operationProfile { operationId }
assessments
assessmentProfile { assessmentId, origin? }
assessmentForm { assessmentId?, operationId?, origin? }
userProfile
```

Each entry factory validates the parameters required to load its view before
adding the entry. A malformed entry must not render an unrelated record or
leave the application on a blank panel.

## Integration Rules

### Sidebar

Sidebar primary items call `reset`:

```text
Dashboard -> reset(dashboard)
Reservas -> reset(reservations)
Hóspedes -> reset(guests)
Caixa -> reset(finance)
```

The same rule applies to rooms, check-in, check-out, timeline, suppliers and
processing operations.

### Related Records

Opening a related record calls `goTo`:

```text
reservationProfile -> goTo(guestProfile entry)
transactionProfile -> goTo(reservationProfile entry)
operationProfile -> goTo(assessmentProfile entry)
```

The related record does not receive a manually selected list destination. Its
back action calls the centralized `back` operation.

### Forms

Opening a form calls `goTo`. Cancel calls `back`. Successful save uses
`replace` when the form should disappear from the back path, or `goTo` when
the product flow intentionally keeps the form as a previous step.

The decision must be explicit per form and documented in the task that changes
it. The default operational behavior is:

```text
profile -> edit -> cancel = profile
profile -> edit -> save   = profile without edit duplicated in history
```

### Topbar

Topbar actions open new screens through the same navigation API. They must use
the current page as the return context and must not mutate the stack by
accidentally rendering a page twice.

## Module Boundaries

`UICOntroller` owns shell composition, creation of the navigation controller,
creation of the `create...Controller` objects, dependency injection, sidebar
wiring and topbar wiring. It does not own profile-specific page renderers,
profile-specific back destinations or a complete route registry.

Domain controllers own their module's view composition and callbacks. They may
call the injected navigation contract directly, create entries for their own
pages and must not manipulate the private history array directly.

When a controller opens a page owned by another module, `UICOntroller` injects
only the specific renderer function required by that relationship. Domain
controllers do not receive a global collection of every controller.

Views render markup and emit user intent through callbacks. They do not import
`navigationController`, inspect route names or infer their origin.

## Error, Permission And Loading Behavior

- A missing entry renderer is a controlled navigation error, not a blank panel.
- Missing required identifiers use the existing view error state or the safe
  dashboard fallback.
- Permission failures do not add unauthorized routes to the stack.
- A failed data load leaves the current route identifiable and preserves the
  rest of the stack.
- Restoring a previous route uses the same loading, empty and error states as a
  direct opening.
- Navigation metadata and domain response objects remain in memory only.

## Accessibility And Cache Busting

The existing back button remains a keyboard-operable, named button. Restored
views should place focus on the page heading or primary content when practical.
Changed controller imports receive new query-string versions so deployed
browsers do not mix old route behavior with new controllers.

## Verification Strategy

The implementation is verified at three levels:

1. Unit-level stack behavior for `goTo`, `back`, `replace`, `reset`, root
   fallback and parameter preservation.
2. Controller integration using mocked views/renderers and route callbacks.
3. Manual end-to-end flows across sidebar, profiles and forms.

The mandatory end-to-end path is:

```text
Caixa -> transação -> hóspede -> Voltar -> transação -> Voltar -> Caixa
```

Additional paths cover reservations, suppliers, rooms, governance profiles,
forms, permission failures and starting a new flow from the sidebar.

## Out Of Scope

- URL routing and deep-link support.
- Browser-history synchronization with `window.history`.
- Persisting navigation across reloads or login sessions.
- Backend route or API changes.
- Changes to business permissions or record ownership.
