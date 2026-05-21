import { findAllRooms, findAllStays, findMetricsSummary } from "../api.js?v=2026-05-20-rooms-active-stays";

const ROOM_STATUS = {
    AVAILABLE: {
        label: "Disponível",
        cardClass: "",
        dotClass: "dot-free",
        textClass: "st-free",
    },
    DISPONIVEL: {
        label: "Disponível",
        cardClass: "",
        dotClass: "dot-free",
        textClass: "st-free",
    },
    OCCUPIED: {
        label: "Ocupado",
        cardClass: "occ",
        dotClass: "dot-occ",
        textClass: "st-occ",
    },
    OCUPADO: {
        label: "Ocupado",
        cardClass: "occ",
        dotClass: "dot-occ",
        textClass: "st-occ",
    },
    CLEANING: {
        label: "Limpeza",
        cardClass: "clean",
        dotClass: "dot-clean",
        textClass: "st-clean",
    },
    LIMPEZA: {
        label: "Limpeza",
        cardClass: "clean",
        dotClass: "dot-clean",
        textClass: "st-clean",
    },
    MAINTENANCE: {
        label: "Manutenção",
        cardClass: "maint",
        dotClass: "dot-maint",
        textClass: "st-maint",
    },
    INACTIVE: {
        label: "Inativo",
        cardClass: "maint",
        dotClass: "dot-maint",
        textClass: "st-maint",
    },
    MANUTENCAO: {
        label: "Manutenção",
        cardClass: "maint",
        dotClass: "dot-maint",
        textClass: "st-maint",
    },
};

const ROOM_TYPE_LABELS = {
    SINGLE: "Individual",
    DOUBLE: "Duplo",
    SUITE: "Suíte",
    FAMILY: "Família",
    STANDARD: "Standard",
};

export function renderRoomsView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main rooms-main">
  <div class="content rooms-page">
    <div class="rooms-kpis">
      ${renderKpi("Total", "...", "dot-dark", "Quartos cadastrados")}
      ${renderKpi("Disponíveis", "...", "dot-lav", "Prontos para reserva")}
      ${renderKpi("Ocupados", "...", "dot-sage", "Com hospedagem ativa")}
      ${renderKpi("Limpeza", "...", "dot-amber", "Aguardando liberação")}
      ${renderKpi("Bloqueados", "...", "dot-rose", "Manutenção ou inativos")}
    </div>

    <div>
      <div class="section-head rooms-section-head">
        <div>
          <div class="section-title">Lista de quartos</div>
          <div class="rooms-subtitle">Carregando quartos cadastrados no sistema.</div>
        </div>
        <div class="rooms-header-actions">
          <div class="legend">
            <div class="leg"><span class="dot dot-occ"></span>Ocupado</div>
            <div class="leg"><span class="dot dot-free"></span>Disponível</div>
            <div class="leg"><span class="dot dot-clean"></span>Limpeza</div>
            <div class="leg"><span class="dot dot-maint"></span>Manutenção</div>
          </div>
          <button id="rooms-new" class="btn btn-primary" type="button"><i class="ti ti-door-enter"></i> Novo quarto</button>
        </div>
      </div>
      <div class="rooms rooms-list">
        ${renderLoadingCards()}
      </div>
    </div>
  </div>
</div>
    `;

    container.querySelector("#rooms-new").addEventListener("click", () => {
        if (typeof options.onNewRoom === "function") {
            options.onNewRoom();
        }
    });

    loadRooms(container, options);
}

async function loadRooms(container, options) {
    try {
        const [roomsResponse, staysResponse, metricsResponse] = await Promise.all([
            findAllRooms(),
            findAllStays(),
            findMetricsSummary(),
        ]);
        const rooms = roomsResponse.data || [];
        const stays = staysResponse.data || [];
        const normalizedRooms = Array.isArray(rooms)
            ? rooms.map((room) => normalizeRoom(room, stays))
            : [];

        renderLoadedState(container, normalizedRooms, options, metricsResponse.data || {});
    } catch (error) {
        renderErrorState(container, error);
    }
}

function renderLoadedState(container, rooms, options, metrics) {
    const kpisContainer = container.querySelector(".rooms-kpis");
    const roomsContainer = container.querySelector(".rooms-list");
    const subtitle = container.querySelector(".rooms-subtitle");

    kpisContainer.innerHTML = `
      ${renderKpi("Total", metrics.totalRooms || 0, "dot-dark", "Quartos cadastrados")}
      ${renderKpi("Disponíveis", metrics.availableRooms || 0, "dot-lav", "Prontos para reserva")}
      ${renderKpi("Ocupados", metrics.occupiedRooms || 0, "dot-sage", "Com hospedagem ativa")}
      ${renderKpi("Limpeza", metrics.cleaningRooms || 0, "dot-amber", "Aguardando liberação")}
      ${renderKpi("Bloqueados", metrics.blockedRooms || 0, "dot-rose", "Manutenção ou inativos")}
    `;

    subtitle.textContent = rooms.length
        ? `${rooms.length} quarto${rooms.length === 1 ? "" : "s"} encontrado${rooms.length === 1 ? "" : "s"}.`
        : "Nenhum quarto cadastrado ainda.";

    roomsContainer.innerHTML = rooms.length
        ? rooms.map(renderRoomCard).join("")
        : `<div class="rooms-empty">Nenhum quarto foi encontrado.</div>`;

    roomsContainer.querySelectorAll("[data-edit-room]").forEach((button) => {
        button.addEventListener("click", () => {
            if (typeof options.onEditRoom === "function") {
                options.onEditRoom(Number(button.dataset.editRoom));
            }
        });
    });
}

function renderErrorState(container, error) {
    const roomsContainer = container.querySelector(".rooms-list");
    const subtitle = container.querySelector(".rooms-subtitle");

    subtitle.textContent = "Não foi possível carregar os quartos.";
    roomsContainer.innerHTML = `
      <div class="rooms-empty rooms-error">
        ${escapeHtml(error.message || "Erro ao buscar quartos.")}
      </div>
    `;
}

function normalizeRoom(room, stays) {
    const activeStay = findActiveStayForRoom(room, stays);
    const statusKey = activeStay ? "OCCUPIED" : normalizeKey(room.status);
    const status = ROOM_STATUS[statusKey] || {
        label: room.status || "Sem status",
        cardClass: "",
        dotClass: "dot-free",
        textClass: "st-free",
    };

    return {
        id: room.id,
        roomNumber: room.roomNumber || room.number || "-",
        type: formatRoomType(room.type),
        capacity: Number(room.capacity || 0),
        dailyRate: Number(room.dailyRate || 0),
        statusKey,
        status,
        activeStay,
    };
}

function renderRoomCard(room) {
    return `
      <div class="room ${room.status.cardClass}" data-room-id="${escapeHtml(room.id || "")}">
        <div class="room-num">${escapeHtml(room.roomNumber)}</div>
        <div class="room-type">${escapeHtml(room.type)}</div>
        <div class="room-st">
          <span class="dot ${room.status.dotClass}"></span>
          <span class="${room.status.textClass}">${escapeHtml(room.status.label)}</span>
        </div>
        <div class="room-meta">
          <span><i class="ti ${room.activeStay ? "ti-user" : "ti-users"}"></i>${escapeHtml(room.activeStay?.guestName || `${room.capacity || "-"} pessoa${room.capacity === 1 ? "" : "s"}`)}</span>
        </div>
        <div class="room-rate-row">
          <div class="room-rate">${escapeHtml(room.activeStay ? `Saída: ${formatDate(room.activeStay.expectedCheckOutDate)}` : `${formatCurrency(room.dailyRate)} / diária`)}</div>
          <button class="room-edit-button" type="button" data-edit-room="${escapeHtml(room.id || "")}" title="Editar quarto"><i class="ti ti-pencil"></i></button>
        </div>
      </div>
    `;
}

function findActiveStayForRoom(room, stays) {
    const today = currentDate();

    return stays.find((stay) => {
        const stayRoomId = Number(stay.roomId);
        const roomId = Number(room.id);
        const status = normalizeKey(stay.status);
        return stayRoomId === roomId
            && status === "ACTIVE"
            && stay.checkInDate <= today
            && stay.expectedCheckOutDate > today;
    });
}

function currentDate() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function formatDate(date) {
    if (!date) {
        return "-";
    }

    const [year, month, day] = String(date).split("-");
    return year && month && day ? `${day}/${month}` : date;
}

function renderKpi(label, value, dotClass, sub) {
    return `
      <div class="rooms-kpi">
        <span class="kpi-dot ${dotClass}"></span>
        <div class="kpi-label">${escapeHtml(label)}</div>
        <div class="kpi-val">${escapeHtml(value)}</div>
        <div class="kpi-sub">${escapeHtml(sub)}</div>
      </div>
    `;
}

function renderLoadingCards() {
    return Array.from({ length: 6 }).map((_, index) => `
      <div class="room rooms-loading-card">
        <div class="room-num">${String(index + 1).padStart(2, "0")}</div>
        <div class="room-type">Carregando...</div>
        <div class="room-st"><span class="dot dot-free"></span><span class="st-free">Aguarde</span></div>
      </div>
    `).join("");
}

function formatRoomType(type) {
    if (!type) {
        return "Tipo não informado";
    }

    const key = normalizeKey(type);

    return ROOM_TYPE_LABELS[key] || String(type)
        .toLowerCase()
        .replaceAll("_", " ")
        .replace(/(^|\s)\S/g, (letter) => letter.toUpperCase());
}

function formatCurrency(value) {
    return new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL",
    }).format(value);
}

function normalizeKey(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toUpperCase();
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
