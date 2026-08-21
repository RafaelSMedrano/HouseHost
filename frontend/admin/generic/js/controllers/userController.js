import { renderUserProfileView } from "../views/userProfileView.js?v=2026-08-10-navigation-accessibility";

export function createUserController({ user, permissions, renderDashboardTopbar, navigation, views = {} }) {
    const renderUserProfile = views.renderUserProfileView || renderUserProfileView;

    function openUserProfilePanel() {
        navigation.reset({
            name: "userProfile",
            params: {},
            render: () => renderUserProfilePanel(),
        });
    }

    function renderUserProfilePanel() {
        renderDashboardTopbar("Perfil do usuário");
        renderUserProfile("main-pannel-container", user, {
            canManageUsers: permissions.canManageUsers,
            canAccessFinance: permissions.canAccessFinance,
            canDeleteOperationalData: permissions.canDeleteOperationalData,
            canManageOperationalData: permissions.canManageOperationalData,
            onBack: () => navigation.back(),
        });
    }

    return { openUserProfilePanel, renderUserProfilePanel };
}
