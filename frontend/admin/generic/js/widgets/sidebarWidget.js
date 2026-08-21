import { renderBrandLogoMarkup } from "./brandLogoWidget.js?v=2026-06-06-pousada-logo";
import { canAccessView } from "../permissions.js?v=2026-08-13-ratings-navigation";

export function renderSidebarWidget(containerId, user = {}) {
    const container = document.getElementById(containerId);
    const username = user.name || "Usuário";
    const role = user.role || "RECEPTION";
    const roleLabel = user.roleLabel || role;
    const photoUrl = user.photoUrl || "";
    const onNavigate = typeof user.onNavigate === "function" ? user.onNavigate : null;
    const onUserProfile = typeof user.onUserProfile === "function" ? user.onUserProfile : null;
    const safeUsername = escapeHtml(username);
    const safeRole = escapeHtml(roleLabel);
    const safePhotoUrl = escapeHtml(photoUrl);

    container.innerHTML = `
<div class="sidebar">
  <div class="brand">
    ${renderBrandLogoMarkup("sidebar")}
  </div>

  <nav aria-label="Navegação principal">
    <div class="nav-group">Geral</div>
    ${navItem(role, "dashboard", "ti-layout-dashboard", "Dashboard", true)}
    ${navItem(role, "rooms", "ti-door", "Quartos", false, '<span class="nav-badge nb-rose">2</span>')}

    ${hasAnyView(role, ["checkin", "checkout", "reservations", "guests", "ratings"]) ? '<div class="nav-group">Hospedagem</div>' : ""}
    ${navItem(role, "checkin", "ti-login", "Check-in")}
    ${navItem(role, "checkout", "ti-logout", "Check-out")}
    ${navItem(role, "reservations", "ti-calendar-event", "Reservas")}
    ${navItem(role, "guests", "ti-users", "Hóspedes")}
    ${navItem(role, "ratings", "ti-stars", "Avaliações")}
    <!-- Item temporariamente oculto:
    <div class="nav-item" data-view="timeline"><i class="ti ti-calendar-time"></i> Calendário</div>
    -->

    ${canAccessView(role, "finance") ? '<div class="nav-group">Operação</div>' : ""}
    ${navItem(role, "finance", "ti-cash-banknote", "Caixa")}
    ${hasAnyView(role, ["suppliers", "processingOperations"]) ? '<div class="nav-group">Privacidade</div>' : ""}
    ${navItem(role, "suppliers", "ti-building-store", "Fornecedores")}
    ${navItem(role, "processingOperations", "ti-shield-lock", "Tratamentos e bases legais")}
    <!-- Itens temporariamente ocultos:
    <div class="nav-item"><i class="ti ti-package"></i> Consumíveis <span class="nav-badge nb-amber">3</span></div>
    <div class="nav-item"><i class="ti ti-tool"></i> Manutenção</div>
    <div class="nav-item"><i class="ti ti-chart-bar"></i> Relatórios</div>
    -->

    <!-- Configuracoes e auditoria serao exibidas aqui quando suas telas forem implementadas. -->
  </nav>

  <button class="sidebar-user" type="button" data-user-profile aria-label="Abrir perfil de ${safeUsername}">
    <div class="avatar">${photoUrl ? `<img src="${safePhotoUrl}" alt="">` : initialsFor(username)}</div>
    <div>
      <div class="user-name">${safeUsername}</div>
      <div class="user-role">${safeRole}</div>
    </div>
  </button>
</div>
    `;

    container.querySelectorAll(".nav-item").forEach((item) => {
        item.addEventListener("click", () => {
            container.querySelectorAll(".nav-item").forEach((navItem) => {
                navItem.classList.remove("active");
                navItem.removeAttribute("aria-current");
            });
            item.classList.add("active");
            item.setAttribute("aria-current", "page");

            if (onNavigate && item.dataset.view) {
                onNavigate(item.dataset.view);
            }
        });
    });

    container.querySelector("[data-user-profile]")?.addEventListener("click", () => {
        if (onUserProfile) {
            onUserProfile();
        }
    });
}

function navItem(role, view, icon, label, active = false, suffix = "") {
    if (!canAccessView(role, view)) {
        return "";
    }

    return `<button class="nav-item ${active ? "active" : ""}" type="button" data-view="${view}"${active ? ' aria-current="page"' : ""}><i class="ti ${icon}" aria-hidden="true"></i> ${label} ${suffix}</button>`;
}

function hasAnyView(role, views) {
    return views.some((view) => canAccessView(role, view));
}

function initialsFor(username) {
    return username
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("") || "HH";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
