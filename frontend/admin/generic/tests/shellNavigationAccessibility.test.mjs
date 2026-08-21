import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const { createNavigationController } = await import(
    "../js/controllers/navigationController.js?v=shell-navigation-accessibility-tests"
);
const { createSidebarController } = await import(
    "../js/controllers/sidebarController.js?v=shell-navigation-accessibility-tests"
);
const { createUserController } = await import(
    "../js/controllers/userController.js?v=shell-navigation-accessibility-tests"
);
const {
    createSidebarRootActions,
    executeTopbarNavigationAction,
    focusCurrentPageHeading,
    syncSidebarCurrentRoot,
} = await import("../js/controllers/UICOntroller.js?v=shell-navigation-accessibility-tests");
const { renderDashboardTopbarWidget } = await import(
    "../js/widgets/dashboardTopbarWidget.js?v=shell-navigation-accessibility-tests"
);

function entry(name, rendered, params = {}) {
    return {
        name,
        params,
        render: () => rendered.push(name),
    };
}

test("navigation announces every successfully rendered entry to the shell", () => {
    const rendered = [];
    const announced = [];
    const navigation = createNavigationController({
        fallbackPage: entry("dashboard", rendered),
        onRendered: (page) => announced.push(page),
    });

    navigation.reset(entry("finance", rendered));
    navigation.goTo(entry("transactionProfile", rendered, { transactionId: 8 }));
    navigation.back();
    navigation.replace(entry("guests", rendered));

    assert.deepEqual(announced, [
        { name: "finance", params: {} },
        { name: "transactionProfile", params: { transactionId: 8 } },
        { name: "finance", params: {} },
        { name: "guests", params: {} },
    ]);
});

test("every sidebar action starts an independent root flow", () => {
    const rendered = [];
    const navigation = createNavigationController({
        fallbackPage: entry("dashboard", rendered),
    });
    const resetFromController = (name) => () => navigation.reset(entry(name, rendered));
    const rootActions = createSidebarRootActions({
        navigation,
        dashboardController: { renderDashboardPanel: () => rendered.push("dashboard") },
        reservationController: { renderReservationsPanel: () => rendered.push("reservations") },
        guestController: { renderGuestsPanel: () => rendered.push("guests") },
        roomController: { openRoomsPanel: resetFromController("rooms") },
        operationsController: {
            openCheckInPanel: resetFromController("checkin"),
            openCheckOutPanel: resetFromController("checkout"),
            openTimelinePanel: resetFromController("timeline"),
        },
        financeController: { openFinancePanel: resetFromController("finance") },
        supplierController: { openSuppliersPanel: resetFromController("suppliers") },
        privacyController: { openDataProcessingOperationsPanel: resetFromController("processingOperations") },
        ratingController: { openRatingsPanel: resetFromController("ratings") },
    });

    for (const [name, resetRoot] of Object.entries(rootActions)) {
        navigation.reset(entry("dashboard", rendered));
        navigation.goTo(entry("unrelatedDetail", rendered));
        resetRoot();
        assert.deepEqual(navigation.current(), { name, params: {} });
        assert.equal(navigation.canGoBack(), false);
    }
});

test("sidebar controller runs only authorized root actions and then closes", () => {
    const called = [];
    let closeCount = 0;
    const sidebar = createSidebarController({
        user: { id: 1, username: "Ana", role: "ADMIN" },
        permissions: { canAccessView: (view) => view === "guests" },
        closeSidebar: () => { closeCount += 1; },
        onUserProfile: () => called.push("userProfile"),
        rootActions: {
            guests: () => called.push("guests"),
            finance: () => called.push("finance"),
        },
    });

    assert.equal(sidebar.onNavigate("finance"), false);
    assert.equal(sidebar.onNavigate("missing"), false);
    assert.equal(closeCount, 0);
    assert.equal(sidebar.onNavigate("guests"), true);
    assert.equal(sidebar.onUserProfile(), true);
    assert.deepEqual(called, ["guests", "userProfile"]);
    assert.equal(closeCount, 2);
});

test("topbar actions append forms and preserve the page that launched them", () => {
    const rendered = [];
    const navigation = createNavigationController({
        fallbackPage: entry("dashboard", rendered),
    });
    const open = (name) => () => navigation.goTo(entry(name, rendered));
    const controllers = {
        reservationController: { openNewReservationPanel: open("reservationForm") },
        guestController: { openGuestFormPanel: open("guestForm") },
        operationsController: {
            openCheckInFormPanel: open("checkinForm"),
            openCheckOutFormPanel: open("checkoutForm"),
        },
    };
    const expectedPages = {
        reservation: "reservationForm",
        guest: "guestForm",
        checkin: "checkinForm",
        checkout: "checkoutForm",
    };

    for (const [action, page] of Object.entries(expectedPages)) {
        navigation.reset(entry("finance", rendered));
        assert.equal(executeTopbarNavigationAction(action, controllers), true);
        assert.equal(navigation.current().name, page);
        navigation.back();
        assert.equal(navigation.current().name, "finance");
    }
    assert.equal(executeTopbarNavigationAction("unknown", controllers), false);
});

test("user profile is a safe root whose back action falls back to dashboard", () => {
    const rendered = [];
    let profileOptions;
    const navigation = createNavigationController({
        fallbackPage: entry("dashboard", rendered),
    });
    const controller = createUserController({
        navigation,
        user: { id: 2, username: "Rafa", role: "ADMIN" },
        permissions: {
            canManageUsers: true,
            canAccessFinance: true,
            canDeleteOperationalData: true,
            canManageOperationalData: true,
        },
        renderDashboardTopbar() {},
        views: {
            renderUserProfileView: (_container, _user, options) => { profileOptions = options; },
        },
    });

    controller.openUserProfilePanel();
    assert.deepEqual(navigation.current(), { name: "userProfile", params: {} });
    assert.equal(navigation.canGoBack(), false);
    profileOptions.onBack();
    assert.deepEqual(navigation.current(), { name: "dashboard", params: {} });
});

test("the shell focuses the page heading without scrolling", () => {
    const focusCalls = [];
    const heading = { focus: (options) => focusCalls.push(options) };
    const root = {
        querySelector: (selector) => selector === "#topbar-container [data-page-heading]" ? heading : null,
    };

    assert.equal(focusCurrentPageHeading(root), true);
    assert.deepEqual(focusCalls, [{ preventScroll: true }]);
    assert.equal(focusCurrentPageHeading({ querySelector: () => null }), false);
});

test("sidebar current-page semantics follow roots and preserve their detail context", () => {
    const itemList = ["dashboard", "finance", "guests"].map((view) => ({
        dataset: { view },
        active: view === "finance",
        ariaCurrent: view === "finance" ? "page" : null,
        classList: {
            add(name) { if (name === "active") this.owner.active = true; },
            remove(name) { if (name === "active") this.owner.active = false; },
        },
        setAttribute(name, value) { if (name === "aria-current") this.ariaCurrent = value; },
        removeAttribute(name) { if (name === "aria-current") this.ariaCurrent = null; },
    }));
    itemList.forEach((item) => { item.classList.owner = item; });
    const root = { querySelectorAll: () => itemList };

    assert.equal(syncSidebarCurrentRoot("transactionProfile", root), false);
    assert.equal(itemList.find((item) => item.dataset.view === "finance").ariaCurrent, "page");
    assert.equal(syncSidebarCurrentRoot("dashboard", root), true);
    assert.equal(itemList.find((item) => item.dataset.view === "dashboard").ariaCurrent, "page");
    assert.equal(syncSidebarCurrentRoot("userProfile", root), true);
    assert.equal(itemList.every((item) => item.ariaCurrent === null), true);
});

test("topbar exposes an escaped, programmatically focusable page heading", () => {
    const container = {
        innerHTML: "",
        querySelectorAll: () => [],
    };
    const originalDocument = globalThis.document;
    globalThis.document = { getElementById: () => container };
    try {
        renderDashboardTopbarWidget("topbar", '<img src=x onerror="alert(1)">', {
            canManageOperationalData: true,
        });
    } finally {
        globalThis.document = originalDocument;
    }

    assert.match(container.innerHTML, /<h1 class="page-title" data-page-heading tabindex="-1">/);
    assert.equal(container.innerHTML.includes("<img src=x"), false);
    assert.match(container.innerHTML, /aria-hidden="true"/);
});

test("back controls are named buttons and navigation state is not persisted", () => {
    const backViewList = [
        "userProfileView.js",
        "guestProfileView.js",
        "reservationProfileView.js",
        "financialTransactionProfileView.js",
        "checkOperationFormView.js",
        "supplierFormView.js",
        "supplierProfileView.js",
        "dataProcessingOperationProfileView.js",
        "legalBasisAssessmentProfileView.js",
        "legalBasisAssessmentFormView.js",
    ];
    backViewList.forEach((fileName) => {
        const source = fs.readFileSync(new URL(`../js/views/${fileName}`, import.meta.url), "utf8");
        assert.match(source, /type="button"[^>]*aria-label="Voltar/);
    });

    const navigationSourceList = [
        "navigationController.js",
        "sidebarController.js",
        "UICOntroller.js",
        "userController.js",
    ].map((fileName) => fs.readFileSync(new URL(`../js/controllers/${fileName}`, import.meta.url), "utf8"));
    navigationSourceList.forEach((source) => {
        assert.doesNotMatch(source, /localStorage|sessionStorage|console\.|navigationFacade|routeRegistry/);
    });
});
