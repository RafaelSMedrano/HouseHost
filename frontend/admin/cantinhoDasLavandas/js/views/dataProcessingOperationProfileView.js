import {
    findDataProcessingOperationById,
    findLegalBasisAssessmentsByOperation,
} from "../api.js?v=2026-07-26-legal-basis-workflow";

export function renderDataProcessingOperationProfileView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    if (!container) {
        return;
    }
    container.innerHTML = `
      <main class="main privacy-governance-main privacy-governance-profile-main">
        <div class="privacy-governance-page">
          <div class="privacy-governance-feedback" role="status" aria-live="polite">Carregando operação de tratamento...</div>
          <div data-processing-operation-profile aria-busy="true">
            <div class="privacy-governance-state loading"><i class="ti ti-loader-2" aria-hidden="true"></i><p>Carregando o perfil da operação...</p></div>
          </div>
        </div>
      </main>`;
    loadOperationProfile(container, options);
}

async function loadOperationProfile(container, options) {
    const feedback = container.querySelector(".privacy-governance-feedback");
    const profileContainer = container.querySelector("[data-processing-operation-profile]");
    try {
        const operationResponse = await findDataProcessingOperationById(options.operationId);
        const operation = operationResponse?.data;
        if (!operation) {
            throw new Error("A operação de tratamento não foi encontrada.");
        }
        let assessmentList = Array.isArray(operation.legalBasisAssessmentList)
                ? operation.legalBasisAssessmentList
                : null;
        if (assessmentList === null) {
            const assessmentResponse = await findLegalBasisAssessmentsByOperation(options.operationId);
            assessmentList = Array.isArray(assessmentResponse?.data) ? assessmentResponse.data : [];
        }
        feedback.textContent = "Perfil da operação carregado.";
        profileContainer.setAttribute("aria-busy", "false");
        profileContainer.innerHTML = buildOperationProfileMarkup(operation, assessmentList, {
            assessmentNavigationAvailable: typeof options.onOpenAssessment === "function",
        });
        profileContainer.querySelector("[data-back-processing-operations]")
                ?.addEventListener("click", () => options.onBack?.());
        profileContainer.querySelectorAll("[data-open-legal-basis-assessment]").forEach((button) => {
            button.addEventListener("click", () => {
                options.onOpenAssessment?.(Number(button.dataset.openLegalBasisAssessment));
            });
        });
        profileContainer.querySelector("[data-new-legal-basis-assessment]")
                ?.addEventListener("click", () => options.onNewAssessment?.(Number(options.operationId)));
    } catch (error) {
        feedback.textContent = error?.message || "Não foi possível carregar a operação de tratamento.";
        profileContainer.setAttribute("aria-busy", "false");
        profileContainer.innerHTML = `
          <div class="privacy-governance-state error">
            <i class="ti ti-alert-triangle" aria-hidden="true"></i>
            <p>${escapeHtml(error?.message || "Não foi possível carregar o perfil.")}</p>
            <button class="dashboard-back-btn" type="button" data-back-processing-operations>Voltar ao inventário</button>
          </div>`;
        profileContainer.querySelector("[data-back-processing-operations]")
                ?.addEventListener("click", () => options.onBack?.());
    }
}

export function buildOperationProfileMarkup(operation, assessmentList = [], options = {}) {
    const isMarketing = operation.operationCode === "WHATSAPP_MARKETING";
    return `
      <header class="privacy-governance-header privacy-operation-profile-header">
        <div>
          <button class="dashboard-back-btn" type="button" data-back-processing-operations><i class="ti ti-arrow-left"></i>Voltar</button>
          <span>${escapeHtml(operation.operationCode || "SEM_CODIGO")}</span>
          <h1>${escapeHtml(operation.operationName || "Operação sem nome")}</h1>
          <p>${escapeHtml(operation.description || "Descrição não informada")}</p>
        </div>
        <div class="privacy-operation-badges">
          ${badge(operation.status === "ACTIVE" ? "Operação ativa" : "Operação inativa", operation.status === "ACTIVE" ? "active" : "inactive")}
          ${badge(readinessLabel(operation.legalBasisReadiness), readinessClass(operation.legalBasisReadiness))}
        </div>
      </header>
      ${isMarketing ? '<div class="privacy-governance-notice danger"><strong>Marketing inativo.</strong><p>O texto histórico não autoriza marketing e não representa prontidão ou consentimento atual.</p></div>' : ""}
      <section class="privacy-inventory-list" aria-label="Inventário da operação">
        ${inventoryListRow("Finalidade e descrição", [detail("Descrição", operation.description), detail("Finalidade operacional", operation.purpose)])}
        ${inventoryListRow("Titulares e dados pessoais", [detail("Categorias de titulares", operation.dataSubjectCategories), detail("Categorias de dados pessoais", operation.personalDataCategories)])}
        ${inventoryListRow("Origem e ações de tratamento", [detail("Origem dos dados", operation.dataSource), detail("Ações de tratamento", operation.processingActions)])}
        ${inventoryListRow("Acesso e destinatários", [detail("Acessos internos", operation.internalAccessRoles), detail("Destinatários externos", operation.externalRecipients)])}
        ${inventoryListRow("Transferência internacional", [detail("Existe transferência", operation.internationalTransfer ? "Sim" : "Não"), detail("Observação", operation.internationalTransfer ? "A transferência deve permanecer documentada no inventário de fornecedores." : "Não indicada neste registro.")])}
        ${inventoryListRow("Retenção e descarte", [detail("Critério de retenção", operation.retentionPeriod), detail("Método de eliminação", operation.deletionMethod)])}
        ${inventoryListRow("Segurança e responsabilidade", [detail("Medidas de segurança", operation.securityMeasures), detail("Área responsável", operation.responsibleArea), detail("Sistema", operation.systemName)])}
        ${inventoryListRow("Situação e revisão do inventário", [detail("Situação operacional", operation.status === "ACTIVE" ? "Ativa" : "Inativa"), detail("Prontidão das bases legais", readinessLabel(operation.legalBasisReadiness)), detail("Revisão genérica do inventário", formatDateTime(operation.reviewedAt)), detail("ID do revisor do inventário", operation.reviewedByUserId)])}
      </section>
      <section class="privacy-governance-notice legacy">
        <strong>Resumo legado de base legal — não é evidência de aprovação</strong>
        <p>${escapeHtml(operation.legalBasis || "Nenhum resumo legado informado.")}</p>
        <small>A prontidão acima é calculada exclusivamente pelas avaliações estruturadas.</small>
      </section>
      <section class="privacy-assessment-section" aria-labelledby="legal-basis-assessment-title">
        <div class="privacy-assessment-heading">
          <div><span>Decisões registradas</span><h2 id="legal-basis-assessment-title">Avaliações por finalidade e versão</h2></div>
          <div class="privacy-assessment-heading-actions"><p>${assessmentList.length} versão(ões) registrada(s).</p>${isMarketing ? "" : '<button class="dashboard-back-btn" type="button" data-new-legal-basis-assessment><i class="ti ti-plus"></i>Nova avaliação</button>'}</div>
        </div>
        ${buildAssessmentHistoryMarkup(assessmentList, options)}
      </section>`;
}

export function buildAssessmentHistoryMarkup(assessmentList, options = {}) {
    const purposeGroupList = groupAssessmentVersions(assessmentList);
    if (purposeGroupList.length === 0) {
        return `<div class="privacy-governance-state empty"><i class="ti ti-file-search" aria-hidden="true"></i><p>Nenhuma avaliação estruturada foi registrada para esta operação.</p></div>`;
    }
    return `<div class="privacy-purpose-list">${purposeGroupList.map((purposeGroup) => `
      <details class="privacy-purpose-group" open>
        <summary><span>${escapeHtml(purposeGroup.purpose)}</span><small>${purposeGroup.assessmentList.length} versão(ões)</small></summary>
        <div class="privacy-version-list">${purposeGroup.assessmentList.map((assessment) => assessmentMarkup(assessment, options)).join("")}</div>
      </details>`).join("")}</div>`;
}

export function groupAssessmentVersions(assessmentList = []) {
    const purposeGroupMap = new Map();
    assessmentList.forEach((assessment) => {
        const purpose = String(assessment.purpose || "Finalidade não informada").trim();
        const purposeKey = purpose.toLocaleLowerCase("pt-BR");
        if (!purposeGroupMap.has(purposeKey)) {
            purposeGroupMap.set(purposeKey, { purpose, assessmentList: [] });
        }
        purposeGroupMap.get(purposeKey).assessmentList.push(assessment);
    });
    return [...purposeGroupMap.values()].map((purposeGroup) => ({
        ...purposeGroup,
        assessmentList: [...purposeGroup.assessmentList].sort(
                (first, second) => Number(second.assessmentVersion || 0) - Number(first.assessmentVersion || 0)
        ),
    }));
}

function assessmentMarkup(assessment, options) {
    const isReadOnly = assessment.status === "APPROVED" || assessment.status === "SUPERSEDED";
    const versionMeaning = assessment.current
            ? "Versão atual"
            : assessment.status === "SUPERSEDED" ? "Versão substituída" : "Versão histórica";
    const lifecycleFactList = [
        assessment.submittedAt ? `Enviada em ${formatDateTime(assessment.submittedAt)}` : null,
        assessment.reviewedAt ? `Revisada em ${formatDateTime(assessment.reviewedAt)}` : null,
        assessment.reviewedByUserId ? `Revisor #${assessment.reviewedByUserId}` : null,
        assessment.status === "REJECTED" ? "Há um motivo de rejeição registrado no detalhe da avaliação." : null,
        isReadOnly ? "Registro somente leitura." : null,
    ].filter(Boolean);
    const navigationAvailable = options.assessmentNavigationAvailable;
    return `
      <article class="privacy-version-row ${statusClass(assessment.status)}">
        <div class="privacy-version-main">
          <div class="privacy-operation-badges">
            ${badge(`Versão ${assessment.assessmentVersion || 1}`, "neutral")}
            ${badge(statusLabel(assessment.status), statusClass(assessment.status))}
            ${badge(versionMeaning, assessment.current ? "current" : "neutral")}
          </div>
          <strong>${escapeHtml(basisLabel(assessment.legalBasis))}</strong>
          <p>${escapeHtml(versionMeaning)}. ${escapeHtml(lifecycleFactList.join(" ") || "Datas de ciclo não disponíveis neste resumo.")}</p>
        </div>
        <button class="dashboard-back-btn" type="button" data-open-legal-basis-assessment="${escapeHtml(assessment.id)}" ${navigationAvailable ? "" : "disabled"} title="${navigationAvailable ? "Abrir avaliação" : "Navegação indisponível"}">
          <i class="ti ti-file-description"></i>Inspecionar avaliação
        </button>
      </article>`;
}

function inventoryListRow(title, detailMarkupList) {
    return `<article class="privacy-inventory-row"><h2>${escapeHtml(title)}</h2><div class="privacy-inventory-row-content">${detailMarkupList.join("")}</div></article>`;
}

function detail(label, value) {
    return `<div class="privacy-inventory-detail"><span>${escapeHtml(label)}</span><p>${escapeHtml(value ?? "Não informado")}</p></div>`;
}

function badge(text, type) {
    return `<span class="privacy-governance-badge ${type}">${escapeHtml(text)}</span>`;
}

function readinessLabel(readiness) {
    return ({NOT_ASSESSED:"Base não avaliada",DRAFT:"Avaliação em rascunho",UNDER_REVIEW:"Avaliação em revisão",APPROVED:"Avaliação aprovada",REJECTED:"Avaliação rejeitada"})[readiness] || "Prontidão não informada";
}

function readinessClass(readiness) {
    return ({APPROVED:"active",DRAFT:"warning",UNDER_REVIEW:"warning",REJECTED:"danger",NOT_ASSESSED:"neutral"})[readiness] || "neutral";
}

function statusLabel(status) {
    return ({DRAFT:"Rascunho",UNDER_REVIEW:"Em revisão",APPROVED:"Aprovada",REJECTED:"Rejeitada",SUPERSEDED:"Substituída"})[status] || status || "Não informado";
}

function statusClass(status) {
    return ({APPROVED:"active",DRAFT:"warning",UNDER_REVIEW:"warning",REJECTED:"danger",SUPERSEDED:"superseded"})[status] || "neutral";
}

function basisLabel(basis) {
    return ({CONSENT:"Consentimento",LEGAL_OR_REGULATORY_OBLIGATION:"Obrigação legal ou regulatória",CONTRACT_OR_PRE_CONTRACT:"Contrato ou procedimentos preliminares",REGULAR_EXERCISE_OF_RIGHTS:"Exercício regular de direitos",PROTECTION_OF_LIFE:"Proteção da vida",LEGITIMATE_INTEREST:"Legítimo interesse",CREDIT_PROTECTION:"Proteção do crédito"})[basis] || basis || "Base não informada";
}

function formatDateTime(value) {
    if (!value) {
        return "Não realizada";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "Data inválida" : new Intl.DateTimeFormat("pt-BR", {
        dateStyle: "short",
        timeStyle: "short",
    }).format(date);
}

function escapeHtml(value) {
    return String(value ?? "").replaceAll("&", "&amp;").replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}
