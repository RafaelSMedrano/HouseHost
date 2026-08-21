import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

globalThis.localStorage = {
    getItem: () => "reservation-command-token",
    setItem() {},
    removeItem() {},
};
globalThis.location = { protocol: "http:", hostname: "localhost", port: "8080" };

const apiModule = await import("../js/api.js?v=reservation-ftp-command-tests");
const { reconcileUncertainReservationSubmission } = await import(
        "../js/views/newReservationView.js?v=reservation-ftp-command-tests"
);
const reservationViewSource = fs.readFileSync(
        new URL("../js/views/newReservationView.js", import.meta.url),
        "utf8"
);
const reservationProfileSource = fs.readFileSync(
        new URL("../js/views/reservationProfileView.js", import.meta.url),
        "utf8"
);

test("FTP API helpers use authenticated booking summary and reconciliation routes", async () => {
    const requestList = [];
    globalThis.fetch = async (url, options) => {
        requestList.push({ url, options });
        return new Response(JSON.stringify({ status: "success", data: { bookingId: 42 } }), {
            status: 200,
            headers: { "Content-Type": "application/json" },
        });
    };

    await apiModule.findFinancialTransactionPlanByBookingId(42);
    await apiModule.reconcileReservationCreation("reservation-command-42");

    assert.equal(requestList[0].url, "/financial-transaction-plans/booking/42");
    assert.equal(requestList[1].url, "/financial-transaction-plans/commands/reservation/reservation-command-42");
    assert.equal(requestList[0].options.headers.Authorization, "Bearer reservation-command-token");
});

test("uncertain reservation submission reconciles the same idempotence key", async () => {
    globalThis.fetch = async (url) => {
        assert.match(url, /commands\/reservation\/same-command/);
        return new Response(JSON.stringify({
            status: "success",
            data: {
                bookingId: 42,
                financialTransactionPlanSummaryDTO: { totalAmount: 100 },
            },
        }), { status: 200, headers: { "Content-Type": "application/json" } });
    };

    const reconciliation = await reconcileUncertainReservationSubmission(
            "same-command",
            new Error("network interrupted")
    );
    assert.equal(reconciliation.state, "committed");
    assert.equal(reconciliation.outcome.bookingId, 42);
});

test("validation failures do not trigger blind reconciliation", async () => {
    let requestCount = 0;
    globalThis.fetch = async () => {
        requestCount += 1;
        return new Response("", { status: 500 });
    };

    const reconciliation = await reconcileUncertainReservationSubmission(
            "same-command",
            { status: 422, message: "Alocação inválida." }
    );
    assert.equal(reconciliation.state, "unconfirmed");
    assert.equal(reconciliation.message, "Alocação inválida.");
    assert.equal(requestCount, 0);
});

test("reservation submission and profile expose authoritative FTP state", () => {
    assert.match(reservationViewSource, /response\.data\?\.booking \|\| response\.data/);
    assert.match(reservationViewSource, /reconcileUncertainReservationSubmission/);
    assert.match(reservationViewSource, /rotateReservationIdempotencyKey/);
    assert.match(reservationViewSource, /Totais confirmados pelo backend/);
    assert.match(reservationProfileSource, /findFinancialTransactionPlanByBookingId/);
    assert.match(reservationProfileSource, /renderFinancialPlanSummary/);
    assert.match(reservationProfileSource, /canAccessCompleteFinancialProfile/);
    assert.doesNotMatch(reservationViewSource, /localStorage/);
});

test("reservation room loading uses the API timeout contract", () => {
    assert.match(reservationViewSource, /api\.js\?v=2026-08-20-api-request-timeout/);
    assert.match(fs.readFileSync(new URL("../js/api.js", import.meta.url), "utf8"), /API_REQUEST_TIMEOUT_MS/);
    assert.match(fs.readFileSync(new URL("../js/api.js", import.meta.url), "utf8"), /requestController\.abort\(\)/);
});
