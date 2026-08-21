import { renderGuestFormView } from "../views/guestFormView.js?v=2026-08-12-guest-origin-channel";
import { renderGuestProfileView } from "../views/guestProfileView.js?v=2026-08-13-checkout-rating-stars";
import { renderGuestsView } from "../views/guestsView.js?v=2026-08-12-guest-status";

export function createGuestController({
    permissions,
    renderDashboardTopbar,
    navigation,
    renderNewReservationPanel,
    views = {},
}) {
    const renderGuestForm = views.renderGuestFormView || renderGuestFormView;
    const renderGuestProfile = views.renderGuestProfileView || renderGuestProfileView;
    const renderGuests = views.renderGuestsView || renderGuestsView;

    function openGuestFormPanel(guestId = null) {
        const normalizedGuestId = optionalRecordId(guestId);
        if (guestId !== null && guestId !== undefined && guestId !== "" && normalizedGuestId === null) {
            return false;
        }

        navigation.goTo({
            name: "guestForm",
            params: normalizedGuestId === null ? {} : { guestId: normalizedGuestId },
            render: () => renderGuestFormPanel(() => navigation.back(), normalizedGuestId),
        });
        return true;
    }

    function openGuestProfilePanel(guestId) {
        const normalizedGuestId = requiredRecordId(guestId);
        if (normalizedGuestId === null) {
            return false;
        }

        navigation.goTo({
            name: "guestProfile",
            params: { guestId: normalizedGuestId },
            render: () => renderGuestProfilePanel(normalizedGuestId),
        });
        return true;
    }

    function renderGuestFormPanel(onBack = () => navigation.back(), guestId = null) {
        renderDashboardTopbar(guestId ? "Editar hospede" : "Novo hospede");
        renderGuestForm("main-pannel-container", {
            guestId,
            canDelete: permissions.canDeleteOperationalData,
            onCancel: onBack,
            onSaved: onBack,
            onDeleted: onBack,
        });
    }

    function renderGuestProfilePanel(guestId, onBack = () => navigation.back()) {
        const normalizedGuestId = requiredRecordId(guestId);
        if (normalizedGuestId === null) {
            return false;
        }

        renderDashboardTopbar("Profile do hospede");
        renderGuestProfile("main-pannel-container", {
            guestId: normalizedGuestId,
            canAccessFinance: permissions.canAccessFinance,
            onBack,
            onEditGuest: (id) => openGuestFormPanel(id),
            onNewReservation: () => openNewReservationFromGuestFlow(),
        });
        return true;
    }

    function renderGuestsPanel() {
        renderDashboardTopbar("Hóspedes");
        renderGuests("main-pannel-container", {
            onNewGuest: () => openGuestFormPanel(),
            onOpenGuest: (guestId) => openGuestProfilePanel(guestId),
            onEditGuest: (guestId) => openGuestFormPanel(guestId),
            onNewReservation: () => openNewReservationFromGuestFlow(),
        });
    }

    function openNewReservationFromGuestFlow() {
        const reservationFormStateHolder = { current: null };
        navigation.goTo({
            name: "reservationForm",
            params: {},
            render: () => renderNewReservationPanel(
                    () => navigation.back(),
                    reservationFormStateHolder
            ),
        });
    }

    return {
        openGuestFormPanel,
        openGuestProfilePanel,
        renderGuestFormPanel,
        renderGuestProfilePanel,
        renderGuestsPanel,
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
