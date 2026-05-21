import { renderMainPannelView } from "../views/mainPannelView.js?v=2026-05-20-dashboard-checkin-button";
import { renderGuestFormView } from "../views/guestFormView.js?v=2026-05-19-guest-status-enum";
import { renderGuestProfileView } from "../views/guestProfileView.js?v=2026-05-20-waiting-payment-green";
import { renderGuestsView } from "../views/guestsView.js?v=2026-05-20-guests-situation-filter";
import { renderNewReservationView } from "../views/newReservationView.js?v=2026-05-20-booking-origin-enum";
import { renderReservationsView } from "../views/reservationsView.js?v=2026-05-20-reservation-status-colors";
import { renderReservationProfileView } from "../views/reservationProfileView.js?v=2026-05-20-reservation-payment-action";
import { renderEditReservationView } from "../views/editReservationView.js?v=2026-05-20-edit-reservation";
import { renderRoomFormView } from "../views/roomFormView.js?v=2026-05-18-timeline";
import { renderRoomsView } from "../views/roomsView.js?v=2026-05-20-rooms-active-stays";
import { renderCheckInView, renderCheckOutView } from "../views/checkOperationsView.js?v=2026-05-20-check-kpis-single-row";
import { renderCheckInFormView, renderCheckOutFormView } from "../views/checkOperationFormView.js?v=2026-05-20-check-form-footer-visible";
import { renderFinanceView } from "../views/financeView.js?v=2026-05-20-cashier-movements";
import { renderRoomTimelineView } from "../views/roomTimelineView.js?v=2026-05-18-timeline";
import { renderUserProfileView } from "../views/userProfileView.js?v=2026-05-20-profile-crud";
import { renderDashboardTopbarWidget } from "../widgets/dashboardTopbarWidget.js?v=2026-05-18-timeline";
import { renderSidebarWidget } from "../widgets/sidebarWidget.js?v=2026-05-20-logo-jpeg";

export function startUIController(containerId, user = {}) {
    const container = document.getElementById(containerId);
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
        role: roleLabel(user.role),
        photoUrl: user.photoUrl,
        onUserProfile: () => {
            renderUserProfilePanel();
            container.classList.remove("sidebar-open");
            sidebarToggleIcon.className = "ti ti-menu-2";
            sidebarToggle.setAttribute("aria-label", "Abrir menu");
        },
        onNavigate: (view) => {
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
            onOpenReservation: (bookingId) => renderReservationProfilePanel(bookingId),
            onEditReservation: (bookingId) => renderEditReservationPanel(bookingId, () => renderReservationsPanel()),
            onCreateCheckIn: (bookingId) => renderCheckInFormPanel({ bookingId, onBack: () => renderReservationsPanel() }),
        });
    }

    function renderUserProfilePanel() {
        topbarContext = {
            newReservationBack: () => renderUserProfilePanel(),
            guestBack: () => renderUserProfilePanel(),
        };
        renderDashboardTopbar("Perfil do usuário");
        renderUserProfileView("main-pannel-container", user, {
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
            onCreateCheckOut: (stayId) => renderCheckOutFormPanel({ stayId, onBack: () => renderDashboardPanel() }),
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
        topbarContext = {
            newReservationBack: () => renderFinancePanel(),
            guestBack: () => renderFinancePanel(),
        };
        renderDashboardTopbar("Caixa");
        renderFinanceView("main-pannel-container");
    }

    function renderRoomFormPanel(roomId = null) {
        topbarContext = {
            newReservationBack: () => renderRoomsPanel(),
            guestBack: () => renderRoomFormPanel(roomId),
        };
        renderDashboardTopbar(roomId ? "Editar quarto" : "Novo quarto");
        renderRoomFormView("main-pannel-container", {
            roomId,
            onCancel: () => renderRoomsPanel(),
            onSaved: () => renderRoomsPanel(),
            onDeleted: () => renderRoomsPanel(),
        });
    }

    function renderDashboardTopbar(title) {
        renderDashboardTopbarWidget("topbar-container", title);
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
            stayId: formOptions.stayId,
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
            stayId: formOptions.stayId,
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
