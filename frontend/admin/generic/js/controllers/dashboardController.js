import { renderMainPannelView } from "../views/mainPannelView.js?v=2026-05-20-dashboard-checkin-button";

export function createDashboardController({
    renderDashboardTopbar,
    navigation,
    renderCheckInFormPanel,
    renderCheckOutFormPanel,
}) {
    function renderDashboardPanel() {
        renderDashboardTopbar("Visão Geral");
        renderMainPannelView("main-pannel-container", {
            onCreateCheckIn: (bookingId) => navigation.goTo({
                name: "checkinForm",
                params: { bookingId },
                render: () => renderCheckInFormPanel({
                    bookingId,
                    onBack: () => navigation.back(),
                }),
            }),
            onCreateCheckOut: (bookingId) => navigation.goTo({
                name: "checkoutForm",
                params: { bookingId },
                render: () => renderCheckOutFormPanel({
                    bookingId,
                    onBack: () => navigation.back(),
                }),
            }),
        });
    }

    return { renderDashboardPanel };
}
