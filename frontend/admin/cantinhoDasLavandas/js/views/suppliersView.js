import { findAllSuppliers } from "../api.js?v=2026-07-26-supplier-management";

export function renderSuppliersView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    container.innerHTML = `
      <main class="main supplier-main"><div class="supplier-page">
        <header class="supplier-header"><div><span>Governança LGPD</span><h1>Fornecedores</h1><p>Inventário interno de prestadores e relações de tratamento.</p></div>
          <button class="btn btn-primary" type="button" data-new-supplier><i class="ti ti-plus"></i>Novo fornecedor</button></header>
        <form class="supplier-filters" data-supplier-filters>
          <label>Nome<input name="name" type="search" placeholder="Buscar fornecedor"></label>
          ${selectField("role", "Papel LGPD", ["OPERATOR", "SUB_OPERATOR", "INDEPENDENT_CONTROLLER", "JOINT_CONTROLLER", "RECIPIENT", "NO_PERSONAL_DATA"])}
          ${selectField("risk", "Risco", ["LOW", "MEDIUM", "HIGH"])}
          ${selectField("governanceStatus", "Governança", ["DRAFT", "PENDING", "APPROVED", "BLOCKED", "INACTIVE"])}
          ${selectField("status", "Situação", ["ACTIVE", "INACTIVE"])}
          <button class="dashboard-back-btn" type="submit"><i class="ti ti-filter"></i>Filtrar</button>
        </form>
        <div class="supplier-feedback" role="status" aria-live="polite"></div>
        <div class="supplier-list" data-supplier-list><div class="supplier-empty">Carregando fornecedores...</div></div>
      </div></main>`;
    container.querySelector("[data-new-supplier]").onclick = () => options.onNewSupplier?.();
    container.querySelector("[data-supplier-filters]").onsubmit = (event) => {
        event.preventDefault();
        loadSuppliers(container, options);
    };
    loadSuppliers(container, options);
}

async function loadSuppliers(container, options) {
    const form = container.querySelector("[data-supplier-filters]");
    const filters = Object.fromEntries(new FormData(form).entries());
    const list = container.querySelector("[data-supplier-list]");
    const feedback = container.querySelector(".supplier-feedback");
    list.innerHTML = '<div class="supplier-empty">Carregando fornecedores...</div>';
    try {
        const response = await findAllSuppliers(filters);
        const supplierList = Array.isArray(response.data) ? response.data : [];
        feedback.textContent = `${supplierList.length} fornecedor(es) encontrado(s).`;
        list.innerHTML = supplierList.length ? supplierList.map(supplierCard).join("")
                : '<div class="supplier-empty">Nenhum fornecedor encontrado.</div>';
        list.querySelectorAll("[data-supplier-id]").forEach((button) => {
            button.onclick = () => options.onOpenSupplier?.(Number(button.dataset.supplierId));
        });
    } catch (error) {
        feedback.textContent = error.message || "Não foi possível carregar fornecedores.";
        list.innerHTML = '<div class="supplier-empty supplier-error">Não foi possível carregar o inventário.</div>';
    }
}

function supplierCard(supplier) {
    return `<button class="supplier-card" type="button" data-supplier-id="${escapeHtml(supplier.id)}">
      <div><span>${escapeHtml(supplier.tradeName || supplier.officialName)}</span><strong>${escapeHtml(supplier.officialName)}</strong><small>${escapeHtml(supplier.principalService || "Serviço não informado")}</small></div>
      <div class="supplier-card-tags">${(supplier.roleList || []).map((role) => badge(label(role), "neutral")).join("")}${badge(label(supplier.highestRisk), riskClass(supplier.highestRisk))}${badge(label(supplier.governanceStatus), "governance")}${badge(label(supplier.status), supplier.status === "ACTIVE" ? "active" : "inactive")}${supplier.overdueReview ? badge("Revisão vencida", "danger") : ""}</div>
      <span class="supplier-review-date">Próxima revisão: ${formatDate(supplier.nextReviewDate)}</span><i class="ti ti-chevron-right"></i>
    </button>`;
}

function selectField(name, title, values) {
    return `<label>${title}<select name="${name}"><option value="">Todos</option>${values.map((value) => `<option value="${value}">${label(value)}</option>`).join("")}</select></label>`;
}
function badge(text, type) { return `<span class="supplier-badge ${type}">${escapeHtml(text)}</span>`; }
function riskClass(risk) { return risk === "HIGH" ? "danger" : risk === "MEDIUM" ? "warning" : "active"; }
function label(value) { return ({OPERATOR:"Operador",SUB_OPERATOR:"Suboperador",INDEPENDENT_CONTROLLER:"Controlador independente",JOINT_CONTROLLER:"Controlador conjunto",RECIPIENT:"Destinatário",NO_PERSONAL_DATA:"Sem dados pessoais",LOW:"Baixo",MEDIUM:"Médio",HIGH:"Alto",DRAFT:"Rascunho",PENDING:"Pendente",APPROVED:"Aprovado",BLOCKED:"Bloqueado",INACTIVE:"Inativo",ACTIVE:"Ativo"})[value] || value || "-"; }
function formatDate(value) { return value ? new Intl.DateTimeFormat("pt-BR").format(new Date(`${value}T12:00:00`)) : "Não definida"; }
function escapeHtml(value) { return String(value ?? "").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#039;"); }
