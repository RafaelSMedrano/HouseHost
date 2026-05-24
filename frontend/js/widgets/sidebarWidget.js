import { renderBrandLogoMarkup } from "./brandLogoWidget.js?v=2026-05-24-generic-logo";

export function renderSidebarWidget(containerId, user = {}) {
    const container = document.getElementById(containerId);
    const username = user.name || "Usuário";
    const role = user.role || "Recepcionista";
    const photoUrl = user.photoUrl || "";
    const onNavigate = typeof user.onNavigate === "function" ? user.onNavigate : null;
    const onUserProfile = typeof user.onUserProfile === "function" ? user.onUserProfile : null;
    const safeUsername = escapeHtml(username);
    const safeRole = escapeHtml(role);
    const safePhotoUrl = escapeHtml(photoUrl);

    container.innerHTML = `
<div class="sidebar">
  <div class="brand">
    ${renderBrandLogoMarkup("sidebar")}
  </div>

  <nav>
    <div class="nav-group">Geral</div>
    <div class="nav-item active" data-view="dashboard"><i class="ti ti-layout-dashboard"></i> Dashboard</div>
    <div class="nav-item" data-view="rooms"><i class="ti ti-door"></i> Quartos <span class="nav-badge nb-rose">2</span></div>

    <div class="nav-group">Hospedagem</div>
    <div class="nav-item" data-view="checkin"><i class="ti ti-login"></i> Check-in</div>
    <div class="nav-item" data-view="checkout"><i class="ti ti-logout"></i> Check-out</div>
    <div class="nav-item" data-view="reservations"><i class="ti ti-calendar-event"></i> Reservas</div>
    <div class="nav-item" data-view="guests"><i class="ti ti-users"></i> Hóspedes</div>
    <!-- Item temporariamente oculto:
    <div class="nav-item" data-view="timeline"><i class="ti ti-calendar-time"></i> Calendário</div>
    -->

    <div class="nav-group">Operação</div>
    <div class="nav-item" data-view="finance"><i class="ti ti-cash-banknote"></i> Caixa</div>
    <!-- Itens temporariamente ocultos:
    <div class="nav-item"><i class="ti ti-package"></i> Consumíveis <span class="nav-badge nb-amber">3</span></div>
    <div class="nav-item"><i class="ti ti-tool"></i> Manutenção</div>
    <div class="nav-item"><i class="ti ti-chart-bar"></i> Relatórios</div>
    -->

    <div class="nav-group">Sistema</div>
    <div class="nav-item"><i class="ti ti-settings"></i> Configurações</div>
  </nav>

  <button class="sidebar-user" type="button" data-user-profile>
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
            container.querySelectorAll(".nav-item").forEach((navItem) => navItem.classList.remove("active"));
            item.classList.add("active");

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
