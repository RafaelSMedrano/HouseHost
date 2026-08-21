export const LEGAL_BASIS_VALUE_LIST = [
    "CONSENT",
    "LEGAL_OR_REGULATORY_OBLIGATION",
    "CONTRACT_OR_PRE_CONTRACT",
    "REGULAR_EXERCISE_OF_RIGHTS",
    "PROTECTION_OF_LIFE",
    "LEGITIMATE_INTEREST",
    "CREDIT_PROTECTION",
];

export const SENSITIVE_DATA_BASIS_VALUE_LIST = [
    "SPECIFIC_CONSENT",
    "LEGAL_OR_REGULATORY_OBLIGATION",
    "PUBLIC_POLICY",
    "RESEARCH",
    "REGULAR_EXERCISE_OF_RIGHTS",
    "PROTECTION_OF_LIFE",
    "HEALTH_PROTECTION",
    "FRAUD_PREVENTION_AND_SECURITY",
];

export function governanceTabsMarkup(activeTab) {
    return `<div class="privacy-governance-tabs" role="tablist" aria-label="Tratamentos e bases legais">
      <button type="button" role="tab" aria-selected="${activeTab === "operations"}" class="${activeTab === "operations" ? "active" : ""}" data-governance-tab="operations">Operações</button>
      <button type="button" role="tab" aria-selected="${activeTab === "assessments"}" class="${activeTab === "assessments" ? "active" : ""}" data-governance-tab="assessments">Bases legais</button>
    </div>`;
}

export function bindGovernanceTabs(container, options) {
    container.querySelector('[data-governance-tab="operations"]')
            ?.addEventListener("click", () => options.onOpenOperations?.());
    container.querySelector('[data-governance-tab="assessments"]')
            ?.addEventListener("click", () => options.onOpenAssessments?.());
}

export function legalBasisLabel(value) {
    return ({
        CONSENT: "Consentimento",
        LEGAL_OR_REGULATORY_OBLIGATION: "Obrigação legal ou regulatória",
        CONTRACT_OR_PRE_CONTRACT: "Contrato ou procedimentos preliminares",
        REGULAR_EXERCISE_OF_RIGHTS: "Exercício regular de direitos",
        PROTECTION_OF_LIFE: "Proteção da vida",
        LEGITIMATE_INTEREST: "Legítimo interesse",
        CREDIT_PROTECTION: "Proteção do crédito",
    })[value] || value || "Base não informada";
}

export function sensitiveDataBasisLabel(value) {
    return ({
        SPECIFIC_CONSENT: "Consentimento específico e destacado",
        LEGAL_OR_REGULATORY_OBLIGATION: "Obrigação legal ou regulatória",
        PUBLIC_POLICY: "Execução de política pública",
        RESEARCH: "Estudos por órgão de pesquisa",
        REGULAR_EXERCISE_OF_RIGHTS: "Exercício regular de direitos",
        PROTECTION_OF_LIFE: "Proteção da vida",
        HEALTH_PROTECTION: "Tutela da saúde",
        FRAUD_PREVENTION_AND_SECURITY: "Prevenção à fraude e segurança",
    })[value] || value || "Não aplicável";
}

export function assessmentStatusLabel(value) {
    return ({ DRAFT: "Rascunho", UNDER_REVIEW: "Em revisão", APPROVED: "Aprovada", REJECTED: "Rejeitada", SUPERSEDED: "Substituída" })[value]
            || value || "Não informado";
}

export function assessmentStatusClass(value) {
    return ({ APPROVED: "active", DRAFT: "warning", UNDER_REVIEW: "warning", REJECTED: "danger", SUPERSEDED: "superseded" })[value]
            || "neutral";
}

export function formatGovernanceDate(value) {
    if (!value) {
        return "Não informado";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "Data inválida" : new Intl.DateTimeFormat("pt-BR", {
        dateStyle: "short",
        timeStyle: "short",
    }).format(date);
}

export function escapeGovernanceHtml(value) {
    return String(value ?? "").replaceAll("&", "&amp;").replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}
