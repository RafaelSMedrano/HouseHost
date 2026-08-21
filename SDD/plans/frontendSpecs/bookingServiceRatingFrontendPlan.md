# Booking Service Rating Frontend Plan

## Governing Specs

- `SDD/specs/bookingServiceRatingSpec.md`;
- `SDD/specs/frontendSpecs/administrativeNavigationHistorySpec.md`;
- all remaining prerequisites declared by the governing spec.

## Objective

Collect the six booking-service scores during checkout and provide accessible
rating history and list experiences integrated with the current
framework-free administrative navigation.

This plan does not authorize implementation. Frontend tasks `031f` through
`035f` are complete after their independent explicit executions.

## Frontend Modules

Expected modules include:

```text
frontend/admin/js/controllers/ratingController.js
frontend/admin/js/views/ratingsView.js
frontend/admin/js/views/checkOperationFormView.js
frontend/admin/js/views/newReservationView.js
frontend/admin/js/api.js
frontend/admin/js/permissions.js
frontend/admin/js/widgets/sidebarWidget.js
frontend/admin/js/controllers/sidebarController.js
frontend/admin/js/controllers/UICOntroller.js
frontend/admin/css/home.css
```

`ratingController` owns ratings-list navigation. Views receive semantic
callbacks for opening guests and bookings; they do not import other domain
views or mutate navigation history directly.

## Checkout Star Controls

Render six named groups, each with five native radio inputs or an equivalently
complete keyboard interaction. Stars begin empty. Selecting position `n` fills
positions `1..n`, stores integer `n` and announces `n de 5`.

All six groups are required before completed checkout submission. Validation
focuses the first unanswered group, preserves every selected score and
observation after failure, disables duplicate submission and uses live-region
feedback. Observations are optional, multiline and limited to 4,000 characters.

The payload sends one nested rating object with exact backend property names.
Remove the generic rating control, preview, state, payload member, formatter and
tests completely from guest and checkout frontend code.

## Ratings Sidebar And List

Add `ratings` to operational view permissions, sidebar rendering, root actions
and current-root synchronization. Sidebar selection performs
`navigation.reset(ratingsEntry)`.

The ratings list table shows, in order, guest, booking, evaluation date, all six
criteria and observations. Scores use a reusable read-only stars presenter
with visible or assistive text `n de 5`. The table supports horizontal overflow
and explicit empty/loading/error states.

Each rating is one non-clickable table row. Only the guest name in the first
column and booking reference in the second column are semantic links, using
callbacks supplied by `UICOntroller`. No rating profile, detail view, row
selection or individual-rating navigation is created.

## Reservation Creation History

Extend exact guest selection state with the selected guest ID. Once selected,
load the first bounded page or complete MVP history required by the table.

Show a text button `Histórico de avaliações` after every exact guest selection,
including when the history is empty. It uses `aria-expanded` and
`aria-controls`, toggles an inline table or accessible empty-history message and
never resets reservation inputs. Changing/clearing the guest closes and clears
the prior history. Request identifiers or abort signals prevent stale responses
from appearing under another guest.

Rows show booking, evaluation date, six score columns and observations.
Booking links open the booking profile while preserving the reservation form as
the predecessor.

## API And Cache Busting

Add API helpers for paginated rating list and guest history. Encode
identifiers and query parameters, preserve established session expiration
handling and never place observations in URLs or logs. Update every importer
version affected by new or changed static modules.

## Verification

Add tests for star selection/fill behavior, keyboard/name semantics, validation,
payload shape, failure preservation, stale history, toggle state, empty/error
states, table links, permissions, navigation history, responsive CSS and cache
version consistency. Run the complete frontend suite and `git diff --check`.

## Out Of Scope

- public rating collection outside authenticated checkout;
- score averages/charts;
- editing guest registration;
- frontend-only authorization;
- persistent browser caching of ratings.
