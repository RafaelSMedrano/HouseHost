import { deleteFinancialTransaction, findAllBookings, findAllFinancialTransactions, findGuestById, revealGuestContact, settleFinancialTransaction } from "../api.js?v=2026-08-11-api-log-transport";
import { guestStatusBadgeClass, guestStatusLabel } from "../guestStatus.js?v=2026-08-12-guest-status";

export function renderGuestProfileView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main guests-main">
  <div class="content guest-profile-page">
    <div class="reservation-profile-loading" role="status" aria-live="polite">Carregando hospede...</div>
  </div>
  <div id="guest-profile-toast" class="booking-toast"><i class="ti ti-alert-circle"></i><span></span></div>
</div>`;

    loadGuestProfile(container, options);
}

async function loadGuestProfile(container, options) {
    try {
        const transactionsRequest = options.canAccessFinance
            ? findAllFinancialTransactions()
            : Promise.resolve({ data: [] });
        const [guestResponse, bookingsResponse, transactionsResponse] = await Promise.all([
            findGuestById(options.guestId),
            findAllBookings(),
            transactionsRequest,
        ]);

        renderProfile(
            container,
            guestResponse.data,
            bookingsResponse.data || [],
            transactionsResponse.data || [],
            options
        );
    } catch (error) {
        container.querySelector(".guest-profile-page").innerHTML = `
          <div class="rooms-empty rooms-error" role="alert">
            <p>${escapeHtml(error.message || "Nao foi possivel carregar o hospede.")}</p>
            <button class="dashboard-back-btn reservation-profile-back" type="button" aria-label="Voltar">Voltar</button>
          </div>
        `;
        container.querySelector(".reservation-profile-back")?.addEventListener("click", () => options.onBack?.());
        showToast(container, error.message || "Nao foi possivel carregar o hospede.");
    }
}

function renderProfile(container, guest, bookings, transactions, options) {
    const guestBookings = bookings.filter((booking) => Number(booking.guestId) === Number(guest.id));
    const guestTransactions = transactions.filter((transaction) =>
        (String(transaction.senderType).toUpperCase() === "GUEST" && Number(transaction.senderId) === Number(guest.id))
        || (String(transaction.receiverType).toUpperCase() === "GUEST" && Number(transaction.receiverId) === Number(guest.id))
    );
    const pendingTransactions = getPayableTransactions(guestTransactions);
    const activeBookings = guestBookings.filter((booking) => ["UNCONFIRMED", "CONFIRMED"].includes(normalizeStatus(booking.status)));
    const activeBookingsInStay = guestBookings.filter((booking) => normalizeStatus(booking.status) === "IN_STAY");
    const hasPaymentAction = options.canAccessFinance
        && ["WAITING_PAYMENT", "DEBTOR"].includes(String(guest.financialStatus || "").toUpperCase())
        && pendingTransactions.length > 0;

    container.querySelector(".guest-profile-page").innerHTML = `
      <div class="reservation-profile-header">
        <button class="dashboard-back-btn reservation-profile-back" type="button" aria-label="Voltar"><i class="ti ti-arrow-left" aria-hidden="true"></i> Voltar</button>
        <div class="guest-profile-title-block">
          <span class="guest-profile-avatar">${initialsFor(guest.fullName)}</span>
          <div class="reservation-profile-title">
            <span>Hospede #${escapeHtml(guest.id)}</span>
            <strong>${escapeHtml(guest.fullName || "-")}</strong>
          </div>
        </div>
        ${guestStatusBadge(guest.status)}
      </div>

      <div class="guest-profile-grid">
        <section class="reservation-profile-panel hero">
          <div class="profile-panel-label">Resumo</div>
          <div class="guest-profile-stats">
            <div><span>Hospedagens</span><strong>${escapeHtml(guest.stayCount ?? guestBookings.filter((booking) => normalizeStatus(booking.status) === "FINISHED").length ?? 0)}</strong></div>
            ${options.canAccessFinance
                ? `<div><span>Total gasto</span><strong>${formatMoney(guest.totalSpent)}</strong></div>`
                : `<div><span>Reservas</span><strong>${escapeHtml(guestBookings.length)}</strong></div>`}
          </div>
          <div class="guest-profile-badges">
            ${guestTypeBadge(guest.guestType)}
            ${options.canAccessFinance ? financialStatusBadge(guest.financialStatus) : ""}
          </div>
          ${hasPaymentAction ? '<button class="guest-payment-cta" type="button" data-go-payments><i class="ti ti-cash"></i> Efetuar pagamento</button>' : ""}
        </section>

        <section class="reservation-profile-panel">
          <div class="profile-panel-label">Contato</div>
          ${contactInfoRow("Telefone", guest.phone, "phone")}
          ${contactInfoRow("Email", guest.email, "email")}
          <button class="guest-contact-reveal" type="button" data-reveal-contact>
            <i class="ti ti-eye"></i> Revelar contato
          </button>
          ${infoRow("Cidade", [guest.city, guest.state].filter(Boolean).join(" - ") || "-")}
          ${infoRow("Endereco", guest.address)}
        </section>

        <section class="reservation-profile-panel">
          <div class="profile-panel-label">Identificacao</div>
          ${infoRow("CPF", guest.documentNumber)}
          ${infoRow("Nascimento", formatDate(guest.birthDate))}
          ${infoRow("Genero", guest.gender)}
        </section>

        <section class="reservation-profile-panel">
          <div class="profile-panel-label">Historico</div>
          ${options.canAccessFinance ? infoRow("Status financeiro", financialStatusText(guest.financialStatus)) : ""}
          ${infoRow("Reservas ativas", activeBookings.length)}
          ${infoRow("Hospedagens ativas", activeBookingsInStay.length)}
          ${options.canAccessFinance ? infoRow("Transacoes", guestTransactions.length) : ""}
          ${infoRow("Ultima estadia", formatDate(guest.lastStayDate))}
          ${infoRow("Origem", guest.originChannel)}
        </section>

        ${hasPaymentAction ? `
          <section id="guest-payment-transactions" class="reservation-profile-panel wide">
            <div class="guest-payment-header">
              <div>
                <div class="profile-panel-label">Pagamentos pendentes</div>
                <p>Transacoes em espera ou em debito vinculadas a este hospede.</p>
              </div>
              <span>${pendingTransactions.length} pendencia${pendingTransactions.length === 1 ? "" : "s"}</span>
            </div>
            <div class="guest-payment-list">
              ${pendingTransactions.map(renderPaymentTransaction).join("")}
            </div>
          </section>
        ` : ""}

        <section class="reservation-profile-panel wide">
          <div class="profile-panel-label">Preferências e restrições</div>
          <div class="guest-profile-care">
            <div>
              <span>Preferências e restrições</span>
              <p>${escapeHtml(guest.preferencesAndRestrictions || "Nenhuma preferência ou restrição registrada.")}</p>
            </div>
            <div>
              <span>Necessidades de acessibilidade</span>
              <p>${escapeHtml(guest.accessibilityNeeds || "Nenhuma necessidade de acessibilidade registrada.")}</p>
            </div>
          </div>
        </section>

        <section class="reservation-profile-panel wide">
          <div class="profile-panel-label">Observacoes</div>
          <div class="profile-notes">
            <div>
              <span>Anotacoes internas</span>
              <p>${escapeHtml(guest.notes || "Nenhuma anotacao registrada.")}</p>
            </div>
          </div>
        </section>

        <section class="reservation-profile-panel wide muted">
          ${infoRow("Criado em", formatDateTime(guest.createdAt))}
          ${infoRow("Atualizado em", formatDateTime(guest.updatedAt))}
        </section>
      </div>

      <div class="guest-profile-actions">
        <button class="btn btn-primary" type="button" data-edit-profile><i class="ti ti-pencil"></i> Editar hospede</button>
        <button class="dashboard-back-btn reservation-profile-back" type="button" data-new-reservation><i class="ti ti-calendar-plus"></i> Nova reserva</button>
      </div>
    `;

    container.querySelector(".reservation-profile-back").addEventListener("click", () => options.onBack?.());
    container.querySelector("[data-edit-profile]").addEventListener("click", () => options.onEditGuest?.(guest.id));
    container.querySelector("[data-new-reservation]").addEventListener("click", () => options.onNewReservation?.(guest.id));
    container.querySelector("[data-reveal-contact]")?.addEventListener("click", (event) => {
        toggleContactVisibility(container, event.currentTarget, guest);
    });
    container.querySelector("[data-go-payments]")?.addEventListener("click", () => {
        container.querySelector("#guest-payment-transactions")?.scrollIntoView({ behavior: "smooth", block: "start" });
    });
    container.querySelectorAll("[data-settle-transaction]").forEach((button) => {
        button.addEventListener("click", () => handleSettleTransaction(container, button, options));
    });
    container.querySelectorAll("[data-delete-transaction]").forEach((button) => {
        button.addEventListener("click", () => handleDeleteTransaction(container, button, options));
    });
}

async function toggleContactVisibility(container, button, guest) {
    if (button.dataset.revealed === "true") {
        container.querySelector("[data-contact-phone]").textContent = guest.phone || "-";
        container.querySelector("[data-contact-email]").textContent = guest.email || "-";
        button.dataset.revealed = "false";
        button.innerHTML = '<i class="ti ti-eye"></i> Revelar contato';
        return;
    }

    button.disabled = true;
    button.innerHTML = '<i class="ti ti-loader-2"></i> Revelando...';

    try {
        const response = await revealGuestContact(guest.id);
        const contact = response.data || {};
        container.querySelector("[data-contact-phone]").textContent = contact.phone || "-";
        container.querySelector("[data-contact-email]").textContent = contact.email || "-";
        button.dataset.revealed = "true";
        button.disabled = false;
        button.innerHTML = '<i class="ti ti-eye-off"></i> Ocultar contato';
        showToast(container, "Contato revelado e acesso registrado na auditoria.", "ti-eye");
    } catch (error) {
        button.disabled = false;
        button.innerHTML = '<i class="ti ti-eye"></i> Revelar contato';
        showToast(container, error.message || "Nao foi possivel revelar o contato.");
    }
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
        showToast(container, "Transacao liquidada com sucesso.");
        await loadGuestProfile(container, options);
    } catch (error) {
        showToast(container, error.message || "Nao foi possivel liquidar a transacao.");
        button.disabled = false;
        button.innerHTML = '<i class="ti ti-check"></i> Liquidar';
    }
}

async function handleDeleteTransaction(container, button, options) {
    const transactionId = Number(button.dataset.deleteTransaction);
    if (!transactionId) {
        return;
    }

    const confirmed = globalThis.confirm("Excluir este pagamento pendente permanentemente?");
    if (!confirmed) {
        return;
    }

    button.disabled = true;
    button.innerHTML = '<i class="ti ti-loader-2"></i> Excluindo';

    try {
        await deleteFinancialTransaction(transactionId);
        showToast(container, "Pagamento excluido com sucesso.", "ti-trash");
        await loadGuestProfile(container, options);
    } catch (error) {
        showToast(container, error.message || "Nao foi possivel excluir o pagamento.");
        button.disabled = false;
        button.innerHTML = '<i class="ti ti-trash"></i> Excluir';
    }
}

function infoRow(label, value) {
    return `
      <div class="reservation-info-row">
        <span>${escapeHtml(label)}</span>
        <strong>${escapeHtml(value ?? "-")}</strong>
      </div>
    `;
}

function contactInfoRow(label, value, field) {
    return `
      <div class="reservation-info-row">
        <span>${escapeHtml(label)}</span>
        <strong data-contact-${field}>${escapeHtml(value ?? "-")}</strong>
      </div>
    `;
}

function getPayableTransactions(transactions) {
    return transactions
        .filter((transaction) => {
            const status = normalizeStatus(transaction.status);
            if (["SETTLED", "PAID"].includes(status)) {
                return false;
            }

            return isTransactionOverdue(transaction) || ["WAITING", "ON_TIME", "PARTIALLY_REALIZED", "LATE", "NOT_REALIZED"].includes(status);
        })
        .sort((a, b) => {
            const dateComparison = String(a.transactionDate || "").localeCompare(String(b.transactionDate || ""));
            return dateComparison || Number(b.id || 0) - Number(a.id || 0);
        });
}

function renderPaymentTransaction(transaction) {
    const overdue = isTransactionOverdue(transaction);
    return `
      <div class="guest-payment-row">
        <div class="guest-payment-main">
          <strong>${escapeHtml(transaction.description || `Transacao #${transaction.id}`)}</strong>
          <span>${escapeHtml(formatDate(transaction.transactionDate))} · ${escapeHtml(transactionStatusText(transaction.status, overdue))}</span>
        </div>
        <div class="guest-payment-value">${formatMoney(transaction.amount)}</div>
        <div class="guest-payment-actions">
          <button class="guest-payment-settle" type="button" data-settle-transaction="${escapeHtml(transaction.id)}">
            <i class="ti ti-check"></i> Liquidar
          </button>
          <button class="guest-payment-delete" type="button" data-delete-transaction="${escapeHtml(transaction.id)}">
            <i class="ti ti-trash"></i> Excluir
          </button>
        </div>
      </div>
    `;
}

function guestTypeBadge(type) {
    const label = type === "VIP" ? "VIP" : type === "NOVO" ? "Novo" : "Regular";
    const css = type === "VIP" ? "vip" : type === "NOVO" ? "new" : "regular";
    return `<span class="guest-type-badge ${css}">${label}</span>`;
}

function guestStatusBadge(status) {
    return `<span class="guest-type-badge ${guestStatusBadgeClass(status)}">${escapeHtml(guestStatusLabel(status))}</span>`;
}

function financialStatusText(status) {
    return {
        WAITING_PAYMENT: "Pagamento em espera",
        PAYMENT_SETTLED: "Pagamento liquidado",
        DEBTOR: "Devedor",
    }[String(status || "").toUpperCase()] || "-";
}

function financialStatusBadge(status) {
    const normalized = String(status || "").toUpperCase();
    const css = normalized === "DEBTOR" ? "vip" : normalized === "WAITING_PAYMENT" ? "new" : "regular";
    return `<span class="guest-type-badge ${css}">${escapeHtml(financialStatusText(normalized))}</span>`;
}

function transactionStatusText(status, overdue = false) {
    if (overdue) {
        return "Em debito";
    }

    return {
        WAITING: "Em espera",
        ON_TIME: "No prazo",
        PARTIALLY_REALIZED: "Parcial",
        LATE: "Atrasada",
        NOT_REALIZED: "Nao realizada",
        CANCELED: "Cancelada",
    }[normalizeStatus(status)] || status || "-";
}

function isTransactionOverdue(transaction) {
    if (!transaction.transactionDate || ["SETTLED", "PAID"].includes(normalizeStatus(transaction.status))) {
        return false;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const transactionDate = new Date(`${transaction.transactionDate}T00:00:00`);
    return !Number.isNaN(transactionDate.getTime()) && transactionDate < today;
}

function normalizeStatus(status) {
    return String(status || "").toUpperCase();
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

function initialsFor(name) {
    return String(name || "?")
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("") || "?";
}

function showToast(container, message, icon = "ti-alert-circle") {
    const toast = container.querySelector("#guest-profile-toast");
    toast.querySelector("span").textContent = message;
    toast.querySelector("i").className = `ti ${icon}`;
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
