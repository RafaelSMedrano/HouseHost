import { findAllGuests, findAllRooms, findBookingById, updateBooking } from "../api.js?v=2026-08-11-api-log-transport";

const origins = [
    { value: "DIRETO_TELEFONE", label: "Direto / Telefone" },
    { value: "WHATSAPP", label: "WhatsApp" },
    { value: "INSTAGRAM", label: "Instagram" },
    { value: "BOOKING", label: "Booking" },
    { value: "AIRBNB", label: "Airbnb" },
    { value: "INDICACAO", label: "Indicacao" },
];

const statuses = [
    { value: "CONFIRMED", label: "Confirmada" },
    { value: "UNCONFIRMED", label: "Não confirmada" },
    { value: "IN_STAY", label: "Em estadia" },
    { value: "FINISHED", label: "Finalizada" },
    { value: "CANCELED", label: "Cancelada" },
];

const paymentMethods = [
    { value: "PIX", label: "Pix" },
    { value: "CREDIT_CARD", label: "Cartao de credito" },
    { value: "DEBIT_CARD", label: "Cartao de debito" },
    { value: "CASH", label: "Dinheiro" },
    { value: "BANK_TRANSFER", label: "Transferencia bancaria" },
    { value: "BOOKING", label: "Booking" },
    { value: "AIRBNB", label: "Airbnb" },
];

export function renderEditReservationView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = `
<div class="main guest-form-main room-form-main">
  <div class="guest-form-content room-form-content">
    <div class="room-form-preview">
      <div id="reservation-edit-preview-id" class="room-form-number">--</div>
      <div class="room-form-preview-info">
        <div id="reservation-edit-preview-title" class="room-form-title empty">Editar reserva</div>
        <div id="reservation-edit-preview-sub" class="room-form-sub">Carregando parametros da reserva</div>
        <div id="reservation-edit-preview-badge" class="room-form-badge available">-</div>
      </div>
    </div>

    <form id="reservation-edit-form" class="guest-form room-form">
      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-lav"><i class="ti ti-calendar-edit"></i></span><div><strong>Reserva</strong><small>Hospede, quarto, periodo e status</small></div></div>
        <div class="guest-fields">
          ${selectField("guestId", "Hospede")}
          ${selectField("roomId", "Quarto")}
          ${field("checkInDate", "Check-in", "date", "", "ti-calendar-event", true)}
          ${field("checkOutDate", "Check-out", "date", "", "ti-calendar-event", true)}
          ${selectField("origin", "Origem", origins)}
          ${selectField("status", "Status", statuses)}
        </div>
      </section>

      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-sage"><i class="ti ti-users"></i></span><div><strong>Ocupacao e valores</strong><small>Pessoas, diaria e total da hospedagem</small></div></div>
        <div class="guest-fields">
          ${field("adults", "Adultos", "number", "2", "ti-users")}
          ${field("children", "Criancas", "number", "0", "ti-mood-kid")}
          ${field("pets", "Pets", "number", "0", "ti-paw")}
          ${field("dailyRate", "Diaria (R$)", "number", "0,00", "ti-currency-real")}
          ${field("discount", "Desconto (R$)", "number", "0,00", "ti-discount-2")}
          ${field("totalAmount", "Total (R$)", "number", "0,00", "ti-receipt")}
        </div>
      </section>

      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-amber"><i class="ti ti-notes"></i></span><div><strong>Observacoes</strong><small>Pedidos especiais e anotacoes internas</small></div></div>
        <div class="guest-fields">
          ${textareaField("specialRequests", "Pedidos especiais")}
          ${textareaField("internalNotes", "Anotacoes internas")}
        </div>
      </section>

      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-rose"><i class="ti ti-credit-card"></i></span><div><strong>Pagamento</strong><small>Parametros registrados na reserva</small></div></div>
        <div class="guest-fields">
          ${selectField("paymentMethod", "Forma de pagamento", paymentMethods)}
          ${field("installments", "Parcelas", "text", "avista", "ti-list-numbers")}
          ${field("paidAmount", "Valor pago (R$)", "number", "0,00", "ti-cash")}
          ${field("paymentDate", "Data de pagamento", "date", "", "ti-calendar-dollar")}
        </div>
      </section>
    </form>
  </div>

  <div class="guest-form-footer">
    <div><span>Modo edicao</span><strong id="reservation-edit-footer-status">Carregando</strong></div>
    <div class="footer-actions">
      <button id="reservation-edit-cancel" class="btn-cancel" type="button"><i class="ti ti-x"></i> Cancelar</button>
      <button id="reservation-edit-save" class="btn-save" type="submit" form="reservation-edit-form"><i class="ti ti-check"></i> Salvar reserva</button>
    </div>
  </div>

  <div id="reservation-edit-toast" class="booking-toast"><i class="ti ti-check"></i><span></span></div>
</div>`;

    bindEditReservation(container, options);
}

function bindEditReservation(container, options) {
    const state = { bookingId: options.bookingId, booking: null, rooms: [], guests: [] };
    const form = container.querySelector("#reservation-edit-form");

    container.querySelector("#reservation-edit-cancel").addEventListener("click", () => options.onCancel?.());
    form.addEventListener("input", () => updatePreview(container));
    form.addEventListener("change", () => updatePreview(container));
    form.addEventListener("submit", (event) => handleSubmit(event, container, options, state));

    loadReservationData(container, state);
}

async function loadReservationData(container, state) {
    try {
        const [bookingResponse, guestsResponse, roomsResponse] = await Promise.all([
            findBookingById(state.bookingId),
            findAllGuests(),
            findAllRooms(),
        ]);

        state.booking = bookingResponse.data;
        state.guests = guestsResponse.data || [];
        state.rooms = roomsResponse.data || [];

        fillSelect(container, "guestId", state.guests.map((guest) => ({ value: guest.id, label: guest.fullName || `Hospede #${guest.id}` })));
        fillSelect(container, "roomId", state.rooms.map((room) => ({ value: room.id, label: `${room.roomNumber} · ${room.type || "Quarto"}` })));
        fillForm(container, state.booking);
        container.querySelector("#reservation-edit-footer-status").textContent = `Reserva #${state.booking.id}`;
        updatePreview(container);
    } catch (error) {
        showToast(container, error.message || "Nao foi possivel carregar a reserva.", "ti-alert-circle");
    }
}

async function handleSubmit(event, container, options, state) {
    event.preventDefault();

    const payload = collectPayload(container);
    if (!payload.guestId || !payload.roomId || !payload.checkInDate || !payload.checkOutDate) {
        showToast(container, "Preencha hospede, quarto, check-in e check-out.", "ti-alert-circle");
        return;
    }

    const saveButton = container.querySelector("#reservation-edit-save");
    saveButton.disabled = true;
    saveButton.innerHTML = '<i class="ti ti-loader-2 spinning"></i> Salvando...';

    try {
        const response = await updateBooking(state.bookingId, payload);
        state.booking = response.data;
        showToast(container, response.message || "Reserva atualizada com sucesso.", "ti-calendar-check");
        options.onSaved?.(response.data);
    } catch (error) {
        showToast(container, error.message || "Erro ao salvar reserva.", "ti-alert-circle");
    } finally {
        saveButton.disabled = false;
        saveButton.innerHTML = '<i class="ti ti-check"></i> Salvar reserva';
    }
}

function fillForm(container, booking) {
    setValue(container, "guestId", booking.guestId);
    setValue(container, "roomId", booking.roomId);
    setValue(container, "checkInDate", booking.checkInDate);
    setValue(container, "checkOutDate", booking.checkOutDate);
    setValue(container, "origin", originValue(booking.origin));
    setValue(container, "status", booking.status || "UNCONFIRMED");
    setValue(container, "adults", booking.adults ?? 0);
    setValue(container, "children", booking.children ?? 0);
    setValue(container, "pets", booking.pets ?? 0);
    setValue(container, "dailyRate", booking.dailyRate ?? 0);
    setValue(container, "discount", booking.discount ?? 0);
    setValue(container, "totalAmount", booking.totalAmount ?? 0);
    setValue(container, "paymentMethod", paymentMethodValue(booking.paymentMethod));
    setValue(container, "installments", booking.installments || "");
    setValue(container, "paidAmount", booking.paidAmount ?? 0);
    setValue(container, "paymentDate", booking.paymentDate || "");
    setValue(container, "specialRequests", booking.specialRequests || "");
    setValue(container, "internalNotes", booking.internalNotes || "");
}

function collectPayload(container) {
    return {
        guestId: numberValue(container, "guestId"),
        roomId: numberValue(container, "roomId"),
        checkInDate: value(container, "checkInDate") || null,
        checkOutDate: value(container, "checkOutDate") || null,
        status: value(container, "status") || "UNCONFIRMED",
        origin: value(container, "origin") || "DIRETO_TELEFONE",
        adults: numberValue(container, "adults") || 0,
        children: numberValue(container, "children") || 0,
        pets: numberValue(container, "pets") || 0,
        dailyRate: decimalValue(container, "dailyRate"),
        discount: decimalValue(container, "discount"),
        paidAmount: decimalValue(container, "paidAmount"),
        paymentDate: value(container, "paymentDate") || null,
        paymentMethod: value(container, "paymentMethod"),
        installments: value(container, "installments"),
        specialRequests: value(container, "specialRequests"),
        internalNotes: value(container, "internalNotes"),
    };
}

function updatePreview(container) {
    const id = container.querySelector("#reservation-edit-footer-status").textContent.replace("Reserva ", "") || "--";
    const guest = selectedLabel(container, "guestId") || "Reserva";
    const room = selectedLabel(container, "roomId");
    const checkIn = value(container, "checkInDate");
    const checkOut = value(container, "checkOutDate");
    const status = selectedLabel(container, "status") || "-";

    container.querySelector("#reservation-edit-preview-id").textContent = id;
    container.querySelector("#reservation-edit-preview-title").textContent = guest;
    container.querySelector("#reservation-edit-preview-title").classList.toggle("empty", !guest);
    container.querySelector("#reservation-edit-preview-sub").textContent = [room, checkIn && checkOut ? `${formatDate(checkIn)} a ${formatDate(checkOut)}` : null].filter(Boolean).join(" · ") || "Edite os parametros da reserva";
    container.querySelector("#reservation-edit-preview-badge").textContent = status;
}

function field(id, label, type, placeholder, icon, required = false) {
    const numberAttrs = type === "number" ? 'min="0" step="0.01"' : "";
    return `
      <label class="guest-field">
        <span>${label}${required ? " *" : ""}</span>
        <div><i class="ti ${icon}"></i><input id="reservation-edit-${id}" type="${type}" placeholder="${placeholder}" ${numberAttrs} ${required ? "required" : ""}></div>
      </label>
    `;
}

function selectField(id, label, options = []) {
    return `
      <label class="guest-field no-icon">
        <span>${label}</span>
        <div><select id="reservation-edit-${id}">
          ${options.map((option) => `<option value="${escapeHtml(option.value)}">${escapeHtml(option.label)}</option>`).join("")}
        </select></div>
      </label>
    `;
}

function textareaField(id, label) {
    return `
      <label class="guest-field wide">
        <span>${label}</span>
        <div><i class="ti ti-notes"></i><textarea id="reservation-edit-${id}" rows="3"></textarea></div>
      </label>
    `;
}

function fillSelect(container, id, options) {
    const select = container.querySelector(`#reservation-edit-${id}`);
    select.innerHTML = options.map((option) => `<option value="${escapeHtml(option.value)}">${escapeHtml(option.label)}</option>`).join("");
}

function setValue(container, id, newValue) {
    const element = container.querySelector(`#reservation-edit-${id}`);
    if (element) element.value = newValue ?? "";
}

function value(container, id) {
    return container.querySelector(`#reservation-edit-${id}`)?.value.trim() || "";
}

function numberValue(container, id) {
    const raw = value(container, id);
    return raw ? Number(raw) : null;
}

function decimalValue(container, id) {
    const raw = value(container, id).replace(",", ".");
    return raw ? Number(raw) : null;
}

function selectedLabel(container, id) {
    const select = container.querySelector(`#reservation-edit-${id}`);
    return select?.selectedOptions?.[0]?.textContent || "";
}

function originValue(origin) {
    const normalized = normalize(origin);
    return {
        diretotelefone: "DIRETO_TELEFONE",
        whatsapp: "WHATSAPP",
        instagram: "INSTAGRAM",
        booking: "BOOKING",
        airbnb: "AIRBNB",
        indicacao: "INDICACAO",
    }[normalized] || "DIRETO_TELEFONE";
}

function paymentMethodValue(method) {
    const normalized = normalize(method);
    return {
        pix: "PIX",
        creditcard: "CREDIT_CARD",
        debitcard: "DEBIT_CARD",
        cash: "CASH",
        banktransfer: "BANK_TRANSFER",
        booking: "BOOKING",
        airbnb: "AIRBNB",
    }[normalized] || "PIX";
}

function normalize(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .replace(/[^a-z0-9]/g, "");
}

function formatDate(date) {
    if (!date) return "-";
    const [, month, day] = date.split("-");
    return `${day}/${month}`;
}

function showToast(container, message, icon) {
    const toast = container.querySelector("#reservation-edit-toast");
    toast.querySelector("span").textContent = message;
    toast.querySelector("i").className = `ti ${icon}`;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2600);
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
