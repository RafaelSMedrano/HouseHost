import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

globalThis.localStorage = {
    getItem: () => "reservation-rating-history-token",
    setItem() {},
    removeItem() {},
};
globalThis.location = { protocol: "http:", hostname: "localhost", port: "8080" };

const newReservationViewModule = await import(
    "../js/views/newReservationView.js?v=reservation-rating-history-tests"
);
const { createReservationController } = await import(
    "../js/controllers/reservationController.js?v=reservation-rating-history-tests"
);
const { createNavigationController } = await import(
    "../js/controllers/navigationController.js?v=reservation-rating-history-tests"
);

test("guest lookup option click selects the guest and closes the dropdown", () => {
    const nameInput = { value: "" };
    const documentInput = { value: "" };
    let dropdownHidden = false;
    let defaultPrevented = false;
    let propagationStopped = false;
    let loadedGuestId = null;
    const container = {
        dataset: {},
        querySelector: (selector) => selector === "#new-reservation-guest-name"
            ? nameInput
            : documentInput,
        querySelectorAll: (selector) => selector === ".guest-lookup-options"
            ? [{ classList: { remove: () => { dropdownHidden = true; } } }]
            : [],
    };
    const state = {
        guests: [{ id: 7, fullName: "Ana Silva", documentNumber: "12345678901" }],
        selectedGuestId: null,
    };
    const event = {
        preventDefault: () => { defaultPrevented = true; },
        stopPropagation: () => { propagationStopped = true; },
    };

    assert.equal(newReservationViewModule.handleGuestLookupOptionSelection(
            event,
            container,
            state,
            { load: (guestId) => { loadedGuestId = guestId; } },
            "7"
    ), true);
    assert.equal(defaultPrevented, true);
    assert.equal(propagationStopped, true);
    assert.equal(dropdownHidden, true);
    assert.equal(nameInput.value, "Ana Silva");
    assert.equal(documentInput.value, "123.456.789-01");
    assert.equal(state.selectedGuestId, 7);
    assert.equal(container.dataset.selectedGuestId, "7");
    assert.equal(loadedGuestId, 7);
});

test("guest lookup uses stable pointer delegation across dynamic option rendering", () => {
    const listenerMap = new Map();
    const optionButton = { dataset: { guestId: "9" } };
    const options = {
        addEventListener: (type, listener) => listenerMap.set(type, listener),
        contains: (element) => element === optionButton,
        classList: { remove() {} },
    };
    const nameInput = { value: "" };
    const documentInput = { value: "" };
    let loadedGuestId = null;
    const container = {
        dataset: {},
        querySelector: (selector) => {
            if (selector === "#new-reservation-guest-name-options") return options;
            if (selector === "#new-reservation-guest-name") return nameInput;
            return documentInput;
        },
        querySelectorAll: (selector) => selector === ".guest-lookup-options"
            ? [options]
            : [],
    };
    const state = {
        guests: [{ id: 9, fullName: "Bia Souza", documentNumber: "98765432100" }],
        selectedGuestId: null,
    };
    newReservationViewModule.bindGuestLookupOptions(
            container,
            state,
            { load: (guestId) => { loadedGuestId = guestId; } },
            "name"
    );
    const event = {
        target: { closest: () => optionButton },
        preventDefault() {},
        stopPropagation() {},
    };

    listenerMap.get("pointerdown")(event);

    assert.equal(nameInput.value, "Bia Souza");
    assert.equal(documentInput.value, "987.654.321-00");
    assert.equal(state.selectedGuestId, 9);
    assert.equal(loadedGuestId, 9);
});

test("guest lookup options are not interactive descendants of their labels", () => {
    const newReservationViewSource = fs.readFileSync(
            new URL("../js/views/newReservationView.js", import.meta.url),
            "utf8"
    );
    assert.doesNotMatch(
            newReservationViewSource,
            /<label class="booking-field guest-lookup-field"/
    );
    assert.match(
            newReservationViewSource,
            /<label class="guest-lookup-label" for="new-reservation-guest-name">/
    );
    assert.match(
            newReservationViewSource,
            /<label class="guest-lookup-label" for="new-reservation-guest-document">/
    );
});

test("guest rating history loader ignores obsolete responses and uses a bounded page", async () => {
    const pendingResponseList = [];
    const requestedArgumentList = [];
    const loadedBookingIdList = [];
    const loader = newReservationViewModule.createGuestRatingHistoryLoader({
        findRatings: (...requestedArgumentListItem) => {
            requestedArgumentList.push(requestedArgumentListItem);
            return new Promise((resolve) => pendingResponseList.push(resolve));
        },
        onLoaded: (ratingSummaryList) => {
            loadedBookingIdList.push(...ratingSummaryList.map((ratingSummary) => ratingSummary.bookingId));
        },
    });

    const firstLoad = loader.load(7);
    const secondLoad = loader.load(8);
    pendingResponseList[1]({ data: { ratingSummaryDTOList: [{ bookingId: 82 }] } });
    await secondLoad;
    pendingResponseList[0]({ data: { ratingSummaryDTOList: [{ bookingId: 71 }] } });
    await firstLoad;

    assert.deepEqual(loadedBookingIdList, [82]);
    assert.deepEqual(requestedArgumentList.map(([guestId, page, size]) => [guestId, page, size]), [
        [7, 0, 100],
        [8, 0, 100],
    ]);
    assert.equal(requestedArgumentList[0][3].signal.aborted, true);
});

test("guest rating history loader distinguishes empty and failed requests", async () => {
    const stateList = [];
    const emptyLoader = newReservationViewModule.createGuestRatingHistoryLoader({
        findRatings: async () => ({ data: { ratingSummaryDTOList: [] } }),
        onLoading: () => stateList.push("loading"),
        onEmpty: () => stateList.push("empty"),
    });
    const errorLoader = newReservationViewModule.createGuestRatingHistoryLoader({
        findRatings: async () => {
            throw new Error("offline");
        },
        onLoading: () => stateList.push("loading-error"),
        onError: () => stateList.push("error"),
    });

    assert.equal(await emptyLoader.load(7), true);
    assert.equal(await errorLoader.load(7), false);
    assert.deepEqual(stateList, ["loading", "empty", "loading-error", "error"]);
});

test("empty guest history keeps the accessible textual toggle available", () => {
    const markup = newReservationViewModule.buildGuestRatingHistoryMarkup([]);

    assert.match(markup, /Histórico de avaliações/);
    assert.match(markup, /aria-expanded="false" aria-controls="guest-rating-history-table"/);
    assert.match(markup, /id="guest-rating-history-table"[^>]*role="status"[^>]*hidden/);
    assert.match(markup, /Nenhuma avaliação encontrada para este hóspede\./);
    assert.equal(markup.includes("<table"), false);
});

test("guest history underlines only the toggle text and not its arrow", () => {
    const markup = newReservationViewModule.buildGuestRatingHistoryMarkup([]);
    const cssSource = fs.readFileSync(new URL("../css/home.css", import.meta.url), "utf8");

    assert.match(markup, /<span>Histórico de avaliações<\/span>/);
    assert.match(markup, /<i class="ti ti-chevron-down" aria-hidden="true"><\/i>/);
    assert.match(
            cssSource,
            /\.guest-rating-history-toggle > span\s*\{[^}]*text-decoration:\s*underline/s
    );
    assert.match(
            cssSource,
            /\.guest-rating-history-toggle > i\s*\{\s*text-decoration:\s*none;\s*\}/s
    );
});

test("guest rating history markup is accessible, escaped and has no rating-row action", () => {
    const markup = newReservationViewModule.buildGuestRatingHistoryMarkup([{
        bookingId: 42,
        evaluatedAt: "2026-08-13T10:30:00Z",
        checkInProcedureScore: 5,
        checkOutProcedureScore: 4,
        accommodationCleanlinessScore: 3,
        teamCommunicationScore: 2,
        locationScore: 1,
        comfortScore: 5,
        observations: "<img src=x onerror=alert(1)>",
    }]);

    assert.match(markup, /Histórico de avaliações/);
    assert.match(markup, /aria-expanded="false" aria-controls="guest-rating-history-table"/);
    assert.match(markup, /data-open-history-booking="42"/);
    assert.match(markup, /aria-label="5 de 5"/);
    assert.match(markup, /aria-label="1 de 5"/);
    assert.equal(markup.includes("<img"), false);
    assert.equal(markup.includes("data-open-rating"), false);
});

test("guest rating history toggle synchronizes expanded and hidden states", () => {
    const attributeMap = new Map([["aria-expanded", "false"]]);
    const toggle = {
        getAttribute: (name) => attributeMap.get(name),
        setAttribute: (name, value) => attributeMap.set(name, value),
    };
    const table = { hidden: true };

    assert.equal(newReservationViewModule.toggleGuestRatingHistory(toggle, table), true);
    assert.equal(attributeMap.get("aria-expanded"), "true");
    assert.equal(table.hidden, false);
    assert.equal(newReservationViewModule.toggleGuestRatingHistory(toggle, table), false);
    assert.equal(attributeMap.get("aria-expanded"), "false");
    assert.equal(table.hidden, true);
});

test("booking history navigation preserves the reservation form as predecessor and restores its state", () => {
    const renderedInitialStateList = [];
    let reservationViewOptions;
    const navigation = createNavigationController();
    const reservationController = createReservationController({
        navigation,
        permissions: { canDeleteOperationalData: false },
        renderDashboardTopbar() {},
        renderCheckInFormPanel() {},
        renderCheckOutFormPanel() {},
        renderGuestProfilePanel() {},
        renderGuestFormPanel() {},
        views: {
            renderNewReservationView: (_containerId, options) => {
                reservationViewOptions = options;
                renderedInitialStateList.push(options.initialState);
            },
            renderReservationProfileView() {},
        },
    });
    const reservationFormStateRecord = {
        selectedGuestId: 7,
        fieldValueMap: { checkin: { value: "2026-08-20", checked: false } },
    };

    reservationController.openNewReservationPanel();
    assert.equal(reservationViewOptions.onOpenBooking(42, reservationFormStateRecord), true);
    assert.deepEqual(navigation.current(), {
        name: "reservationProfile",
        params: { bookingId: 42 },
    });
    navigation.back();

    assert.deepEqual(navigation.current(), { name: "reservationForm", params: {} });
    assert.deepEqual(renderedInitialStateList, [null, reservationFormStateRecord]);
});

test("guest history CSS stays horizontally scrollable and cache chain reaches the shell", () => {
    const cssSource = fs.readFileSync(new URL("../css/home.css", import.meta.url), "utf8");
    const controllerSource = fs.readFileSync(
            new URL("../js/controllers/reservationController.js", import.meta.url),
            "utf8"
    );
    const uiControllerSource = fs.readFileSync(
            new URL("../js/controllers/UICOntroller.js", import.meta.url),
            "utf8"
    );
    assert.match(cssSource, /\.guest-rating-history-table-wrap\s*\{[^}]*overflow-x:\s*auto/s);
    assert.match(controllerSource, /newReservationView\.js\?v=2026-08-20-reservation-room-loading-fix/);
    assert.match(uiControllerSource, /guestController\.js\?v=2026-08-13-guest-rating-history/);
    assert.match(uiControllerSource, /reservationController\.js\?v=2026-08-13-history-arrow-decoration/);
});
