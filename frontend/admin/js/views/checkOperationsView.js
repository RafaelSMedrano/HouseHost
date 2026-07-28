import { findAllCheckIns, findAllCheckOuts, findMetricsSummary } from "../api.js?v=2026-05-18-metrics";

export function renderCheckInView(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = `
<div class="main guests-main">
  <div class="content guests-page">
    ${kpisShell("checkin", "Check-ins realizados")}
    <div class="guests-toolbar">
      <label class="guests-search">
        <i class="ti ti-search"></i>
        <input id="checkin-search" type="text" placeholder="Hóspede, quarto, reserva, placa..." autocomplete="off">
      </label>
      <select id="checkin-status" class="guests-select">
        <option value="">Todos os status</option>
        <option value="COMPLETED">Concluído</option>
        <option value="PENDING">Pendente</option>
        <option value="CANCELLED">Cancelado</option>
        <option value="NO_SHOW">No-show</option>
      </select>
    </div>
    ${tableShell("checkin", "Hóspede / reserva", "Entrada", "Placa")}
  </div>
  <div id="checkin-toast" class="booking-toast"><i class="ti ti-check"></i><span></span></div>
</div>`;

    bindCheckInList(container);
}

export function renderCheckOutView(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = `
<div class="main guests-main">
  <div class="content guests-page">
    ${kpisShell("checkout", "Check-outs realizados")}
    <div class="guests-toolbar">
      <label class="guests-search">
        <i class="ti ti-search"></i>
        <input id="checkout-search" type="text" placeholder="Hóspede, quarto, estadia..." autocomplete="off">
      </label>
      <select id="checkout-status" class="guests-select">
        <option value="">Todos os status</option>
        <option value="COMPLETED">Concluído</option>
        <option value="PENDING">Pendente</option>
        <option value="CANCELLED">Cancelado</option>
      </select>
    </div>
    ${tableShell("checkout", "Hóspede / estadia", "Saída", "Extras")}
  </div>
  <div id="checkout-toast" class="booking-toast"><i class="ti ti-check"></i><span></span></div>
</div>`;

    bindCheckOutList(container);
}

function bindCheckInList(container) {
    const state = { items: [], metrics: {}, query: "", status: "" };
    container.querySelector("#checkin-search").addEventListener("input", (event) => {
        state.query = event.target.value.trim().toLowerCase();
        renderCheckIns(container, state);
    });
    container.querySelector("#checkin-status").addEventListener("change", (event) => {
        state.status = event.target.value;
        renderCheckIns(container, state);
    });
    loadCheckIns(container, state);
}

async function loadCheckIns(container, state) {
    try {
        const [response, metricsResponse] = await Promise.all([
            findAllCheckIns(),
            findMetricsSummary(),
        ]);
        state.items = response.data || [];
        state.metrics = metricsResponse.data || {};
        renderCheckIns(container, state);
    } catch (error) {
        showToast(container, "checkin", error.message, "ti-alert-circle");
        container.querySelector("#checkin-list-count").textContent = "Não foi possível carregar check-ins.";
    }
}

function renderCheckIns(container, state) {
    const data = filteredItems(state, checkInSearchText).sort((a, b) => String(b.createdAt || "").localeCompare(String(a.createdAt || "")));
    const tbody = container.querySelector("#checkin-table-body");
    const empty = container.querySelector("#checkin-empty");

    renderKpis(container, "checkin", state.metrics, data);
    container.querySelector("#checkin-list-count").innerHTML = `Mostrando <strong>${data.length}</strong> de <strong>${state.items.length}</strong> check-ins realizados`;
    empty.classList.toggle("active", data.length === 0);
    tbody.innerHTML = data.map((item) => `
      <tr>
        <td><div class="guest-row-main"><span class="guest-row-avatar">${initialsFor(item.guestName)}</span><div><strong>${escapeHtml(item.guestName || "-")}</strong><small>Reserva #${escapeHtml(item.bookingId || "-")}</small></div></div></td>
        <td>${escapeHtml(item.roomNumber || "-")}</td>
        <td>${formatDateTime(item.createdAt)}</td>
        <td class="mono">${escapeHtml(item.vehiclePlate || "-")}</td>
        <td>${statusBadge(item.status)}</td>
      </tr>
    `).join("");
}

function bindCheckOutList(container) {
    const state = { items: [], metrics: {}, query: "", status: "" };
    container.querySelector("#checkout-search").addEventListener("input", (event) => {
        state.query = event.target.value.trim().toLowerCase();
        renderCheckOuts(container, state);
    });
    container.querySelector("#checkout-status").addEventListener("change", (event) => {
        state.status = event.target.value;
        renderCheckOuts(container, state);
    });
    loadCheckOuts(container, state);
}

async function loadCheckOuts(container, state) {
    try {
        const [response, metricsResponse] = await Promise.all([
            findAllCheckOuts(),
            findMetricsSummary(),
        ]);
        state.items = response.data || [];
        state.metrics = metricsResponse.data || {};
        renderCheckOuts(container, state);
    } catch (error) {
        showToast(container, "checkout", error.message, "ti-alert-circle");
        container.querySelector("#checkout-list-count").textContent = "Não foi possível carregar check-outs.";
    }
}

function renderCheckOuts(container, state) {
    const data = filteredItems(state, checkOutSearchText).sort((a, b) => String(b.actualCheckOutAt || "").localeCompare(String(a.actualCheckOutAt || "")));
    const tbody = container.querySelector("#checkout-table-body");
    const empty = container.querySelector("#checkout-empty");

    renderKpis(container, "checkout", state.metrics, data);
    container.querySelector("#checkout-list-count").innerHTML = `Mostrando <strong>${data.length}</strong> de <strong>${state.items.length}</strong> check-outs realizados`;
    empty.classList.toggle("active", data.length === 0);
    tbody.innerHTML = data.map((item) => `
      <tr>
        <td><div class="guest-row-main"><span class="guest-row-avatar">${initialsFor(item.guestName)}</span><div><strong>${escapeHtml(item.guestName || "-")}</strong><small>Reserva #${escapeHtml(item.bookingId || "-")}</small></div></div></td>
        <td>${escapeHtml(item.roomNumber || "-")}</td>
        <td>${formatDateTime(item.actualCheckOutAt)}</td>
        <td>${formatMoney(item.extraCharges)}</td>
        <td>${statusBadge(item.status)}</td>
      </tr>
    `).join("");
}

function filteredItems(state, toSearchText) {
    return state.items.filter((item) => {
        const matchesStatus = !state.status || item.status === state.status;
        const matchesQuery = !state.query || toSearchText(item).includes(state.query);
        return matchesStatus && matchesQuery;
    });
}

function checkInSearchText(item) {
    return `${item.guestName || ""} ${item.roomNumber || ""} ${item.bookingId || ""} ${item.vehiclePlate || ""} ${item.status || ""}`.toLowerCase();
}

function checkOutSearchText(item) {
    return `${item.guestName || ""} ${item.roomNumber || ""} ${item.bookingId || ""} ${item.status || ""}`.toLowerCase();
}

function renderKpis(container, prefix, metrics, filtered) {
    const remainingToday = prefix === "checkin" ? metrics.dashboardPendingCheckInsToday : metrics.dashboardPendingCheckOutsToday;
    const today = prefix === "checkin" ? metrics.checkInsToday : metrics.checkOutsToday;
    const week = prefix === "checkin" ? metrics.checkInsThisWeek : metrics.checkOutsThisWeek;
    const month = prefix === "checkin" ? metrics.checkInsThisMonth : metrics.checkOutsThisMonth;

    container.querySelector(`#${prefix}-kpi-remaining-today`).textContent = String(remainingToday || 0);
    container.querySelector(`#${prefix}-kpi-today`).textContent = String(today || 0);
    container.querySelector(`#${prefix}-kpi-week`).textContent = String(week || 0);
    container.querySelector(`#${prefix}-kpi-month`).textContent = String(month || 0);
}

function kpisShell(prefix, title) {
    const operationLabel = prefix === "checkin" ? "Check-ins" : "Check-outs";
    const remainingLabel = `${operationLabel} restantes para hoje`;
    return `
      <div class="guests-kpis check-kpis">
        <div class="guests-kpi"><span class="kpi-dot dot-sage"></span><div class="kpi-label">${remainingLabel}</div><div id="${prefix}-kpi-remaining-today" class="kpi-val">-</div><div class="kpi-sub">previstos pendentes</div></div>
        <div class="guests-kpi"><span class="kpi-dot dot-confirmed"></span><div class="kpi-label">Realizados hoje</div><div id="${prefix}-kpi-today" class="kpi-val">-</div><div class="kpi-sub">${title}</div></div>
        <div class="guests-kpi no-kpi-dot"><div class="kpi-label">Realizados na semana</div><div id="${prefix}-kpi-week" class="kpi-val">-</div><div class="kpi-sub">semana atual</div></div>
        <div class="guests-kpi no-kpi-dot"><div class="kpi-label">Realizados no mês</div><div id="${prefix}-kpi-month" class="kpi-val">-</div><div class="kpi-sub">mês atual</div></div>
      </div>
    `;
}

function tableShell(prefix, firstColumn, dateColumn, valueColumn) {
    return `
      <div class="guests-table-wrap">
        <table class="guests-table">
          <thead>
            <tr>
              <th>${firstColumn}</th>
              <th>Quarto</th>
              <th>${dateColumn}</th>
              <th>${valueColumn}</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody id="${prefix}-table-body"></tbody>
        </table>
        <div id="${prefix}-empty" class="guests-empty">
          <i class="ti ti-clipboard-off"></i>
          <p>Nenhum registro encontrado.</p>
        </div>
        <div class="guests-pagination"><span id="${prefix}-list-count">Carregando registros...</span></div>
      </div>
    `;
}

function statusBadge(status) {
    const normalized = status || "-";
    const label = {
        COMPLETED: "Concluído",
        PENDING: "Pendente",
        CANCELLED: "Cancelado",
        NO_SHOW: "No-show",
    }[normalized] || normalized;
    const css = normalized === "COMPLETED" ? "regular" : normalized === "PENDING" ? "new" : "vip";
    return `<span class="guest-type-badge ${css}">${escapeHtml(label)}</span>`;
}

function formatDateTime(valueToFormat) {
    if (!valueToFormat) return "-";
    const date = new Date(valueToFormat);
    return Number.isNaN(date.getTime()) ? valueToFormat : date.toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });
}

function formatMoney(valueToFormat) {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(Number(valueToFormat) || 0);
}

function currentDate() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

function initialsFor(name) {
    return String(name || "?")
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("") || "?";
}

function showToast(container, prefix, message, icon) {
    const toast = container.querySelector(`#${prefix}-toast`);
    toast.querySelector("span").textContent = message;
    toast.querySelector("i").className = `ti ${icon}`;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2600);
}

function escapeHtml(valueToEscape) {
    return String(valueToEscape ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
