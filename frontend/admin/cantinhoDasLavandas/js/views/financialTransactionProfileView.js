import {
    findAllCashiers,
    findFinancialTransactionById,
} from "../api.js?v=2026-06-15-finance-profiles";

export function renderFinancialTransactionProfileView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main finance-main">
  <div class="content reservation-profile-page financial-transaction-profile-page">
    <div class="reservation-profile-loading">Carregando transação financeira...</div>
  </div>
  <div id="financial-transaction-toast" class="booking-toast"><i class="ti ti-alert-circle"></i><span></span></div>
</div>`;

    loadTransaction(container, options);
}

async function loadTransaction(container, options) {
    try {
        const [transactionResponse, cashiersResponse] = await Promise.all([
            findFinancialTransactionById(options.transactionId),
            findAllCashiers(),
        ]);
        renderProfile(container, transactionResponse.data, cashiersResponse.data || [], options);
    } catch (error) {
        container.querySelector(".financial-transaction-profile-page").innerHTML = `
          <div class="rooms-empty rooms-error">${escapeHtml(error.message || "Não foi possível carregar a transação financeira.")}</div>
        `;
        showToast(container, error.message || "Não foi possível carregar a transação financeira.");
    }
}

function renderProfile(container, transaction, cashiers, options) {
    const cashierById = new Map(cashiers.map((cashier) => [Number(cashier.id), cashier]));
    const bookingId = bookingIdFor(transaction);
    const senderName = partyName(transaction, "sender", cashierById);
    const receiverName = partyName(transaction, "receiver", cashierById);

    container.querySelector(".financial-transaction-profile-page").innerHTML = `
      <div class="reservation-profile-header">
        <button class="dashboard-back-btn reservation-profile-back" type="button"><i class="ti ti-arrow-left"></i> Voltar</button>
        <div class="reservation-profile-title">
          <span>Transação financeira #${escapeHtml(transaction.id)}</span>
          <strong>${escapeHtml(transaction.description || "Movimento financeiro")}</strong>
        </div>
        <div class="reservation-profile-header-actions">
          ${statusBadge(transaction.status)}
        </div>
      </div>

      <div class="reservation-profile-grid financial-transaction-profile-grid">
        <section class="reservation-profile-panel hero">
          <div class="profile-panel-label">Resumo</div>
          <div class="financial-transaction-value">${formatMoney(transaction.amount)}</div>
          ${infoRow("Data", formatDate(transaction.transactionDate))}
          ${infoRow("Tipo", typeText(transaction.type))}
          ${infoRow("Método", methodText(transaction.method))}
        </section>

        <section class="reservation-profile-panel">
          <div class="profile-panel-label">Participantes</div>
          ${infoRow("Pagante", senderName)}
          ${infoRow("Tipo do pagante", partyTypeText(transaction.senderType))}
          ${infoRow("ID do pagante", transaction.senderId)}
          ${infoRow("Recebedor", receiverName)}
          ${infoRow("Tipo do recebedor", partyTypeText(transaction.receiverType))}
          ${infoRow("ID do recebedor", transaction.receiverId)}
        </section>

        <section class="reservation-profile-panel">
          <div class="profile-panel-label">Origem</div>
          ${bookingId ? infoRowHtml("Reserva", reservationLink(bookingId)) : infoRow("Origem", sourceTypeText(transaction.sourceType))}
          ${infoRow("ID da origem", transaction.sourceId)}
          ${infoRow("Classe", transactionClassText(transaction.transactionClass))}
          ${installmentRows(transaction)}
        </section>

        <section class="reservation-profile-panel wide">
          <div class="profile-panel-label">Valores da transação</div>
          <div class="financial-transaction-amounts">
            ${infoRow("Valor total", formatMoney(transaction.amount))}
            ${infoRow("Entrada", formatMoney(transaction.entryAmount))}
            ${infoRow("Saída", formatMoney(transaction.expenseAmount))}
            ${infoRow("Status", statusText(transaction.status))}
          </div>
        </section>

        <section class="reservation-profile-panel wide">
          <div class="profile-panel-label">Descrição</div>
          <p class="financial-transaction-description">${escapeHtml(transaction.description || "Nenhuma descrição registrada.")}</p>
        </section>

        <section class="reservation-profile-panel wide muted">
          ${infoRow("Criada em", formatDateTime(transaction.createdAt))}
          ${infoRow("Atualizada em", formatDateTime(transaction.updatedAt))}
        </section>
      </div>
    `;

    container.querySelector(".reservation-profile-back")?.addEventListener("click", () => options.onBack?.());
    container.querySelector("[data-open-booking]")?.addEventListener("click", () => {
        options.onOpenReservation?.(Number(bookingId));
    });
}

function reservationLink(bookingId) {
    return `<button class="reservation-info-link" type="button" data-open-booking="${escapeHtml(bookingId)}">Reserva #${escapeHtml(bookingId)}</button>`;
}

function bookingIdFor(transaction) {
    return String(transaction.sourceType || "").toUpperCase() === "BOOKING"
        ? transaction.sourceId
        : null;
}

function partyName(transaction, side, cashierById) {
    const type = String(transaction[`${side}Type`] || "").toUpperCase();
    const id = Number(transaction[`${side}Id`]);

    if (type === "GUEST") {
        return `Hóspede #${id}`;
    }

    if (type === "CASHIER") {
        return cashierById.get(id)?.name || `Caixa #${id}`;
    }

    return id ? `${type || "Participante"} #${id}` : "-";
}

function installmentRows(transaction) {
    if (transaction.installmentNumber) {
        return [
            infoRow("Parcela", `${transaction.installmentNumber}/${transaction.totalInstallments || "-"}`),
            infoRow("Status da parcela", statusText(transaction.installmentStatus)),
        ].join("");
    }

    if (transaction.installmentsQuantity) {
        return [
            infoRow("Parcelas", transaction.installmentsQuantity),
            infoRow("Status do plano", statusText(transaction.installmentPlanStatus)),
        ].join("");
    }

    return "";
}

function infoRow(label, value) {
    return `
      <div class="reservation-info-row">
        <span>${escapeHtml(label)}</span>
        <strong>${escapeHtml(value ?? "-")}</strong>
      </div>
    `;
}

function infoRowHtml(label, value) {
    return `
      <div class="reservation-info-row">
        <span>${escapeHtml(label)}</span>
        <strong>${value}</strong>
      </div>
    `;
}

function statusBadge(status) {
    const normalized = String(status || "-").toUpperCase();
    return `<span class="finance-status status-${normalized.toLowerCase()}">${escapeHtml(statusText(normalized))}</span>`;
}

function statusText(status) {
    return {
        WAITING: "Em espera",
        SETTLED: "Liquidada",
        PAID: "Paga",
        ON_TIME: "No prazo",
        PARTIALLY_REALIZED: "Parcial",
        LATE: "Atrasada",
        NOT_REALIZED: "Não realizada",
        CANCELED: "Cancelada",
    }[String(status || "").toUpperCase()] || status || "-";
}

function typeText(type) {
    return {
        ENTRY: "Entrada",
        EXPENSE: "Saída",
        TRANSFER: "Transferência",
    }[String(type || "").toUpperCase()] || type || "-";
}

function methodText(method) {
    return {
        PIX: "Pix",
        CREDIT_CARD: "Cartão de crédito",
        DEBIT_CARD: "Cartão de débito",
        CASH: "Dinheiro",
        BANK_TRANSFER: "Transferência bancária",
        BOOKING: "Booking",
        AIRBNB: "Airbnb",
    }[String(method || "").toUpperCase()] || method || "-";
}

function sourceTypeText(sourceType) {
    return {
        MANUAL: "Manual",
        BOOKING: "Reserva",
        STAY: "Estadia",
        CHECK_IN: "Check-in",
        CHECK_OUT: "Check-out",
        INSTALLMENT: "Parcela",
        GUEST: "Hóspede",
    }[String(sourceType || "").toUpperCase()] || sourceType || "-";
}

function partyTypeText(type) {
    return {
        CASHIER: "Caixa",
        GUEST: "Hóspede",
    }[String(type || "").toUpperCase()] || type || "-";
}

function transactionClassText(transactionClass) {
    return {
        FinancialTransaction: "Transação financeira",
        InstallmentPlanTransaction: "Plano parcelado",
        InstallmentTransaction: "Parcela",
    }[transactionClass] || transactionClass || "-";
}

function formatDate(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(`${value}T00:00:00`);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleDateString("pt-BR");
}

function formatDateTime(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("pt-BR");
}

function formatMoney(value) {
    return `R$ ${(Number(value) || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`;
}

function showToast(container, message) {
    const toast = container.querySelector("#financial-transaction-toast");
    toast.querySelector("span").textContent = message;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2600);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
