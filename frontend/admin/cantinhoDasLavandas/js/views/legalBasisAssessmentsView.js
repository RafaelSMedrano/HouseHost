import { findAllDataProcessingOperations } from "../api.js?v=2026-07-26-legal-basis-workflow";
import {
    assessmentStatusClass,
    assessmentStatusLabel,
    bindGovernanceTabs,
    escapeGovernanceHtml,
    governanceTabsMarkup,
    legalBasisLabel,
    LEGAL_BASIS_VALUE_LIST,
} from "./legalBasisAssessmentPresentation.js?v=2026-07-26-legal-basis-workflow";

export function renderLegalBasisAssessmentsView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    if (!container) {
        return;
    }
    container.innerHTML = `<main class="main privacy-governance-main privacy-governance-list-main"><div class="privacy-governance-page">
      <header class="privacy-governance-header"><div><span>Governança LGPD</span><h1>Tratamentos e bases legais</h1><p>Avaliações registradas por finalidade, operação e versão.</p></div></header>
      ${governanceTabsMarkup("assessments")}
      <form class="privacy-governance-filters" data-legal-basis-filters>
        <label for="legal-basis-search">Buscar finalidade, base ou operação<input id="legal-basis-search" name="search" type="search" autocomplete="off" placeholder="Ex.: reservas"></label>
        ${selectField("legal-basis-status", "status", "Situação", ["DRAFT", "UNDER_REVIEW", "APPROVED", "REJECTED", "SUPERSEDED"], assessmentStatusLabel)}
        ${selectField("legal-basis-type", "legalBasis", "Base legal", LEGAL_BASIS_VALUE_LIST, legalBasisLabel)}
        ${selectField("legal-basis-version", "versionState", "Versão", ["CURRENT", "HISTORICAL"], (value) => value === "CURRENT" ? "Atual" : "Histórica")}
        <label for="legal-basis-operation">Operação<select id="legal-basis-operation" name="processingOperationId"><option value="">Todas</option></select></label>
        <button class="dashboard-back-btn" type="reset"><i class="ti ti-filter-off"></i>Limpar</button>
      </form>
      <div class="privacy-governance-feedback" role="status" aria-live="polite" data-legal-basis-feedback>Carregando avaliações...</div>
      <div class="privacy-governance-list" data-legal-basis-list aria-busy="true">${stateMarkup("loading", "Carregando avaliações de bases legais...")}</div>
    </div></main>`;
    bindGovernanceTabs(container, options);
    const state = { assessmentList: [], loaded: false };
    const form = container.querySelector("[data-legal-basis-filters]");
    form.addEventListener("input", () => renderFilteredList(container, state, options));
    form.addEventListener("change", () => renderFilteredList(container, state, options));
    form.addEventListener("reset", () => setTimeout(() => renderFilteredList(container, state, options), 0));
    loadAssessments(container, state, options);
}

async function loadAssessments(container, state, options) {
    const list = container.querySelector("[data-legal-basis-list]");
    const feedback = container.querySelector("[data-legal-basis-feedback]");
    try {
        const response = await findAllDataProcessingOperations();
        const operationList = Array.isArray(response?.data) ? response.data : [];
        state.assessmentList = flattenLegalBasisAssessments(operationList);
        state.loaded = true;
        populateOperations(container, operationList);
        renderFilteredList(container, state, options);
    } catch (error) {
        state.loaded = false;
        list.setAttribute("aria-busy", "false");
        feedback.textContent = error?.message || "Não foi possível carregar as avaliações.";
        list.innerHTML = stateMarkup("error", "Falha ao carregar as avaliações. Isso não significa que o inventário esteja vazio.", true);
        list.querySelector("[data-retry-legal-bases]")?.addEventListener("click", () => loadAssessments(container, state, options));
    }
}

function renderFilteredList(container, state, options) {
    if (!state.loaded) {
        return;
    }
    const list = container.querySelector("[data-legal-basis-list]");
    const feedback = container.querySelector("[data-legal-basis-feedback]");
    const filters = Object.fromEntries(new FormData(container.querySelector("[data-legal-basis-filters]")).entries());
    const filteredAssessmentList = filterLegalBasisAssessments(state.assessmentList, filters);
    list.setAttribute("aria-busy", "false");
    if (state.assessmentList.length === 0) {
        feedback.textContent = "Inventário carregado sem avaliações cadastradas.";
        list.innerHTML = stateMarkup("empty", "Nenhuma avaliação de base legal está cadastrada.");
        return;
    }
    if (filteredAssessmentList.length === 0) {
        feedback.textContent = "Nenhuma avaliação corresponde aos filtros.";
        list.innerHTML = stateMarkup("filtered", "Nenhum resultado para estes filtros.");
        return;
    }
    feedback.textContent = `${filteredAssessmentList.length} de ${state.assessmentList.length} avaliação(ões) exibida(s).`;
    list.innerHTML = `<div class="privacy-assessment-list-header" aria-hidden="true"><span>Finalidade e operação</span><span>Base legal</span><span>Situação e versão</span><span>Abrir</span></div>${filteredAssessmentList.map(buildLegalBasisAssessmentRowMarkup).join("")}`;
    list.querySelectorAll("[data-legal-basis-assessment-id]").forEach((button) => {
        button.addEventListener("click", () => options.onOpenAssessment?.(Number(button.dataset.legalBasisAssessmentId)));
    });
}

export function flattenLegalBasisAssessments(operationList = []) {
    return operationList.flatMap((operation) => {
        const assessmentList = Array.isArray(operation.legalBasisAssessmentList) ? operation.legalBasisAssessmentList : [];
        return assessmentList.map((assessment) => ({
            ...assessment,
            processingOperationId: operation.id,
            operationName: operation.operationName,
            operationCode: operation.operationCode,
        }));
    });
}

export function filterLegalBasisAssessments(assessmentList, filters = {}) {
    const query = normalize(filters.search);
    return assessmentList.filter((assessment) => {
        const searchable = normalize(`${assessment.purpose || ""} ${legalBasisLabel(assessment.legalBasis)} ${assessment.operationName || ""} ${assessment.operationCode || ""}`);
        return (!query || searchable.includes(query))
                && (!filters.status || assessment.status === filters.status)
                && (!filters.legalBasis || assessment.legalBasis === filters.legalBasis)
                && (!filters.processingOperationId || String(assessment.processingOperationId) === filters.processingOperationId)
                && (!filters.versionState || (filters.versionState === "CURRENT" ? assessment.current : !assessment.current));
    });
}

export function buildLegalBasisAssessmentRowMarkup(assessment) {
    const versionMeaning = assessment.current ? "Versão atual" : assessment.status === "SUPERSEDED" ? "Versão substituída" : "Versão histórica";
    return `<button class="privacy-assessment-row" type="button" data-legal-basis-assessment-id="${escapeGovernanceHtml(assessment.id)}">
      <span class="privacy-assessment-row-identity"><strong>${escapeGovernanceHtml(assessment.purpose || "Finalidade não informada")}</strong><small>${escapeGovernanceHtml(assessment.operationName || "Operação não informada")} · ${escapeGovernanceHtml(assessment.operationCode || "SEM_CODIGO")}</small></span>
      <strong>${escapeGovernanceHtml(legalBasisLabel(assessment.legalBasis))}</strong>
      <span class="privacy-operation-badges"><span class="privacy-governance-badge ${assessmentStatusClass(assessment.status)}">${escapeGovernanceHtml(assessmentStatusLabel(assessment.status))}</span><span class="privacy-governance-badge neutral">Versão ${escapeGovernanceHtml(assessment.assessmentVersion || 1)}</span><span class="privacy-governance-badge ${assessment.current ? "current" : "neutral"}">${versionMeaning}</span></span>
      <span class="privacy-operation-open"><span>Ver perfil</span><i class="ti ti-chevron-right" aria-hidden="true"></i></span>
    </button>`;
}

function populateOperations(container, operationList) {
    const select = container.querySelector("#legal-basis-operation");
    operationList.forEach((operation) => {
        const option = document.createElement("option");
        option.value = operation.id;
        option.textContent = `${operation.operationName} (${operation.operationCode})`;
        select.appendChild(option);
    });
}

function selectField(id, name, label, valueList, formatter) {
    return `<label for="${id}">${label}<select id="${id}" name="${name}"><option value="">Todos</option>${valueList.map((value) => `<option value="${value}">${escapeGovernanceHtml(formatter(value))}</option>`).join("")}</select></label>`;
}

function stateMarkup(type, message, retry = false) {
    return `<div class="privacy-governance-state ${type}"><i class="ti ${type === "error" ? "ti-alert-triangle" : "ti-file-search"}" aria-hidden="true"></i><p>${escapeGovernanceHtml(message)}</p>${retry ? '<button class="dashboard-back-btn" type="button" data-retry-legal-bases>Tentar novamente</button>' : ""}</div>`;
}

function normalize(value) {
    return String(value || "").normalize("NFD").replaceAll(/[\u0300-\u036f]/g, "").trim().toLowerCase();
}
