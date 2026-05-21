export function renderDashboardTopbarWidget(containerId, title = "") {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
  <div class="topbar dashboard-topbar">
    <div class="topbar-title-group">
      <div>
        <div class="page-title">${title}</div>
      </div>
    </div>
    <div class="topbar-right">
      <button class="btn dashboard-action-btn" type="button" data-dashboard-action="checkin">
        <i class="ti ti-login"></i> Adicionar check-in
      </button>
      <button class="btn dashboard-action-btn" type="button" data-dashboard-action="checkout">
        <i class="ti ti-logout"></i> Adicionar check-out
      </button>
      <button class="btn dashboard-action-btn" type="button" data-dashboard-action="guest">
        <i class="ti ti-user-plus"></i> Novo hospede
      </button>
      <button class="btn btn-primary" type="button" data-dashboard-action="reservation">
        <i class="ti ti-calendar-plus"></i> Nova reserva
      </button>
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
