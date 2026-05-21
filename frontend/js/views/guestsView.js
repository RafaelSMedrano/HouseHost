import { deleteGuest, findAllBookings, findAllGuests, findAllStays, findMetricsSummary } from "../api.js?v=2026-05-18-metrics";

export function renderGuestsView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main guests-main">
  <div class="content guests-page">
    <div class="guests-kpis">
      <div class="guests-kpi"><span class="kpi-dot dot-sage"></span><div class="kpi-label">Em estadia</div><div id="guests-kpi-stay" class="kpi-val">-</div><div class="kpi-sub">hospedes ativos</div></div>
      <div class="guests-kpi"><span class="kpi-dot dot-lav"></span><div class="kpi-label">Com reserva</div><div id="guests-kpi-booking" class="kpi-val">-</div><div class="kpi-sub">reservas futuras ou atuais</div></div>
      <div class="guests-kpi"><span class="kpi-dot dot-dark"></span><div class="kpi-label">Quantidade de hospedes</div><div id="guests-kpi-total" class="kpi-val">-</div><div class="kpi-sub">total na base</div></div>
    </div>

    <div class="guests-toolbar">
      <label class="guests-search">
        <i class="ti ti-search"></i>
        <input id="guests-search" type="text" placeholder="Nome, email, cidade, telefone..." autocomplete="off">
      </label>
      <select id="guests-type" class="guests-select">
        <option value="">Todos os tipos</option>
        <option value="vip">VIP</option>
        <option value="new">Novo</option>
        <option value="regular">Regular</option>
      </select>
      <select id="guests-financial-status" class="guests-select">
        <option value="">Todas as situações</option>
        <option value="WAITING_PAYMENT">Pagamento em espera</option>
        <option value="PAYMENT_SETTLED">Pagamento liquidado</option>
        <option value="DEBTOR">Devedor</option>
      </select>
      <select id="guests-status" class="guests-select">
        <option value="">Todos os status</option>
        <option value="IN_BOOKING">Com reserva</option>
        <option value="IN_STAY">Em estadia</option>
        <option value="GOT_CHECKOUT">Com check out</option>
      </select>
      <button id="guests-new" class="btn btn-primary" type="button"><i class="ti ti-user-plus"></i> Novo hospede</button>
    </div>

    <div class="guests-table-wrap">
      <table class="guests-table">
        <thead>
          <tr>
            <th>Hospede</th>
            <th>Status</th>
            <th>Situacao</th>
            <th>Reservas</th>
            <th>Total gasto</th>
            <th>Cidade</th>
            <th>Telefone</th>
            <th></th>
          </tr>
        </thead>
        <tbody id="guests-table-body"></tbody>
      </table>
      <div id="guests-empty" class="guests-empty">
        <i class="ti ti-users-off"></i>
        <p>Nenhum hospede encontrado.</p>
      </div>
      <div class="guests-pagination"><span id="guests-count">Carregando hospedes...</span></div>
    </div>
  </div>
  <div id="guests-toast" class="booking-toast"><i class="ti ti-check"></i><span></span></div>
</div>
    `;

    bindGuestsView(container, options);
}

function bindGuestsView(container, options) {
    const state = { guests: [], bookings: [], stays: [], query: "", type: "", financialStatus: "", status: "" };
    const search = container.querySelector("#guests-search");
    const type = container.querySelector("#guests-type");
    const financialStatus = container.querySelector("#guests-financial-status");
    const status = container.querySelector("#guests-status");

    container.querySelector("#guests-new").addEventListener("click", () => {
        if (typeof options.onNewGuest === "function") {
            options.onNewGuest();
        }
    });

    search.addEventListener("input", () => {
        state.query = search.value.trim().toLowerCase();
        renderGuestsTable(container, state, options);
    });

    type.addEventListener("change", () => {
        state.type = type.value;
        renderGuestsTable(container, state, options);
    });

    financialStatus.addEventListener("change", () => {
        state.financialStatus = financialStatus.value;
        renderGuestsTable(container, state, options);
    });

    status.addEventListener("change", () => {
        state.status = status.value;
        renderGuestsTable(container, state, options);
    });

    loadGuests(container, state, options);
}

async function loadGuests(container, state, options) {
    try {
        const [response, bookingsResponse, staysResponse, metricsResponse] = await Promise.all([
            findAllGuests(),
            findAllBookings(),
            findAllStays(),
            findMetricsSummary(),
        ]);
        state.bookings = bookingsResponse.data || [];
        state.stays = staysResponse.data || [];
        state.guests = (response.data || []).map((guest) => ({
            ...guest,
            displayStatus: resolveGuestDisplayStatus(guest, state.bookings, state.stays),
        }));
        renderGuestsKpis(container, metricsResponse.data || {});
        renderGuestsTable(container, state, options);
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
        container.querySelector("#guests-count").textContent = "Nao foi possivel carregar hospedes.";
    }
}

function renderGuestsKpis(container, metrics) {
    container.querySelector("#guests-kpi-stay").textContent = String(metrics.guestsInStay || 0);
    container.querySelector("#guests-kpi-booking").textContent = String(metrics.guestsWithBooking || 0);
    container.querySelector("#guests-kpi-total").textContent = String(metrics.totalGuests || 0);
}

function renderGuestsTable(container, state, options) {
    const tbody = container.querySelector("#guests-table-body");
    const empty = container.querySelector("#guests-empty");
    const count = container.querySelector("#guests-count");
    const data = getFilteredGuests(state);

    tbody.innerHTML = data.map((guest) => `
      <tr>
        <td><div class="guest-row-main"><span class="guest-row-avatar">${initialsFor(guest.fullName)}</span><div><button class="guest-name-button" type="button" data-open-guest="${guest.id}">${escapeHtml(guest.fullName || "-")}</button><small>${guestTypeText(guest.guestType)}</small></div></div></td>
        <td>${guestStatusBadge(guest.displayStatus)}</td>
        <td>${financialStatusBadge(guest.financialStatus)}</td>
        <td>${countGuestBookings(guest.id, state.bookings)}</td>
        <td>${formatMoney(guest.totalSpent)}</td>
        <td>${escapeHtml([guest.city, guest.state].filter(Boolean).join(" - ") || "-")}</td>
        <td>${escapeHtml(guest.phone || "-")}</td>
        <td><div class="td-actions">
          <button type="button" data-edit-guest="${guest.id}" title="Editar"><i class="ti ti-pencil"></i></button>
          <button type="button" data-reserve-guest="${guest.id}" title="Nova reserva"><i class="ti ti-calendar-plus"></i></button>
          <button type="button" data-delete-guest="${guest.id}" title="Remover"><i class="ti ti-trash"></i></button>
        </div></td>
      </tr>
    `).join("");

    empty.classList.toggle("active", data.length === 0);
    count.innerHTML = `Mostrando <strong>${data.length}</strong> de <strong>${state.guests.length}</strong> hospedes`;

    tbody.querySelectorAll("[data-open-guest]").forEach((button) => {
        button.addEventListener("click", () => options.onOpenGuest?.(Number(button.dataset.openGuest)));
    });

    tbody.querySelectorAll("[data-edit-guest]").forEach((button) => {
        button.addEventListener("click", () => options.onEditGuest?.(Number(button.dataset.editGuest)));
    });

    tbody.querySelectorAll("[data-reserve-guest]").forEach((button) => {
        button.addEventListener("click", () => options.onNewReservation?.(Number(button.dataset.reserveGuest)));
    });

    tbody.querySelectorAll("[data-delete-guest]").forEach((button) => {
        button.addEventListener("click", () => handleDeleteGuest(container, state, options, Number(button.dataset.deleteGuest)));
    });
}

function getFilteredGuests(state) {
    return state.guests.filter((guest) => {
        const text = `${guest.fullName || ""} ${guest.email || ""} ${guest.phone || ""} ${guest.city || ""}`.toLowerCase();
        const matchesQuery = !state.query || text.includes(state.query);
        const matchesType = !state.type || guest.guestType === state.type;
        const matchesFinancialStatus = !state.financialStatus || String(guest.financialStatus || "").toUpperCase() === state.financialStatus;
        const matchesStatus = !state.status || normalizeGuestStatus(guest.displayStatus) === state.status;
        return matchesQuery && matchesType && matchesFinancialStatus && matchesStatus;
    });
}

async function handleDeleteGuest(container, state, options, guestId) {
    if (!window.confirm("Excluir este hospede permanentemente?")) {
        return;
    }

    try {
        await deleteGuest(guestId);
        state.guests = state.guests.filter((guest) => guest.id !== guestId);
        const metricsResponse = await findMetricsSummary();
        renderGuestsKpis(container, metricsResponse.data || {});
        renderGuestsTable(container, state, options);
        showToast(container, "Hospede removido com sucesso.", "ti-trash");
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
    }
}

function guestTypeText(type) {
    const normalized = String(type || "").toLowerCase();
    const label = normalized === "vip" ? "VIP" : ["new", "novo"].includes(normalized) ? "Novo" : "Regular";
    return escapeHtml(label);
}

function guestStatusBadge(status) {
    const normalized = normalizeGuestStatus(status);
    const css = normalized === "IN_STAY" ? "new" : normalized === "GOT_CHECKOUT" ? "vip" : "regular";
    return `<span class="guest-type-badge ${css}">${escapeHtml(guestStatusText(normalized))}</span>`;
}

function guestStatusText(status) {
    const labels = {
        IN_BOOKING: "Com reserva",
        IN_STAY: "Em estadia",
        GOT_CHECKOUT: "Com check out",
    };
    const normalized = normalizeGuestStatus(status);
    return labels[normalized] || normalized;
}

function financialStatusBadge(status) {
    const normalized = String(status || "").toUpperCase();
    const css = normalized === "DEBTOR" ? "vip" : normalized === "WAITING_PAYMENT" ? "new" : "regular";
    return `<span class="guest-type-badge ${css}">${escapeHtml(financialStatusText(normalized))}</span>`;
}

function financialStatusText(status) {
    return {
        WAITING_PAYMENT: "Pagamento em espera",
        PAYMENT_SETTLED: "Pagamento liquidado",
        DEBTOR: "Devedor",
    }[String(status || "").toUpperCase()] || "-";
}

function normalizeGuestStatus(status) {
    const normalized = String(status || "IN_BOOKING").toUpperCase();
    const aliases = {
        COM_RESERVA: "IN_BOOKING",
        EM_ESTADIA: "IN_STAY",
        COM_CHECK_OUT: "GOT_CHECKOUT",
    };
    return aliases[normalized] || normalized;
}

function resolveGuestDisplayStatus(guest, bookings, stays) {
    if (hasActiveStay(guest.id, stays)) {
        return "IN_STAY";
    }

    if (hasBooking(guest.id, bookings)) {
        return "IN_BOOKING";
    }

    if (hasCheckedOutStay(guest.id, stays)) {
        return "GOT_CHECKOUT";
    }

    return normalizeGuestStatus(guest.status);
}

function hasActiveStay(guestId, stays) {
    return stays.some((stay) => Number(stay.guestId) === Number(guestId)
        && normalizeStayStatus(stay.status) === "ACTIVE");
}

function hasCheckedOutStay(guestId, stays) {
    return stays.some((stay) => Number(stay.guestId) === Number(guestId)
        && normalizeStayStatus(stay.status) === "CHECKED_OUT");
}

function countGuestBookings(guestId, bookings) {
    return bookings.filter((booking) => Number(booking.guestId) === Number(guestId)).length;
}

function hasBooking(guestId, bookings) {
    return bookings.some((booking) => Number(booking.guestId) === Number(guestId)
        && ["PENDING", "CONFIRMED"].includes(normalizeBookingStatus(booking.status)));
}

function normalizeStayStatus(status) {
    return String(status || "").toUpperCase();
}

function normalizeBookingStatus(status) {
    return String(status || "").toUpperCase();
}

function formatMoney(value) {
    const amount = Number(value) || 0;
    return `R$ ${amount.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`;
}

function initialsFor(name) {
    return String(name || "?")
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("") || "?";
}

function showToast(container, message, icon) {
    const toast = container.querySelector("#guests-toast");
    toast.querySelector("span").textContent = message;
    toast.querySelector("i").className = `ti ${icon}`;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2600);
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
