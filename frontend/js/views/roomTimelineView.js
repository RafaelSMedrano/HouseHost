import { renderRoomTimelineWidget } from "../widgets/roomTimelineWidget.js?v=2026-05-18-timeline-widget";

export function renderRoomTimelineView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main rooms-main">
  <div class="content room-timeline-page">
    <div id="room-timeline-widget"></div>
  </div>
  <div id="room-timeline-toast" class="booking-toast"><i class="ti ti-alert-circle"></i><span></span></div>
</div>`;

    renderRoomTimelineWidget("room-timeline-widget", {
        initialScale: "month",
        onError: (error) => showToast(container, error.message || "Não foi possível carregar o calendário."),
    });
}

function showToast(container, message) {
    const toast = container.querySelector("#room-timeline-toast");
    toast.querySelector("span").textContent = message;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2600);
}
