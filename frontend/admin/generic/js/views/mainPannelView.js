import { findAllBookings, findAllCheckIns, findAllCheckOuts, findMetricsSummary } from "../api.js?v=2026-08-11-api-log-transport";
import { renderRoomTimelineWidget } from "../widgets/roomTimelineWidget.js?v=2026-05-18-timeline-widget";

export function renderMainPannelView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    container.innerHTML = `
<div class="main">
  <div class="content">
    <!-- MÉTRICAS -->
    <div class="metrics">
      <div class="mc">
        <div class="mc-label">Ocupação</div>
        <div id="dashboard-occupancy" class="mc-value">-</div>
        <div id="dashboard-occupancy-sub" class="mc-sub"><span class="chip chip-lav">...</span> carregando</div>
      </div>
      <div class="mc">
        <div class="mc-label">Check-ins hoje</div>
        <div id="dashboard-checkins" class="mc-value">-</div>
        <div id="dashboard-checkins-sub" class="mc-sub"><span class="chip chip-lav">...</span></div>
      </div>
      <div class="mc">
        <div class="mc-label">Check-outs hoje</div>
        <div id="dashboard-checkouts" class="mc-value">-</div>
        <div id="dashboard-checkouts-sub" class="mc-sub"><span class="chip chip-amber">...</span></div>
      </div>
      <div class="mc">
        <div class="mc-label">Receita do mês</div>
        <div id="dashboard-revenue" class="mc-value">R$ -</div>
        <div id="dashboard-revenue-sub" class="mc-sub"><span class="chip chip-green">...</span></div>
      </div>
    </div>

    <div id="dashboard-room-timeline"></div>

    <!-- PAINEIS INFERIORES -->
    <div class="panels">
      <div class="panel">
        <div class="panel-title">Check-ins & check-outs de hoje</div>
        <div id="dashboard-today-events"></div>
      </div>

      <div class="panel">
        <div class="panel-title">Consumíveis — estoque atual</div>
        <div class="cons">
          <div class="con-item">
            <div class="con-icon" style="background:var(--lav-pale)"><i class="ti ti-droplet" style="color:var(--lav)"></i></div>
            <div class="con-body">
              <div class="con-name">Amenidades (xampu, sabonete)</div>
              <div class="con-bar"><div class="con-fill fill-ok" style="width:72%"></div></div>
            </div>
            <div class="con-qty qty-ok">72 un.</div>
          </div>
          <div class="con-item">
            <div class="con-icon" style="background:#FFF5E6"><i class="ti ti-bed" style="color:var(--amber)"></i></div>
            <div class="con-body">
              <div class="con-name">Enxoval (toalhas, lençóis)</div>
              <div class="con-bar"><div class="con-fill fill-low" style="width:28%"></div></div>
            </div>
            <div class="con-qty qty-low">28 jogos</div>
          </div>
          <div class="con-item">
            <div class="con-icon" style="background:#FFF5E6"><i class="ti ti-coffee" style="color:var(--amber)"></i></div>
            <div class="con-body">
              <div class="con-name">Café, chá e sachês</div>
              <div class="con-bar"><div class="con-fill fill-low" style="width:35%"></div></div>
            </div>
            <div class="con-qty qty-low">35 cx.</div>
          </div>
          <div class="con-item">
            <div class="con-icon" style="background:var(--rose-pale)"><i class="ti ti-spray" style="color:var(--rose)"></i></div>
            <div class="con-body">
              <div class="con-name">Produtos de limpeza ⚠</div>
              <div class="con-bar"><div class="con-fill fill-crit" style="width:8%"></div></div>
            </div>
            <div class="con-qty qty-crit">8 un.</div>
          </div>
          <div class="con-item">
            <div class="con-icon" style="background:var(--sage-pale)"><i class="ti ti-leaf" style="color:var(--sage)"></i></div>
            <div class="con-body">
              <div class="con-name">Mini geleias artesanais</div>
              <div class="con-bar"><div class="con-fill fill-ok" style="width:60%"></div></div>
            </div>
            <div class="con-qty qty-ok">60 un.</div>
          </div>
        </div>
      </div>
    </div>

  </div><!-- /content -->
</div><!-- /main -->
    `;

    renderRoomTimelineWidget("dashboard-room-timeline", {
        compact: true,
        initialScale: "week",
        title: "Calendário de ocupação",
    });
    loadDashboardData(container, options);
}

async function loadDashboardData(container, options) {
    try {
        const [bookingsResponse, checkInsResponse, checkOutsResponse, metricsResponse] = await Promise.all([
            findAllBookings(),
            findAllCheckIns(),
            findAllCheckOuts(),
            findMetricsSummary(),
        ]);
        const bookings = bookingsResponse.data || [];
        const checkIns = checkInsResponse.data || [];
        const checkOuts = checkOutsResponse.data || [];
        updateDashboardMetrics(container, metricsResponse.data || {});
        renderTodayEvents(container, { bookings, checkIns, checkOuts }, options);
    } catch (error) {
        container.querySelector("#dashboard-today-events").innerHTML = `<div class="ci-item"><div><div class="ci-name">Não foi possível carregar a dashboard</div><div class="ci-info">${escapeHtml(error.message || "Erro ao buscar dados.")}</div></div></div>`;
    }
}

function updateDashboardMetrics(container, metrics) {
    container.querySelector("#dashboard-occupancy").textContent = `${metrics.dashboardOccupancyPercent || 0}%`;
    container.querySelector("#dashboard-occupancy-sub").innerHTML = `<span class="chip chip-lav">${metrics.occupiedRooms || 0}/${metrics.totalRooms || 0}</span> quartos ocupados`;
    container.querySelector("#dashboard-checkins").textContent = String(metrics.dashboardDoneCheckInsToday || 0);
    container.querySelector("#dashboard-checkins-sub").innerHTML = `<span class="chip chip-lav">${metrics.dashboardPendingCheckInsToday || 0} pendente${metrics.dashboardPendingCheckInsToday === 1 ? "" : "s"}</span> previstos hoje`;
    container.querySelector("#dashboard-checkouts").textContent = String(metrics.dashboardDoneCheckOutsToday || 0);
    container.querySelector("#dashboard-checkouts-sub").innerHTML = `<span class="chip chip-amber">${metrics.dashboardPendingCheckOutsToday || 0} pendente${metrics.dashboardPendingCheckOutsToday === 1 ? "" : "s"}</span> previstos hoje`;
    container.querySelector("#dashboard-revenue").textContent = formatCompactCurrency(metrics.dashboardMonthlyRevenue);
    container.querySelector("#dashboard-revenue-sub").innerHTML = `<span class="chip chip-green">${metrics.dashboardMonthlyBookings || 0} reserva${metrics.dashboardMonthlyBookings === 1 ? "" : "s"}</span> no mês`;
}

function renderTodayEvents(container, { bookings, checkIns, checkOuts }, options) {
    const today = currentDate();
    const doneCheckInBookingIds = new Set(checkIns.filter((item) => isSameDate(item.createdAt, today)).map((item) => Number(item.bookingId)));
    const doneCheckOutBookingIds = new Set(checkOuts.filter((item) => isSameDate(item.actualCheckOutAt, today)).map((item) => Number(item.bookingId)));
    const checkInRows = bookings
        .filter((booking) => booking.checkInDate === today && isBlockingBooking(booking) && !doneCheckInBookingIds.has(Number(booking.id)))
        .map((booking) => renderTodayItem(booking.guestName, `${booking.adults || 0} adulto${booking.adults === 1 ? "" : "s"} · Check-in`, booking.roomNumber, "Prev. hoje", "Check-in", "act-in", "checkin", booking.id));
    const checkOutRows = bookings
        .filter((booking) => booking.checkOutDate === today && normalizeKey(booking.status) === "IN_STAY" && !doneCheckOutBookingIds.has(Number(booking.id)))
        .map((booking) => renderTodayItem(booking.guestName, "Check-out", booking.roomNumber, "Até hoje", "Checkout", "act-out", "checkout", booking.id));

    container.querySelector("#dashboard-today-events").innerHTML = [...checkInRows, ...checkOutRows].join("")
        || `<div class="ci-item"><div><div class="ci-name">Sem pendências para hoje</div><div class="ci-info">Check-ins e check-outs do dia estão em dia.</div></div></div>`;

    container.querySelectorAll("[data-dashboard-checkin-booking]").forEach((button) => {
        button.addEventListener("click", () => options.onCreateCheckIn?.(Number(button.dataset.dashboardCheckinBooking)));
    });

    container.querySelectorAll("[data-dashboard-checkout-booking]").forEach((button) => {
        button.addEventListener("click", () => options.onCreateCheckOut?.(Number(button.dataset.dashboardCheckoutBooking)));
    });
}

function renderTodayItem(name, info, roomNumber, time, action, actionClass, operation, id) {
    const actionAttribute = operation === "checkin"
        ? `data-dashboard-checkin-booking="${escapeHtml(id)}"`
        : `data-dashboard-checkout-booking="${escapeHtml(id)}"`;

    return `
      <div class="ci-item">
        <div>
          <div class="ci-name">${escapeHtml(name || "-")}</div>
          <div class="ci-info">${escapeHtml(info)}</div>
        </div>
        <div class="ci-right">
          <div class="ci-room">Qto ${escapeHtml(roomNumber || "-")}</div>
          <div class="ci-time">${escapeHtml(time)}</div>
          <button class="act ${actionClass}" type="button" ${actionAttribute}>${escapeHtml(action)}</button>
        </div>
      </div>
    `;
}

function isBlockingBooking(booking) {
    return ["UNCONFIRMED", "CONFIRMED", "IN_STAY"].includes(normalizeKey(booking.status));
}

function isSameDate(dateTime, date) {
    return String(dateTime || "").slice(0, 10) === date;
}

function currentDate() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function formatCompactCurrency(value) {
    const amount = Number(value) || 0;
    return amount.toLocaleString("pt-BR", {
        style: "currency",
        currency: "BRL",
        maximumFractionDigits: 0,
    });
}

function normalizeKey(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toUpperCase();
}

function escapeHtml(valueToEscape) {
    return String(valueToEscape || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
