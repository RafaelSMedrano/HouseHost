import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const {
    buildRatingRowMarkup,
    buildReadOnlyStarsMarkup,
    renderRatingsView,
} = await import("../js/views/ratingsView.js?v=ratings-list-page-tests");

const ratingsViewSource = fs.readFileSync(
        new URL("../js/views/ratingsView.js", import.meta.url),
        "utf8"
);
const homeCssSource = fs.readFileSync(new URL("../css/home.css", import.meta.url), "utf8");

const ratingSummary = {
    bookingId: 42,
    guestId: 7,
    guestName: "Ana & <script>alert(1)</script>",
    bookingCheckInDate: "2026-08-10",
    bookingCheckOutDate: "2026-08-13",
    evaluatedAt: "2026-08-13T11:30:00",
    checkInProcedureScore: 5,
    checkOutProcedureScore: 4,
    accommodationCleanlinessScore: 3,
    teamCommunicationScore: 2,
    locationScore: 1,
    comfortScore: 4,
    observations: "Ótima estadia <img src=x onerror=alert(1)>",
};

test("ratings table exposes all semantic columns in the required order", () => {
    const container = renderIntoContainer({
        ratingSummaryDTOList: [ratingSummary],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
    });
    const headingList = [...container.innerHTML.matchAll(/<th scope="col">([^<]+)<\/th>/g)]
            .map((headingMatch) => headingMatch[1]);

    assert.deepEqual(headingList, [
        "Hóspede",
        "Reserva",
        "Avaliada em",
        "Check-in",
        "Checkout",
        "Limpeza",
        "Comunicação",
        "Localização",
        "Conforto",
        "Observações",
    ]);
    assert.match(container.innerHTML, /<table class="ratings-table">/);
    assert.match(container.innerHTML, /role="region"[^>]*aria-label="Avaliações de hospedagem"[^>]*tabindex="0"/);
});

test("read-only score renders five stars and accessible selected text", () => {
    const starsMarkup = buildReadOnlyStarsMarkup(4);

    assert.match(starsMarkup, /aria-label="4 de 5"/);
    assert.equal((starsMarkup.match(/read-only-star filled/g) || []).length, 4);
    assert.equal((starsMarkup.match(/class="read-only-star(?: filled)?"/g) || []).length, 5);
    assert.match(starsMarkup, /read-only-score">4 de 5/);
});

test("rating row escapes backend text and links only guest and booking", () => {
    const rowMarkup = buildRatingRowMarkup(ratingSummary);
    const guestLinkPosition = rowMarkup.indexOf("data-open-guest");
    const bookingLinkPosition = rowMarkup.indexOf("data-open-booking");
    const evaluationCellPosition = rowMarkup.indexOf("<td><time");

    assert.equal((rowMarkup.match(/<a /g) || []).length, 2);
    assert.ok(guestLinkPosition >= 0 && guestLinkPosition < bookingLinkPosition);
    assert.ok(bookingLinkPosition < evaluationCellPosition);
    assert.match(rowMarkup, /data-open-guest="7"/);
    assert.match(rowMarkup, /data-open-booking="42"/);
    assert.doesNotMatch(rowMarkup, /<tr[^>]*(onclick|data-open|tabindex)/);
    assert.doesNotMatch(rowMarkup, /<script>|<img/);
    assert.match(rowMarkup, /Ana &amp; &lt;script&gt;alert\(1\)&lt;\/script&gt;/);
    assert.match(rowMarkup, /&lt;img src=x onerror=alert\(1\)&gt;/);
});

test("guest and booking links prevent hash navigation and emit only semantic callbacks", () => {
    const guestLink = interactiveElement({ openGuest: "7" });
    const bookingLink = interactiveElement({ openBooking: "42" });
    const openedGuestIdList = [];
    const openedBookingIdList = [];
    const container = {
        innerHTML: "",
        querySelectorAll(selector) {
            return {
                "[data-open-guest]": [guestLink],
                "[data-open-booking]": [bookingLink],
                "[data-ratings-page]": [],
            }[selector] || [];
        },
    };
    const originalDocument = globalThis.document;
    globalThis.document = { getElementById: () => container };

    try {
        renderRatingsView("main", {
            pageData: {
                ratingSummaryDTOList: [ratingSummary],
                page: 0,
                totalElements: 1,
                totalPages: 1,
            },
            onOpenGuest: (guestId) => openedGuestIdList.push(guestId),
            onOpenBooking: (bookingId) => openedBookingIdList.push(bookingId),
        });
        guestLink.activate();
        bookingLink.activate();
    } finally {
        globalThis.document = originalDocument;
    }

    assert.equal(guestLink.defaultPrevented, true);
    assert.equal(bookingLink.defaultPrevented, true);
    assert.deepEqual(openedGuestIdList, ["7"]);
    assert.deepEqual(openedBookingIdList, ["42"]);
});

test("ratings are presented newest first without mutating the backend page", () => {
    const ratingSummaryList = [
        { ...ratingSummary, bookingId: 1, evaluatedAt: "2026-08-11T10:00:00" },
        { ...ratingSummary, bookingId: 2, evaluatedAt: "2026-08-13T10:00:00" },
    ];
    const container = renderIntoContainer({
        ratingSummaryDTOList: ratingSummaryList,
        page: 0,
        totalElements: 2,
        totalPages: 1,
    });

    assert.ok(container.innerHTML.indexOf("Reserva #2") < container.innerHTML.indexOf("Reserva #1"));
    assert.equal(ratingSummaryList[0].bookingId, 1);
});

test("pagination is bounded and exposes named controls", () => {
    const container = renderIntoContainer({
        ratingSummaryDTOList: [ratingSummary],
        page: 1,
        size: 20,
        totalElements: 45,
        totalPages: 3,
    });

    assert.match(container.innerHTML, /aria-label="Paginação de avaliações"/);
    assert.match(container.innerHTML, /data-ratings-page="0"[^>]*aria-label="Página anterior"/);
    assert.match(container.innerHTML, /Página 2 de 3/);
    assert.match(container.innerHTML, /data-ratings-page="2"[^>]*aria-label="Próxima página"/);
});

test("ratings table keeps horizontal overflow and no rating detail action", () => {
    assert.match(homeCssSource, /\.ratings-table-region \{[\s\S]*?overflow-x: auto;/);
    assert.match(homeCssSource, /\.ratings-table \{[\s\S]*?min-width: 1420px;/);
    assert.match(homeCssSource, /\.ratings-related-link:focus-visible/);
    assert.doesNotMatch(ratingsViewSource, /ratingProfile|ratingDetail|openRating|data-open-rating/);
});

function renderIntoContainer(pageData) {
    const originalDocument = globalThis.document;
    const container = {
        innerHTML: "",
        querySelectorAll: () => [],
    };
    globalThis.document = { getElementById: () => container };
    try {
        assert.equal(renderRatingsView("main", { pageData }), true);
        return container;
    } finally {
        globalThis.document = originalDocument;
    }
}

function interactiveElement(dataset) {
    let clickListener;
    return {
        dataset,
        defaultPrevented: false,
        addEventListener(eventName, listener) {
            if (eventName === "click") {
                clickListener = listener;
            }
        },
        activate() {
            clickListener({
                preventDefault: () => {
                    this.defaultPrevented = true;
                },
            });
        },
    };
}
