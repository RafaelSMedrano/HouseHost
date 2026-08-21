import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const allocationModule = await import("../js/financialAllocation.js?v=reservation-ftp-allocation-tests");
const reservationViewSource = fs.readFileSync(
        new URL("../js/views/newReservationView.js", import.meta.url),
        "utf8"
);
const homeCssSource = fs.readFileSync(
        new URL("../css/home.css", import.meta.url),
        "utf8"
);

test("allocation summary uses integer cents and distinguishes complete, incomplete and excessive states", () => {
    assert.deepEqual(allocationModule.calculateAllocationSummary(10000, [2500, 7500]), {
        totalCents: 10000,
        allocatedCents: 10000,
        remainingCents: 0,
        state: "complete",
    });
    assert.equal(allocationModule.calculateAllocationSummary(10000, [2500]).state, "incomplete");
    assert.equal(allocationModule.calculateAllocationSummary(10000, [10001]).state, "excessive");
    assert.equal(allocationModule.toCents("10,05"), 1005);
    assert.equal(allocationModule.toCents("10.999"), null);
});

test("residual cents are assigned only to the last installment", () => {
    const installmentPreviewList = allocationModule.calculateInstallmentPreview(10001, 3, "2026-08-20");
    assert.deepEqual(installmentPreviewList.map(({ amountCents }) => amountCents), [3333, 3333, 3335]);
    assert.deepEqual(installmentPreviewList.map(({ dueDate }) => dueDate), [
        "2026-08-20",
        "2026-09-20",
        "2026-10-20",
    ]);
});

test("reservation payment controls reveal card installments and require payment confirmation", () => {
    assert.match(reservationViewSource, /id="valorDiaria"/);
    assert.match(reservationViewSource, /id="desconto"/);
    assert.match(reservationViewSource, /id="downPaymentEnabled"/);
    assert.match(reservationViewSource, /id="checkInPaymentEnabled"/);
    assert.match(reservationViewSource, /id="checkOutPaymentEnabled"/);
    assert.match(reservationViewSource, /id="currentPaymentMethod"/);
    assert.match(
        reservationViewSource,
        /<div class="booking-fields financial-allocation-fields financial-current-payment"[^>]*data-financial-fields="currentPayment"/,
    );
    assert.doesNotMatch(reservationViewSource, /financial-allocation-group financial-current-payment/);
    assert.doesNotMatch(reservationViewSource, /<legend>Pagamento no momento<\/legend>/);
    assert.match(reservationViewSource, /<span>Valor do pagamento<\/span>/);
    assert.match(reservationViewSource, /id="currentPaymentAmount"[^>]*readonly/);
    assert.match(reservationViewSource, /paymentAllocation: buildReservationFinancialAllocation/);
    assert.match(reservationViewSource, /id="downPaymentInstallments"/);
    assert.match(reservationViewSource, /id="checkInPaymentReceived"/);
    assert.match(reservationViewSource, /id="checkOutPaymentReceived"/);
    assert.match(reservationViewSource, /id="currentPaymentInstallments"/);
    assert.match(reservationViewSource, /id="currentPaymentReceived"/);
    assert.match(reservationViewSource, /currentPaymentDisabled: summary\.state === "complete"/);
    assert.doesNotMatch(
        reservationViewSource,
        /currentPaymentMethod"\)\.disabled = allocationSummary\.currentPaymentCents <= 0/,
    );
    assert.match(
        homeCssSource,
        /\[data-current-installment-fields\]\[hidden\]\s*\{\s*display:\s*none;/,
    );
    assert.ok(
        reservationViewSource.indexOf('id="downPaymentInstallments"')
            < reservationViewSource.indexOf('id="downPaymentReceived"'),
        "signal installments must appear before payment confirmation",
    );
    assert.ok(
        reservationViewSource.indexOf('id="currentPaymentAmount"')
            < reservationViewSource.indexOf('id="currentPaymentMethod"')
            && reservationViewSource.indexOf('id="currentPaymentInstallments"')
                < reservationViewSource.indexOf('id="currentPaymentReceived"'),
        "current payment must follow the signal field order",
    );
    assert.doesNotMatch(reservationViewSource, /name="downPaymentStructure"/);
    assert.doesNotMatch(reservationViewSource, /id="downPaymentDate"/);
    assert.doesNotMatch(reservationViewSource, /id="paymentMethod"/);
    assert.doesNotMatch(reservationViewSource, /id="installments"/);
    assert.doesNotMatch(reservationViewSource, /paidAmount/);
});

test("allocation payload excludes protected financial identity and preserves idempotence key", () => {
    const allocation = allocationModule.buildReservationFinancialAllocation({
        downPayment: {
            enabled: true,
            amountCents: 1200,
            received: true,
            method: "PIX",
        },
        checkInPayment: { enabled: true, amountCents: 8800, received: true },
        checkOutPayment: { enabled: false, amountCents: null },
    });
    assert.deepEqual(allocation, {
        currentPayment: { enabled: false },
        downPayment: {
            enabled: true,
            amount: "12.00",
            received: true,
            method: "PIX",
            installmentsQuantity: null,
        },
        checkInPayment: { enabled: true, amount: "88.00", received: true },
        checkOutPayment: { enabled: false },
    });
    assert.equal(Object.hasOwn(allocation.downPayment, "transactionId"), false);
});

test("current payment receives the unallocated amount and selected method", () => {
    const allocation = allocationModule.buildReservationFinancialAllocation({
        currentPayment: { enabled: true, amountCents: 8800, method: "CASH", received: true },
        downPayment: { enabled: false },
        checkInPayment: { enabled: true, amountCents: 1200 },
        checkOutPayment: { enabled: false },
    });
    assert.deepEqual(allocation.currentPayment, {
        enabled: true,
        amount: "88.00",
        method: "CASH",
        installmentsQuantity: null,
        received: true,
    });
});
