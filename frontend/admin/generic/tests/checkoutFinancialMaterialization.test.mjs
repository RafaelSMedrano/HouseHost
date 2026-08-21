import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const { collectCheckOutPayload } = await import(
        "../js/views/checkOperationFormView.js?v=checkout-ftp-materialization-tests"
);
const checkoutSource = fs.readFileSync(
        new URL("../js/views/checkOperationFormView.js", import.meta.url),
        "utf8"
);
const apiSource = fs.readFileSync(new URL("../js/api.js", import.meta.url), "utf8");

function createCheckoutContainer({ structure = "INSTALLMENT", method = "PIX", installments = "4" } = {}) {
    const fieldValueMap = {
        bookingId: "42",
        actualCheckOutAt: "2026-08-20T11:30",
        extraCharges: "25.50",
        pendingAmount: "0",
        performedBy: "Recepção",
        notes: "Checkout concluído",
        status: "COMPLETED",
        checkoutPaymentMethod: method,
        checkoutInstallments: installments,
    };
    return {
        querySelector(selector) {
            if (selector === 'input[name="checkoutPaymentStructure"]:checked') {
                return { value: structure };
            }
            const fieldName = selector.replace("#", "");
            return { value: fieldValueMap[fieldName] || "", checked: false };
        },
    };
}

test("checkout sends one minimized nested FTP materialization command", () => {
    const payload = collectCheckOutPayload(
            createCheckoutContainer(),
            {
                scheduledFinancialComponent: {
                    id: 88,
                    type: "PLAN_CHECK_OUT_PAYMENT",
                    amount: "180.00",
                    status: "WAITING",
                },
                paymentCommandIdempotencyKey: "checkout-command-88",
            }
    );

    assert.deepEqual(payload.paymentMaterialization, {
        structure: "INSTALLMENT",
        method: "PIX",
        installmentsQuantity: 4,
        idempotencyKey: "checkout-command-88",
    });
    ["planId", "scheduledFinancialTransactionId", "transactionId", "sourceType", "sourceId"].forEach(
            (protectedMember) => assert.equal(Object.hasOwn(payload.paymentMaterialization, protectedMember), false)
    );
    assert.equal(payload.extraCharges, 25.5);
    assert.equal(payload.pendingAmount, 0);
    assert.equal(payload.rating.observations, null);
});

test("checkout without an eligible scheduled payment remains independent", () => {
    const payload = collectCheckOutPayload(createCheckoutContainer(), {
        scheduledFinancialComponent: null,
        paymentCommandIdempotencyKey: "checkout-command-absence",
    });
    assert.equal(payload.paymentMaterialization, null);
    assert.equal(payload.extraCharges, 25.5);
    assert.equal(payload.pendingAmount, 0);
});

test("checkout FTP flow preserves rating and extra-charge boundaries", () => {
    const checkoutStart = checkoutSource.indexOf("export function renderCheckOutFormView");
    const checkoutMarkupAndFlow = checkoutSource.slice(checkoutStart);
    assert.match(checkoutSource, /PLAN_CHECK_OUT_PAYMENT/);
    assert.match(checkoutMarkupAndFlow, /paymentMaterialization/);
    assert.match(checkoutMarkupAndFlow, /reconcileFinancialReplacement/);
    assert.match(checkoutMarkupAndFlow, /checkoutPaymentStructure/);
    assert.match(checkoutMarkupAndFlow, /extraCharges/);
    assert.match(checkoutMarkupAndFlow, /pendingAmountPaid/);
    assert.match(checkoutMarkupAndFlow, /buildCheckOutRatingControlsMarkup/);
    assert.doesNotMatch(checkoutMarkupAndFlow, /deleteScheduled|deleteFinancial|createFinancialTransaction/);
    assert.match(apiSource, /commands\/replacement/);
});

test("checkout FTP state exposes accessible loading, conflict and success outcomes", () => {
    assert.match(checkoutSource, /id="checkout-financial-state" role="status" aria-live="polite"/);
    assert.match(checkoutSource, /O pagamento agendado mudou/);
    assert.match(checkoutSource, /Confirmo a materialização do pagamento agendado/);
    assert.match(checkoutSource, /Pagamento definitivo criado/);
    assert.match(checkoutSource, /state\.submitting = true/);
    assert.match(checkoutSource, /setAttribute\("aria-busy", "true"\)/);
});
