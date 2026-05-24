import { findMetricsSummary } from "../api.js?v=2026-05-20-login-real-data";
import { renderBrandLogoMarkup } from "./brandLogoWidget.js?v=2026-05-24-generic-logo";

export function renderMetricsResumeWidget(containerId) {
    const container = document.getElementById(containerId);

    container.innerHTML = `
        <div class="metrics-resume-widget">
            <div class="deco-circle deco-c1"></div>
            <div class="deco-circle deco-c2"></div>
            <div class="deco-circle deco-c3"></div>

            <svg class="flowers" width="180" height="220" viewBox="0 0 180 220" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M90 220 Q88 180 86 140 Q84 100 88 60" stroke="white" stroke-width="1.5" stroke-linecap="round" fill="none"/>
                <path d="M90 220 Q95 185 100 150 Q105 120 98 85" stroke="white" stroke-width="1.5" stroke-linecap="round" fill="none"/>
                <path d="M90 220 Q80 195 70 165 Q62 138 68 105" stroke="white" stroke-width="1.5" stroke-linecap="round" fill="none"/>
                <ellipse cx="68" cy="100" rx="4" ry="8" fill="white" transform="rotate(-15 68 100)"/>
                <ellipse cx="62" cy="108" rx="4" ry="8" fill="white" transform="rotate(-10 62 108)"/>
                <ellipse cx="74" cy="92" rx="3.5" ry="7" fill="white" transform="rotate(-20 74 92)"/>
                <ellipse cx="88" cy="55" rx="4" ry="9" fill="white" transform="rotate(5 88 55)"/>
                <ellipse cx="82" cy="63" rx="4" ry="8" fill="white" transform="rotate(0 82 63)"/>
                <ellipse cx="94" cy="47" rx="3.5" ry="8" fill="white" transform="rotate(10 94 47)"/>
                <ellipse cx="98" cy="80" rx="4" ry="8" fill="white" transform="rotate(15 98 80)"/>
                <ellipse cx="104" cy="90" rx="4" ry="8" fill="white" transform="rotate(18 104 90)"/>
                <ellipse cx="92" cy="72" rx="3.5" ry="7" fill="white" transform="rotate(10 92 72)"/>
                <path d="M80 140 Q68 130 65 118" stroke="white" stroke-width="1.5" fill="none" stroke-linecap="round"/>
                <path d="M95 155 Q108 148 112 135" stroke="white" stroke-width="1.5" fill="none" stroke-linecap="round"/>
                <path d="M85 175 Q74 168 72 158" stroke="white" stroke-width="1.2" fill="none" stroke-linecap="round"/>
            </svg>

            <div class="left-brand">
                ${renderBrandLogoMarkup("login")}
            </div>

            <div class="left-center">
                <div class="illus-tag">
                    <i class="ti ti-lock-open"></i>
                    Sistema de Gestão
                </div>
                <h2 class="left-headline">Bem-vindo<br>de <em>volta.</em></h2>
                <p class="left-desc">
                    Gerencie reservas, quartos e hóspedes em um só lugar. Tudo que você precisa, ao alcance de um clique.
                </p>

                <div class="left-stats">
                    <div class="stat">
                        <div class="stat-val" id="login-occupied-rooms">-</div>
                        <div class="stat-label">Ocupados</div>
                    </div>
                    <div class="stat">
                        <div class="stat-val" id="login-available-rooms">-</div>
                        <div class="stat-label">Livres</div>
                    </div>
                    <div class="stat">
                        <div class="stat-val" id="login-checkins-today">-</div>
                        <div class="stat-label">Check-ins hoje</div>
                    </div>
                </div>
            </div>

            <div class="left-footer">
                © 2025 HouseHost · Sistema de gestão para hospedagens
            </div>
        </div>
    `;

    loadLoginMetrics(container);
}

async function loadLoginMetrics(container) {
    try {
        const response = await findMetricsSummary();
        const metrics = response.data || {};

        setMetricText(container, "login-occupied-rooms", metrics.occupiedRooms);
        setMetricText(container, "login-available-rooms", metrics.availableRooms);
        setMetricText(container, "login-checkins-today", metrics.dashboardDoneCheckInsToday ?? metrics.checkInsToday);
    } catch (error) {
        console.error("Erro ao carregar metricas do login.", error);
        setMetricText(container, "login-occupied-rooms", 0);
        setMetricText(container, "login-available-rooms", 0);
        setMetricText(container, "login-checkins-today", 0);
    }
}

function setMetricText(container, id, value) {
    const element = container.querySelector(`#${id}`);
    if (!element) {
        return;
    }
    element.textContent = Number.isFinite(Number(value)) ? Number(value).toLocaleString("pt-BR") : "0";
}
