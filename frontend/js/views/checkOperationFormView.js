import { createCheckIn, createCheckOut, findAllBookings, findAllStays } from "../api.js?v=2026-05-18-checkforms";

export function renderCheckInFormView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main rooms-main">
  <div class="content check-form-page">
    <form id="checkin-form" class="check-form-card">
      <div class="check-form-header">
        <div>
          <span>Novo registro</span>
          <h1>Adicionar check-in</h1>
        </div>
        <button class="dashboard-back-btn" type="button" id="checkin-cancel"><i class="ti ti-arrow-left"></i> Voltar</button>
      </div>

      <div id="checkin-booking-preview" class="check-booking-preview hidden">
        <div class="check-preview-icon"><i class="ti ti-user-check"></i></div>
        <div>
          <span>Reserva selecionada</span>
          <strong>-</strong>
          <small>-</small>
        </div>
      </div>

      <div class="check-form-grid">
        ${selectField("bookingId", "Reserva", "ti-calendar-event")}
        ${selectField("stayId", "Estadia existente", "ti-bed")}
        ${field("actualCheckInAt", "Data e hora do check-in", "datetime-local", "ti-clock")}
        ${field("expectedCheckOutDate", "Check-out previsto", "date", "ti-calendar")}
        ${field("adults", "Adultos", "number", "ti-users")}
        ${field("children", "Crianças", "number", "ti-baby-carriage")}
        ${field("pets", "Pets", "number", "ti-paw")}
        ${field("vehiclePlate", "Placa do carro", "text", "ti-car")}
        ${field("vehicleModel", "Modelo do carro", "text", "ti-car-suv")}
        ${field("performedBy", "Responsável", "text", "ti-user-check")}
        ${selectStaticField("status", "Status", "ti-flag", [
            ["COMPLETED", "Concluído"],
            ["PENDING", "Pendente"],
            ["CANCELLED", "Cancelado"],
            ["NO_SHOW", "No-show"],
        ])}
      </div>

      <div class="check-form-switches">
        ${switchField("documentVerified", "Documento conferido")}
        ${switchField("paymentVerified", "Pagamento conferido")}
        ${switchField("registrationFormSigned", "Ficha assinada")}
        ${switchField("rulesAccepted", "Regras aceitas")}
        ${switchField("keysDelivered", "Chaves entregues")}
      </div>

      ${textareaField("notes", "Observações")}

      <div class="check-form-footer">
        <button class="dashboard-back-btn" type="button" id="checkin-cancel-footer">Cancelar</button>
        <button class="btn btn-primary" type="submit"><i class="ti ti-device-floppy"></i> Salvar check-in</button>
      </div>
    </form>
  </div>
  <div id="checkin-form-toast" class="booking-toast"><i class="ti ti-check"></i><span></span></div>
</div>`;

    bindCheckInForm(container, options);
}

export function renderCheckOutFormView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main rooms-main">
  <div class="content check-form-page">
    <form id="checkout-form" class="check-form-card">
      <div class="check-form-header">
        <div>
          <span>Novo registro</span>
          <h1>Adicionar checkout</h1>
        </div>
        <button class="dashboard-back-btn" type="button" id="checkout-cancel"><i class="ti ti-arrow-left"></i> Voltar</button>
      </div>

      <div class="check-form-grid">
        ${selectField("stayId", "Estadia", "ti-bed")}
        ${field("actualCheckOutAt", "Data e hora do checkout", "datetime-local", "ti-clock")}
        ${field("extraCharges", "Extras (R$)", "number", "ti-currency-real")}
        ${field("pendingAmount", "Valor pendente (R$)", "number", "ti-cash")}
        ${field("performedBy", "Responsável", "text", "ti-user-check")}
        ${selectStaticField("status", "Status", "ti-flag", [
            ["COMPLETED", "Concluído"],
            ["PENDING", "Pendente"],
            ["CANCELLED", "Cancelado"],
        ])}
      </div>

      <div class="check-form-switches">
        ${switchField("roomInspected", "Quarto inspecionado")}
        ${switchField("keysReturned", "Chaves devolvidas")}
        ${switchField("consumablesChecked", "Consumo conferido")}
        ${switchField("pendingAmountPaid", "Pendência paga")}
      </div>

      ${textareaField("notes", "Observações")}

      <div class="check-form-footer">
        <button class="dashboard-back-btn" type="button" id="checkout-cancel-footer">Cancelar</button>
        <button class="btn btn-primary" type="submit"><i class="ti ti-device-floppy"></i> Salvar checkout</button>
      </div>
    </form>
  </div>
  <div id="checkout-form-toast" class="booking-toast"><i class="ti ti-check"></i><span></span></div>
</div>`;

    bindCheckOutForm(container, options);
}

async function bindCheckInForm(container, options) {
    const cancel = () => options.onCancel?.();
    container.querySelector("#checkin-cancel").addEventListener("click", cancel);
    container.querySelector("#checkin-cancel-footer").addEventListener("click", cancel);
    setValue(container, "actualCheckInAt", toInputDateTime(new Date()));
    setValue(container, "status", "COMPLETED");

    try {
        const [bookingsResponse, staysResponse] = await Promise.all([
            findAllBookings(),
            findAllStays(),
        ]);
        fillBookingSelect(container, bookingsResponse.data || []);
        fillStaySelect(container, staysResponse.data || [], true);
        prefillCheckInForm(container, options);
    } catch (error) {
        showToast(container, "checkin", error.message || "Não foi possível carregar opções.", "ti-alert-circle");
    }

    container.querySelector("#bookingId").addEventListener("change", (event) => {
        const option = event.target.selectedOptions[0];
        if (option?.dataset.checkout) {
            setValue(container, "expectedCheckOutDate", option.dataset.checkout);
        }
        if (option?.dataset.adults) setValue(container, "adults", option.dataset.adults);
        if (option?.dataset.children) setValue(container, "children", option.dataset.children);
        if (option?.dataset.pets) setValue(container, "pets", option.dataset.pets);
        updateCheckInBookingPreview(container, option);
    });

    container.querySelector("#checkin-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = collectCheckInPayload(container);

        try {
            await createCheckIn(payload);
            showToast(container, "checkin", "Check-in cadastrado com sucesso.", "ti-check");
            setTimeout(() => options.onSaved?.(), 500);
        } catch (error) {
            showToast(container, "checkin", error.message || "Erro ao salvar check-in.", "ti-alert-circle");
        }
    });
}

async function bindCheckOutForm(container, options) {
    const cancel = () => options.onCancel?.();
    container.querySelector("#checkout-cancel").addEventListener("click", cancel);
    container.querySelector("#checkout-cancel-footer").addEventListener("click", cancel);
    setValue(container, "actualCheckOutAt", toInputDateTime(new Date()));
    setValue(container, "status", "COMPLETED");

    try {
        const staysResponse = await findAllStays();
        fillStaySelect(container, staysResponse.data || [], false);
        prefillCheckOutForm(container, options);
    } catch (error) {
        showToast(container, "checkout", error.message || "Não foi possível carregar estadias.", "ti-alert-circle");
    }

    container.querySelector("#checkout-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = collectCheckOutPayload(container);

        try {
            await createCheckOut(payload);
            showToast(container, "checkout", "Checkout cadastrado com sucesso.", "ti-check");
            setTimeout(() => options.onSaved?.(), 500);
        } catch (error) {
            showToast(container, "checkout", error.message || "Erro ao salvar checkout.", "ti-alert-circle");
        }
    });
}

function prefillCheckInForm(container, options) {
    if (options.bookingId) {
        setValue(container, "bookingId", String(options.bookingId));
        const select = container.querySelector("#bookingId");
        select.dispatchEvent(new Event("change"));
        updateCheckInBookingPreview(container, select.selectedOptions[0]);
    }

    if (options.stayId) {
        setValue(container, "stayId", String(options.stayId));
    }
}

function prefillCheckOutForm(container, options) {
    if (options.stayId) {
        setValue(container, "stayId", String(options.stayId));
    }
}

function collectCheckInPayload(container) {
    return {
        bookingId: numberOrNull(value(container, "bookingId")),
        stayId: numberOrNull(value(container, "stayId")),
        actualCheckInAt: value(container, "actualCheckInAt") || null,
        expectedCheckOutDate: value(container, "expectedCheckOutDate") || null,
        adults: numberOrNull(value(container, "adults")),
        children: numberOrNull(value(container, "children")),
        pets: numberOrNull(value(container, "pets")),
        documentVerified: checked(container, "documentVerified"),
        paymentVerified: checked(container, "paymentVerified"),
        registrationFormSigned: checked(container, "registrationFormSigned"),
        rulesAccepted: checked(container, "rulesAccepted"),
        keysDelivered: checked(container, "keysDelivered"),
        vehiclePlate: value(container, "vehiclePlate"),
        vehicleModel: value(container, "vehicleModel"),
        performedBy: value(container, "performedBy"),
        notes: value(container, "notes"),
        status: value(container, "status"),
    };
}

function collectCheckOutPayload(container) {
    return {
        stayId: numberOrNull(value(container, "stayId")),
        actualCheckOutAt: value(container, "actualCheckOutAt") || null,
        roomInspected: checked(container, "roomInspected"),
        keysReturned: checked(container, "keysReturned"),
        consumablesChecked: checked(container, "consumablesChecked"),
        pendingAmountPaid: checked(container, "pendingAmountPaid"),
        extraCharges: numberOrNull(value(container, "extraCharges")),
        pendingAmount: numberOrNull(value(container, "pendingAmount")),
        performedBy: value(container, "performedBy"),
        notes: value(container, "notes"),
        status: value(container, "status"),
    };
}

function fillBookingSelect(container, bookings) {
    const select = container.querySelector("#bookingId");
    select.innerHTML = `<option value="">Selecionar reserva</option>` + bookings
        .filter((booking) => booking.status !== "CANCELLED")
        .map((booking) => `
          <option value="${escapeHtml(booking.id)}" data-checkin="${escapeHtml(booking.checkInDate || "")}" data-checkout="${escapeHtml(booking.checkOutDate || "")}" data-adults="${escapeHtml(booking.adults || "")}" data-children="${escapeHtml(booking.children || "")}" data-pets="${escapeHtml(booking.pets || "")}" data-guest="${escapeHtml(booking.guestName || "")}" data-room="${escapeHtml(booking.roomNumber || "")}">
            #${escapeHtml(booking.id)} · ${escapeHtml(booking.guestName || "-")} · Qto ${escapeHtml(booking.roomNumber || "-")} · ${formatDate(booking.checkInDate)}-${formatDate(booking.checkOutDate)}
          </option>
        `).join("");
}

function updateCheckInBookingPreview(container, option) {
    const preview = container.querySelector("#checkin-booking-preview");
    if (!preview) {
        return;
    }

    if (!option?.value) {
        preview.classList.add("hidden");
        return;
    }

    preview.querySelector("strong").textContent = option.dataset.guest || "-";
    preview.querySelector("small").textContent = [
        `Reserva #${option.value}`,
        option.dataset.room ? `Qto ${option.dataset.room}` : null,
        option.dataset.checkin && option.dataset.checkout ? `${formatDate(option.dataset.checkin)} a ${formatDate(option.dataset.checkout)}` : null,
    ].filter(Boolean).join(" · ");
    preview.classList.remove("hidden");
}

function fillStaySelect(container, stays, includeEmpty) {
    const select = container.querySelector("#stayId");
    select.innerHTML = `${includeEmpty ? `<option value="">Selecionar estadia</option>` : `<option value="">Selecione uma estadia ativa</option>`}` + stays
        .filter((stay) => stay.status === "ACTIVE")
        .map((stay) => `
          <option value="${escapeHtml(stay.id)}">
            #${escapeHtml(stay.id)} · ${escapeHtml(stay.guestName || "-")} · Qto ${escapeHtml(stay.roomNumber || "-")} · saída ${formatDate(stay.expectedCheckOutDate)}
          </option>
        `).join("");
}

function field(id, label, type, icon) {
    return `
      <label class="check-field">
        <span>${label}</span>
        <div><i class="ti ${icon}"></i><input id="${id}" type="${type}" ${type === "number" ? `step="0.01"` : ""}></div>
      </label>
    `;
}

function selectField(id, label, icon) {
    return `
      <label class="check-field">
        <span>${label}</span>
        <div><i class="ti ${icon}"></i><select id="${id}"><option value="">Carregando...</option></select></div>
      </label>
    `;
}

function selectStaticField(id, label, icon, options) {
    return `
      <label class="check-field">
        <span>${label}</span>
        <div><i class="ti ${icon}"></i><select id="${id}">${options.map(([value, text]) => `<option value="${value}">${text}</option>`).join("")}</select></div>
      </label>
    `;
}

function switchField(id, label) {
    return `<label class="check-switch"><span>${label}</span><input id="${id}" type="checkbox" checked></label>`;
}

function textareaField(id, label) {
    return `
      <label class="check-field wide">
        <span>${label}</span>
        <div><textarea id="${id}" rows="4"></textarea></div>
      </label>
    `;
}

function value(container, id) {
    return container.querySelector(`#${id}`)?.value?.trim() || "";
}

function setValue(container, id, nextValue) {
    const input = container.querySelector(`#${id}`);
    if (input) input.value = nextValue || "";
}

function checked(container, id) {
    return Boolean(container.querySelector(`#${id}`)?.checked);
}

function numberOrNull(nextValue) {
    return nextValue === "" || nextValue == null ? null : Number(nextValue);
}

function toInputDateTime(date) {
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}

function formatDate(date) {
    if (!date) return "-";
    const [, month, day] = String(date).split("-");
    return month && day ? `${day}/${month}` : date;
}

function showToast(container, prefix, message, icon) {
    const toast = container.querySelector(`#${prefix}-form-toast`);
    toast.querySelector("span").textContent = message;
    toast.querySelector("i").className = `ti ${icon}`;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2600);
}

function escapeHtml(valueToEscape) {
    return String(valueToEscape ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
