const CENT_DECIMAL_PATTERN = /^\d+(?:[.,]\d{0,2})?$/;

export function toCents(value) {
    const normalizedValue = String(value ?? "").trim().replace(",", ".");
    if (!normalizedValue || !CENT_DECIMAL_PATTERN.test(normalizedValue)) {
        return null;
    }

    const [wholePart, decimalPart = ""] = normalizedValue.split(".");
    const cents = Number(`${wholePart}${decimalPart.padEnd(2, "0")}`);
    return Number.isSafeInteger(cents) ? cents : null;
}

export function centsToDecimal(cents) {
    if (!Number.isSafeInteger(cents) || cents < 0) {
        return null;
    }

    return (cents / 100).toFixed(2);
}

export function formatCents(cents) {
    if (!Number.isSafeInteger(cents)) {
        return "Indisponível";
    }

    return new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL",
    }).format(cents / 100);
}

export function calculateAllocationSummary(totalCents, allocationCentsList) {
    if (!Number.isSafeInteger(totalCents) || totalCents < 0) {
        return {
            totalCents: null,
            allocatedCents: null,
            remainingCents: null,
            state: "unavailable",
        };
    }

    const allocatedCents = allocationCentsList.every(Number.isSafeInteger)
        ? allocationCentsList.reduce((sum, amountCents) => sum + amountCents, 0)
        : null;
    if (!Number.isSafeInteger(allocatedCents)) {
        return {
            totalCents,
            allocatedCents: null,
            remainingCents: null,
            state: "unavailable",
        };
    }

    const remainingCents = totalCents - allocatedCents;
    return {
        totalCents,
        allocatedCents,
        remainingCents,
        state: remainingCents === 0
            ? "complete"
            : remainingCents > 0
                ? "incomplete"
                : "excessive",
    };
}

export function calculateInstallmentPreview(amountCents, installmentsQuantity, firstDueDate) {
    if (!Number.isSafeInteger(amountCents)
            || amountCents <= 0
            || !Number.isInteger(installmentsQuantity)
            || installmentsQuantity < 2
            || installmentsQuantity > 12) {
        return [];
    }

    const baseCents = Math.floor(amountCents / installmentsQuantity);
    const residualCents = amountCents - baseCents * installmentsQuantity;
    return Array.from({ length: installmentsQuantity }, (_, index) => ({
        number: index + 1,
        amountCents: index === installmentsQuantity - 1
            ? baseCents + residualCents
            : baseCents,
        dueDate: addMonths(firstDueDate, index),
    }));
}

export function buildReservationFinancialAllocation(state) {
    const currentPayment = state.currentPayment?.enabled
        ? {
            enabled: true,
            amount: centsToDecimal(state.currentPayment.amountCents),
            method: state.currentPayment.method || null,
            installmentsQuantity: state.currentPayment.method === "CREDIT_CARD"
                    ? state.currentPayment.installmentsQuantity
                    : null,
            received: Boolean(state.currentPayment.received),
        }
        : { enabled: false };
    const downPayment = state.downPayment?.enabled
        ? {
            enabled: true,
            amount: centsToDecimal(state.downPayment.amountCents),
            received: Boolean(state.downPayment.received),
            method: state.downPayment.method || null,
            installmentsQuantity: state.downPayment.method === "CREDIT_CARD"
                    ? state.downPayment.installmentsQuantity
                    : null,
        }
        : { enabled: false };
    const futurePayment = (payment) => payment?.enabled
        ? {
            enabled: true,
            amount: centsToDecimal(payment.amountCents),
            received: Boolean(payment.received),
        }
        : { enabled: false };

    return {
        currentPayment,
        downPayment,
        checkInPayment: futurePayment(state.checkInPayment),
        checkOutPayment: futurePayment(state.checkOutPayment),
    };
}

export function createReservationIdempotencyKey() {
    const randomUuid = globalThis.crypto?.randomUUID?.();
    if (randomUuid) {
        return randomUuid;
    }

    return `reservation-${Date.now()}-${Math.random().toString(36).slice(2, 12)}`;
}

function addMonths(dateValue, monthOffset) {
    if (!dateValue) {
        return null;
    }
    const date = new Date(`${dateValue}T12:00:00`);
    if (Number.isNaN(date.getTime())) {
        return null;
    }
    date.setMonth(date.getMonth() + monthOffset);
    return date.toISOString().slice(0, 10);
}
