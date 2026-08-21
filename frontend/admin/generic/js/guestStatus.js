const statusPresentationMap = {
    WITH_UNCONFIRMED_BOOKING: {
        label: "Com reserva não confirmada",
        badgeClass: "status-unconfirmed",
    },
    WITH_CONFIRMED_BOOKING: {
        label: "Com reserva confirmada",
        badgeClass: "status-confirmed",
    },
    IN_STAY: {
        label: "Em estadia",
        badgeClass: "status-stay",
    },
    INACTIVE: {
        label: "Inativo",
        badgeClass: "status-inactive",
    },
};

const legacyStatusAliasMap = {
    IN_BOOKING: "WITH_UNCONFIRMED_BOOKING",
    COM_RESERVA: "WITH_UNCONFIRMED_BOOKING",
    EM_ESTADIA: "IN_STAY",
    GOT_CHECKOUT: "INACTIVE",
    COM_CHECK_OUT: "INACTIVE",
    INATIVO: "INACTIVE",
};

export function normalizeGuestStatus(status) {
    const normalizedStatus = String(status || "INACTIVE").toUpperCase();
    return legacyStatusAliasMap[normalizedStatus] || normalizedStatus;
}

export function guestStatusLabel(status) {
    const normalizedStatus = normalizeGuestStatus(status);
    return statusPresentationMap[normalizedStatus]?.label || normalizedStatus;
}

export function guestStatusBadgeClass(status) {
    const normalizedStatus = normalizeGuestStatus(status);
    return statusPresentationMap[normalizedStatus]?.badgeClass || "status-inactive";
}
