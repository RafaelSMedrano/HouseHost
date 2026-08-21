import {
    approveLegalBasisAssessment,
    createLegalBasisAssessmentRevision,
    findDataProcessingOperationById,
    findLegalBasisAssessmentById,
    rejectLegalBasisAssessment,
    submitLegalBasisAssessment,
} from "../api.js?v=2026-08-11-api-log-transport";
import {
    assessmentStatusClass,
    assessmentStatusLabel,
    escapeGovernanceHtml,
    formatGovernanceDate,
    legalBasisLabel,
    sensitiveDataBasisLabel,
} from "./legalBasisAssessmentPresentation.js?v=2026-07-26-legal-basis-workflow";

export function renderLegalBasisAssessmentProfileView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    if (!container) {
        return;
    }
    container.innerHTML = `<main class="main privacy-governance-main privacy-governance-profile-main"><div class="privacy-governance-page">
      <div class="privacy-governance-feedback" role="status" aria-live="polite">Carregando avaliação...</div>
      <div data-legal-basis-profile aria-busy="true"><div class="privacy-governance-state loading"><i class="ti ti-loader-2" aria-hidden="true"></i><p>Carregando o perfil da avaliação...</p></div></div>
    </div></main>`;
    loadProfile(container, options);
}

async function loadProfile(container, options) {
    const feedback = container.querySelector(".privacy-governance-feedback");
    const profile = container.querySelector("[data-legal-basis-profile]");
    try {
        const assessmentResponse = await findLegalBasisAssessmentById(options.assessmentId);
        const assessment = assessmentResponse?.data;
        if (!assessment) {
            throw new Error("A avaliação de base legal não foi encontrada.");
        }
        const operationResponse = await findDataProcessingOperationById(assessment.processingOperationId);
        const operation = operationResponse?.data || {};
        profile.setAttribute("aria-busy", "false");
        profile.innerHTML = buildLegalBasisAssessmentProfileMarkup(assessment, operation);
        feedback.textContent = "Perfil da avaliação carregado.";
        bindActions(container, assessment, options);
    } catch (error) {
        profile.setAttribute("aria-busy", "false");
        feedback.textContent = error?.message || "Não foi possível carregar a avaliação.";
        profile.innerHTML = `<div class="privacy-governance-state error"><i class="ti ti-alert-triangle" aria-hidden="true"></i><p>${escapeGovernanceHtml(feedback.textContent)}</p><button class="dashboard-back-btn" type="button" data-assessment-back aria-label="Voltar">Voltar</button></div>`;
        profile.querySelector("[data-assessment-back]")?.addEventListener("click", () => options.onBack?.());
    }
}

function bindActions(container, assessment, options) {
    container.querySelector("[data-assessment-back]")?.addEventListener("click", () => options.onBack?.());
    container.querySelector("[data-open-related-operation]")?.addEventListener("click", () => options.onOpenOperation?.(assessment.processingOperationId));
    container.querySelector("[data-edit-assessment]")?.addEventListener("click", () => options.onEdit?.(assessment.id, assessment.processingOperationId));
    container.querySelector("[data-submit-assessment]")?.addEventListener("click", () => runAction(container, options, {
        confirmation: `Enviar a avaliação “${assessment.purpose}” para revisão?`,
        action: () => submitLegalBasisAssessment(assessment.id),
        success: "Avaliação enviada para revisão.",
    }));
    container.querySelector("[data-approve-assessment]")?.addEventListener("click", () => runAction(container, options, {
        confirmation: `Aprovar a avaliação “${assessment.purpose}”? Esta ação registra uma decisão humana de governança; não é certificação jurídica automática.`,
        action: () => approveLegalBasisAssessment(assessment.id),
        success: "Decisão de aprovação registrada.",
    }));
    container.querySelector("[data-reject-assessment]")?.addEventListener("click", async () => {
        const reason = globalThis.prompt?.("Informe o motivo da rejeição:")?.trim();
        if (!reason) {
            setFeedback(container, "A rejeição exige um motivo.", true);
            return;
        }
        await runAction(container, options, {
            confirmation: `Rejeitar a avaliação “${assessment.purpose}”?`,
            action: () => rejectLegalBasisAssessment(assessment.id, reason),
            success: "Rejeição registrada.",
        });
    });
    container.querySelector("[data-revise-assessment]")?.addEventListener("click", () => runAction(container, options, {
        confirmation: `Criar uma nova versão de “${assessment.purpose}”? A versão aprovada permanecerá preservada.`,
        action: () => createLegalBasisAssessmentRevision(assessment.id),
        success: "Nova versão criada como rascunho.",
        onSuccess: (response) => options.onEdit?.(response?.data?.id, assessment.processingOperationId),
    }));
}

async function runAction(container, options, actionOptions) {
    if (!globalThis.confirm?.(actionOptions.confirmation)) {
        return;
    }
    const buttonList = [...container.querySelectorAll("[data-assessment-actions] button")];
    buttonList.forEach((button) => button.disabled = true);
    setFeedback(container, "Processando decisão...");
    try {
        const response = await actionOptions.action();
        setFeedback(container, actionOptions.success);
        if (actionOptions.onSuccess) {
            actionOptions.onSuccess(response);
            return;
        }
        options.onReload?.();
    } catch (error) {
        setFeedback(container, error?.message || "Não foi possível concluir a ação.", true);
        buttonList.forEach((button) => button.disabled = false);
    }
}

export function buildLegalBasisAssessmentProfileMarkup(assessment, operation = {}) {
    return `<header class="privacy-governance-header privacy-operation-profile-header"><div>
      <button class="dashboard-back-btn" type="button" data-assessment-back aria-label="Voltar"><i class="ti ti-arrow-left" aria-hidden="true"></i>Voltar</button>
      <span>Avaliação de base legal · versão ${escapeGovernanceHtml(assessment.assessmentVersion || 1)}</span>
      <h1>${escapeGovernanceHtml(assessment.purpose || "Finalidade não informada")}</h1>
      <p>${escapeGovernanceHtml(operation.operationName || "Operação não informada")} · ${escapeGovernanceHtml(operation.operationCode || "SEM_CODIGO")}</p>
    </div><div class="privacy-operation-badges"><span class="privacy-governance-badge ${assessmentStatusClass(assessment.status)}">${escapeGovernanceHtml(assessmentStatusLabel(assessment.status))}</span><span class="privacy-governance-badge neutral">${assessment.previousVersionId ? `Revisão de #${escapeGovernanceHtml(assessment.previousVersionId)}` : "Versão inicial"}</span></div></header>
    <div class="privacy-profile-actions" data-assessment-actions>
      <button class="dashboard-back-btn" type="button" data-open-related-operation>Ver operação relacionada</button>
      ${assessmentActionMarkup(assessment)}
    </div>
    ${assessment.status === "APPROVED" || assessment.status === "SUPERSEDED" ? '<div class="privacy-governance-notice"><strong>Registro somente leitura</strong><p>A aprovação registra uma decisão humana de governança e não representa certificação jurídica automática.</p></div>' : ""}
    <section class="privacy-inventory-list" aria-label="Evidências da avaliação">
      ${profileRow("Identificação", [detail("Operação", `${operation.operationName || "Não informada"} (${operation.operationCode || "SEM_CODIGO"})`), detail("Finalidade", assessment.purpose), detail("Base legal", legalBasisLabel(assessment.legalBasis)), detail("Referência na LGPD", assessment.lgpdReference)])}
      ${profileRow("Análise comum", [detail("Justificativa", assessment.justification), detail("Categorias de dados pessoais", assessment.personalDataCategories), detail("Necessidade e minimização", assessment.necessityAssessment)])}
      ${basisSpecificRows(assessment)}
      ${profileRow("Dados sensíveis", [detail("Há dados sensíveis", assessment.sensitiveData ? "Sim" : "Não"), detail("Base específica", sensitiveDataBasisLabel(assessment.sensitiveDataLegalBasis)), detail("Indispensabilidade", assessment.sensitiveDataIndispensability), detail("Salvaguardas", assessment.safeguards)])}
      ${profileRow("Ciclo e responsabilidade", [detail("Situação", assessmentStatusLabel(assessment.status)), detail("Versão", assessment.assessmentVersion), detail("Versão anterior", assessment.previousVersionId), detail("Criada em", formatGovernanceDate(assessment.createdAt)), detail("Atualizada em", formatGovernanceDate(assessment.updatedAt)), detail("Enviada em", formatGovernanceDate(assessment.submittedAt)), detail("Revisada em", formatGovernanceDate(assessment.reviewedAt)), detail("ID do revisor", assessment.reviewedByUserId), detail("Motivo da rejeição", assessment.rejectionReason)])}
    </section>`;
}

function basisSpecificRows(assessment) {
    if (assessment.legalBasis === "LEGAL_OR_REGULATORY_OBLIGATION") {
        return profileRow("Obrigação legal", [detail("Norma concreta da obrigação", assessment.legalReference), detail("Obrigação aplicável", assessment.legalObligationDescription)]);
    }
    if (assessment.legalBasis === "CONTRACT_OR_PRE_CONTRACT") {
        return profileRow("Contexto contratual", [detail("Relação ou procedimento preliminar", assessment.contractualContext)]);
    }
    if (assessment.legalBasis === "CONSENT") {
        return profileRow("Consentimento", [detail("Coleta", assessment.consentCollectionMechanism), detail("Evidência", assessment.consentEvidenceMechanism), detail("Revogação", assessment.consentWithdrawalMechanism)]);
    }
    if (assessment.legalBasis === "LEGITIMATE_INTEREST") {
        return profileRow("Teste de legítimo interesse", [detail("Interesse", assessment.legitimateInterest), detail("Expectativa do titular", assessment.legitimateExpectation), detail("Impacto sobre direitos", assessment.rightsImpactAssessment), detail("Salvaguardas", assessment.safeguards), detail("Conclusão", assessment.balancingConclusion)]);
    }
    return profileRow("Fundamentação específica", [detail("Base selecionada", legalBasisLabel(assessment.legalBasis)), detail("Justificativa factual", assessment.justification)]);
}

function assessmentActionMarkup(assessment) {
    if (assessment.status === "DRAFT") {
        return '<button class="dashboard-back-btn" type="button" data-edit-assessment>Editar rascunho</button><button class="btn btn-primary" type="button" data-submit-assessment>Enviar para revisão</button>';
    }
    if (assessment.status === "UNDER_REVIEW") {
        return '<button class="dashboard-back-btn" type="button" data-reject-assessment>Rejeitar</button><button class="btn btn-primary" type="button" data-approve-assessment>Aprovar decisão</button>';
    }
    if (assessment.status === "APPROVED") {
        return '<button class="btn btn-primary" type="button" data-revise-assessment>Criar nova versão</button>';
    }
    return "";
}

function profileRow(title, detailMarkupList) {
    return `<article class="privacy-inventory-row"><h2>${escapeGovernanceHtml(title)}</h2><div class="privacy-inventory-row-content">${detailMarkupList.join("")}</div></article>`;
}

function detail(label, value) {
    return `<div class="privacy-inventory-detail"><span>${escapeGovernanceHtml(label)}</span><p>${escapeGovernanceHtml(value ?? "Não informado")}</p></div>`;
}

function setFeedback(container, message, error = false) {
    const feedback = container.querySelector(".privacy-governance-feedback");
    feedback.textContent = message;
    feedback.classList.toggle("error", error);
}
