import { findAllFinancialTransactions, findBookingById, findFinancialTransactionPlanByBookingId, settleFinancialTransaction } from "../api.js?v=2026-08-20-reservation-ftp-command";

const statusLabels = {
    CONFIRMED: "Confirmada",
    UNCONFIRMED: "Não confirmada",
    CANCELED: "Cancelada",
    IN_STAY: "Em estadia",
    FINISHED: "Finalizada",
};

export function renderReservationProfileView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main reservations-main">
  <div class="content reservation-profile-page">
    <div class="reservation-profile-loading" role="status" aria-live="polite">Carregando reserva...</div>
  </div>
  <div id="reservation-profile-toast" class="booking-toast"><i class="ti ti-alert-circle"></i><span></span></div>
</div>`;

    loadReservation(container, options);
}

async function loadReservation(container, options) {
    try {
        const [response, transactionsResponse, financialPlanResult] = await Promise.all([
            findBookingById(options.bookingId),
            findAllFinancialTransactions(),
            findFinancialTransactionPlanByBookingId(options.bookingId)
                    .then((financialPlanResponse) => ({ data: financialPlanResponse.data }))
                    .catch(() => ({ data: null })),
        ]);
        renderProfile(
                container,
                response.data,
                transactionsResponse.data || [],
                financialPlanResult.data,
                options
        );
    } catch (error) {
        container.querySelector(".reservation-profile-page").innerHTML = `
          <div class="rooms-empty rooms-error" role="alert">
            <p>${escapeHtml(error.message || "Não foi possível carregar a reserva.")}</p>
            <button class="dashboard-back-btn reservation-profile-back" type="button" aria-label="Voltar">Voltar</button>
          </div>
        `;
        container.querySelector(".reservation-profile-back")?.addEventListener("click", () => options.onBack?.());
        showToast(container, error.message || "Não foi possível carregar a reserva.");
    }
}

function renderProfile(container, reservation, transactions, financialTransactionPlan, options) {
    const status = String(reservation.status || "UNCONFIRMED").toUpperCase();
    const nights = calculateNights(reservation.checkInDate, reservation.checkOutDate);
    const balance = (Number(reservation.totalAmount) || 0) - (Number(reservation.paidAmount) || 0);
    const paymentStatus = reservation.paymentStatusLabel || getPaymentStatus(reservation);
    const paymentStatusClass = normalizePaymentStatus(reservation.paymentStatus);
    const pendingTransactions = getReservationPayableTransactions(reservation, transactions);
    const canPay = normalizePaymentStatus(reservation.paymentStatus) !== "paid" && pendingTransactions.length > 0;

    container.querySelector(".reservation-profile-page").innerHTML = `
      <div class="reservation-profile-header">
        <button class="dashboard-back-btn reservation-profile-back" type="button" aria-label="Voltar"><i class="ti ti-arrow-left" aria-hidden="true"></i> Voltar</button>
        <div class="reservation-profile-title">
          <span>Reserva #${escapeHtml(reservation.id)}</span>
          <button class="reservation-profile-guest-link" type="button" data-open-guest="${escapeHtml(reservation.guestId)}">${escapeHtml(reservation.guestName || "-")}</button>
        </div>
        <div class="reservation-profile-header-actions">
          <button class="reservation-edit-button" type="button" data-edit-reservation="${escapeHtml(reservation.id)}"><i class="ti ti-pencil"></i> Editar reserva</button>
          <span class="reservation-status status-${normalizeStatus(status)}"><span></span>${escapeHtml(statusLabels[status] || status)}</span>
        </div>
      </div>

      <div class="reservation-profile-grid">
        <section class="reservation-profile-panel hero">
          <div class="profile-panel-label">Hospedagem</div>
          <div class="profile-stay-main">
            <div>
              <span>Check-in</span>
              <strong>${formatDate(reservation.checkInDate)}</strong>
            </div>
            <i class="ti ti-arrow-right"></i>
            <div>
              <span>Check-out</span>
              <strong>${formatDate(reservation.checkOutDate)}</strong>
            </div>
          </div>
          <div class="profile-stay-meta">
            <div><span>Noites</span><strong>${nights}</strong></div>
            <div><span>Quarto</span><strong>${escapeHtml(reservation.roomNumber || "-")}</strong></div>
            <div><span>Origem</span><strong>${escapeHtml(reservation.origin || "-")}</strong></div>
          </div>
          <div class="profile-stay-payment">
            <span>Valor total</span>
            <strong>${formatMoney(reservation.totalAmount)}</strong>
          </div>
          <div class="profile-stay-payment">
            <span>Status do pagamento</span>
            <strong><span class="reservation-payment-status ${paymentStatusClass}">${escapeHtml(paymentStatus)}</span></strong>
          </div>
          ${canPay ? '<button class="guest-payment-cta" type="button" data-go-reservation-payments><i class="ti ti-cash"></i> Efetuar pagamento</button>' : ""}
        </section>

        <section class="reservation-profile-panel">
          <div class="profile-panel-label">Hóspede</div>
          ${infoRowHtml("Nome", guestLink(reservation))}
          ${infoRow("ID do hóspede", reservation.guestId)}
          ${infoRow("Adultos", reservation.adults)}
          ${infoRow("Crianças", reservation.children)}
          ${infoRow("Pets", reservation.pets)}
        </section>

        <section class="reservation-profile-panel">
          <div class="profile-panel-label">Financeiro</div>
          ${infoRow("Valor total", formatMoney(reservation.totalAmount))}
          ${infoRow("Diária", formatMoney(reservation.dailyRate))}
          ${infoRow("Desconto", formatMoney(reservation.discount))}
          ${infoRow("Pago", formatMoney(reservation.paidAmount))}
          ${infoRow("Saldo", formatMoney(balance))}
          ${infoRow("Pagamento", reservation.paymentMethod)}
          ${infoRow("Parcelas", reservation.installments)}
          ${infoRow("Data pagamento", formatDate(reservation.paymentDate))}
        </section>

        ${renderFinancialPlanSummary(financialTransactionPlan, options)}

        ${canPay ? `
          <section id="reservation-payment-transactions" class="reservation-profile-panel wide">
            <div class="guest-payment-header">
              <div>
                <div class="profile-panel-label">Pagamentos da reserva</div>
                <p>Transacoes pendentes vinculadas a esta reserva.</p>
              </div>
              <span>${pendingTransactions.length} pendencia${pendingTransactions.length === 1 ? "" : "s"}</span>
            </div>
            <div class="guest-payment-list">
              ${pendingTransactions.map(renderPaymentTransaction).join("")}
            </div>
          </section>
        ` : ""}

        <section class="reservation-profile-panel wide">
          <div class="profile-panel-label">Observações</div>
          <div class="profile-notes">
            <div>
              <span>Pedidos especiais</span>
              <p>${escapeHtml(reservation.specialRequests || "Nenhum pedido especial registrado.")}</p>
            </div>
            <div>
              <span>Notas internas</span>
              <p>${escapeHtml(reservation.internalNotes || "Nenhuma nota interna registrada.")}</p>
            </div>
          </div>
        </section>

        <section class="reservation-profile-panel wide muted">
          ${infoRow("Criada em", formatDateTime(reservation.createdAt))}
          ${infoRow("Atualizada em", formatDateTime(reservation.updatedAt))}
        </section>
      </div>
    `;

    container.querySelector(".reservation-profile-back").addEventListener("click", () => {
        if (typeof options.onBack === "function") {
            options.onBack();
        }
    });

    container.querySelector("[data-edit-reservation]")?.addEventListener("click", () => {
        options.onEditReservation?.(Number(reservation.id));
    });

    container.querySelectorAll("[data-open-guest]").forEach((button) => {
        button.addEventListener("click", () => {
            options.onOpenGuest?.(Number(button.dataset.openGuest));
        });
    });

    container.querySelector("[data-go-reservation-payments]")?.addEventListener("click", () => {
        container.querySelector("#reservation-payment-transactions")?.scrollIntoView({ behavior: "smooth", block: "start" });
    });

    container.querySelectorAll("[data-settle-transaction]").forEach((button) => {
        button.addEventListener("click", () => handleSettleTransaction(container, button, options));
    });
}

function renderFinancialPlanSummary(financialTransactionPlan, options) {
    if (!financialTransactionPlan) {
        return `
          <section class="reservation-profile-panel wide muted" aria-live="polite">
            <div class="profile-panel-label">Plano financeiro</div>
            <p>Nenhum plano financeiro FTP está associado a esta reserva.</p>
          </section>
        `;
    }

    const componentSummaryDTOList = financialTransactionPlan.componentSummaryDTOList || [];
    const canAccessCompleteFinancialProfile = Boolean(options.permissions?.canAccessFinance);
    return `
      <section class="reservation-profile-panel wide reservation-financial-plan" aria-live="polite">
        <div class="guest-payment-header">
          <div>
            <div class="profile-panel-label">Plano financeiro da reserva</div>
            <p>Resumo autorizado das alocações e do estado atual do FTP.</p>
          </div>
          <span>${escapeHtml(financialPlanStatusLabel(financialTransactionPlan.status))}</span>
        </div>
        <div class="reservation-financial-plan-summary">
          ${infoRow("Total do plano", formatMoney(financialTransactionPlan.totalAmount))}
          ${infoRow("Liquidado", formatMoney(financialTransactionPlan.settledAmount))}
          ${infoRow("Em aberto", formatMoney(financialTransactionPlan.outstandingAmount))}
          ${infoRow("Prazo", formatDate(financialTransactionPlan.planDueDate))}
        </div>
        <div class="reservation-financial-components">
          ${componentSummaryDTOList.length
                ? componentSummaryDTOList.map(renderFinancialComponent).join("")
                : '<p>Nenhuma finalidade financeira configurada.</p>'}
        </div>
        ${canAccessCompleteFinancialProfile
                ? '<p class="reservation-financial-permission-note">Seu perfil possui acesso ao contexto financeiro administrativo completo.</p>'
                : ''}
      </section>
    `;
}

function renderFinancialComponent(financialComponentSummary) {
    return `
      <div class="reservation-financial-component">
        <strong>${escapeHtml(financialPurposeLabel(financialComponentSummary.type))}</strong>
        <span>${formatMoney(financialComponentSummary.amount)} · ${escapeHtml(financialComponentSummary.structure === "INSTALLMENT" ? `${financialComponentSummary.installmentsQuantity || "-"} parcelas` : "À vista")}</span>
        <small>${escapeHtml(financialPlanComponentStatusLabel(financialComponentSummary.status))} · vencimento ${escapeHtml(formatDate(financialComponentSummary.dueDate))}</small>
      </div>
    `;
}

function financialPurposeLabel(type) {
    return {
        PLAN_DOWN_PAYMENT: "Sinal",
        PLAN_CHECK_IN_PAYMENT: "Pagamento no check-in",
        PLAN_CHECK_OUT_PAYMENT: "Pagamento no checkout",
    }[String(type || "").toUpperCase()] || "Finalidade financeira";
}

function financialPlanStatusLabel(status) {
    return {
        ACTIVE: "Ativo",
        PARTIALLY_SETTLED: "Parcialmente pago",
        OVERDUE: "Em atraso",
        SETTLED: "Liquidado",
        CANCELED: "Cancelado",
    }[String(status || "").toUpperCase()] || "Indisponível";
}

function financialPlanComponentStatusLabel(status) {
    return {
        WAITING: "Agendado",
        PAID: "Pago",
        SETTLED: "Liquidado",
        CANCELED: "Cancelado",
        ON_TIME: "Em dia",
        LATE: "Em atraso",
    }[String(status || "").toUpperCase()] || "Status indisponível";
}

function getPaymentStatus(reservation) {
    const total = Number(reservation.totalAmount) || 0;
    const paid = Number(reservation.paidAmount) || 0;

    if (total > 0 && paid >= total) {
        return "Pago";
    }

    if (paid > 0) {
        return "Parcial";
    }

    return "Em espera";
}

function normalizePaymentStatus(status) {
    return {
        PAID: "paid",
        PARTIAL: "partial",
        WAITING: "waiting",
    }[String(status || "").toUpperCase()] || "waiting";
}

async function handleSettleTransaction(container, button, options) {
    const transactionId = Number(button.dataset.settleTransaction);
    if (!transactionId) {
        return;
    }

    button.disabled = true;
    button.innerHTML = '<i class="ti ti-loader-2"></i> Liquidando';

    try {
        await settleFinancialTransaction(transactionId);
        showToast(container, "Pagamento liquidado com sucesso.");
        await loadReservation(container, options);
    } catch (error) {
        showToast(container, error.message || "Nao foi possivel liquidar o pagamento.");
        button.disabled = false;
        button.innerHTML = '<i class="ti ti-check"></i> Liquidar';
    }
}

function getReservationPayableTransactions(reservation, transactions) {
    return transactions
        .filter((transaction) => String(transaction.sourceType || "").toUpperCase() === "BOOKING")
        .filter((transaction) => Number(transaction.sourceId) === Number(reservation.id))
        .filter((transaction) => {
            const status = String(transaction.status || "").toUpperCase();
            return !["SETTLED", "PAID"].includes(status);
        })
        .sort((a, b) => String(a.transactionDate || "").localeCompare(String(b.transactionDate || "")) || Number(b.id || 0) - Number(a.id || 0));
}

function renderPaymentTransaction(transaction) {
    return `
      <div class="guest-payment-row">
        <div class="guest-payment-main">
          <strong>${escapeHtml(transaction.description || `Transacao #${transaction.id}`)}</strong>
          <span>${escapeHtml(formatDate(transaction.transactionDate))} · ${escapeHtml(transactionStatusText(transaction.status))}</span>
        </div>
        <div class="guest-payment-value">${formatMoney(transaction.amount)}</div>
        <div class="guest-payment-actions">
          <button class="guest-payment-settle" type="button" data-settle-transaction="${escapeHtml(transaction.id)}">
            <i class="ti ti-check"></i> Liquidar
          </button>
        </div>
      </div>
    `;
}

function transactionStatusText(status) {
    return {
        WAITING: "Em espera",
        ON_TIME: "Em dia",
        LATE: "Em atraso",
        PARTIALLY_REALIZED: "Parcial",
        NOT_REALIZED: "Nao realizado",
        CANCELED: "Cancelado",
    }[String(status || "").toUpperCase()] || status || "-";
}

function infoRow(label, value) {
    return `
      <div class="reservation-info-row">
        <span>${escapeHtml(label)}</span>
        <strong>${escapeHtml(value ?? "-")}</strong>
      </div>
    `;
}

function infoRowHtml(label, htmlValue) {
    return `
      <div class="reservation-info-row">
        <span>${escapeHtml(label)}</span>
        <strong>${htmlValue}</strong>
      </div>
    `;
}

function guestLink(reservation) {
    return `<button class="reservation-info-link" type="button" data-open-guest="${escapeHtml(reservation.guestId)}">${escapeHtml(reservation.guestName || "-")}</button>`;
}

function normalizeStatus(status) {
    return {
        CONFIRMED: "confirmada",
        UNCONFIRMED: "pendente",
        CANCELED: "cancelada",
        IN_STAY: "got_checkin",
        FINISHED: "got_checkout",
    }[status] || String(status || "pendente").toLowerCase();
}

function calculateNights(checkin, checkout) {
    if (!checkin || !checkout) {
        return 0;
    }

    return Math.max(0, Math.round((new Date(checkout) - new Date(checkin)) / 86400000));
}

function formatDate(value) {
    if (!value) {
        return "-";
    }

    const [year, month, day] = String(value).split("-");
    return year && month && day ? `${day}/${month}/${year}` : value;
}

function formatDateTime(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

function formatMoney(value) {
    return `R$ ${(Number(value) || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`;
}

function showToast(container, message) {
    const toast = container.querySelector("#reservation-profile-toast");
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
