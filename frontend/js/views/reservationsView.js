import { deleteBooking, findAllBookings, findMetricsSummary } from "../api.js?v=2026-05-19-delete-booking";

const statusLabels = {
    confirmada: "Confirmada",
    pendente: "Pendente",
    checkin: "Check-in feito",
    checkout: "Check-out",
    got_checkin: "Check-in realizado",
    cancelada: "Cancelada",
};

const months = ["Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"];

export function renderReservationsView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main reservations-main">
  <div class="content reservations-page">
    <div class="reservations-kpis">
      <div class="reservations-kpi"><span class="kpi-dot dot-confirmed"></span><div class="kpi-label">Confirmadas</div><div id="reservations-kpi-confirmed" class="kpi-val">-</div><div class="kpi-sub">reservas ativas</div></div>
      <div class="reservations-kpi"><span class="kpi-dot dot-lav"></span><div class="kpi-label">Check-ins realizados</div><div id="reservations-kpi-checkin" class="kpi-val">-</div><div class="kpi-sub">reservas com check-in</div></div>
      <div class="reservations-kpi"><span class="kpi-dot dot-rose"></span><div class="kpi-label">Check-outs hoje</div><div id="reservations-kpi-checkout-today" class="kpi-val">-</div><div class="kpi-sub">a liberar</div></div>
      <div class="reservations-kpi no-kpi-dot"><div class="kpi-label">Receita total</div><div id="reservations-kpi-revenue" class="kpi-val kpi-money">R$ -</div><div class="kpi-sub">reservas listadas</div></div>
    </div>

    <div class="reservations-toolbar-row">
      <div class="reservations-tabs">
        <button class="reservations-tab active" type="button" data-tab="list"><i class="ti ti-list"></i> Lista</button>
        <button class="reservations-tab" type="button" data-tab="calendar"><i class="ti ti-calendar-month"></i> Calendario</button>
      </div>

      <div class="reservations-toolbar">
        <label class="reservations-search">
          <i class="ti ti-search"></i>
          <input id="reservation-search" type="text" placeholder="Buscar hospede, quarto..." autocomplete="off">
        </label>
        <select id="reservation-status" class="reservations-select">
          <option value="">Todos os status</option>
          <option value="confirmada">Confirmada</option>
          <option value="pendente">Pendente</option>
          <option value="got_checkin">Check-in realizado</option>
          <option value="cancelada">Cancelada</option>
        </select>
      </div>
    </div>

    <section id="reservations-list-panel" class="reservations-panel active">
      <div class="reservations-table-wrap">
        <table class="reservations-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Hospede</th>
              <th>Quarto</th>
              <th>Check-in</th>
              <th>Check-out</th>
              <th>Pessoas</th>
              <th>Origem</th>
              <th>Valor</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody id="reservations-table-body"></tbody>
        </table>
        <div id="reservations-empty" class="reservations-empty">
          <i class="ti ti-calendar-off"></i>
          <p>Nenhuma reserva encontrada para este filtro.</p>
        </div>
        <div class="reservations-pagination">
          <div>Mostrando <strong id="reservations-count">0</strong> de <strong id="reservations-total">0</strong> reservas</div>
          <div class="reservations-pages"><button type="button"><i class="ti ti-chevron-left"></i></button><button class="active" type="button">1</button><button type="button"><i class="ti ti-chevron-right"></i></button></div>
        </div>
      </div>
    </section>

    <section id="reservations-calendar-panel" class="reservations-panel">
      <div class="reservations-calendar">
        <div class="calendar-head">
          <div id="reservations-calendar-title">-</div>
          <div class="calendar-nav">
            <button id="calendar-prev" class="reservations-icon-btn" type="button"><i class="ti ti-chevron-left"></i></button>
            <button id="calendar-next" class="reservations-icon-btn" type="button"><i class="ti ti-chevron-right"></i></button>
          </div>
        </div>
        <div class="calendar-days"><span>Dom</span><span>Seg</span><span>Ter</span><span>Qua</span><span>Qui</span><span>Sex</span><span>Sab</span></div>
        <div id="reservations-calendar-grid" class="calendar-grid"></div>
      </div>
    </section>
  </div>
  <div id="reservations-toast" class="booking-toast"><i class="ti ti-check"></i><span></span></div>
</div>
    `;

    bindReservationsView(container, options);
}

function bindReservationsView(container, options) {
    const now = new Date();
    const state = { reservations: [], query: "", status: "", year: now.getFullYear(), month: now.getMonth() };
    const searchInput = container.querySelector("#reservation-search");
    const statusSelect = container.querySelector("#reservation-status");

    const renderFiltered = () => renderTable(container, state, getFilteredReservations(state), options);

    searchInput.addEventListener("input", () => {
        state.query = searchInput.value.trim().toLowerCase();
        renderFiltered();
    });

    statusSelect.addEventListener("change", () => {
        state.status = statusSelect.value;
        renderFiltered();
    });

    container.querySelectorAll(".reservations-tab").forEach((tab) => {
        tab.addEventListener("click", () => {
            container.querySelectorAll(".reservations-tab").forEach((item) => item.classList.remove("active"));
            container.querySelectorAll(".reservations-panel").forEach((panel) => panel.classList.remove("active"));
            tab.classList.add("active");
            container.querySelector(`#reservations-${tab.dataset.tab}-panel`).classList.add("active");

            if (tab.dataset.tab === "calendar") {
                renderCalendar(container, state);
            }
        });
    });

    container.querySelector("#calendar-prev").addEventListener("click", () => {
        state.month -= 1;
        if (state.month < 0) {
            state.month = 11;
            state.year -= 1;
        }
        renderCalendar(container, state);
    });

    container.querySelector("#calendar-next").addEventListener("click", () => {
        state.month += 1;
        if (state.month > 11) {
            state.month = 0;
            state.year += 1;
        }
        renderCalendar(container, state);
    });

    loadReservations(container, state, options);
}

async function loadReservations(container, state, options) {
    try {
        const [response, metricsResponse] = await Promise.all([
            findAllBookings(),
            findMetricsSummary(),
        ]);
        state.reservations = (response.data || []).map(normalizeBooking);
        renderKpis(container, metricsResponse.data || {});
        renderTable(container, state, getFilteredReservations(state), options);
        renderCalendar(container, state);
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
        renderTable(container, state, [], options);
    }
}

function normalizeBooking(booking) {
    return {
        rawId: booking.id,
        id: `#${String(booking.id).padStart(3, "0")}`,
        guest: booking.guestName || "-",
        city: `Hospede #${booking.guestId || "-"}`,
        room: booking.roomNumber || "-",
        checkin: booking.checkInDate,
        checkout: booking.checkOutDate,
        nights: calculateNights(booking.checkInDate, booking.checkOutDate),
        adults: booking.adults || 0,
        children: booking.children || 0,
        pets: booking.pets || 0,
        total: Number(booking.totalAmount) || 0,
        status: normalizeStatus(booking.status),
        origin: booking.origin || "-",
    };
}

function getFilteredReservations(state) {
    return state.reservations.filter((reservation) => {
        const text = `${reservation.id} ${reservation.guest} ${reservation.room} ${reservation.city}`.toLowerCase();
        const matchesQuery = !state.query || text.includes(state.query);
        const matchesStatus = !state.status || reservation.status === state.status;
        return matchesQuery && matchesStatus;
    });
}

function renderKpis(container, metrics) {
    container.querySelector("#reservations-kpi-confirmed").textContent = String(metrics.confirmedBookings || 0);
    container.querySelector("#reservations-kpi-checkin").textContent = String(metrics.gotCheckinBookings || 0);
    container.querySelector("#reservations-kpi-checkout-today").textContent = String(metrics.checkOutsTodayByReservation || 0);
    container.querySelector("#reservations-kpi-revenue").textContent = formatMoney(metrics.bookingsTotalRevenue);
}

function renderTable(container, state, data, options) {
    const tbody = container.querySelector("#reservations-table-body");
    const empty = container.querySelector("#reservations-empty");
    const count = container.querySelector("#reservations-count");
    const total = container.querySelector("#reservations-total");

    tbody.innerHTML = data.map((reservation) => `
      <tr class="reservation-click-row" data-reservation-id="${escapeHtml(reservation.rawId)}">
        <td class="td-id">${reservation.id}</td>
        <td><div class="td-guest">${escapeHtml(reservation.guest)}</div><div class="td-guest-sub">${escapeHtml(reservation.city)}</div></td>
        <td class="td-room">${escapeHtml(reservation.room)}</td>
        <td class="td-dates">${formatShortDate(reservation.checkin)} <span>${reservation.nights}n</span></td>
        <td class="td-dates">${formatShortDate(reservation.checkout)}</td>
        <td class="td-room">${formatPax(reservation)}</td>
        <td class="td-origin">${escapeHtml(reservation.origin)}</td>
        <td class="td-value">${formatMoney(reservation.total)}</td>
        <td><span class="reservation-status status-${reservation.status}"><span></span>${statusLabels[reservation.status] || reservation.status}</span></td>
        <td><div class="td-actions"><button class="reservation-checkin-btn" type="button" data-checkin-reservation="${escapeHtml(reservation.rawId)}" title="Check-in"><i class="ti ti-login"></i><span>Check-in</span></button><button type="button" data-edit-reservation="${escapeHtml(reservation.rawId)}" title="Editar reserva"><i class="ti ti-pencil"></i></button><button type="button" data-delete-reservation="${escapeHtml(reservation.rawId)}" title="Excluir reserva"><i class="ti ti-trash"></i></button></div></td>
      </tr>
    `).join("");

    empty.classList.toggle("active", data.length === 0);
    count.textContent = String(data.length);
    total.textContent = String(state.reservations.length);

    tbody.querySelectorAll("[data-reservation-id]").forEach((row) => {
        row.addEventListener("click", () => {
            if (typeof options.onOpenReservation === "function") {
                options.onOpenReservation(Number(row.dataset.reservationId));
            }
        });
    });

    tbody.querySelectorAll(".td-actions button").forEach((button) => {
        button.addEventListener("click", (event) => {
            event.stopPropagation();
        });
    });

    tbody.querySelectorAll("[data-checkin-reservation]").forEach((button) => {
        button.addEventListener("click", () => {
            if (typeof options.onCreateCheckIn === "function") {
                options.onCreateCheckIn(Number(button.dataset.checkinReservation));
            }
        });
    });

    tbody.querySelectorAll("[data-edit-reservation]").forEach((button) => {
        button.addEventListener("click", () => {
            if (typeof options.onEditReservation === "function") {
                options.onEditReservation(Number(button.dataset.editReservation));
            }
        });
    });

    tbody.querySelectorAll("[data-delete-reservation]").forEach((button) => {
        button.addEventListener("click", () => handleDeleteReservation(container, state, options, Number(button.dataset.deleteReservation)));
    });
}

async function handleDeleteReservation(container, state, options, reservationId) {
    if (!reservationId) {
        return;
    }

    const confirmed = globalThis.confirm("Excluir esta reserva permanentemente?");
    if (!confirmed) {
        return;
    }

    try {
        await deleteBooking(reservationId);
        showToast(container, "Reserva excluida com sucesso.", "ti-trash");
        await loadReservations(container, state, options);
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
    }
}

function renderCalendar(container, state) {
    const title = container.querySelector("#reservations-calendar-title");
    const grid = container.querySelector("#reservations-calendar-grid");
    const firstDay = new Date(state.year, state.month, 1).getDay();
    const daysInMonth = new Date(state.year, state.month + 1, 0).getDate();

    title.textContent = `${months[state.month]} ${state.year}`;
    grid.innerHTML = "";

    for (let index = 0; index < firstDay; index += 1) {
        grid.insertAdjacentHTML("beforeend", `<div class="calendar-cell muted"></div>`);
    }

    for (let day = 1; day <= daysInMonth; day += 1) {
        const date = `${state.year}-${String(state.month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
        const bookings = state.reservations.filter((reservation) => reservation.checkin === date || reservation.checkout === date);
        const dots = bookings.slice(0, 3).map((reservation) => `<span class="calendar-dot status-${reservation.status}"></span>`).join("");
        grid.insertAdjacentHTML("beforeend", `<div class="calendar-cell ${bookings.length ? "has-booking" : ""}"><strong>${day}</strong><div>${dots}</div></div>`);
    }
}

function normalizeStatus(status) {
    return {
        CONFIRMED: "confirmada",
        PENDING: "pendente",
        CANCELLED: "cancelada",
        GOT_CHECKIN: "got_checkin",
    }[status] || String(status || "pendente").toLowerCase();
}

function calculateNights(checkin, checkout) {
    if (!checkin || !checkout) {
        return 0;
    }

    return Math.max(0, Math.round((new Date(checkout) - new Date(checkin)) / 86400000));
}

function formatShortDate(date) {
    if (!date) {
        return "-";
    }

    const [, month, day] = date.split("-");
    return `${day}/${month}`;
}

function formatMoney(value) {
    return `R$ ${(Number(value) || 0).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`;
}

function formatPax(reservation) {
    return `${reservation.adults}A${reservation.children ? ` ${reservation.children}C` : ""}${reservation.pets ? ` ${reservation.pets}P` : ""}`;
}

function showToast(container, message, icon) {
    const toast = container.querySelector("#reservations-toast");
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
