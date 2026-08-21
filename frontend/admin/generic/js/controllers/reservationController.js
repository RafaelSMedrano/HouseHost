import { renderNewReservationView } from "../views/newReservationView.js?v=2026-08-20-reservation-room-loading-fix";
import { renderReservationsView } from "../views/reservationsView.js?v=2026-05-20-booking-origin-enum";
import { renderReservationProfileView } from "../views/reservationProfileView.js?v=2026-08-10-navigation-accessibility";
import { renderEditReservationView } from "../views/editReservationView.js?v=2026-06-06-checkout-status";

export function createReservationController({
    permissions,
    renderDashboardTopbar,
    navigation,
    renderCheckInFormPanel,
    renderCheckOutFormPanel,
    renderGuestProfilePanel,
    renderGuestFormPanel,
    views = {},
}) {
    const renderNewReservation = views.renderNewReservationView || renderNewReservationView;
    const renderReservations = views.renderReservationsView || renderReservationsView;
    const renderReservationProfile = views.renderReservationProfileView || renderReservationProfileView;
    const renderEditReservation = views.renderEditReservationView || renderEditReservationView;

    function openNewReservationPanel() {
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

    function replaceWithReservationProfile(bookingId) {
        const normalizedBookingId = requiredRecordId(bookingId);
        if (normalizedBookingId === null) {
            return false;
        }

        navigation.replace({
            name: "reservationProfile",
            params: { bookingId: normalizedBookingId },
            render: () => renderReservationProfilePanel(
                normalizedBookingId,
                () => navigation.back(),
            ),
        });
        return true;
    }

    function openReservationProfilePanel(bookingId) {
        const normalizedBookingId = requiredRecordId(bookingId);
        if (normalizedBookingId === null) {
            return false;
        }

        navigation.goTo({
            name: "reservationProfile",
            params: { bookingId: normalizedBookingId },
            render: () => renderReservationProfilePanel(normalizedBookingId),
        });
        return true;
    }

    function openEditReservationPanel(bookingId) {
        const normalizedBookingId = requiredRecordId(bookingId);
        if (normalizedBookingId === null) {
            return false;
        }

        navigation.goTo({
            name: "reservationForm",
            params: { bookingId: normalizedBookingId },
            render: () => renderEditReservationPanel(
                normalizedBookingId,
                () => navigation.back(),
                () => replaceWithReservationProfile(normalizedBookingId),
            ),
        });
        return true;
    }

    function openOperationForm(name, bookingId, renderPanel) {
        const normalizedBookingId = requiredRecordId(bookingId);
        if (normalizedBookingId === null) {
            return false;
        }

        navigation.goTo({
            name,
            params: { bookingId: normalizedBookingId },
            render: () => renderPanel({
                bookingId: normalizedBookingId,
                onBack: () => navigation.back(),
            }),
        });
        return true;
    }

    function renderReservationsPanel() {
        renderDashboardTopbar("Reservas");
        renderReservations("main-pannel-container", {
            canDelete: permissions.canDeleteOperationalData,
            onOpenReservation: (bookingId) => openReservationProfilePanel(bookingId),
            onEditReservation: (bookingId) => openEditReservationPanel(bookingId),
            onCreateCheckIn: (bookingId) => openOperationForm("checkinForm", bookingId, renderCheckInFormPanel),
            onCreateCheckOut: (bookingId) => openOperationForm("checkoutForm", bookingId, renderCheckOutFormPanel),
        });
    }

    function renderReservationProfilePanel(bookingId, onBack = () => navigation.back()) {
        const normalizedBookingId = requiredRecordId(bookingId);
        if (normalizedBookingId === null) {
            return false;
        }

        renderDashboardTopbar("Profile da reserva");
        renderReservationProfile("main-pannel-container", {
            bookingId: normalizedBookingId,
            permissions,
            onBack,
            onEditReservation: (id) => {
                const normalizedId = requiredRecordId(id);
                if (normalizedId === null) {
                    return false;
                }
                navigation.goTo({
                    name: "reservationForm",
                    params: { bookingId: normalizedId },
                    render: () => renderEditReservationPanel(
                        normalizedId,
                        () => navigation.back(),
                        () => navigation.back(),
                    ),
                });
                return true;
            },
            onOpenGuest: (guestId) => {
                const normalizedGuestId = requiredRecordId(guestId);
                if (normalizedGuestId === null) {
                    return false;
                }
                navigation.goTo({
                    name: "guestProfile",
                    params: { guestId: normalizedGuestId },
                    render: () => renderGuestProfilePanel(
                        normalizedGuestId,
                        () => navigation.back(),
                    ),
                });
                return true;
            },
        });
        return true;
    }

    function renderEditReservationPanel(
        bookingId,
        onBack = () => navigation.back(),
        onSaved = () => replaceWithReservationProfile(bookingId),
    ) {
        renderDashboardTopbar("Editar reserva");
        renderEditReservation("main-pannel-container", {
            bookingId,
            onCancel: onBack,
            onSaved,
        });
    }

    function renderNewReservationPanel(
        onBackToReservations = () => navigation.back(),
        reservationFormStateHolder = { current: null },
    ) {
        renderDashboardTopbar("Nova reserva");
        renderNewReservation("main-pannel-container", {
            initialState: reservationFormStateHolder.current,
            onBackToReservations,
            onOpenBooking: (bookingId, reservationFormStateRecord) => {
                reservationFormStateHolder.current = reservationFormStateRecord;
                return openReservationProfilePanel(bookingId);
            },
            onRegisterGuest: () => navigation.goTo({
                name: "guestForm",
                params: {},
                render: () => renderGuestFormPanel(
                    () => navigation.back(),
                ),
            }),
            onSaved: (reservation) => {
                reservationFormStateHolder.current = null;
                const bookingId = reservation?.id;

                if (bookingId) {
                    replaceWithReservationProfile(bookingId);
                    return;
                }

                navigation.replace({
                    name: "reservations",
                    params: {},
                    render: () => renderReservationsPanel(),
                });
            },
        });
    }

    return {
        openNewReservationPanel,
        openReservationProfilePanel,
        openEditReservationPanel,
        renderReservationsPanel,
        renderReservationProfilePanel,
        renderEditReservationPanel,
        renderNewReservationPanel,
    };
}

function requiredRecordId(value) {
    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : null;
}
