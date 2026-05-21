import { findAllBookings, findAllRooms, findAllStays } from "../api.js?v=2026-05-18-metrics";

const SCALES = {
    week: { label: "Detalhado", days: 30, pastDays: 10, cellWidth: 112 },
    month: { label: "30 dias", days: 90, pastDays: 30, cellWidth: 44 },
    quarter: { label: "90 dias", days: 180, pastDays: 60, cellWidth: 22 },
};

const STATUS_LABELS = {
    CONFIRMED: "Confirmada",
    PENDING: "Pendente",
    GOT_CHECKIN: "Check-in",
    CANCELLED: "Cancelada",
};

export function renderRoomTimelineWidget(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    const initialScale = SCALES[options.initialScale] ? options.initialScale : "month";
    const title = options.title || "Calendário de ocupação";
    const compactClass = options.compact ? " compact" : "";

    container.innerHTML = `
      <div class="room-timeline-widget${compactClass}">
        <div class="room-timeline-toolbar">
          <div>
            <div class="section-title">${escapeHtml(title)}</div>
            <div class="room-timeline-subtitle rooms-subtitle">Carregando quartos e reservas.</div>
          </div>
          <div class="room-timeline-actions">
            <button class="reservations-icon-btn" type="button" data-timeline-prev title="Período anterior"><i class="ti ti-chevron-left"></i></button>
            <button class="btn dashboard-action-btn" type="button" data-timeline-today><i class="ti ti-calendar-dot"></i> Hoje</button>
            <button class="reservations-icon-btn" type="button" data-timeline-next title="Próximo período"><i class="ti ti-chevron-right"></i></button>
            <div class="timeline-scale-control">
              ${Object.entries(SCALES).map(([key, scale]) => `<button class="${key === initialScale ? "active" : ""}" type="button" data-scale="${key}">${scale.label}</button>`).join("")}
            </div>
          </div>
        </div>

        <div class="timeline-legend">
          <div class="leg"><span class="timeline-status-dot dot-lav"></span>Disponível</div>
          <div class="leg"><span class="timeline-status-dot dot-sage"></span>Ocupado</div>
          <div class="leg"><span class="timeline-status-dot dot-amber"></span>Limpeza</div>
          <div class="leg"><span class="timeline-status-dot dot-rose"></span>Manutenção</div>
          <div class="leg"><span class="timeline-status-dot dot-muted"></span>Inativo</div>
        </div>

        <div class="room-timeline-shell">
          <div class="room-timeline-loading">Carregando calendário...</div>
        </div>
      </div>
    `;

    bindTimeline(container, initialScale, options.onError);
}

function bindTimeline(container, initialScale, onError) {
    const state = {
        rooms: [],
        bookings: [],
        stays: [],
        scale: initialScale,
        startDate: startDateForScale(initialScale),
        shouldFocusToday: true,
    };

    container.querySelector("[data-timeline-prev]").addEventListener("click", () => {
        state.startDate = addDays(state.startDate, -SCALES[state.scale].days);
        state.shouldFocusToday = false;
        renderTimeline(container, state);
    });

    container.querySelector("[data-timeline-next]").addEventListener("click", () => {
        state.startDate = addDays(state.startDate, SCALES[state.scale].days);
        state.shouldFocusToday = false;
        renderTimeline(container, state);
    });

    container.querySelector("[data-timeline-today]").addEventListener("click", () => {
        state.startDate = startDateForScale(state.scale);
        state.shouldFocusToday = true;
        renderTimeline(container, state);
    });

    container.querySelectorAll("[data-scale]").forEach((button) => {
        button.addEventListener("click", () => {
            state.scale = button.dataset.scale;
            state.startDate = startDateForScale(state.scale);
            state.shouldFocusToday = true;
            container.querySelectorAll("[data-scale]").forEach((item) => item.classList.remove("active"));
            button.classList.add("active");
            renderTimeline(container, state);
        });
    });

    bindHorizontalTimelineScroll(container);
    loadTimelineData(container, state, onError);
}

function bindHorizontalTimelineScroll(container) {
    const shell = container.querySelector(".room-timeline-shell");
    let isDragging = false;
    let dragStartX = 0;
    let dragStartScrollLeft = 0;

    shell.addEventListener("wheel", (event) => {
        if (Math.abs(event.deltaY) <= Math.abs(event.deltaX)) {
            return;
        }

        event.preventDefault();
        shell.scrollLeft += event.deltaY;
    }, { passive: false });

    shell.addEventListener("pointerdown", (event) => {
        if (event.button !== 0) {
            return;
        }

        isDragging = true;
        dragStartX = event.clientX;
        dragStartScrollLeft = shell.scrollLeft;
        shell.classList.add("is-dragging");
        shell.setPointerCapture(event.pointerId);
    });

    shell.addEventListener("pointermove", (event) => {
        if (!isDragging) {
            return;
        }

        shell.scrollLeft = dragStartScrollLeft - (event.clientX - dragStartX);
    });

    shell.addEventListener("pointerup", (event) => {
        isDragging = false;
        shell.classList.remove("is-dragging");
        shell.releasePointerCapture(event.pointerId);
    });

    shell.addEventListener("pointercancel", () => {
        isDragging = false;
        shell.classList.remove("is-dragging");
    });
}

async function loadTimelineData(container, state, onError) {
    try {
        const [roomsResponse, bookingsResponse, staysResponse] = await Promise.all([
            findAllRooms(),
            findAllBookings(),
            findAllStays(),
        ]);

        state.rooms = (roomsResponse.data || []).sort((a, b) => String(a.roomNumber || "").localeCompare(String(b.roomNumber || ""), "pt-BR", { numeric: true }));
        state.bookings = (bookingsResponse.data || []).filter((booking) => booking.roomId && booking.checkInDate && booking.checkOutDate);
        state.stays = (staysResponse.data || []).filter((stay) => stay.roomId && stay.checkInDate && stay.expectedCheckOutDate);
        renderTimeline(container, state);
    } catch (error) {
        container.querySelector(".room-timeline-shell").innerHTML = `<div class="rooms-empty rooms-error">${escapeHtml(error.message || "Não foi possível carregar o calendário.")}</div>`;
        if (typeof onError === "function") {
            onError(error);
        }
    }
}

function renderTimeline(container, state) {
    const shell = container.querySelector(".room-timeline-shell");
    const scale = SCALES[state.scale];
    const dates = Array.from({ length: scale.days }, (_, index) => addDays(state.startDate, index));
    const visibleEnd = addDays(state.startDate, scale.days);
    const width = scale.days * scale.cellWidth;
    const subtitle = `${formatFullDate(state.startDate)} até ${formatFullDate(addDays(visibleEnd, -1))} · ${state.rooms.length} quarto${state.rooms.length === 1 ? "" : "s"}`;

    container.querySelector(".room-timeline-subtitle").textContent = subtitle;

    if (!state.rooms.length) {
        shell.innerHTML = `<div class="rooms-empty">Nenhum quarto cadastrado ainda.</div>`;
        return;
    }

    shell.innerHTML = `
      <div class="timeline-grid" style="--timeline-width:${width}px; --timeline-cell:${scale.cellWidth}px; --timeline-days:${scale.days};">
        <div class="timeline-corner">Quarto</div>
        <div class="timeline-header">
          ${dates.map(renderDateHeader).join("")}
        </div>
        <div class="timeline-body">
          ${state.rooms.map((room) => renderRoomRow(room, state.bookings, state.stays, state.startDate, visibleEnd, scale)).join("")}
        </div>
      </div>
    `;

    if (state.shouldFocusToday) {
        focusToday(shell, state.startDate, scale.cellWidth);
        state.shouldFocusToday = false;
    }
}

function renderDateHeader(date) {
    const isToday = toDateKey(date) === toDateKey(startOfToday());
    return `
      <div class="timeline-date-cell ${isToday ? "today" : ""}">
        <span>${weekdayLabel(date)}</span>
        <strong>${String(date.getDate()).padStart(2, "0")}</strong>
        <small>${monthLabel(date)}</small>
      </div>
    `;
}

function renderRoomRow(room, bookings, stays, startDate, visibleEnd, scale) {
    const roomBookings = bookings
        .filter((booking) => Number(booking.roomId) === Number(room.id))
        .filter((booking) => bookingIntersectsPeriod(booking, startDate, visibleEnd))
        .filter((booking) => normalizeStatus(booking.status) !== "CANCELLED");
    const status = currentRoomStatus(room, bookings, stays);

    return `
      <div class="timeline-room-row">
        <div class="timeline-room-cell">
          <strong>${escapeHtml(room.roomNumber || "-")}</strong>
          <span>${escapeHtml(roomTypeLabel(room.type))}</span>
          <small class="timeline-room-status"><span class="timeline-status-dot ${status.dotClass}"></span>${escapeHtml(status.label)}</small>
        </div>
        <div class="timeline-track">
          ${renderDayGuides(scale.days)}
          ${roomBookings.map((booking) => renderBookingBlock(booking, startDate, visibleEnd, scale.cellWidth)).join("")}
        </div>
      </div>
    `;
}

function renderBookingBlock(booking, startDate, visibleEnd, cellWidth) {
    const checkIn = parseDate(booking.checkInDate);
    const checkOut = parseDate(booking.checkOutDate);
    const clippedStart = checkIn < startDate ? startDate : checkIn;
    const clippedEnd = checkOut > visibleEnd ? visibleEnd : checkOut;
    const left = diffDays(startDate, clippedStart) * cellWidth;
    const width = Math.max(cellWidth * 0.65, diffDays(clippedStart, clippedEnd) * cellWidth);
    const status = normalizeStatus(booking.status);

    return `
      <div class="timeline-booking status-${status.toLowerCase()}" style="left:${left}px;width:${width}px;" title="${escapeHtml(booking.guestName || "Reserva")} · ${formatShortDate(booking.checkInDate)} a ${formatShortDate(booking.checkOutDate)}">
        <strong>${escapeHtml(booking.guestName || "Reserva")}</strong>
        <span>${escapeHtml(formatShortDate(booking.checkInDate))} - ${escapeHtml(formatShortDate(booking.checkOutDate))} · ${escapeHtml(STATUS_LABELS[status] || status)}</span>
      </div>
    `;
}

function renderDayGuides(days) {
    return Array.from({ length: days }, () => `<span class="timeline-guide"></span>`).join("");
}

function bookingIntersectsPeriod(booking, startDate, endDate) {
    const checkIn = parseDate(booking.checkInDate);
    const checkOut = parseDate(booking.checkOutDate);
    return checkIn < endDate && checkOut > startDate;
}

function normalizeStatus(status) {
    return String(status || "PENDING").toUpperCase();
}

function parseDate(value) {
    const [year, month, day] = String(value).split("-").map(Number);
    return new Date(year, month - 1, day);
}

function startOfToday() {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

function startDateForScale(scaleKey) {
    const scale = SCALES[scaleKey];
    return addDays(startOfToday(), -scale.pastDays);
}

function focusToday(shell, startDate, cellWidth) {
    const daysUntilToday = diffDays(startDate, startOfToday());
    const roomColumnWidth = 170;
    const targetLeft = Math.max(0, roomColumnWidth + (daysUntilToday * cellWidth) - (shell.clientWidth * 0.35));
    shell.scrollLeft = targetLeft;
}

function addDays(date, amount) {
    const next = new Date(date);
    next.setDate(next.getDate() + amount);
    return next;
}

function diffDays(first, second) {
    return Math.round((second - first) / 86400000);
}

function toDateKey(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

function weekdayLabel(date) {
    return ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"][date.getDay()];
}

function monthLabel(date) {
    return ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"][date.getMonth()];
}

function formatFullDate(date) {
    return `${String(date.getDate()).padStart(2, "0")}/${String(date.getMonth() + 1).padStart(2, "0")}/${date.getFullYear()}`;
}

function formatShortDate(value) {
    if (!value) {
        return "-";
    }

    const [year, month, day] = String(value).split("-");
    return year && month && day ? `${day}/${month}` : value;
}

function roomTypeLabel(type) {
    const labels = {
        SINGLE: "Individual",
        DOUBLE: "Duplo",
        SUITE: "Suíte",
        FAMILY: "Família",
        STANDARD: "Standard",
    };
    return labels[String(type || "").toUpperCase()] || type || "Quarto";
}

function currentRoomStatus(room, bookings, stays) {
    const today = startOfToday();
    const hasActiveStay = stays.some((stay) => Number(stay.roomId) === Number(room.id)
        && normalizeStatus(stay.status) === "ACTIVE"
        && parseDate(stay.checkInDate) <= today
        && parseDate(stay.expectedCheckOutDate) > today);
    const hasActiveBooking = bookings.some((booking) => Number(booking.roomId) === Number(room.id)
        && isBlockingBooking(booking)
        && parseDate(booking.checkInDate) <= today
        && parseDate(booking.checkOutDate) > today);

    if (hasActiveStay || hasActiveBooking) {
        return { label: "Ocupado", dotClass: "dot-sage" };
    }

    return roomStatus(room.status);
}

function isBlockingBooking(booking) {
    return ["PENDING", "CONFIRMED", "GOT_CHECKIN"].includes(normalizeStatus(booking.status));
}

function roomStatus(status) {
    const normalized = String(status || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toUpperCase();

    if (normalized === "OCCUPIED" || normalized === "OCUPADO") {
        return { label: "Ocupado", dotClass: "dot-sage" };
    }
    if (normalized === "CLEANING" || normalized === "LIMPEZA") {
        return { label: "Limpeza", dotClass: "dot-amber" };
    }
    if (normalized === "MAINTENANCE" || normalized === "MANUTENCAO") {
        return { label: "Manutenção", dotClass: "dot-rose" };
    }
    if (normalized === "INACTIVE" || normalized === "INATIVO") {
        return { label: "Inativo", dotClass: "dot-muted" };
    }
    return { label: "Disponível", dotClass: "dot-lav" };
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
