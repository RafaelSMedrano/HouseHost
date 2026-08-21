import { renderMainPannelView } from "../views/mainPannelView.js?v=2026-05-20-dashboard-checkin-button";
import { renderGuestFormView } from "../views/guestFormView.js?v=2026-06-15-contact-reveal";
import { renderGuestProfileView } from "../views/guestProfileView.js?v=2026-06-15-contact-reveal";
import { renderGuestsView } from "../views/guestsView.js?v=2026-06-14-contact-masking";
import { renderNewReservationView } from "../views/newReservationView.js?v=2026-05-20-booking-origin-enum";
import { renderReservationsView } from "../views/reservationsView.js?v=2026-06-14-role-access";
import { renderReservationProfileView } from "../views/reservationProfileView.js?v=2026-06-06-checkout-status";
import { renderEditReservationView } from "../views/editReservationView.js?v=2026-06-06-checkout-status";
import { renderRoomFormView } from "../views/roomFormView.js?v=2026-06-14-role-access";
import { renderRoomsView } from "../views/roomsView.js?v=2026-06-14-role-access";
import { renderCheckInView, renderCheckOutView } from "../views/checkOperationsView.js?v=2026-05-20-check-kpis-single-row";
import { renderCheckInFormView, renderCheckOutFormView } from "../views/checkOperationFormView.js?v=2026-06-06-checkout-flow";
import { renderFinanceView } from "../views/financeView.js?v=2026-06-15-finance-profiles";
import { renderFinancialTransactionProfileView } from "../views/financialTransactionProfileView.js?v=2026-06-15-finance-profiles";
import { renderRoomTimelineView } from "../views/roomTimelineView.js?v=2026-05-18-timeline";
import { renderUserProfileView } from "../views/userProfileView.js?v=2026-06-14-role-access";
import { renderSuppliersView } from "../views/suppliersView.js?v=2026-07-26-vertical-governance-flow";
import { renderSupplierFormView } from "../views/supplierFormView.js?v=2026-07-26-vertical-governance-flow";
import { renderSupplierProfileView } from "../views/supplierProfileView.js?v=2026-07-26-vertical-governance-flow";
import { renderDataProcessingOperationsView } from "../views/dataProcessingOperationsView.js?v=2026-07-26-legal-basis-workflow";
import { renderDataProcessingOperationProfileView } from "../views/dataProcessingOperationProfileView.js?v=2026-07-26-legal-basis-workflow";
import { renderLegalBasisAssessmentsView } from "../views/legalBasisAssessmentsView.js?v=2026-07-26-legal-basis-workflow";
import { renderLegalBasisAssessmentProfileView } from "../views/legalBasisAssessmentProfileView.js?v=2026-07-27-lgpd-reference";
import { renderLegalBasisAssessmentFormView } from "../views/legalBasisAssessmentFormView.js?v=2026-07-27-lgpd-reference";
import { renderDashboardTopbarWidget } from "../widgets/dashboardTopbarWidget.js?v=2026-06-14-role-access";
import { renderSidebarWidget } from "../widgets/sidebarWidget.js?v=2026-07-26-processing-governance";
import { permissionsFor } from "../permissions.js?v=2026-07-26-processing-governance";

export function startUIController(containerId, user = {}) {
    const container = document.getElementById(containerId);
    const permissions = permissionsFor(user.role);
    let topbarContext = {
        newReservationBack: () => renderDashboardPanel(),
        guestBack: () => renderDashboardPanel(),
    };

    if (!container) {
        return;
    }

    container.className = "dashboard-home";
    container.innerHTML = `
        <div id="sidebar-container" class="ui-sidebar"></div>
        <div id="topbar-container" class="ui-topbar"></div>
        <div id="main-pannel-container" class="ui-main-pannel"></div>
        <button id="sidebar-toggle" class="sidebar-toggle" type="button" aria-label="Abrir menu">
            <i class="ti ti-menu-2"></i>
        </button>
    `;

    renderSidebarWidget("sidebar-container", {
        id: user.id,
        name: user.username,
        role: user.role,
        roleLabel: roleLabel(user.role),
        photoUrl: user.photoUrl,
        onUserProfile: () => {
            renderUserProfilePanel();
            container.classList.remove("sidebar-open");
            sidebarToggleIcon.className = "ti ti-menu-2";
            sidebarToggle.setAttribute("aria-label", "Abrir menu");
        },
        onNavigate: (view) => {
            if (!permissions.canAccessView(view)) {
                return;
            }

            if (view === "reservations") {
                renderReservationsPanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
                return;
            }

            if (view === "dashboard") {
                renderDashboardPanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
                return;
            }

            if (view === "guests") {
                renderGuestsPanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
                return;
            }

            if (view === "rooms") {
                renderRoomsPanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
                return;
            }

            if (view === "checkin") {
                renderCheckInPanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
                return;
            }

            if (view === "checkout") {
                renderCheckOutPanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
                return;
            }

            if (view === "timeline") {
                renderTimelinePanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
                return;
            }

            if (view === "finance") {
                renderFinancePanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
                return;
            }

            if (view === "suppliers") {
                renderSuppliersPanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
                return;
            }

            if (view === "processingOperations") {
                renderDataProcessingOperationsPanel();
                container.classList.remove("sidebar-open");
                sidebarToggleIcon.className = "ti ti-menu-2";
                sidebarToggle.setAttribute("aria-label", "Abrir menu");
            }
        },
    });

    document.getElementById("topbar-container").addEventListener("dashboard-topbar-action", (event) => {
        handleTopbarAction(event.detail.action);
    });

    renderDashboardPanel();

    const sidebarToggle = document.getElementById("sidebar-toggle");
    const sidebarToggleIcon = sidebarToggle.querySelector("i");

    sidebarToggle.onclick = () => {
        const isOpen = container.classList.toggle("sidebar-open");
        sidebarToggleIcon.className = isOpen ? "ti ti-x" : "ti ti-menu-2";
        sidebarToggle.setAttribute("aria-label", isOpen ? "Fechar menu" : "Abrir menu");
    };

    function renderReservationsPanel() {
        topbarContext = {
            newReservationBack: () => renderReservationsPanel(),
            guestBack: () => renderReservationsPanel(),
        };
        renderDashboardTopbar("Reservas");
        renderReservationsView("main-pannel-container", {
            canDelete: permissions.canDeleteOperationalData,
            onOpenReservation: (bookingId) => renderReservationProfilePanel(bookingId),
            onEditReservation: (bookingId) => renderEditReservationPanel(bookingId, () => renderReservationsPanel()),
            onCreateCheckIn: (bookingId) => renderCheckInFormPanel({ bookingId, onBack: () => renderReservationsPanel() }),
            onCreateCheckOut: (bookingId) => renderCheckOutFormPanel({ bookingId, onBack: () => renderReservationsPanel() }),
        });
    }

    function renderUserProfilePanel() {
        topbarContext = {
            newReservationBack: () => renderUserProfilePanel(),
            guestBack: () => renderUserProfilePanel(),
        };
        renderDashboardTopbar("Perfil do usuário");
        renderUserProfileView("main-pannel-container", user, {
            canManageUsers: permissions.canManageUsers,
            canAccessFinance: permissions.canAccessFinance,
            canDeleteOperationalData: permissions.canDeleteOperationalData,
            canManageOperationalData: permissions.canManageOperationalData,
            onBack: () => renderDashboardPanel(),
        });
    }

    function renderEditReservationPanel(bookingId, onBack = () => renderReservationsPanel()) {
        topbarContext = {
            newReservationBack: onBack,
            guestBack: () => renderEditReservationPanel(bookingId, onBack),
        };
        renderDashboardTopbar("Editar reserva");
        renderEditReservationView("main-pannel-container", {
            bookingId,
            onCancel: onBack,
            onSaved: () => renderReservationProfilePanel(bookingId),
        });
    }

    function renderReservationProfilePanel(bookingId) {
        topbarContext = {
            newReservationBack: () => renderReservationProfilePanel(bookingId),
            guestBack: () => renderReservationProfilePanel(bookingId),
        };
        renderDashboardTopbar("Profile da reserva");
        renderReservationProfileView("main-pannel-container", {
            bookingId,
            onBack: () => renderReservationsPanel(),
            onEditReservation: (id) => renderEditReservationPanel(id, () => renderReservationProfilePanel(id)),
            onOpenGuest: (guestId) => renderGuestProfilePanel(guestId),
        });
    }

    function renderDashboardPanel() {
        topbarContext = {
            newReservationBack: () => renderDashboardPanel(),
            guestBack: () => renderDashboardPanel(),
        };
        renderDashboardTopbar("Visão Geral");
        renderMainPannelView("main-pannel-container", {
            onCreateCheckIn: (bookingId) => renderCheckInFormPanel({ bookingId, onBack: () => renderDashboardPanel() }),
            onCreateCheckOut: (bookingId) => renderCheckOutFormPanel({ bookingId, onBack: () => renderDashboardPanel() }),
        });
    }

    function renderNewReservationPanel(onBackToReservations = () => renderReservationsPanel()) {
        topbarContext = {
            newReservationBack: onBackToReservations,
            guestBack: () => renderNewReservationPanel(onBackToReservations),
        };
        renderDashboardTopbar("Nova reserva");
        renderNewReservationView("main-pannel-container", {
            onBackToReservations,
            onRegisterGuest: () => renderGuestFormPanel(() => renderNewReservationPanel(onBackToReservations)),
            onSaved: () => renderReservationsPanel(),
        });
    }

    function renderGuestFormPanel(onBack = () => renderDashboardPanel(), guestId = null) {
        topbarContext = {
            newReservationBack: onBack,
            guestBack: () => renderGuestFormPanel(onBack),
        };
        renderDashboardTopbar(guestId ? "Editar hospede" : "Novo hospede");
        renderGuestFormView("main-pannel-container", {
            guestId,
            canDelete: permissions.canDeleteOperationalData,
            onCancel: onBack,
            onSaved: onBack,
            onDeleted: onBack,
        });
    }

    function renderGuestProfilePanel(guestId) {
        topbarContext = {
            newReservationBack: () => renderGuestProfilePanel(guestId),
            guestBack: () => renderGuestProfilePanel(guestId),
        };
        renderDashboardTopbar("Profile do hospede");
        renderGuestProfileView("main-pannel-container", {
            guestId,
            canAccessFinance: permissions.canAccessFinance,
            onBack: () => renderGuestsPanel(),
            onEditGuest: (id) => renderGuestFormPanel(() => renderGuestProfilePanel(id), id),
            onNewReservation: () => renderNewReservationPanel(() => renderGuestProfilePanel(guestId)),
        });
    }

    function renderGuestsPanel() {
        topbarContext = {
            newReservationBack: () => renderGuestsPanel(),
            guestBack: () => renderGuestsPanel(),
        };
        renderDashboardTopbar("Hóspedes");
        renderGuestsView("main-pannel-container", {
            onNewGuest: () => renderGuestFormPanel(() => renderGuestsPanel()),
            onOpenGuest: (guestId) => renderGuestProfilePanel(guestId),
            onEditGuest: (guestId) => renderGuestFormPanel(() => renderGuestsPanel(), guestId),
            onNewReservation: () => renderNewReservationPanel(() => renderGuestsPanel()),
        });
    }

    function renderRoomsPanel() {
        topbarContext = {
            newReservationBack: () => renderRoomsPanel(),
            guestBack: () => renderRoomsPanel(),
        };
        renderDashboardTopbar("Quartos");
        renderRoomsView("main-pannel-container", {
            canManage: permissions.canManageOperationalData,
            onNewRoom: () => renderRoomFormPanel(),
            onEditRoom: (roomId) => renderRoomFormPanel(roomId),
        });
    }

    function renderTimelinePanel() {
        topbarContext = {
            newReservationBack: () => renderTimelinePanel(),
            guestBack: () => renderTimelinePanel(),
        };
        renderDashboardTopbar("Calendário");
        renderRoomTimelineView("main-pannel-container");
    }

    function renderFinancePanel() {
        if (!permissions.canAccessFinance) {
            renderDashboardPanel();
            return;
        }

        topbarContext = {
            newReservationBack: () => renderFinancePanel(),
            guestBack: () => renderFinancePanel(),
        };
        renderDashboardTopbar("Caixa");
        renderFinanceView("main-pannel-container", {
            onOpenReservation: (bookingId) => renderReservationProfilePanel(bookingId),
            onOpenTransaction: (transactionId) => renderFinancialTransactionProfilePanel(transactionId),
        });
    }

    function renderFinancialTransactionProfilePanel(transactionId) {
        topbarContext = {
            newReservationBack: () => renderFinancialTransactionProfilePanel(transactionId),
            guestBack: () => renderFinancialTransactionProfilePanel(transactionId),
        };
        renderDashboardTopbar("Transação financeira");
        renderFinancialTransactionProfileView("main-pannel-container", {
            transactionId,
            onBack: () => renderFinancePanel(),
            onOpenReservation: (bookingId) => renderReservationProfilePanel(bookingId),
        });
    }

    function renderSuppliersPanel() {
        if (!permissions.canAccessView("suppliers")) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar("Fornecedores");
        renderSuppliersView("main-pannel-container", {
            onNewSupplier: () => renderSupplierFormPanel(),
            onOpenSupplier: (supplierId) => renderSupplierProfilePanel(supplierId),
        });
    }

    function renderSupplierFormPanel(supplierId = null) {
        renderDashboardTopbar(supplierId ? "Editar fornecedor" : "Novo fornecedor");
        renderSupplierFormView("main-pannel-container", {
            supplierId,
            onCancel: () => supplierId ? renderSupplierProfilePanel(supplierId) : renderSuppliersPanel(),
            onSaved: (supplier) => renderSupplierProfilePanel(supplier.id),
        });
    }

    function renderSupplierProfilePanel(supplierId) {
        renderDashboardTopbar("Detalhes do fornecedor");
        renderSupplierProfileView("main-pannel-container", {
            supplierId,
            onBack: () => renderSuppliersPanel(),
            onEdit: (id) => renderSupplierFormPanel(id),
        });
    }

    function renderDataProcessingOperationsPanel() {
        if (!permissions.canAccessView("processingOperations")) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar("Tratamentos e bases legais");
        renderDataProcessingOperationsView("main-pannel-container", {
            onOpenOperation: (operationId) => renderDataProcessingOperationProfilePanel(operationId),
            onOpenAssessments: () => renderLegalBasisAssessmentsPanel(),
        });
    }

    function renderLegalBasisAssessmentsPanel() {
        if (!permissions.canAccessView("processingOperations")) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar("Tratamentos e bases legais");
        renderLegalBasisAssessmentsView("main-pannel-container", {
            onOpenOperations: () => renderDataProcessingOperationsPanel(),
            onOpenAssessment: (assessmentId) => renderLegalBasisAssessmentProfilePanel(assessmentId, {
                type: "assessmentList",
            }),
        });
    }

    function renderDataProcessingOperationProfilePanel(operationId) {
        if (!permissions.canAccessView("processingOperations")) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar("Detalhes do tratamento");
        renderDataProcessingOperationProfileView("main-pannel-container", {
            operationId,
            onBack: () => renderDataProcessingOperationsPanel(),
            onOpenAssessment: (assessmentId) => renderLegalBasisAssessmentProfilePanel(assessmentId, {
                type: "operation",
                operationId,
            }),
            onNewAssessment: (id) => renderLegalBasisAssessmentFormPanel({ operationId: id }),
        });
    }

    function renderLegalBasisAssessmentProfilePanel(assessmentId, origin = { type: "assessmentList" }) {
        if (!permissions.canAccessView("processingOperations")) {
            renderDashboardPanel();
            return;
        }
        const onBack = origin.type === "operation"
                ? () => renderDataProcessingOperationProfilePanel(origin.operationId)
                : () => renderLegalBasisAssessmentsPanel();
        renderDashboardTopbar("Detalhes da base legal");
        renderLegalBasisAssessmentProfileView("main-pannel-container", {
            assessmentId,
            onBack,
            onOpenOperation: (operationId) => renderDataProcessingOperationProfilePanel(operationId),
            onEdit: (id, operationId) => renderLegalBasisAssessmentFormPanel({
                assessmentId: id,
                operationId,
                origin,
                onCancel: () => renderLegalBasisAssessmentProfilePanel(id, origin),
            }),
            onReload: () => renderLegalBasisAssessmentProfilePanel(assessmentId, origin),
        });
    }

    function renderLegalBasisAssessmentFormPanel(formOptions) {
        if (!permissions.canAccessView("processingOperations")) {
            renderDashboardPanel();
            return;
        }
        const onCancel = formOptions.onCancel
                || (() => renderDataProcessingOperationProfilePanel(formOptions.operationId));
        renderDashboardTopbar(formOptions.assessmentId ? "Editar base legal" : "Nova base legal");
        renderLegalBasisAssessmentFormView("main-pannel-container", {
            ...formOptions,
            onCancel,
            onSaved: (assessment) => renderLegalBasisAssessmentProfilePanel(assessment.id, {
                ...(formOptions.origin || { type: "operation", operationId: assessment.processingOperationId }),
            }),
        });
    }

    function renderRoomFormPanel(roomId = null) {
        topbarContext = {
            newReservationBack: () => renderRoomsPanel(),
            guestBack: () => renderRoomFormPanel(roomId),
        };
        renderDashboardTopbar(roomId ? "Editar quarto" : "Novo quarto");
        renderRoomFormView("main-pannel-container", {
            roomId,
            canDelete: permissions.canDeleteOperationalData,
            onCancel: () => renderRoomsPanel(),
            onSaved: () => renderRoomsPanel(),
            onDeleted: () => renderRoomsPanel(),
        });
    }

    function renderDashboardTopbar(title) {
        renderDashboardTopbarWidget("topbar-container", title, {
            canManageOperationalData: permissions.canManageOperationalData,
        });
    }

    function handleTopbarAction(action) {
        if (action === "reservation") {
            renderNewReservationPanel(topbarContext.newReservationBack);
            return;
        }

        if (action === "guest") {
            renderGuestFormPanel(topbarContext.guestBack);
            return;
        }

        if (action === "checkin") {
            renderCheckInFormPanel();
            return;
        }

        if (action === "checkout") {
            renderCheckOutFormPanel();
        }
    }

    function renderCheckInPanel() {
        topbarContext = {
            newReservationBack: () => renderCheckInPanel(),
            guestBack: () => renderCheckInPanel(),
        };
        renderDashboardTopbar("Check-in");
        renderCheckInView("main-pannel-container");
    }

    function renderCheckInFormPanel(formOptions = {}) {
        const onBack = formOptions.onBack || (() => renderCheckInPanel());
        topbarContext = {
            newReservationBack: () => renderCheckInFormPanel(formOptions),
            guestBack: () => renderCheckInFormPanel(formOptions),
        };
        renderDashboardTopbar("Adicionar check-in");
        renderCheckInFormView("main-pannel-container", {
            bookingId: formOptions.bookingId,
            onCancel: onBack,
            onSaved: onBack,
        });
    }

    function renderCheckOutPanel() {
        topbarContext = {
            newReservationBack: () => renderCheckOutPanel(),
            guestBack: () => renderCheckOutPanel(),
        };
        renderDashboardTopbar("Check-out");
        renderCheckOutView("main-pannel-container");
    }

    function renderCheckOutFormPanel(formOptions = {}) {
        const onBack = formOptions.onBack || (() => renderCheckOutPanel());
        topbarContext = {
            newReservationBack: () => renderCheckOutFormPanel(formOptions),
            guestBack: () => renderCheckOutFormPanel(formOptions),
        };
        renderDashboardTopbar("Adicionar checkout");
        renderCheckOutFormView("main-pannel-container", {
            bookingId: formOptions.bookingId,
            onCancel: onBack,
            onSaved: onBack,
        });
    }
}

function roleLabel(role) {
    const labels = {
        CEO: "Chefe Executivo Organizacional (CEO)",
        CTO: "Diretor Geral de Tecnologia (CTO)",
        ADMIN: "Administrador",
        MANAGER: "Gerente",
        RECEPTION: "Recepção",
        HOUSEKEEPING: "Governança",
    };

    return labels[role] || role || "Recepção";
}
