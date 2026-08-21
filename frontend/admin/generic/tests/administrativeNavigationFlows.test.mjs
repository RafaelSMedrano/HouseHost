import assert from "node:assert/strict";
import test from "node:test";

const { createNavigationController } = await import(
    "../js/controllers/navigationController.js?v=administrative-navigation-flow-tests"
);
const { createFinanceController } = await import(
    "../js/controllers/financeController.js?v=administrative-navigation-flow-tests"
);
const { createGuestController } = await import(
    "../js/controllers/guestController.js?v=administrative-navigation-flow-tests"
);

function createFlowHarness() {
    const rendered = [];
    const entry = (name, params = {}) => ({
        name,
        params,
        render: () => rendered.push({ name, params }),
    });
    const navigation = createNavigationController({
        fallbackPage: entry("dashboard"),
    });

    return { entry, navigation, rendered };
}

test("finance transaction guest flow returns one page at a time", () => {
    const { entry, navigation, rendered } = createFlowHarness();

    navigation.reset(entry("finance"));
    navigation.goTo(entry("transactionProfile", { transactionId: 25 }));
    navigation.goTo(entry("guestProfile", { guestId: 10 }));

    navigation.back();
    assert.deepEqual(navigation.current(), {
        name: "transactionProfile",
        params: { transactionId: 25 },
    });

    navigation.back();
    assert.deepEqual(navigation.current(), { name: "finance", params: {} });
    assert.deepEqual(rendered.slice(-2).map(({ name }) => name), [
        "transactionProfile",
        "finance",
    ]);
});

test("mandatory finance transaction guest path works through controller callbacks", () => {
    const rendered = [];
    let financeOptions;
    let transactionOptions;
    let guestOptions;
    const navigation = createNavigationController({
        fallbackPage: {
            name: "dashboard",
            params: {},
            render: () => rendered.push("dashboard"),
        },
    });
    const guestController = createGuestController({
        navigation,
        permissions: { canDeleteOperationalData: true, canAccessFinance: true },
        renderDashboardTopbar: (title) => rendered.push(title),
        renderNewReservationPanel() {},
        views: {
            renderGuestProfileView: (_container, options) => { guestOptions = options; },
        },
    });
    const financeController = createFinanceController({
        navigation,
        permissions: { canAccessFinance: true },
        renderDashboardTopbar: (title) => rendered.push(title),
        renderDashboardPanel: () => rendered.push("dashboard"),
        renderReservationProfilePanel() {},
        renderGuestProfilePanel: (guestId, onBack) => guestController.renderGuestProfilePanel(guestId, onBack),
        views: {
            renderFinanceView: (_container, options) => { financeOptions = options; },
            renderFinancialTransactionProfileView: (_container, options) => { transactionOptions = options; },
        },
    });

    financeController.openFinancePanel();
    financeOptions.onOpenTransaction(25);
    transactionOptions.onOpenGuest(10);
    assert.deepEqual(navigation.current(), { name: "guestProfile", params: { guestId: 10 } });

    guestOptions.onBack();
    assert.deepEqual(navigation.current(), {
        name: "transactionProfile",
        params: { transactionId: 25 },
    });
    transactionOptions.onBack();
    assert.deepEqual(navigation.current(), { name: "finance", params: {} });
    assert.deepEqual(rendered.filter((title) => title !== "dashboard"), [
        "Caixa",
        "Transação financeira",
        "Profile do hospede",
        "Transação financeira",
        "Caixa",
    ]);
});

test("finance permission denial resets to dashboard without retaining finance", () => {
    const rendered = [];
    const navigation = createNavigationController({
        fallbackPage: { name: "dashboard", params: {}, render: () => rendered.push("dashboard") },
    });
    const financeController = createFinanceController({
        navigation,
        permissions: { canAccessFinance: false },
        renderDashboardTopbar() {},
        renderDashboardPanel: () => rendered.push("authorized-dashboard"),
        renderReservationProfilePanel() {},
        renderGuestProfilePanel() {},
    });

    navigation.reset({ name: "guests", params: {}, render() {} });
    assert.equal(financeController.openFinancePanel(), false);
    assert.deepEqual(navigation.current(), { name: "dashboard", params: {} });
    assert.equal(navigation.canGoBack(), false);
    assert.equal(rendered.includes("authorized-dashboard"), true);
});

test("transaction reservation guest flow preserves every predecessor", () => {
    const { entry, navigation } = createFlowHarness();

    navigation.reset(entry("finance"));
    navigation.goTo(entry("transactionProfile", { transactionId: 25 }));
    navigation.goTo(entry("reservationProfile", { bookingId: 7 }));
    navigation.goTo(entry("guestProfile", { guestId: 10 }));

    navigation.back();
    assert.equal(navigation.current().name, "reservationProfile");
    navigation.back();
    assert.equal(navigation.current().name, "transactionProfile");
    navigation.back();
    assert.equal(navigation.current().name, "finance");
});

test("reservation guest flow returns to reservation and then its root list", () => {
    const { entry, navigation } = createFlowHarness();

    navigation.reset(entry("reservations"));
    navigation.goTo(entry("reservationProfile", { bookingId: 7 }));
    navigation.goTo(entry("guestProfile", { guestId: 10 }));

    navigation.back();
    assert.equal(navigation.current().name, "reservationProfile");
    navigation.back();
    assert.equal(navigation.current().name, "reservations");
});

test("saving an edit opened from a profile removes the form without duplicating the profile", () => {
    const { entry, navigation } = createFlowHarness();

    navigation.reset(entry("reservations"));
    navigation.goTo(entry("reservationProfile", { bookingId: 7 }));
    navigation.goTo(entry("reservationForm", { bookingId: 7 }));
    navigation.back();

    assert.deepEqual(navigation.current(), {
        name: "reservationProfile",
        params: { bookingId: 7 },
    });
    navigation.back();
    assert.equal(navigation.current().name, "reservations");
});

test("saving an edit opened from a list replaces the form with the resulting profile", () => {
    const { entry, navigation } = createFlowHarness();

    navigation.reset(entry("reservations"));
    navigation.goTo(entry("reservationForm", { bookingId: 7 }));
    navigation.replace(entry("reservationProfile", { bookingId: 7 }));

    assert.equal(navigation.current().name, "reservationProfile");
    navigation.back();
    assert.equal(navigation.current().name, "reservations");
});
