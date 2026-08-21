export function createSidebarController({ user, permissions, closeSidebar, onUserProfile, rootActions }) {
    return {
        id: user.id,
        name: user.username,
        role: user.role,
        roleLabel: roleLabel(user.role),
        photoUrl: user.photoUrl,
        onUserProfile: () => {
            onUserProfile();
            closeSidebar();
            return true;
        },
        onNavigate: (view) => {
            if (!permissions.canAccessView(view)) {
                return false;
            }

            const resetRoot = rootActions[view];
            if (!resetRoot) {
                return false;
            }

            resetRoot();
            closeSidebar();
            return true;
        },
    };
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
