import { renderDataProcessingOperationsView } from "../views/dataProcessingOperationsView.js?v=2026-07-26-legal-basis-workflow";
import { renderDataProcessingOperationProfileView } from "../views/dataProcessingOperationProfileView.js?v=2026-08-10-navigation-accessibility";
import { renderLegalBasisAssessmentsView } from "../views/legalBasisAssessmentsView.js?v=2026-07-26-legal-basis-workflow";
import { renderLegalBasisAssessmentProfileView } from "../views/legalBasisAssessmentProfileView.js?v=2026-08-10-navigation-accessibility";
import { renderLegalBasisAssessmentFormView } from "../views/legalBasisAssessmentFormView.js?v=2026-08-10-navigation-accessibility";

export function createPrivacyController({
    permissions,
    renderDashboardTopbar,
    navigation,
    renderDashboardPanel,
    views = {},
}) {
    const renderDataProcessingOperations = views.renderDataProcessingOperationsView || renderDataProcessingOperationsView;
    const renderDataProcessingOperationProfile = views.renderDataProcessingOperationProfileView || renderDataProcessingOperationProfileView;
    const renderLegalBasisAssessments = views.renderLegalBasisAssessmentsView || renderLegalBasisAssessmentsView;
    const renderLegalBasisAssessmentProfile = views.renderLegalBasisAssessmentProfileView || renderLegalBasisAssessmentProfileView;
    const renderLegalBasisAssessmentForm = views.renderLegalBasisAssessmentFormView || renderLegalBasisAssessmentFormView;

    function allowed() {
        return permissions.canAccessView("processingOperations");
    }

    function dashboardEntry() {
        return {
            name: "dashboard",
            params: {},
            render: () => renderDashboardPanel(),
        };
    }

    function resetToDashboard() {
        navigation.reset(dashboardEntry());
    }

    function navigateIfAllowed(method, entry) {
        if (!allowed()) {
            resetToDashboard();
            return false;
        }

        navigation[method](entry);
        return true;
    }

    function openDataProcessingOperationsPanel() {
        return navigateIfAllowed("reset", {
            name: "processingOperations",
            params: {},
            render: () => renderDataProcessingOperationsPanel(),
        });
    }

    function openLegalBasisAssessmentsPanel() {
        return navigateIfAllowed("goTo", {
            name: "assessments",
            params: {},
            render: () => renderLegalBasisAssessmentsPanel(),
        });
    }

    function openOperationProfilePanel(operationId) {
        const normalizedOperationId = requiredRecordId(operationId);
        if (normalizedOperationId === null) {
            return false;
        }

        return navigateIfAllowed("goTo", operationProfileEntry(normalizedOperationId));
    }

    function openAssessmentProfilePanel(assessmentId, origin = { type: "assessmentList" }) {
        const normalizedAssessmentId = requiredRecordId(assessmentId);
        if (normalizedAssessmentId === null) {
            return false;
        }

        const normalizedOrigin = normalizeAssessmentOrigin(origin);
        return navigateIfAllowed("goTo", assessmentProfileEntry(normalizedAssessmentId, normalizedOrigin));
    }

    function openAssessmentFormPanel(formOptions = {}) {
        const assessmentId = optionalRecordId(formOptions.assessmentId);
        const operationId = requiredRecordId(formOptions.operationId);
        if ((formOptions.assessmentId != null && assessmentId === null) || operationId === null) {
            return false;
        }

        const origin = normalizeAssessmentOrigin(formOptions.origin, operationId);
        const normalizedOptions = {
            ...(assessmentId === null ? {} : { assessmentId }),
            operationId,
            origin,
            saveMode: formOptions.saveMode === "back" ? "back" : "replace",
        };

        return navigateIfAllowed("goTo", {
            name: "assessmentForm",
            params: {
                ...(assessmentId === null ? {} : { assessmentId }),
                operationId,
                origin,
            },
            render: () => renderAssessmentFormPanel(normalizedOptions),
        });
    }

    function operationProfileEntry(operationId) {
        return {
            name: "operationProfile",
            params: { operationId },
            render: () => renderOperationProfilePanel(operationId),
        };
    }

    function assessmentProfileEntry(assessmentId, origin) {
        return {
            name: "assessmentProfile",
            params: { assessmentId, origin },
            render: () => renderAssessmentProfilePanel(assessmentId, origin),
        };
    }

    function renderDataProcessingOperationsPanel() {
        if (!allowed()) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar("Tratamentos e bases legais");
        renderDataProcessingOperations("main-pannel-container", {
            onOpenOperation: (operationId) => openOperationProfilePanel(operationId),
            onOpenAssessments: () => openLegalBasisAssessmentsPanel(),
        });
    }

    function renderLegalBasisAssessmentsPanel() {
        if (!allowed()) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar("Tratamentos e bases legais");
        renderLegalBasisAssessments("main-pannel-container", {
            onOpenOperations: () => navigateIfAllowed("goTo", {
                name: "processingOperations",
                params: {},
                render: () => renderDataProcessingOperationsPanel(),
            }),
            onOpenAssessment: (assessmentId) => openAssessmentProfilePanel(assessmentId),
        });
    }

    function renderOperationProfilePanel(operationId) {
        if (!allowed()) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar("Detalhes do tratamento");
        renderDataProcessingOperationProfile("main-pannel-container", {
            operationId,
            onBack: () => navigation.back(),
            onOpenAssessment: (assessmentId) => openAssessmentProfilePanel(
                assessmentId,
                { type: "operation", operationId },
            ),
            onNewAssessment: (id) => openAssessmentFormPanel({
                operationId: id,
                origin: { type: "operation", operationId: id },
                saveMode: "replace",
            }),
        });
    }

    function renderAssessmentProfilePanel(assessmentId, origin = { type: "assessmentList" }) {
        if (!allowed()) {
            renderDashboardPanel();
            return;
        }
        const normalizedOrigin = normalizeAssessmentOrigin(origin);
        renderDashboardTopbar("Detalhes da base legal");
        renderLegalBasisAssessmentProfile("main-pannel-container", {
            assessmentId,
            onBack: () => navigation.back(),
            onOpenOperation: (operationId) => openOperationProfilePanel(operationId),
            onEdit: (id, operationId) => openAssessmentFormPanel({
                assessmentId: id,
                operationId,
                origin: normalizedOrigin,
                saveMode: Number(id) === Number(assessmentId) ? "back" : "replace",
            }),
            onReload: () => renderAssessmentProfilePanel(assessmentId, normalizedOrigin),
        });
    }

    function renderAssessmentFormPanel(formOptions = {}) {
        if (!allowed()) {
            renderDashboardPanel();
            return;
        }
        const { saveMode, ...viewOptions } = formOptions;
        renderDashboardTopbar(formOptions.assessmentId ? "Editar base legal" : "Nova base legal");
        renderLegalBasisAssessmentForm("main-pannel-container", {
            ...viewOptions,
            onCancel: () => navigation.back(),
            onSaved: (assessment) => handleAssessmentSaved(formOptions, assessment),
        });
    }

    function handleAssessmentSaved(formOptions, assessment) {
        if (formOptions.saveMode === "back") {
            navigation.back();
            return;
        }

        const assessmentId = requiredRecordId(assessment?.id);
        if (assessmentId === null) {
            navigation.back();
            return;
        }

        const origin = normalizeAssessmentOrigin(
            formOptions.origin,
            assessment?.processingOperationId || formOptions.operationId,
        );
        navigation.replace(assessmentProfileEntry(assessmentId, origin));
    }

    return {
        openDataProcessingOperationsPanel,
        openLegalBasisAssessmentsPanel,
        openOperationProfilePanel,
        openAssessmentProfilePanel,
        openAssessmentFormPanel,
        renderDataProcessingOperationsPanel,
        renderLegalBasisAssessmentsPanel,
        renderOperationProfilePanel,
        renderAssessmentProfilePanel,
        renderAssessmentFormPanel,
    };
}

function optionalRecordId(value) {
    return value === null || value === undefined || value === ""
        ? null
        : requiredRecordId(value);
}

function requiredRecordId(value) {
    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : null;
}

function normalizeAssessmentOrigin(origin, fallbackOperationId = null) {
    const operationId = requiredRecordId(origin?.operationId ?? fallbackOperationId);
    if ((origin?.type === "operation" || origin == null) && operationId !== null) {
        return { type: "operation", operationId };
    }

    return { type: "assessmentList" };
}
