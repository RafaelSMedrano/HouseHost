import { renderFinanceView } from "../views/financeView.js?v=2026-06-15-finance-profiles";
import { renderFinancialTransactionProfileView } from "../views/financialTransactionProfileView.js?v=2026-08-17-financial-classification-removal";

export function createFinanceController({
    permissions,
    renderDashboardTopbar,
    navigation,
    renderDashboardPanel,
    renderReservationProfilePanel,
    renderGuestProfilePanel,
    views = {},
}) {
    const renderFinance = views.renderFinanceView || renderFinanceView;
    const renderFinancialTransactionProfile = views.renderFinancialTransactionProfileView
        || renderFinancialTransactionProfileView;

    function openFinancePanel() {
        if (!permissions.canAccessFinance) {
            navigation.reset(dashboardEntry());
            return false;
        }

        navigation.reset({
            name: "finance",
            params: {},
            render: () => renderFinancePanel(),
        });
        return true;
    }

    function dashboardEntry() {
        return {
            name: "dashboard",
            params: {},
            render: () => renderDashboardPanel(),
        };
    }

    function navigateToReservation(bookingId) {
        const normalizedBookingId = requiredRecordId(bookingId);
        if (normalizedBookingId === null) {
            return false;
        }

        navigation.goTo({
            name: "reservationProfile",
            params: { bookingId: normalizedBookingId },
            render: () => renderReservationProfilePanel(
                normalizedBookingId,
                () => navigation.back(),
            ),
        });
        return true;
    }

    function openTransactionProfilePanel(transactionId) {
        const normalizedTransactionId = requiredRecordId(transactionId);
        if (normalizedTransactionId === null) {
            return false;
        }

        navigation.goTo({
            name: "transactionProfile",
            params: { transactionId: normalizedTransactionId },
            render: () => renderTransactionProfilePanel(normalizedTransactionId),
        });
        return true;
    }

    function navigateToGuest(guestId) {
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
    }

    function renderFinancePanel() {
        if (!permissions.canAccessFinance) {
            renderDashboardPanel();
            return false;
        }
        renderDashboardTopbar("Caixa");
        renderFinance("main-pannel-container", {
            onOpenReservation: (bookingId) => navigateToReservation(bookingId),
            onOpenTransaction: (transactionId) => openTransactionProfilePanel(transactionId),
        });
        return true;
    }

    function renderTransactionProfilePanel(transactionId, onBack = () => navigation.back()) {
        const normalizedTransactionId = requiredRecordId(transactionId);
        if (normalizedTransactionId === null) {
            return false;
        }

        renderDashboardTopbar("Transação financeira");
        renderFinancialTransactionProfile("main-pannel-container", {
            transactionId: normalizedTransactionId,
            onBack,
            onOpenReservation: (bookingId) => navigateToReservation(bookingId),
            onOpenGuest: (guestId) => navigateToGuest(guestId),
        });
        return true;
    }

    return {
        openFinancePanel,
        openTransactionProfilePanel,
        renderFinancePanel,
        renderTransactionProfilePanel,
    };
}

function requiredRecordId(value) {
    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : null;
}
