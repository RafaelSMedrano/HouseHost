import { createBookingFromForm, findAllBookings, findAllGuests, findAllRooms } from "../api.js?v=2026-05-18-rooms";

export function renderNewReservationView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="main new-reservation-main">
  <div class="new-reservation-content">
    <div class="new-reservation-header">
      <div>
        <div class="new-reservation-eyebrow">Reservas</div>
        <h1>Nova <em>reserva</em></h1>
        <p>Preencha os dados abaixo para registrar uma nova hospedagem.</p>
      </div>
      <div class="booking-preview">
        <div class="bp-label">Resumo da reserva</div>
        <div class="bp-nights" id="previewNights">-<span>noites</span></div>
        <div class="bp-total" id="previewTotal">R$ -</div>
        <div class="bp-total-label">Total estimado</div>
      </div>
    </div>

    <form id="new-reservation-form" class="new-reservation-form">
      <section class="booking-section open">
        <button class="booking-section-head" type="button">
          <span class="section-icon icon-lav"><i class="ti ti-user"></i></span>
          <span><strong>Hospede da reserva</strong><small>Busque um hospede cadastrado por nome ou CPF</small></span>
          <i class="ti ti-chevron-down"></i>
        </button>
        <div class="booking-section-body">
          <div class="guest-reservation-row">
            <div class="guest-reservation-fields">
              <label class="booking-field guest-lookup-field"><span>Nome do hospede cadastrado</span><div><i class="ti ti-user-search"></i><input id="new-reservation-guest-name" type="text" placeholder="Informe o nome ou use o CPF ao lado" autocomplete="off"><div id="new-reservation-guest-name-options" class="guest-lookup-options"></div></div></label>
              <label class="booking-field guest-lookup-field"><span>CPF do hospede</span><div><i class="ti ti-id-badge"></i><input id="new-reservation-guest-document" type="text" placeholder="Informe o CPF ou use o nome ao lado" maxlength="14" autocomplete="off"><div id="new-reservation-guest-document-options" class="guest-lookup-options"></div></div></label>
            </div>
            <button id="new-reservation-register-guest" class="guest-register-btn" type="button"><i class="ti ti-user-plus"></i> Cadastrar hospede</button>
          </div>
        </div>
      </section>

      <section class="booking-section open">
        <button class="booking-section-head" type="button">
          <span class="section-icon icon-sage"><i class="ti ti-door"></i></span>
          <span><strong>Quarto & Periodo</strong><small>Acomodacao e datas de hospedagem</small></span>
          <i class="ti ti-chevron-down"></i>
        </button>
        <div class="booking-section-body">
          <div class="booking-field booking-full">
            <span>Quarto <strong>*</strong></span>
            <div id="reservation-room-grid" class="room-radio-grid">
              ${renderRoomLoadingCards()}
            </div>
          </div>

          <div class="booking-fields">
            <label class="booking-field"><span>Check-in <strong>*</strong></span><div><i class="ti ti-calendar-event"></i><input id="checkin" type="date" required></div></label>
            <label class="booking-field"><span>Check-out <strong>*</strong></span><div><i class="ti ti-calendar-event"></i><input id="checkout" type="date" required></div></label>
          </div>
          <div id="dateAlert" class="date-alert hidden"><i class="ti ti-alert-triangle"></i><span>A data de check-out deve ser posterior ao check-in. Revise as datas.</span></div>

          <div class="booking-separator"></div>

          <div class="booking-counters">
            ${counter("adultos", "Adultos", 2)}
            ${counter("criancas", "Criancas", 0)}
            ${counter("pets", "Pets", 0)}
          </div>
        </div>
      </section>

      <section class="booking-section open">
        <button class="booking-section-head" type="button">
          <span class="section-icon icon-rose"><i class="ti ti-antenna"></i></span>
          <span><strong>Origem & Status</strong><small>De onde veio a reserva e situacao atual</small></span>
          <i class="ti ti-chevron-down"></i>
        </button>
        <div class="booking-section-body">
          <div class="booking-field booking-full">
            <span>Origem da reserva</span>
            <div class="booking-tags">
              ${tag("origem", "or-direct", "DIRETO_TELEFONE", "Direto / Telefone", "ti-phone", true)}
              ${tag("origem", "or-whats", "WHATSAPP", "WhatsApp", "ti-brand-whatsapp")}
              ${tag("origem", "or-insta", "INSTAGRAM", "Instagram", "ti-brand-instagram")}
              ${tag("origem", "or-booking", "BOOKING", "Booking", "ti-world")}
              ${tag("origem", "or-airbnb", "AIRBNB", "Airbnb", "ti-home-star")}
              ${tag("origem", "or-indicacao", "INDICACAO", "Indicacao", "ti-users")}
            </div>
          </div>
          <div class="booking-separator"></div>
          <div class="booking-field booking-full">
            <span>Status da reserva <strong>*</strong></span>
            <div class="booking-status-pills">
              ${statusPill("st-conf", "confirmada", "Confirmada", true)}
              ${statusPill("st-pend", "pendente", "Pendente")}
              ${statusPill("st-in", "got_checkin", "Check-in realizado")}
              ${statusPill("st-canc", "cancelada", "Cancelada")}
            </div>
          </div>
        </div>
      </section>

      <section class="booking-section open">
        <button class="booking-section-head" type="button">
          <span class="section-icon icon-lav"><i class="ti ti-notes"></i></span>
          <span><strong>Observacoes</strong><small>Pedidos especiais e anotacoes internas</small></span>
          <i class="ti ti-chevron-down"></i>
        </button>
        <div class="booking-section-body">
          <div class="booking-fields">
            <label class="booking-field booking-full no-icon"><span>Pedidos especiais do hospede</span><div><textarea id="specialRequests" placeholder="Ex: quarto no andar terreo, berco para bebe, intolerancia alimentar..."></textarea></div></label>
            <label class="booking-field booking-full no-icon"><span>Anotacoes internas</span><div><textarea id="internalNotes" placeholder="Ex: hospede VIP, segunda visita, promocao aplicada..."></textarea></div><small>Visivel apenas para a equipe. Nao aparece para o hospede.</small></label>
          </div>
        </div>
      </section>

      <section class="booking-section open">
        <button class="booking-section-head" type="button">
          <span class="section-icon icon-amb"><i class="ti ti-credit-card"></i></span>
          <span><strong>Pagamento</strong><small>Forma de pagamento e valores</small></span>
          <i class="ti ti-chevron-down"></i>
        </button>
        <div class="booking-section-body">
          <div class="booking-fields">
            <label class="booking-field no-icon"><span>Forma de pagamento <strong>*</strong></span><div><select id="paymentMethod" required><option value="" disabled selected>Selecione</option><option>Pix</option><option>Cartao de credito</option><option>Cartao de debito</option><option>Dinheiro</option><option>Transferencia bancaria</option><option>Booking</option><option>Airbnb</option></select></div></label>
            <label class="booking-field no-icon"><span>Parcelas</span><div><select id="installments"><option>A vista</option><option>2x</option><option>3x</option><option>Entrada + saldo no check-in</option></select></div></label>
            <label class="booking-field"><span>Valor da diaria (R$)</span><div><i class="ti ti-currency-real"></i><input id="valorDiaria" type="number" min="0" step="0.01" placeholder="0,00"></div></label>
            <label class="booking-field"><span>Desconto (R$)</span><div><i class="ti ti-discount"></i><input id="desconto" type="number" min="0" step="0.01" placeholder="0,00"></div></label>
            <label class="booking-field"><span>Valor pago / sinal (R$)</span><div><i class="ti ti-cash"></i><input id="paidAmount" type="number" min="0" step="0.01" placeholder="0,00"></div></label>
            <label class="booking-field"><span>Data do pagamento</span><div><i class="ti ti-calendar-check"></i><input id="paymentDate" type="date"></div></label>
            <label class="booking-check-field booking-full"><input id="paymentCompleted" type="checkbox"><span><strong>Pagamento realizado</strong><small>Marque para criar a transacao financeira como PAID.</small></span></label>
          </div>
        </div>
      </section>
    </form>
  </div>

  <div class="new-reservation-footer">
    <div><span>Total estimado</span><strong id="footerTotal">R$ -</strong></div>
    <div class="footer-actions">
      <button id="new-reservation-cancel" class="btn-cancel" type="button"><i class="ti ti-x"></i> Cancelar</button>
      <button id="new-reservation-draft" class="btn-draft" type="button"><i class="ti ti-device-floppy"></i> Salvar rascunho</button>
      <button id="saveBtn" class="btn-save" type="submit" form="new-reservation-form"><i class="ti ti-check"></i> Confirmar reserva</button>
    </div>
  </div>

  <div id="new-reservation-toast" class="booking-toast"><i class="ti ti-check"></i><span>Reserva salva com sucesso!</span></div>
</div>
    `;

    bindNewReservationView(container, options);
}

function bindNewReservationView(container, options) {
    const counts = { adultos: 2, criancas: 0, pets: 0 };
    const mins = { adultos: 1, criancas: 0, pets: 0 };
    const backToReservations = () => {
        if (typeof options.onBackToReservations === "function") {
            options.onBackToReservations();
        }
    };

    container.querySelector("#new-reservation-cancel").addEventListener("click", backToReservations);
    container.querySelector("#new-reservation-draft").addEventListener("click", () => showToast(container, "Rascunho salvo!", "ti-device-floppy"));
    container.querySelector("#new-reservation-register-guest").addEventListener("click", () => {
        if (typeof options.onRegisterGuest === "function") {
            options.onRegisterGuest();
            return;
        }

        showToast(container, "Abra o cadastro de hospede para continuar.", "ti-user-plus");
    });
    setupGuestLookup(container);

    container.querySelectorAll(".booking-section-head").forEach((head) => {
        head.addEventListener("click", () => head.closest(".booking-section").classList.toggle("open"));
    });

    container.querySelectorAll("[data-counter]").forEach((button) => {
        button.addEventListener("click", () => {
            const key = button.dataset.counter;
            const delta = Number(button.dataset.delta);
            counts[key] = Math.max(mins[key], counts[key] + delta);
            container.querySelector(`#${key}`).textContent = String(counts[key]);
        });
    });

    ["#checkin", "#checkout", "#valorDiaria", "#desconto"].forEach((selector) => {
        container.querySelector(selector).addEventListener("input", () => updatePreview(container));
        container.querySelector(selector).addEventListener("change", () => updatePreview(container));
    });

    loadRooms(container);
    container.querySelector("#new-reservation-form").addEventListener("submit", (event) => handleReservationSubmit(event, container, counts, options));
}

async function loadRooms(container) {
    const grid = container.querySelector("#reservation-room-grid");

    try {
        const [roomsResponse, bookingsResponse] = await Promise.all([
            findAllRooms(),
            findAllBookings(),
        ]);
        const bookings = bookingsResponse.data || [];
        const rooms = (roomsResponse.data || []).map((room) => ({
            ...room,
            activeBooking: findActiveBookingForRoom(room, bookings),
        }));
        grid.innerHTML = rooms.length
            ? rooms.map(renderRoomCard).join("")
            : `<div class="rooms-empty">Nenhum quarto cadastrado ainda.</div>`;

        grid.querySelectorAll('input[name="quarto"]').forEach((radio) => {
            radio.addEventListener("change", () => {
                const price = Number(radio.dataset.dailyRate) || 0;
                container.querySelector("#valorDiaria").value = price ? String(price) : "";
                updatePreview(container);
            });
        });
    } catch (error) {
        grid.innerHTML = `<div class="rooms-empty rooms-error">${escapeHtml(error.message || "Nao foi possivel carregar os quartos.")}</div>`;
    }
}

async function setupGuestLookup(container) {
    const state = { guests: [], activeField: null };
    const nameInput = container.querySelector("#new-reservation-guest-name");
    const documentInput = container.querySelector("#new-reservation-guest-document");

    bindGuestLookupInput(container, state, nameInput, "name");
    bindGuestLookupInput(container, state, documentInput, "document");

    document.addEventListener("click", (event) => {
        if (!container.contains(event.target) || !event.target.closest(".guest-lookup-field")) {
            hideGuestLookupOptions(container);
        }
    });

    try {
        const response = await findAllGuests();
        state.guests = response.data || [];
        if (state.activeField) {
            renderGuestLookupOptions(container, state, state.activeField);
        }
    } catch (error) {
        state.guests = [];
    }
}

function bindGuestLookupInput(container, state, input, field) {
    input.addEventListener("focus", () => renderGuestLookupOptions(container, state, field));
    input.addEventListener("click", () => renderGuestLookupOptions(container, state, field));
    input.addEventListener("input", () => {
        if (field === "document") {
            maskCpf(input);
        }

        renderGuestLookupOptions(container, state, field);
    });
}

function renderGuestLookupOptions(container, state, field) {
    state.activeField = field;
    const options = container.querySelector(`#new-reservation-guest-${field}-options`);
    const input = container.querySelector(field === "name" ? "#new-reservation-guest-name" : "#new-reservation-guest-document");
    const term = normalizeSearch(input.value);
    const guests = filterGuests(state.guests, term, field).slice(0, 8);

    if (!state.guests.length) {
        options.innerHTML = `<div class="guest-lookup-empty">Nenhum hospede carregado.</div>`;
        showGuestLookupOptions(container, field);
        return;
    }

    if (!guests.length) {
        options.innerHTML = `<div class="guest-lookup-empty">Nenhum hospede encontrado.</div>`;
        showGuestLookupOptions(container, field);
        return;
    }

    options.innerHTML = guests.map((guest) => `
        <button type="button" class="guest-lookup-option" data-guest-id="${escapeHtml(guest.id)}">
          <span class="guest-lookup-avatar">${escapeHtml(initialsFor(guest.fullName))}</span>
          <span><strong>${escapeHtml(guest.fullName || "Hospede sem nome")}</strong><small>${escapeHtml(guest.documentNumber || "CPF nao informado")}</small></span>
        </button>
    `).join("");

    options.querySelectorAll("[data-guest-id]").forEach((button) => {
        button.addEventListener("click", () => {
            const guest = state.guests.find((item) => String(item.id) === button.dataset.guestId);
            selectGuest(container, guest);
        });
    });

    showGuestLookupOptions(container, field);
}

function filterGuests(guests, term, field) {
    if (!term) {
        return guests;
    }

    return guests.filter((guest) => {
        const name = normalizeSearch(guest.fullName);
        const documentNumber = normalizeSearch(guest.documentNumber);
        const primary = field === "document" ? documentNumber : name;
        return primary.includes(term) || name.includes(term) || documentNumber.includes(term);
    });
}

function selectGuest(container, guest) {
    if (!guest) {
        return;
    }

    const documentInput = container.querySelector("#new-reservation-guest-document");
    container.querySelector("#new-reservation-guest-name").value = guest.fullName || "";
    documentInput.value = guest.documentNumber || "";
    maskCpf(documentInput);
    hideGuestLookupOptions(container);
}

function showGuestLookupOptions(container, field) {
    hideGuestLookupOptions(container);
    const options = container.querySelector(`#new-reservation-guest-${field}-options`);
    options.classList.add("show");
    options.closest(".booking-section")?.classList.add("lookup-open");
}

function hideGuestLookupOptions(container) {
    container.querySelectorAll(".guest-lookup-options").forEach((options) => options.classList.remove("show"));
    container.querySelectorAll(".booking-section.lookup-open").forEach((section) => section.classList.remove("lookup-open"));
}

async function handleReservationSubmit(event, container, counts, options) {
    event.preventDefault();

    const payload = collectReservationPayload(container, counts);
    if (!payload.guest.fullName && !payload.guest.documentNumber) {
        showToast(container, "Informe o nome ou CPF de um hospede cadastrado.", "ti-alert-circle");
        return;
    }

    if (!payload.reservation.roomId) {
        showToast(container, "Selecione um quarto para a reserva.", "ti-alert-circle");
        return;
    }

    const saveButton = container.querySelector("#saveBtn");
    saveButton.innerHTML = '<i class="ti ti-loader-2 spinning"></i> Salvando...';
    saveButton.disabled = true;

    try {
        const response = await createBookingFromForm(payload);
        showToast(container, response.message || "Reserva confirmada com sucesso!", "ti-calendar-check");

        if (typeof options.onSaved === "function") {
            options.onSaved(response.data);
        }
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
    } finally {
        saveButton.innerHTML = '<i class="ti ti-check"></i> Confirmar reserva';
        saveButton.disabled = false;
    }
}

function collectReservationPayload(container, counts) {
    return {
        guest: {
            fullName: value(container, "#new-reservation-guest-name"),
            documentNumber: value(container, "#new-reservation-guest-document"),
        },
        reservation: {
            roomId: numberCheckedValue(container, 'input[name="quarto"]'),
            roomCode: "",
            checkInDate: value(container, "#checkin") || null,
            checkOutDate: value(container, "#checkout") || null,
            adults: counts.adultos,
            children: counts.criancas,
            pets: counts.pets,
        },
        payment: {
            paymentMethod: value(container, "#paymentMethod"),
            installments: value(container, "#installments"),
            dailyRate: numberValue(container, "#valorDiaria"),
            discount: numberValue(container, "#desconto"),
            paidAmount: numberValue(container, "#paidAmount"),
            paymentDate: value(container, "#paymentDate") || null,
            paymentCompleted: checked(container, "#paymentCompleted"),
        },
        origin: checkedValue(container, 'input[name="origem"]'),
        status: checkedValue(container, 'input[name="status"]'),
        specialRequests: value(container, "#specialRequests"),
        internalNotes: value(container, "#internalNotes"),
    };
}

function renderRoomCard(room) {
    const status = room.activeBooking ? roomStatus("OCCUPIED") : roomStatus(room.status);
    const detail = `${formatCurrency(room.dailyRate)} / noite · ${room.capacity || "-"} pessoa${room.capacity === 1 ? "" : "s"}`;
    const disabled = status.disabled ? "disabled" : "";
    const disabledClass = status.disabled ? "disabled" : "";

    return `
      <label class="room-radio-card ${disabledClass}">
        <input type="radio" name="quarto" value="${escapeHtml(room.id)}" data-daily-rate="${escapeHtml(room.dailyRate || 0)}" ${disabled}>
        <span><i class="ti ${roomIcon(room.type)}"></i><strong>${escapeHtml(room.roomNumber)} · ${escapeHtml(roomTypeLabel(room.type))}</strong><small>${escapeHtml(detail)} · ${escapeHtml(status.label)}</small></span>
      </label>
    `;
}

function findActiveBookingForRoom(room, bookings) {
    const today = currentDate();

    return bookings.find((booking) => {
        const bookingRoomId = Number(booking.roomId);
        const roomId = Number(room.id);
        const status = normalizeKey(booking.status);
        const blocksRoom = ["PENDING", "CONFIRMED", "GOT_CHECKIN"].includes(status);
        return bookingRoomId === roomId
            && blocksRoom
            && booking.checkInDate <= today
            && booking.checkOutDate > today;
    });
}

function renderRoomLoadingCards() {
    return Array.from({ length: 4 }).map((_, index) => `
      <label class="room-radio-card disabled">
        <input type="radio" name="quarto" disabled>
        <span><i class="ti ti-loader-2 spinning"></i><strong>Carregando quarto ${index + 1}</strong><small>Aguarde...</small></span>
      </label>
    `).join("");
}

function counter(id, label, value) {
    return `
      <div class="booking-field">
        <span>${label}</span>
        <div class="counter-wrap">
          <button type="button" data-counter="${id}" data-delta="-1">-</button>
          <strong id="${id}">${value}</strong>
          <button type="button" data-counter="${id}" data-delta="1">+</button>
        </div>
      </div>
    `;
}

function tag(name, id, value, label, icon, checked = false) {
    return `
      <label class="booking-tag">
        <input type="radio" name="${name}" id="${id}" value="${value}" ${checked ? "checked" : ""}>
        <span><i class="ti ${icon}"></i>${label}</span>
      </label>
    `;
}

function statusPill(id, value, label, checked = false) {
    return `
      <label class="booking-status-pill">
        <input type="radio" name="status" id="${id}" value="${value}" ${checked ? "checked" : ""}>
        <span><i></i>${label}</span>
      </label>
    `;
}

function updatePreview(container) {
    const checkin = container.querySelector("#checkin").value;
    const checkout = container.querySelector("#checkout").value;
    const dailyRate = Number(container.querySelector("#valorDiaria").value) || 0;
    const discount = Number(container.querySelector("#desconto").value) || 0;
    const alert = container.querySelector("#dateAlert");

    let nights = 0;
    if (checkin && checkout) {
        nights = Math.max(0, Math.round((new Date(checkout) - new Date(checkin)) / 86400000));
    }

    alert.classList.toggle("hidden", !(checkin && checkout && new Date(checkout) <= new Date(checkin)));

    const total = Math.max(0, nights * dailyRate - discount);
    const totalText = total > 0 ? `R$ ${total.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}` : "R$ -";

    container.querySelector("#previewNights").innerHTML = nights > 0 ? `${nights}<span>noite${nights > 1 ? "s" : ""}</span>` : '-<span>noites</span>';
    container.querySelector("#previewTotal").textContent = totalText;
    container.querySelector("#footerTotal").textContent = totalText;
}

function value(container, selector) {
    return container.querySelector(selector)?.value.trim() || "";
}

function checkedValue(container, selector) {
    return container.querySelector(`${selector}:checked`)?.value || "";
}

function numberCheckedValue(container, selector) {
    const selectedValue = checkedValue(container, selector);
    return selectedValue ? Number(selectedValue) : null;
}

function numberValue(container, selector) {
    const rawValue = value(container, selector);
    return rawValue === "" ? null : Number(rawValue);
}

function checked(container, selector) {
    return Boolean(container.querySelector(selector)?.checked);
}

function roomStatus(status) {
    const normalizedStatus = normalizeKey(status);
    if (normalizedStatus === "OCCUPIED" || normalizedStatus === "OCUPADO") {
        return { label: "Ocupado", disabled: false };
    }
    if (normalizedStatus === "MAINTENANCE" || normalizedStatus === "MANUTENCAO") {
        return { label: "Manutenção", disabled: true };
    }
    if (normalizedStatus === "INACTIVE" || normalizedStatus === "INATIVO") {
        return { label: "Inativo", disabled: true };
    }
    return { label: "Disponível", disabled: false };
}

function roomTypeLabel(type) {
    const labels = {
        SINGLE: "Individual",
        DOUBLE: "Duplo",
        SUITE: "Suíte",
        FAMILY: "Família",
        STANDARD: "Standard",
    };
    const normalizedType = normalizeKey(type);
    return labels[normalizedType] || String(type || "Tipo não informado").replaceAll("_", " ");
}

function roomIcon(type) {
    const normalizedType = normalizeKey(type);
    if (normalizedType === "FAMILY") return "ti-home-star";
    if (normalizedType === "SUITE") return "ti-home";
    if (normalizedType === "STANDARD") return "ti-bed";
    return "ti-door";
}

function formatCurrency(value) {
    return new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL",
    }).format(Number(value) || 0);
}

function currentDate() {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function normalizeKey(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toUpperCase();
}

function normalizeSearch(value) {
    return String(value || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/\D/g, "")
        || String(value || "")
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "")
            .toLowerCase()
            .trim();
}

function initialsFor(name) {
    const parts = String(name || "").trim().split(/\s+/).filter(Boolean);
    if (!parts.length) {
        return "?";
    }

    return parts.slice(0, 2).map((part) => part[0]).join("").toUpperCase();
}

function maskCpf(input) {
    let value = input.value.replace(/\D/g, "").substring(0, 11);
    value = value.replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
    input.value = value;
}

function maskPhone(input) {
    let value = input.value.replace(/\D/g, "").substring(0, 11);
    if (value.length <= 10) {
        value = value.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{4})(\d)/, "$1-$2");
    } else {
        value = value.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{5})(\d)/, "$1-$2");
    }
    input.value = value;
}

function showToast(container, message, icon) {
    const toast = container.querySelector("#new-reservation-toast");
    toast.querySelector("span").textContent = message;
    toast.querySelector("i").className = `ti ${icon}`;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2600);
}

function escapeHtml(valueToEscape) {
    return String(valueToEscape || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
