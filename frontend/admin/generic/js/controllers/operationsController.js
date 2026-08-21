import { renderCheckInView, renderCheckOutView } from "../views/checkOperationsView.js?v=2026-05-20-check-kpis-single-row";
import { renderCheckInFormView, renderCheckOutFormView } from "../views/checkOperationFormView.js?v=2026-08-20-ftp-checkout-materialization";
import { renderRoomTimelineView } from "../views/roomTimelineView.js?v=2026-05-18-timeline";

export function createOperationsController({ renderDashboardTopbar, navigation, views = {} }) {
    const renderCheckIn = views.renderCheckInView || renderCheckInView;
    const renderCheckOut = views.renderCheckOutView || renderCheckOutView;
    const renderCheckInForm = views.renderCheckInFormView || renderCheckInFormView;
    const renderCheckOutForm = views.renderCheckOutFormView || renderCheckOutFormView;
    const renderRoomTimeline = views.renderRoomTimelineView || renderRoomTimelineView;

    function openCheckInPanel() {
        navigation.reset({
            name: "checkin",
            params: {},
            render: () => renderCheckInPanel(),
        });
    }

    function openCheckOutPanel() {
        navigation.reset({
            name: "checkout",
            params: {},
            render: () => renderCheckOutPanel(),
        });
    }

    function openTimelinePanel() {
        navigation.reset({
            name: "timeline",
            params: {},
            render: () => renderTimelinePanel(),
        });
    }

    function openCheckInFormPanel(options = {}) {
        const bookingId = optionalRecordId(options.bookingId);
        if (options.bookingId != null && bookingId === null) {
            return false;
        }

        navigation.goTo({
            name: "checkinForm",
            params: bookingId === null ? {} : { bookingId },
            render: () => renderCheckInFormPanel({
                bookingId,
                onBack: () => navigation.back(),
            }),
        });
        return true;
    }

    function openCheckOutFormPanel(options = {}) {
        const bookingId = optionalRecordId(options.bookingId);
        if (options.bookingId != null && bookingId === null) {
            return false;
        }

        navigation.goTo({
            name: "checkoutForm",
            params: bookingId === null ? {} : { bookingId },
            render: () => renderCheckOutFormPanel({
                bookingId,
                onBack: () => navigation.back(),
            }),
        });
        return true;
    }

    function renderCheckInPanel() {
        renderDashboardTopbar("Check-in");
        renderCheckIn("main-pannel-container");
    }

    function renderCheckInFormPanel(formOptions = {}) {
        const onBack = formOptions.onBack || (() => navigation.back());
        renderDashboardTopbar("Adicionar check-in");
        renderCheckInForm("main-pannel-container", {
            bookingId: formOptions.bookingId,
            onCancel: onBack,
            onSaved: onBack,
        });
    }

    function renderCheckOutPanel() {
        renderDashboardTopbar("Check-out");
        renderCheckOut("main-pannel-container");
    }

    function renderCheckOutFormPanel(formOptions = {}) {
        const onBack = formOptions.onBack || (() => navigation.back());
        renderDashboardTopbar("Adicionar checkout");
        renderCheckOutForm("main-pannel-container", {
            bookingId: formOptions.bookingId,
            onCancel: onBack,
            onSaved: onBack,
        });
    }

    function renderTimelinePanel() {
        renderDashboardTopbar("Calendário");
        renderRoomTimeline("main-pannel-container");
    }

    return {
        openCheckInPanel,
        openCheckInFormPanel,
        openCheckOutPanel,
        openCheckOutFormPanel,
        openTimelinePanel,
        renderCheckInPanel,
        renderCheckInFormPanel,
        renderCheckOutPanel,
        renderCheckOutFormPanel,
        renderTimelinePanel,
    };
}

function optionalRecordId(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }

    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : null;
}
