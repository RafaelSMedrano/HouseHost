import { renderDashboardTopbarWidget } from "../widgets/dashboardTopbarWidget.js?v=2026-08-10-navigation-accessibility";
import { renderSidebarWidget } from "../widgets/sidebarWidget.js?v=2026-08-13-ratings-navigation";
import { permissionsFor } from "../permissions.js?v=2026-08-13-ratings-navigation";
import { createNavigationController } from "./navigationController.js?v=2026-08-10-navigation-accessibility";
import { createSidebarController } from "./sidebarController.js?v=2026-08-10-navigation-accessibility";
import { createDashboardController } from "./dashboardController.js?v=2026-08-10-direct-navigation-dependencies";
import { createGuestController } from "./guestController.js?v=2026-08-13-guest-rating-history";
import { createReservationController } from "./reservationController.js?v=2026-08-13-history-arrow-decoration";
import { createRoomController } from "./roomController.js?v=2026-08-10-operational-governance-navigation";
import { createOperationsController } from "./operationsController.js?v=2026-08-20-ftp-checkout-materialization";
import { createFinanceController } from "./financeController.js?v=2026-08-17-financial-classification-removal";
import { createSupplierController } from "./supplierController.js?v=2026-08-10-navigation-accessibility";
import { createPrivacyController } from "./privacyController.js?v=2026-08-10-navigation-accessibility";
import { createUserController } from "./userController.js?v=2026-08-10-navigation-accessibility";
import { createRatingController } from "./ratingController.js?v=2026-08-13-ratings-list";

export function startUIController(containerId, user = {}) {
    const container = document.getElementById(containerId);
    if (!container) return;

    const permissions = permissionsFor(user.role);
    container.className = "dashboard-home";
    container.innerHTML = `
        <div id="sidebar-container" class="ui-sidebar"></div>
        <div id="topbar-container" class="ui-topbar"></div>
        <div id="main-pannel-container" class="ui-main-pannel"></div>
        <button id="sidebar-toggle" class="sidebar-toggle" type="button" aria-label="Abrir menu" aria-controls="sidebar-container" aria-expanded="false">
            <i class="ti ti-menu-2" aria-hidden="true"></i>
        </button>
    `;

    const sidebarToggle = document.getElementById("sidebar-toggle");
    const sidebarToggleIcon = sidebarToggle.querySelector("i");

    function closeSidebar() {
        container.classList.remove("sidebar-open");
        sidebarToggleIcon.className = "ti ti-menu-2";
        sidebarToggle.setAttribute("aria-label", "Abrir menu");
        sidebarToggle.setAttribute("aria-expanded", "false");
    }

    function renderDashboardTopbar(title) {
        renderDashboardTopbarWidget("topbar-container", title, {
            canManageOperationalData: permissions.canManageOperationalData,
        });
    }

    let dashboardController;
    let guestController;
    let reservationController;
    let roomController;
    let operationsController;
    let financeController;
    let supplierController;
    let privacyController;
    let userController;
    let ratingController;

    // O fallback fecha sobre dashboardController, inicializado antes da
    // primeira navegação.
    const navigation = createNavigationController({
        fallbackPage: {
            name: "dashboard",
            params: {},
            render: () => dashboardController.renderDashboardPanel(),
        },
        onRendered: (page) => {
            syncSidebarCurrentRoot(page.name);
            focusCurrentPageHeading();
        },
    });

    operationsController = createOperationsController({
        navigation,
        renderDashboardTopbar,
    });
    reservationController = createReservationController({
        navigation,
        permissions,
        renderDashboardTopbar,
        renderCheckInFormPanel: (options) => operationsController.renderCheckInFormPanel(options),
        renderCheckOutFormPanel: (options) => operationsController.renderCheckOutFormPanel(options),
        renderGuestProfilePanel: (guestId, onBack) => guestController.renderGuestProfilePanel(guestId, onBack),
        renderGuestFormPanel: (onBack) => guestController.renderGuestFormPanel(onBack),
    });
    guestController = createGuestController({
        navigation,
        permissions,
        renderDashboardTopbar,
        renderNewReservationPanel: (onBack) => reservationController.renderNewReservationPanel(onBack),
    });
    dashboardController = createDashboardController({
        navigation,
        renderDashboardTopbar,
        renderCheckInFormPanel: (options) => operationsController.renderCheckInFormPanel(options),
        renderCheckOutFormPanel: (options) => operationsController.renderCheckOutFormPanel(options),
    });
    roomController = createRoomController({
        navigation,
        permissions,
        renderDashboardTopbar,
    });
    financeController = createFinanceController({
        navigation,
        permissions,
        renderDashboardTopbar,
        renderDashboardPanel: () => dashboardController.renderDashboardPanel(),
        renderReservationProfilePanel: (bookingId, onBack) => reservationController.renderReservationProfilePanel(bookingId, onBack),
        renderGuestProfilePanel: (guestId, onBack) => guestController.renderGuestProfilePanel(guestId, onBack),
    });
    supplierController = createSupplierController({
        navigation,
        permissions,
        renderDashboardTopbar,
        renderDashboardPanel: () => dashboardController.renderDashboardPanel(),
    });
    privacyController = createPrivacyController({
        navigation,
        permissions,
        renderDashboardTopbar,
        renderDashboardPanel: () => dashboardController.renderDashboardPanel(),
    });
    userController = createUserController({
        navigation,
        user,
        permissions,
        renderDashboardTopbar,
    });
    ratingController = createRatingController({
        navigation,
        permissions,
        renderDashboardTopbar,
        renderGuestProfilePanel: (guestId, onBack) =>
            guestController.renderGuestProfilePanel(guestId, onBack),
        renderReservationProfilePanel: (bookingId, onBack) =>
            reservationController.renderReservationProfilePanel(bookingId, onBack),
    });

    renderSidebarWidget("sidebar-container", createSidebarController({
        user,
        permissions,
        closeSidebar,
        onUserProfile: () => userController.openUserProfilePanel(),
        rootActions: createSidebarRootActions({
            navigation,
            dashboardController,
            reservationController,
            guestController,
            roomController,
            operationsController,
            financeController,
            supplierController,
            privacyController,
            ratingController,
        }),
    }));

    document.getElementById("topbar-container").addEventListener("dashboard-topbar-action", (event) => {
        handleTopbarAction(event.detail.action);
    });

    sidebarToggle.onclick = () => {
        const isOpen = container.classList.toggle("sidebar-open");
        sidebarToggleIcon.className = isOpen ? "ti ti-x" : "ti ti-menu-2";
        sidebarToggle.setAttribute("aria-label", isOpen ? "Fechar menu" : "Abrir menu");
        sidebarToggle.setAttribute("aria-expanded", String(isOpen));
    };

    navigation.reset({
        name: "dashboard",
        params: {},
        render: () => dashboardController.renderDashboardPanel(),
    });

    function handleTopbarAction(action) {
        executeTopbarNavigationAction(action, {
            reservationController,
            guestController,
            operationsController,
        });
    }
}

export function createSidebarRootActions({
    navigation,
    dashboardController,
    reservationController,
    guestController,
    roomController,
    operationsController,
    financeController,
    supplierController,
    privacyController,
    ratingController,
}) {
    return {
        dashboard: () => resetRoot(navigation, "dashboard", () => dashboardController.renderDashboardPanel()),
        reservations: () => resetRoot(navigation, "reservations", () => reservationController.renderReservationsPanel()),
        guests: () => resetRoot(navigation, "guests", () => guestController.renderGuestsPanel()),
        rooms: () => roomController.openRoomsPanel(),
        checkin: () => operationsController.openCheckInPanel(),
        checkout: () => operationsController.openCheckOutPanel(),
        timeline: () => operationsController.openTimelinePanel(),
        finance: () => financeController.openFinancePanel(),
        suppliers: () => supplierController.openSuppliersPanel(),
        processingOperations: () => privacyController.openDataProcessingOperationsPanel(),
        ratings: () => ratingController.openRatingsPanel(),
    };
}

export function executeTopbarNavigationAction(action, {
    reservationController,
    guestController,
    operationsController,
}) {
    const actionMap = {
        reservation: () => reservationController.openNewReservationPanel(),
        guest: () => guestController.openGuestFormPanel(),
        checkin: () => operationsController.openCheckInFormPanel(),
        checkout: () => operationsController.openCheckOutFormPanel(),
    };
    const navigate = actionMap[action];
    if (!navigate) {
        return false;
    }

    navigate();
    return true;
}

function resetRoot(navigation, name, render) {
    return navigation.reset({ name, params: {}, render });
}

export function focusCurrentPageHeading(root = document) {
    const heading = root.querySelector("#topbar-container [data-page-heading]");
    if (!heading) {
        return false;
    }

    heading.focus({ preventScroll: true });
    return true;
}

export function syncSidebarCurrentRoot(pageName, root = document) {
    const itemList = [...root.querySelectorAll("#sidebar-container [data-view]")];
    const currentItem = itemList.find((item) => item.dataset.view === pageName);
    if (!currentItem && pageName !== "userProfile") {
        return false;
    }

    itemList.forEach((item) => {
        item.classList.remove("active");
        item.removeAttribute("aria-current");
    });
    if (currentItem) {
        currentItem.classList.add("active");
        currentItem.setAttribute("aria-current", "page");
    }
    return true;
}
