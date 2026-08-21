import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const { collectCheckInPayload } = await import(
        "../js/views/checkOperationFormView.js?v=checkin-ftp-materialization-tests"
);
const checkOperationFormSource = fs.readFileSync(
        new URL("../js/views/checkOperationFormView.js", import.meta.url),
        "utf8"
);
const homeCssSource = fs.readFileSync(new URL("../css/home.css", import.meta.url), "utf8");

function createCheckInContainer({ structure = "INSTALLMENT", method = "PIX", installments = "3" } = {}) {
    const fieldValueMap = {
        bookingId: "42",
        adults: "2",
        children: "1",
        pets: "0",
        vehiclePlate: "ABC1234",
        vehicleModel: "Hatch",
        performedBy: "Recepção",
        notes: "Chegou no horário",
        status: "COMPLETED",
        checkinPaymentMethod: method,
        checkinInstallments: installments,
    };
    return {
        querySelector(selector) {
            if (selector === 'input[name="checkinPaymentStructure"]:checked') {
                return { value: structure };
            }
            if (selector.startsWith("#")) {
                const fieldName = selector.slice(1);
                return { value: fieldValueMap[fieldName] || "", checked: false };
            }
            return null;
        },
    };
}

test("check-in payload sends one nested materialization command without protected identity", () => {
    const payload = collectCheckInPayload(
            createCheckInContainer(),
            {
                scheduledFinancialComponent: {
                    id: 77,
                    type: "PLAN_CHECK_IN_PAYMENT",
                    amount: "100.00",
                    status: "WAITING",
                },
                paymentCommandIdempotencyKey: "checkin-command-77",
            }
    );

    assert.deepEqual(payload.paymentMaterialization, {
        structure: "INSTALLMENT",
        method: "PIX",
        installmentsQuantity: 3,
        idempotencyKey: "checkin-command-77",
    });
    ["planId", "scheduledFinancialTransactionId", "transactionId", "sourceType", "sourceId"].forEach(
            (protectedMember) => assert.equal(Object.hasOwn(payload.paymentMaterialization, protectedMember), false)
    );
});

test("check-in without an eligible scheduled component sends no fabricated payment", () => {
    const payload = collectCheckInPayload(createCheckInContainer(), {
        scheduledFinancialComponent: null,
        paymentCommandIdempotencyKey: "checkin-command-absence",
    });
    assert.equal(payload.paymentMaterialization, null);
});

test("check-in materialization exposes accessible loading, conflict, confirmation and success states", () => {
    assert.match(checkOperationFormSource, /Carregando pagamento agendado/);
    assert.match(checkOperationFormSource, /Nenhum pagamento FTP está agendado/);
    assert.match(checkOperationFormSource, /O pagamento agendado mudou/);
    assert.match(checkOperationFormSource, /Confirmo a materialização do pagamento agendado/);
    assert.match(checkOperationFormSource, /reconcileFinancialReplacement/);
    assert.match(checkOperationFormSource, /Pagamento definitivo criado/);
    assert.match(checkOperationFormSource, /paymentCommandIdempotencyKey/);
    assert.match(checkOperationFormSource, /createCheckIn\(payload\)/);
    assert.match(homeCssSource, /\.checkin-financial-authoritative/);
    assert.match(homeCssSource, /\.checkin-financial-controls/);
});

test("checkout remains independent from check-in FTP materialization", () => {
    const checkoutStart = checkOperationFormSource.indexOf("export function renderCheckOutFormView");
    assert.equal(checkoutStart >= 0, true);
    const checkoutSource = checkOperationFormSource.slice(
            checkoutStart,
            checkOperationFormSource.indexOf("async function bindCheckInForm")
    );
    assert.match(checkoutSource, /extraCharges/);
    assert.match(checkoutSource, /pendingAmountPaid/);
    assert.match(checkoutSource, /buildCheckOutRatingControlsMarkup/);
    assert.doesNotMatch(checkoutSource, /checkinPaymentMethod/);
});
