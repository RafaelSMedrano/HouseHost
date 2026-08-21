import { findAllDataProcessingOperations } from "../api.js?v=2026-08-11-api-log-transport";
import { bindGovernanceTabs, governanceTabsMarkup } from "./legalBasisAssessmentPresentation.js?v=2026-07-26-legal-basis-workflow";

const PENDING_STATUSES = new Set(["DRAFT", "UNDER_REVIEW"]);

export function renderDataProcessingOperationsView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    if (!container) {
        return;
    }

    container.innerHTML = `
      <main class="main privacy-governance-main privacy-governance-list-main">
        <div class="privacy-governance-page">
          <header class="privacy-governance-header">
            <div>
              <span>Governança LGPD</span>
              <h1>Tratamentos e bases legais</h1>
              <p>Inventário de operações e situação das avaliações de bases legais.</p>
            </div>
          </header>
          ${governanceTabsMarkup("operations")}
          <form class="privacy-governance-filters" data-processing-operation-filters>
            <label for="processing-operation-search">Buscar por nome ou código
              <input id="processing-operation-search" name="search" type="search" autocomplete="off" placeholder="Ex.: reservas">
            </label>
            ${selectField("processing-operation-status", "status", "Situação operacional", ["ACTIVE", "INACTIVE"])}
            ${selectField("processing-operation-readiness", "readiness", "Prontidão da base legal", ["NOT_ASSESSED", "DRAFT", "UNDER_REVIEW", "APPROVED", "REJECTED"])}
            ${selectField("processing-operation-work", "work", "Trabalho pendente", ["PENDING", "REJECTED", "CLEAR"])}
            <label for="processing-operation-area">Área responsável
              <select id="processing-operation-area" name="responsibleArea"><option value="">Todas</option></select>
            </label>
            <button class="dashboard-back-btn" type="reset"><i class="ti ti-filter-off"></i>Limpar</button>
          </form>
          <div class="privacy-governance-feedback" role="status" aria-live="polite" data-processing-operation-feedback>Carregando operações...</div>
          <div class="privacy-governance-list" data-processing-operation-list aria-busy="true">
            ${stateMarkup("loading", "Carregando o inventário de tratamentos...")}
          </div>
        </div>
      </main>`;

    const state = { operationList: [], loaded: false };
    bindGovernanceTabs(container, options);
    const filterForm = container.querySelector("[data-processing-operation-filters]");
    filterForm.addEventListener("input", () => renderFilteredList(container, state, options));
    filterForm.addEventListener("change", () => renderFilteredList(container, state, options));
    filterForm.addEventListener("reset", () => {
        setTimeout(() => renderFilteredList(container, state, options), 0);
    });
    loadOperations(container, state, options);
}

async function loadOperations(container, state, options) {
    const operationListContainer = container.querySelector("[data-processing-operation-list]");
    const feedback = container.querySelector("[data-processing-operation-feedback]");
    operationListContainer.setAttribute("aria-busy", "true");
    feedback.textContent = "Carregando operações...";

    try {
        const response = await findAllDataProcessingOperations();
        state.operationList = Array.isArray(response?.data) ? response.data : [];
        state.loaded = true;
        populateResponsibleAreas(container, state.operationList);
        renderFilteredList(container, state, options);
    } catch (error) {
        state.loaded = false;
        feedback.textContent = error?.message || "Não foi possível carregar as operações de tratamento.";
        operationListContainer.setAttribute("aria-busy", "false");
        operationListContainer.innerHTML = stateMarkup(
                "error",
                "Falha ao carregar o inventário. Isso não significa que o inventário esteja vazio.",
                true
        );
        operationListContainer.querySelector("[data-retry-processing-operations]")
                ?.addEventListener("click", () => loadOperations(container, state, options));
    }
}

function renderFilteredList(container, state, options) {
    if (!state.loaded) {
        return;
    }
    const operationListContainer = container.querySelector("[data-processing-operation-list]");
    const feedback = container.querySelector("[data-processing-operation-feedback]");
    const filterForm = container.querySelector("[data-processing-operation-filters]");
    const filters = Object.fromEntries(new FormData(filterForm).entries());
    const filteredOperationList = filterDataProcessingOperations(state.operationList, filters);

    operationListContainer.setAttribute("aria-busy", "false");
    if (state.operationList.length === 0) {
        feedback.textContent = "Inventário carregado sem operações cadastradas.";
        operationListContainer.innerHTML = stateMarkup("empty", "Nenhuma operação está cadastrada no inventário.");
        return;
    }
    if (filteredOperationList.length === 0) {
        feedback.textContent = "Nenhuma operação corresponde aos filtros selecionados.";
        operationListContainer.innerHTML = stateMarkup("filtered", "Nenhum resultado para estes filtros.");
        return;
    }

    feedback.textContent = `${filteredOperationList.length} de ${state.operationList.length} operação(ões) exibida(s).`;
    operationListContainer.innerHTML = `
      <div class="privacy-operation-list-header" aria-hidden="true">
        <span>Operação</span>
        <span>Situação e base legal</span>
        <span>Avaliações e revisão</span>
        <span>Abrir</span>
      </div>
      ${filteredOperationList.map(buildOperationListRowMarkup).join("")}`;
    operationListContainer.querySelectorAll("[data-processing-operation-id]").forEach((button) => {
        button.addEventListener("click", () => {
            options.onOpenOperation?.(Number(button.dataset.processingOperationId));
        });
    });
}

export function filterDataProcessingOperations(operationList, filters = {}) {
    const query = normalizeSearch(filters.search);
    return operationList.filter((operation) => {
        const assessmentList = assessmentSummaryList(operation);
        const currentAssessmentList = assessmentList.filter((assessment) => assessment.current);
        const searchableText = normalizeSearch(`${operation.operationName || ""} ${operation.operationCode || ""}`);
        const hasPending = currentAssessmentList.some((assessment) => PENDING_STATUSES.has(assessment.status));
        const hasRejected = currentAssessmentList.some((assessment) => assessment.status === "REJECTED");
        const matchesWork = !filters.work
                || (filters.work === "PENDING" && hasPending)
                || (filters.work === "REJECTED" && hasRejected)
                || (filters.work === "CLEAR" && !hasPending && !hasRejected);
        return (!query || searchableText.includes(query))
                && (!filters.status || operation.status === filters.status)
                && (!filters.readiness || operation.legalBasisReadiness === filters.readiness)
                && (!filters.responsibleArea || operation.responsibleArea === filters.responsibleArea)
                && matchesWork;
    });
}

export function buildOperationListRowMarkup(operation) {
    const assessmentList = assessmentSummaryList(operation);
    const currentAssessmentList = assessmentList.filter((assessment) => assessment.current);
    const pendingAssessmentList = currentAssessmentList.filter((assessment) => PENDING_STATUSES.has(assessment.status));
    const rejectedAssessmentList = currentAssessmentList.filter((assessment) => assessment.status === "REJECTED");
    const isMarketing = operation.operationCode === "WHATSAPP_MARKETING";
    return `
      <button class="privacy-operation-row" type="button" data-processing-operation-id="${escapeHtml(operation.id)}">
        <div class="privacy-operation-identity">
          <span>${escapeHtml(operation.operationCode || "SEM_CODIGO")}</span>
          <strong>${escapeHtml(operation.operationName || "Operação sem nome")}</strong>
          <small>${escapeHtml(operation.responsibleArea || "Área não informada")}</small>
        </div>
        <div class="privacy-operation-badges">
          ${badge(statusLabel(operation.status), statusClass(operation.status))}
          ${badge(readinessLabel(operation.legalBasisReadiness), readinessClass(operation.legalBasisReadiness))}
          ${pendingAssessmentList.length ? badge(`${pendingAssessmentList.length} pendente(s)`, "warning") : ""}
          ${rejectedAssessmentList.length ? badge(`${rejectedAssessmentList.length} rejeitada(s)`, "danger") : ""}
          ${isMarketing ? badge("Marketing inativo — sem autorização atual", "inactive") : ""}
        </div>
        <div class="privacy-operation-metrics">
          <span><strong>${currentAssessmentList.length}</strong> avaliação(ões) atual(is)</span>
          <span>Revisão do inventário: ${escapeHtml(formatDateTime(operation.reviewedAt))}</span>
          <span>Última aprovação legal: ${escapeHtml(lastApprovalText(assessmentList))}</span>
        </div>
        <span class="privacy-operation-open"><span>Ver perfil</span><i class="ti ti-chevron-right" aria-hidden="true"></i></span>
      </button>`;
}

function populateResponsibleAreas(container, operationList) {
    const areaSelect = container.querySelector("#processing-operation-area");
    const areaList = [...new Set(operationList.map((operation) => operation.responsibleArea).filter(Boolean))]
            .sort((first, second) => first.localeCompare(second, "pt-BR"));
    areaList.forEach((area) => {
        const option = document.createElement("option");
        option.value = area;
        option.textContent = area;
        areaSelect.appendChild(option);
    });
}

function assessmentSummaryList(operation) {
    return Array.isArray(operation.legalBasisAssessmentList) ? operation.legalBasisAssessmentList : [];
}

function lastApprovalText(assessmentList) {
    const approvedDateList = assessmentList
            .filter((assessment) => assessment.status === "APPROVED" && assessment.reviewedAt)
            .map((assessment) => assessment.reviewedAt)
            .sort();
    return approvedDateList.length ? formatDateTime(approvedDateList.at(-1)) : "não disponível no resumo";
}

function selectField(id, name, title, valueList) {
    return `<label for="${id}">${title}<select id="${id}" name="${name}"><option value="">Todos</option>${valueList
            .map((value) => `<option value="${value}">${escapeHtml(filterLabel(value))}</option>`).join("")}</select></label>`;
}

function stateMarkup(type, message, retry = false) {
    return `<div class="privacy-governance-state ${type}"><i class="ti ${type === "error" ? "ti-alert-triangle" : "ti-file-search"}" aria-hidden="true"></i><p>${escapeHtml(message)}</p>${retry ? '<button class="dashboard-back-btn" type="button" data-retry-processing-operations>Tentar novamente</button>' : ""}</div>`;
}

function badge(text, type) {
    return `<span class="privacy-governance-badge ${type}">${escapeHtml(text)}</span>`;
}

function normalizeSearch(value) {
    return String(value || "").normalize("NFD").replaceAll(/[\u0300-\u036f]/g, "").trim().toLowerCase();
}

function statusLabel(status) {
    return status === "ACTIVE" ? "Operação ativa" : status === "INACTIVE" ? "Operação inativa" : status || "Situação não informada";
}

function readinessLabel(readiness) {
    return ({NOT_ASSESSED:"Base não avaliada",DRAFT:"Avaliação em rascunho",UNDER_REVIEW:"Avaliação em revisão",APPROVED:"Avaliação aprovada",REJECTED:"Avaliação rejeitada"})[readiness] || "Prontidão não informada";
}

function filterLabel(value) {
    return ({ACTIVE:"Ativa",INACTIVE:"Inativa",NOT_ASSESSED:"Não avaliada",DRAFT:"Rascunho",UNDER_REVIEW:"Em revisão",APPROVED:"Aprovada",REJECTED:"Rejeitada",PENDING:"Rascunho ou revisão",CLEAR:"Sem pendência/rejeição"})[value] || value;
}

function statusClass(status) {
    return status === "ACTIVE" ? "active" : "inactive";
}

function readinessClass(readiness) {
    return ({APPROVED:"active",DRAFT:"warning",UNDER_REVIEW:"warning",REJECTED:"danger",NOT_ASSESSED:"neutral"})[readiness] || "neutral";
}

function formatDateTime(value) {
    if (!value) {
        return "não realizada";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "data inválida" : new Intl.DateTimeFormat("pt-BR", {
        dateStyle: "short",
        timeStyle: "short",
    }).format(date);
}

function escapeHtml(value) {
    return String(value ?? "").replaceAll("&", "&amp;").replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}
