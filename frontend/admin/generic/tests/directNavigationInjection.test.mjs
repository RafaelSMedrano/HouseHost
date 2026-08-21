import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const { createGuestController } = await import(
    "../js/controllers/guestController.js?v=direct-navigation-injection-tests"
);
const { createOperationsController } = await import(
    "../js/controllers/operationsController.js?v=direct-navigation-injection-tests"
);
const { createFinanceController } = await import(
    "../js/controllers/financeController.js?v=direct-navigation-injection-tests"
);
const { createReservationController } = await import(
    "../js/controllers/reservationController.js?v=direct-navigation-injection-tests"
);
const { createRatingController } = await import(
    "../js/controllers/ratingController.js?v=direct-navigation-injection-tests"
);

function createNavigationSpy() {
    const calls = [];
    return {
        calls,
        goTo: (entry) => calls.push({ method: "goTo", entry }),
        reset: (entry) => calls.push({ method: "reset", entry }),
        replace: (entry) => calls.push({ method: "replace", entry }),
        back: () => calls.push({ method: "back" }),
    };
}

test("a domain controller submits dynamic entries directly to navigation", () => {
    const navigation = createNavigationSpy();
    const guestController = createGuestController({
        permissions: {
            canDeleteOperationalData: false,
            canAccessFinance: false,
        },
        navigation,
        renderNewReservationPanel() {},
        setTopbarContext() {},
        renderDashboardTopbar() {},
    });

    guestController.openGuestFormPanel(10);

    assert.equal(navigation.calls.length, 1);
    assert.equal(navigation.calls[0].method, "goTo");
    assert.deepEqual(navigation.calls[0].entry.params, { guestId: 10 });
    assert.equal(typeof navigation.calls[0].entry.render, "function");
});

test("a second domain controller uses the same navigation contract without a facade", () => {
    const navigation = createNavigationSpy();
    const operationsController = createOperationsController({
        navigation,
        setTopbarContext() {},
        renderDashboardTopbar() {},
    });

    operationsController.openCheckInFormPanel({ bookingId: 7 });

    assert.equal(navigation.calls.length, 1);
    assert.equal(navigation.calls[0].method, "goTo");
    assert.deepEqual(navigation.calls[0].entry, {
        name: "checkinForm",
        params: { bookingId: 7 },
        render: navigation.calls[0].entry.render,
    });
});

test("rating controller creates one lazy root entry directly in navigation", () => {
    const navigation = createNavigationSpy();
    const ratingController = createRatingController({
        navigation,
        permissions: { canAccessView: (view) => view === "ratings" },
        renderDashboardTopbar() {},
        renderGuestProfilePanel() {},
        renderReservationProfilePanel() {},
        loadDependencies: async () => ({
            findAllRatings: async () => ({ data: { ratingSummaryDTOList: [] } }),
            renderRatingsLoadingView() {},
            renderRatingsView() {},
            renderRatingsErrorView() {},
        }),
    });

    assert.equal(ratingController.openRatingsPanel(), true);
    assert.equal(navigation.calls.length, 1);
    assert.equal(navigation.calls[0].method, "reset");
    assert.deepEqual(navigation.calls[0].entry.params, { page: 0, size: 20 });
    assert.equal(typeof navigation.calls[0].entry.render, "function");
});

test("profile entry factories reject missing or malformed required identifiers", () => {
    const guestNavigation = createNavigationSpy();
    const guestController = createGuestController({
        permissions: { canDeleteOperationalData: false, canAccessFinance: false },
        navigation: guestNavigation,
        renderNewReservationPanel() {},
        renderDashboardTopbar() {},
    });
    assert.equal(guestController.openGuestProfilePanel(), false);
    assert.equal(guestController.openGuestProfilePanel("invalid"), false);
    assert.equal(guestController.openGuestFormPanel("invalid"), false);
    assert.equal(guestNavigation.calls.length, 0);

    const reservationNavigation = createNavigationSpy();
    const reservationController = createReservationController({
        permissions: { canDeleteOperationalData: false },
        navigation: reservationNavigation,
        renderDashboardTopbar() {},
        renderCheckInFormPanel() {},
        renderCheckOutFormPanel() {},
        renderGuestProfilePanel() {},
        renderGuestFormPanel() {},
    });
    assert.equal(reservationController.openReservationProfilePanel(0), false);
    assert.equal(reservationController.openEditReservationPanel(null), false);
    assert.equal(reservationNavigation.calls.length, 0);

    const financeNavigation = createNavigationSpy();
    const financeController = createFinanceController({
        permissions: { canAccessFinance: true },
        navigation: financeNavigation,
        renderDashboardTopbar() {},
        renderDashboardPanel() {},
        renderReservationProfilePanel() {},
        renderGuestProfilePanel() {},
    });
    assert.equal(financeController.openTransactionProfilePanel(undefined), false);
    assert.equal(financeController.openTransactionProfilePanel(-1), false);
    assert.equal(financeNavigation.calls.length, 0);
});

test("domain controllers receive navigation directly and no facade or route registry remains", () => {
    const controllerFiles = [
        "dashboardController.js",
        "guestController.js",
        "reservationController.js",
        "roomController.js",
        "operationsController.js",
        "financeController.js",
        "supplierController.js",
        "privacyController.js",
        "userController.js",
        "ratingController.js",
    ];

    controllerFiles.forEach((fileName) => {
        const source = fs.readFileSync(new URL(`../js/controllers/${fileName}`, import.meta.url), "utf8");
        assert.match(source, /navigation/);
        assert.doesNotMatch(source, /navigationFacade|createAdministrativeNavigationFacade|routeRegistry|navigationRoutes/);
    });

    const navigationSource = fs.readFileSync(
        new URL("../js/controllers/navigationController.js", import.meta.url),
        "utf8",
    );
    assert.doesNotMatch(navigationSource, /import\s|routeRegistry|navigationRoutes/);
});
