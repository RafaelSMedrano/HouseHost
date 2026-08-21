const ROLE_GROUPS = {
    administrators: new Set(["CEO", "CTO", "ADMIN"]),
    management: new Set(["CEO", "CTO", "ADMIN", "MANAGER"]),
    operational: new Set(["CEO", "CTO", "ADMIN", "MANAGER", "RECEPTION"]),
    all: new Set(["CEO", "CTO", "ADMIN", "MANAGER", "RECEPTION", "HOUSEKEEPING"]),
};

const VIEW_ACCESS = {
    dashboard: ROLE_GROUPS.all,
    rooms: ROLE_GROUPS.all,
    checkin: ROLE_GROUPS.operational,
    checkout: ROLE_GROUPS.operational,
    reservations: ROLE_GROUPS.operational,
    guests: ROLE_GROUPS.operational,
    timeline: ROLE_GROUPS.operational,
    finance: ROLE_GROUPS.management,
    audit: ROLE_GROUPS.administrators,
    suppliers: ROLE_GROUPS.administrators,
    processingOperations: ROLE_GROUPS.administrators,
    settings: ROLE_GROUPS.administrators,
};

export function canAccessView(role, view) {
    return Boolean(VIEW_ACCESS[view]?.has(normalizeRole(role)));
}

export function permissionsFor(role) {
    const normalizedRole = normalizeRole(role);

    return {
        role: normalizedRole,
        canManageUsers: ROLE_GROUPS.administrators.has(normalizedRole),
        canAccessFinance: ROLE_GROUPS.management.has(normalizedRole),
        canDeleteOperationalData: ROLE_GROUPS.management.has(normalizedRole),
        canManageOperationalData: ROLE_GROUPS.operational.has(normalizedRole),
        canAccessView: (view) => canAccessView(normalizedRole, view),
    };
}

function normalizeRole(role) {
    return String(role || "RECEPTION").trim().toUpperCase();
}
