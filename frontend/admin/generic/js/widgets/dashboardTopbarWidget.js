export function renderDashboardTopbarWidget(containerId, title = "", permissions = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
  <div class="topbar dashboard-topbar">
    <div class="topbar-title-group">
      <div>
        <h1 class="page-title" data-page-heading tabindex="-1">${escapeHtml(title)}</h1>
      </div>
    </div>
    <div class="topbar-right">
      ${permissions.canManageOperationalData ? `<button class="btn dashboard-action-btn" type="button" data-dashboard-action="checkin">
        <i class="ti ti-login" aria-hidden="true"></i> Adicionar check-in
      </button>
      <button class="btn dashboard-action-btn" type="button" data-dashboard-action="checkout">
        <i class="ti ti-logout" aria-hidden="true"></i> Adicionar check-out
      </button>
      <button class="btn dashboard-action-btn" type="button" data-dashboard-action="guest">
        <i class="ti ti-user-plus" aria-hidden="true"></i> Novo hospede
      </button>
      <button class="btn btn-primary" type="button" data-dashboard-action="reservation">
        <i class="ti ti-calendar-plus" aria-hidden="true"></i> Nova reserva
      </button>` : ""}
    </div>
  </div>
    `;

    container.querySelectorAll("[data-dashboard-action]").forEach((button) => {
        button.addEventListener("click", () => {
            container.dispatchEvent(new CustomEvent("dashboard-topbar-action", {
                bubbles: true,
                detail: { action: button.dataset.dashboardAction },
            }));
        });
    });
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
