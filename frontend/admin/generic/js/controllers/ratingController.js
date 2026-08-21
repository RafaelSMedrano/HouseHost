const RATINGS_PAGE_SIZE = 20;
const RATINGS_DEPENDENCY_VERSION = "2026-08-13-ratings-list";

export function createRatingController({
    navigation,
    permissions,
    renderDashboardTopbar,
    renderGuestProfilePanel,
    renderReservationProfilePanel,
    loadDependencies = loadRatingDependencies,
}) {
    let renderSequence = 0;

    function openRatingsPanel() {
        if (!permissions.canAccessView("ratings")) {
            return false;
        }

        navigation.reset(createRatingsEntry());
        return true;
    }

    function createRatingsEntry(page = 0, size = RATINGS_PAGE_SIZE) {
        return {
            name: "ratings",
            params: { page, size },
            render: (params) => renderRatingsPanel(params),
        };
    }

    async function renderRatingsPanel({ page = 0, size = RATINGS_PAGE_SIZE } = {}) {
        const currentRenderSequence = ++renderSequence;
        renderDashboardTopbar("Avaliações");
        const dependencies = await loadDependencies();
        dependencies.renderRatingsLoadingView("main-pannel-container");

        try {
            const response = await dependencies.findAllRatings(page, size);
            if (currentRenderSequence !== renderSequence) {
                return false;
            }
            dependencies.renderRatingsView("main-pannel-container", {
                pageData: response?.data ?? {},
                onOpenGuest: (guestId) => openGuestProfile(guestId),
                onOpenBooking: (bookingId) => openReservationProfile(bookingId),
                onPageChange: (nextPage) => openRatingsPage(nextPage, size),
            });
            return true;
        } catch (error) {
            if (currentRenderSequence !== renderSequence || error?.name === "AbortError") {
                return false;
            }
            dependencies.renderRatingsErrorView("main-pannel-container");
            return false;
        }
    }

    function openRatingsPage(page, size) {
        const normalizedPage = boundedPage(page);
        if (normalizedPage === null) {
            return false;
        }
        navigation.replace(createRatingsEntry(normalizedPage, size));
        return true;
    }

    function openGuestProfile(guestId) {
        const normalizedGuestId = requiredRecordId(guestId);
        if (normalizedGuestId === null) {
            return false;
        }
        navigation.goTo({
            name: "guestProfile",
            params: { guestId: normalizedGuestId },
            render: () => renderGuestProfilePanel(
                    normalizedGuestId,
                    () => navigation.back()
            ),
        });
        return true;
    }

    function openReservationProfile(bookingId) {
        const normalizedBookingId = requiredRecordId(bookingId);
        if (normalizedBookingId === null) {
            return false;
        }
        navigation.goTo({
            name: "reservationProfile",
            params: { bookingId: normalizedBookingId },
            render: () => renderReservationProfilePanel(
                    normalizedBookingId,
                    () => navigation.back()
            ),
        });
        return true;
    }

    return {
        openRatingsPanel,
        renderRatingsPanel,
    };
}

async function loadRatingDependencies() {
    const [apiModule, ratingsViewModule] = await Promise.all([
        import(`../api.js?v=${RATINGS_DEPENDENCY_VERSION}`),
        import(`../views/ratingsView.js?v=${RATINGS_DEPENDENCY_VERSION}`),
    ]);
    return {
        findAllRatings: apiModule.findAllRatings,
        renderRatingsLoadingView: ratingsViewModule.renderRatingsLoadingView,
        renderRatingsView: ratingsViewModule.renderRatingsView,
        renderRatingsErrorView: ratingsViewModule.renderRatingsErrorView,
    };
}

function requiredRecordId(value) {
    const id = Number(value);
    return Number.isSafeInteger(id) && id > 0 ? id : null;
}

function boundedPage(value) {
    const page = Number(value);
    return Number.isSafeInteger(page) && page >= 0 ? page : null;
}
