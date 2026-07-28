import {
    createLegalBasisAssessmentDraft,
    findDataProcessingOperationById,
    findLegalBasisAssessmentById,
    updateLegalBasisAssessmentDraft,
} from "../api.js?v=2026-07-26-legal-basis-workflow";
import {
    escapeGovernanceHtml,
    legalBasisLabel,
    LEGAL_BASIS_VALUE_LIST,
    sensitiveDataBasisLabel,
    SENSITIVE_DATA_BASIS_VALUE_LIST,
} from "./legalBasisAssessmentPresentation.js?v=2026-07-26-legal-basis-workflow";

const NARRATIVE_FIELD_LIST = ["justification", "personalDataCategories", "necessityAssessment", "legalReference", "legalObligationDescription", "contractualContext", "consentCollectionMechanism", "consentEvidenceMechanism", "consentWithdrawalMechanism", "legitimateInterest", "legitimateExpectation", "rightsImpactAssessment", "safeguards", "balancingConclusion", "sensitiveDataIndispensability"];

export function renderLegalBasisAssessmentFormView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    if (!container) {
        return;
    }
    container.innerHTML = `<main class="main privacy-governance-main privacy-governance-profile-main"><form class="privacy-governance-page privacy-assessment-form" data-assessment-form>
      <header class="privacy-governance-header"><div><button class="dashboard-back-btn" type="button" data-form-cancel><i class="ti ti-arrow-left"></i>Voltar</button><span>Governança LGPD</span><h1>${options.assessmentId ? "Editar avaliação" : "Nova avaliação de base legal"}</h1><p data-operation-name>Carregando operação relacionada...</p></div></header>
      <div class="privacy-governance-feedback" role="status" aria-live="polite"></div>
      <section class="privacy-form-section"><h2>Finalidade e base</h2><p>Um registro representa uma finalidade específica e uma única base legal.</p><div class="privacy-form-grid">
        ${input("purpose", "Finalidade específica *", "text", 500)}
        ${select("legalBasis", "Base legal ordinária *", LEGAL_BASIS_VALUE_LIST, legalBasisLabel)}
        ${textarea("justification", "Justificativa factual", "Explique por que a base se aplica à finalidade.")}
        ${textarea("personalDataCategories", "Categorias de dados pessoais", "Liste somente os dados avaliados.")}
        ${textarea("necessityAssessment", "Necessidade e minimização", "Explique por que os dados são adequados, relevantes e limitados.")}
      </div></section>
      <section class="privacy-form-section" data-basis-section="LEGAL_OR_REGULATORY_OBLIGATION"><h2>Obrigação legal ou regulatória</h2><p>A referência da LGPD é preenchida automaticamente. Informe abaixo a lei ou o regulamento externo que cria a obrigação concreta.</p><div class="privacy-form-grid">${textarea("legalReference", "Norma concreta da obrigação", "Identifique a lei ou o regulamento fiscal, contábil ou de hospedagem aplicável.")}${textarea("legalObligationDescription", "Obrigação aplicável", "Explique o que a norma exige do controlador.")}</div></section>
      <section class="privacy-form-section" data-basis-section="CONTRACT_OR_PRE_CONTRACT"><h2>Contrato ou procedimentos preliminares</h2><div class="privacy-form-grid">${textarea("contractualContext", "Contexto contratual", "Descreva a relação ou solicitação do titular e por que os dados são necessários.")}</div></section>
      <section class="privacy-form-section" data-basis-section="CONSENT"><h2>Consentimento</h2><p>O consentimento deve ser específico, demonstrável e revogável. Ele não reativa marketing automaticamente.</p><div class="privacy-form-grid">${textarea("consentCollectionMechanism", "Mecanismo de coleta", "Como o consentimento é obtido?")}${textarea("consentEvidenceMechanism", "Mecanismo de evidência", "Como a manifestação é comprovada?")}${textarea("consentWithdrawalMechanism", "Mecanismo de revogação", "Como o titular revoga com facilidade equivalente?")}</div></section>
      <section class="privacy-form-section" data-basis-section="LEGITIMATE_INTEREST"><h2>Teste de legítimo interesse</h2><div class="privacy-form-grid">${textarea("legitimateInterest", "Interesse legítimo", "Identifique o interesse concreto.")}${textarea("legitimateExpectation", "Expectativa legítima", "Avalie a expectativa razoável do titular.")}${textarea("rightsImpactAssessment", "Impacto sobre direitos", "Avalie riscos aos direitos e liberdades.")}${textarea("safeguards", "Salvaguardas", "Registre as medidas que reduzem impactos.")}${textarea("balancingConclusion", "Conclusão do balanceamento", "Registre a conclusão responsável.")}</div></section>
      <section class="privacy-form-section"><h2>Dados sensíveis</h2><label class="privacy-checkbox"><input name="sensitiveData" type="checkbox">Esta finalidade envolve dados pessoais sensíveis</label><div class="privacy-form-grid" data-sensitive-fields>${select("sensitiveDataLegalBasis", "Base específica para dados sensíveis", SENSITIVE_DATA_BASIS_VALUE_LIST, sensitiveDataBasisLabel)}${textarea("sensitiveDataIndispensability", "Indispensabilidade", "Demonstre por que o tratamento sensível é indispensável.")}${textarea("safeguardsSensitive", "Salvaguardas adicionais", "Use este campo para complementar as salvaguardas.")}</div></section>
      <footer class="privacy-profile-actions"><button class="dashboard-back-btn" type="button" data-form-cancel>Cancelar</button><button class="btn btn-primary" type="submit" data-save-assessment>Salvar rascunho</button></footer>
    </form></main>`;
    container.querySelectorAll("[data-form-cancel]").forEach((button) => button.addEventListener("click", () => options.onCancel?.()));
    const form = container.querySelector("[data-assessment-form]");
    form.addEventListener("change", (event) => {
        if (event.target.name === "legalBasis" || event.target.name === "sensitiveData") {
            updateConditionalFields(form);
        }
    });
    form.addEventListener("submit", (event) => saveAssessment(event, container, options));
    updateConditionalFields(form);
    loadForm(container, options);
}

async function loadForm(container, options) {
    const form = container.querySelector("[data-assessment-form]");
    try {
        let assessment = null;
        if (options.assessmentId) {
            const response = await findLegalBasisAssessmentById(options.assessmentId);
            assessment = response?.data;
            if (assessment?.status !== "DRAFT") {
                throw new Error("Somente avaliações em rascunho podem ser editadas.");
            }
            fillForm(form, assessment);
        }
        const operationId = assessment?.processingOperationId || options.operationId;
        form.dataset.processingOperationId = operationId;
        const operationResponse = await findDataProcessingOperationById(operationId);
        container.querySelector("[data-operation-name]").textContent = `${operationResponse?.data?.operationName || "Operação"} · ${operationResponse?.data?.operationCode || "SEM_CODIGO"}`;
        updateConditionalFields(form);
    } catch (error) {
        setFeedback(container, error?.message || "Não foi possível carregar o formulário.", true);
        form.querySelector("[data-save-assessment]").disabled = true;
    }
}

async function saveAssessment(event, container, options) {
    event.preventDefault();
    const form = event.currentTarget;
    const button = form.querySelector("[data-save-assessment]");
    button.disabled = true;
    setFeedback(container, "Salvando rascunho...");
    try {
        const payload = assessmentPayload(form);
        const response = options.assessmentId
                ? await updateLegalBasisAssessmentDraft(options.assessmentId, payload)
                : await createLegalBasisAssessmentDraft(form.dataset.processingOperationId, payload);
        setFeedback(container, "Rascunho salvo.");
        options.onSaved?.(response?.data);
    } catch (error) {
        setFeedback(container, error?.message || "Não foi possível salvar o rascunho.", true);
        button.disabled = false;
    }
}

export function assessmentPayload(form) {
    const data = new FormData(form);
    const payload = {
        purpose: value(data, "purpose"),
        legalBasis: value(data, "legalBasis"),
        sensitiveData: data.has("sensitiveData"),
        sensitiveDataLegalBasis: data.has("sensitiveData") ? value(data, "sensitiveDataLegalBasis") : null,
    };
    NARRATIVE_FIELD_LIST.forEach((name) => payload[name] = value(data, name));
    const sensitiveSafeguards = value(data, "safeguardsSensitive");
    if (sensitiveSafeguards) {
        payload.safeguards = [payload.safeguards, sensitiveSafeguards].filter(Boolean).join("\n");
    }
    return payload;
}

function updateConditionalFields(form) {
    const basis = form.elements.legalBasis.value;
    form.querySelectorAll("[data-basis-section]").forEach((section) => {
        section.hidden = section.dataset.basisSection !== basis;
        section.querySelectorAll("input, select, textarea").forEach((field) => field.disabled = section.hidden);
    });
    const sensitiveFields = form.querySelector("[data-sensitive-fields]");
    sensitiveFields.hidden = !form.elements.sensitiveData.checked;
    sensitiveFields.querySelectorAll("input, select, textarea").forEach((field) => field.disabled = sensitiveFields.hidden);
}

function fillForm(form, assessment) {
    ["purpose", "legalBasis", ...NARRATIVE_FIELD_LIST, "sensitiveDataLegalBasis"].forEach((name) => {
        if (form.elements[name]) {
            form.elements[name].value = assessment[name] ?? "";
        }
    });
    form.elements.sensitiveData.checked = Boolean(assessment.sensitiveData);
    if (assessment.sensitiveData && assessment.legalBasis !== "LEGITIMATE_INTEREST") {
        form.elements.safeguardsSensitive.value = assessment.safeguards ?? "";
    }
}

function input(name, label, type, maxlength) {
    return `<label>${label}<input name="${name}" type="${type}" maxlength="${maxlength}" required></label>`;
}

function textarea(name, label, help) {
    return `<label>${label}<small>${help}</small><textarea name="${name}" maxlength="4000" rows="4"></textarea></label>`;
}

function select(name, label, valueList, formatter) {
    return `<label>${label}<select name="${name}" required><option value="">Selecione</option>${valueList.map((item) => `<option value="${item}">${escapeGovernanceHtml(formatter(item))}</option>`).join("")}</select></label>`;
}

function value(formData, name) {
    const result = String(formData.get(name) || "").trim();
    return result || null;
}

function setFeedback(container, message, error = false) {
    const feedback = container.querySelector(".privacy-governance-feedback");
    feedback.textContent = message;
    feedback.classList.toggle("error", error);
}
