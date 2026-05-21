import { findAllCashiers, findCashierEntriesByCashierId, findCashierExpensesByCashierId } from "../api.js?v=2026-05-20-cashier-movements";

const MAIN_CASHIER_ID = 1;

export function renderFinanceView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main finance-main">
  <div class="content finance-page">
    <section class="metrics finance-metrics">
      <div class="mc">
        <div class="mc-label">Valor em caixa</div>
        <div id="finance-cash-on-hand" class="mc-value">R$ -</div>
        <div class="mc-sub"><span class="chip chip-green">Caixa #${MAIN_CASHIER_ID}</span> saldo atual</div>
      </div>
      <div class="mc">
        <div class="mc-label">Valor esperado mensal</div>
        <div id="finance-month-expected" class="mc-value">R$ -</div>
        <div class="mc-sub"><span id="finance-movement-count" class="chip chip-lav">...</span> movimentos no mês</div>
      </div>
      <div class="mc">
        <div class="mc-label">Balanço do mês</div>
        <div id="finance-month-balance" class="mc-value">R$ -</div>
        <div class="mc-sub"><span class="chip chip-amber">Mês atual</span> entradas - saídas</div>
      </div>
      <div class="mc">
        <div class="mc-label">Entradas e saídas</div>
        <div id="finance-month-movements" class="mc-value">R$ -</div>
        <div class="mc-sub"><span id="finance-entry-amount" class="chip chip-green">R$ 0,00 entradas</span><span id="finance-expense-amount" class="chip chip-rose">R$ 0,00 saídas</span></div>
      </div>
    </section>

    <section class="finance-panel">
      <div class="finance-panel-head">
        <div>
          <span>Movimentos do caixa</span>
          <strong>Entradas e saídas do Caixa #${MAIN_CASHIER_ID}</strong>
        </div>
      </div>

      <div class="finance-table-wrap">
        <table class="finance-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Data</th>
              <th>Descrição</th>
              <th>Origem</th>
              <th>Tipo</th>
              <th>Status</th>
              <th>Valor</th>
            </tr>
          </thead>
          <tbody id="finance-transactions-body"></tbody>
        </table>
        <div id="finance-empty" class="finance-empty">Nenhuma entrada ou saída relacionada ao caixa principal.</div>
      </div>
    </section>
  </div>
  <div id="finance-toast" class="booking-toast"><i class="ti ti-alert-circle"></i><span></span></div>
</div>
    `;

    loadFinance(container);
}

async function loadFinance(container) {
    try {
        const [cashiersResponse, entriesResponse, expensesResponse] = await Promise.all([
            findAllCashiers(),
            findCashierEntriesByCashierId(MAIN_CASHIER_ID),
            findCashierExpensesByCashierId(MAIN_CASHIER_ID),
        ]);
        const cashiers = cashiersResponse.data || [];
        const mainCashier = cashiers.find((cashier) => Number(cashier.id) === MAIN_CASHIER_ID);
        const movements = [
            ...(entriesResponse.data || []).map(normalizeEntry),
            ...(expensesResponse.data || []).map(normalizeExpense),
        ].sort((a, b) => String(b.date || "").localeCompare(String(a.date || "")) || Number(b.id || 0) - Number(a.id || 0));
        const monthlyMovements = movements.filter(isCurrentMonthDashboardMovement);
        const monthlyEntryAmount = monthlyMovements
            .filter((movement) => movement.direction === "ENTRY")
            .reduce((total, movement) => total + moneyValue(movement.amount), 0);
        const monthlyExpenseAmount = monthlyMovements
            .filter((movement) => movement.direction === "EXPENSE")
            .reduce((total, movement) => total + Math.abs(moneyValue(movement.amount)), 0);
        const monthlyBalance = monthlyEntryAmount - monthlyExpenseAmount;

        container.querySelector("#finance-cash-on-hand").textContent = formatMoney(mainCashier?.cashOnHand);
        container.querySelector("#finance-month-expected").textContent = formatMoney(monthlyBalance);
        container.querySelector("#finance-month-balance").textContent = formatMoney(monthlyBalance);
        container.querySelector("#finance-month-movements").textContent = `${formatCompactMoney(monthlyEntryAmount)} / ${formatCompactMoney(monthlyExpenseAmount)}`;
        container.querySelector("#finance-movement-count").textContent = `${monthlyMovements.length} mov.`;
        container.querySelector("#finance-entry-amount").textContent = `${formatMoney(monthlyEntryAmount)} entradas`;
        container.querySelector("#finance-expense-amount").textContent = `${formatMoney(monthlyExpenseAmount)} saídas`;
        renderMovements(container, movements);
    } catch (error) {
        showToast(container, error.message || "Nao foi possivel carregar caixa.");
        renderMovements(container, []);
    }
}

function normalizeEntry(entry) {
    return {
        id: entry.id,
        date: entry.entryDate,
        description: entry.description,
        source: entry.source || sourceTransactionText(entry),
        direction: "ENTRY",
        status: entry.status,
        amount: moneyValue(entry.amount),
    };
}

function normalizeExpense(expense) {
    return {
        id: expense.id,
        date: expense.expenseDate,
        description: expense.description,
        source: expense.category || sourceTransactionText(expense),
        direction: "EXPENSE",
        status: expense.status,
        amount: -Math.abs(moneyValue(expense.amount)),
    };
}

function renderMovements(container, movements) {
    const tbody = container.querySelector("#finance-transactions-body");
    const empty = container.querySelector("#finance-empty");

    tbody.innerHTML = movements.map((movement) => `
      <tr>
        <td class="td-id">#${escapeHtml(movement.id)}</td>
        <td>${escapeHtml(formatDate(movement.date))}</td>
        <td><div class="finance-description">${escapeHtml(movement.description || "-")}</div></td>
        <td>${escapeHtml(movement.source || "-")}</td>
        <td>${directionBadge(movement)}</td>
        <td>${statusBadge(movement.status)}</td>
        <td class="finance-money">${formatMoney(movement.amount)}</td>
      </tr>
    `).join("");

    empty.classList.toggle("active", movements.length === 0);
}

function isCurrentMonthDashboardMovement(movement) {
    if (!isDashboardStatus(movement)) {
        return false;
    }

    const date = parseDate(movement.date);
    if (!date) {
        return false;
    }

    const today = new Date();
    return date.getFullYear() === today.getFullYear() && date.getMonth() === today.getMonth();
}

function isDashboardStatus(movement) {
    return ["SETTLED", "PAID", "WAITING"].includes(String(movement.status || "").toUpperCase());
}

function directionBadge(movement) {
    const isEntry = movement.direction === "ENTRY";
    const css = isEntry ? "entry" : "expense";
    const label = isEntry ? "Entrada" : "Saída";
    const icon = isEntry ? "ti-arrow-down-left" : "ti-arrow-up-right";
    return `<span class="finance-direction ${css}"><i class="ti ${icon}"></i>${label}</span>`;
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
        NOT_REALIZED: "Nao realizada",
        CANCELED: "Cancelada",
    }[status] || status;
}

function sourceTransactionText(movement) {
    if (!movement.sourceTransactionId) {
        return null;
    }

    return `FT #${movement.sourceTransactionId}`;
}

function parseDate(value) {
    if (!value) {
        return null;
    }

    const date = new Date(`${value}T00:00:00`);
    return Number.isNaN(date.getTime()) ? null : date;
}

function formatDate(value) {
    const date = parseDate(value);
    if (!date) {
        return "-";
    }

    return date.toLocaleDateString("pt-BR");
}

function moneyValue(value) {
    return Number(value) || 0;
}

function formatMoney(value) {
    return `R$ ${moneyValue(value).toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`;
}

function formatCompactMoney(value) {
    return `R$ ${moneyValue(value).toLocaleString("pt-BR", {
        maximumFractionDigits: 1,
        notation: "compact",
        compactDisplay: "short",
    })}`;
}

function showToast(container, message) {
    const toast = container.querySelector("#finance-toast");
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
