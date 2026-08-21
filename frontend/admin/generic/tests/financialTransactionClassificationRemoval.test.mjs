import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const financialTransactionProfileSource = fs.readFileSync(
    new URL("../js/views/financialTransactionProfileView.js", import.meta.url),
    "utf8",
);

test("financial transaction profile retains type without obsolete directional amounts", () => {
    assert.equal(financialTransactionProfileSource.includes("transaction.type"), true);
    assert.equal(financialTransactionProfileSource.includes("transaction.entryAmount"), false);
    assert.equal(financialTransactionProfileSource.includes("transaction.expenseAmount"), false);
    assert.match(financialTransactionProfileSource, /infoRow\("Valor", formatMoney\(transaction\.amount\)\)/);
});

test("financial transaction profile uses the authoritative plan taxonomy", () => {
    [
        "STANDARD",
        "PLAN_DOWN_PAYMENT",
        "PLAN_CHECK_IN_PAYMENT",
        "PLAN_CHECK_OUT_PAYMENT",
        "PLAN_TRANSACTION",
        "INSTALLMENT_PLAN_BLOCK",
        "INSTALLMENT_TRANSACTION",
    ].forEach((type) => assert.equal(financialTransactionProfileSource.includes(type), true));

    [
        "PLAN_SIGNAL_TRANSACTIONAL",
        "PLAN_TRANSACTIONAL:",
        "INSTALLTMENT_PLAN_TRANSACTION",
    ].forEach((type) => assert.equal(financialTransactionProfileSource.includes(type), false));
});
