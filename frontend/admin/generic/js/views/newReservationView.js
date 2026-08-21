import { createBookingFromForm, findAllBookings, findAllRooms, findGuestsByDocumentNumber, findGuestsByName, findRatingsByGuestId, reconcileReservationCreation } from "../api.js?v=2026-08-20-api-request-timeout";
import { buildReadOnlyStarsMarkup } from "./ratingsView.js?v=2026-08-13-ratings-list";
import {
    buildReservationFinancialAllocation,
    calculateAllocationSummary,
    centsToDecimal,
    createReservationIdempotencyKey,
    formatCents,
    toCents,
} from "../financialAllocation.js?v=2026-08-20-reservation-ftp-allocation";

const GUEST_RATING_HISTORY_PAGE_SIZE = 100;
const GUEST_RATING_COLUMN_LIST = Object.freeze([
    ["checkInProcedureScore", "Check-in"],
    ["checkOutProcedureScore", "Checkout"],
    ["accommodationCleanlinessScore", "Limpeza"],
    ["teamCommunicationScore", "Comunicação"],
    ["locationScore", "Localização"],
    ["comfortScore", "Conforto"],
]);

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
              <div class="booking-field guest-lookup-field"><label class="guest-lookup-label" for="new-reservation-guest-name">Nome do hospede cadastrado</label><div><i class="ti ti-user-search"></i><input id="new-reservation-guest-name" type="text" placeholder="Informe o nome ou use o CPF ao lado" autocomplete="off"><div id="new-reservation-guest-name-options" class="guest-lookup-options"></div></div></div>
              <div class="booking-field guest-lookup-field"><label class="guest-lookup-label" for="new-reservation-guest-document">CPF do hospede</label><div><i class="ti ti-id-badge"></i><input id="new-reservation-guest-document" type="text" placeholder="Informe o CPF ou use o nome ao lado" maxlength="14" autocomplete="off"><div id="new-reservation-guest-document-options" class="guest-lookup-options"></div></div></div>
            </div>
            <button id="new-reservation-register-guest" class="guest-register-btn" type="button"><i class="ti ti-user-plus"></i> Cadastrar hospede</button>
          </div>
          <div id="guest-rating-history" class="guest-rating-history" aria-live="polite"></div>
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
              ${statusPill("st-conf", "CONFIRMED", "Confirmada", true)}
              ${statusPill("st-pend", "UNCONFIRMED", "Não confirmada")}
              ${statusPill("st-in", "IN_STAY", "Em estadia")}
              ${statusPill("st-canc", "CANCELED", "Cancelada")}
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
          <span><strong>Alocação financeira</strong><small>Defina quando cada parte da reserva será paga</small></span>
          <i class="ti ti-chevron-down"></i>
        </button>
        <div class="booking-section-body">
          <div class="booking-fields">
            <label class="booking-field no-icon"><span>Diária (R$) <strong>*</strong></span><div><input id="valorDiaria" type="number" min="0" step="0.01" inputmode="decimal" placeholder="0,00" required></div></label>
            <label class="booking-field no-icon"><span>Desconto (R$)</span><div><input id="desconto" type="number" min="0" step="0.01" inputmode="decimal" value="0" placeholder="0,00"></div></label>
          </div>
          <div id="reservation-financial-summary" class="financial-allocation-summary" aria-live="polite">
            <div><span>Total da reserva</span><strong id="allocationTotal">R$ -</strong></div>
            <div><span>Total alocado</span><strong id="allocationAllocated">R$ -</strong></div>
            <div><span>Valor restante</span><strong id="allocationRemaining">R$ -</strong></div>
            <p id="allocationSummaryState" role="status">Informe datas, quarto e valores para calcular a alocação.</p>
          </div>
          <div class="financial-allocation-groups">
            <fieldset class="financial-allocation-group">
              <legend>Sinal</legend>
              <label class="booking-check-field"><input id="downPaymentEnabled" type="checkbox"><span><strong>Ativar sinal</strong><small>O sinal pode ser recebido agora ou ficar agendado.</small></span></label>
              <div class="booking-fields financial-allocation-fields" data-financial-fields="downPayment" hidden>
                <label class="booking-field no-icon"><span>Valor do sinal</span><div><input id="downPaymentAmount" type="text" inputmode="decimal" placeholder="0,00" aria-describedby="downPaymentError"></div></label>
                <label class="booking-field no-icon"><span>Forma de pagamento</span><div><select id="downPaymentMethod"><option value="">Selecione</option><option value="PIX">Pix</option><option value="CREDIT_CARD">Cartão de crédito</option><option value="DEBIT_CARD">Cartão de débito</option><option value="CASH">Dinheiro</option><option value="BANK_TRANSFER">Transferência bancária</option><option value="BOOKING">Booking</option><option value="AIRBNB">Airbnb</option></select></div></label>
                <label class="booking-field no-icon" data-installment-fields hidden><span>Quantidade de parcelas</span><div><select id="downPaymentInstallments">${Array.from({ length: 11 }, (_, index) => `<option value="${index + 2}">${index + 2}x</option>`).join("")}</select></div></label>
                <label class="booking-check-field booking-full"><input id="downPaymentReceived" type="checkbox"><span><strong>Pagamento efetuado</strong></span></label>
                <p id="downPaymentError" class="financial-allocation-error" role="alert"></p>
              </div>
            </fieldset>
            <fieldset class="financial-allocation-group">
              <legend>Pagamento no check-in</legend>
              <label class="booking-check-field"><input id="checkInPaymentEnabled" type="checkbox"><span><strong>Agendar pagamento no check-in</strong><small>O valor e a data ficam definidos agora; a estrutura será escolhida no check-in.</small></span></label>
              <div class="booking-fields financial-allocation-fields" data-financial-fields="checkInPayment" hidden>
                <label class="booking-field no-icon booking-full"><span>Valor alocado</span><div><input id="checkInPaymentAmount" type="text" inputmode="decimal" placeholder="0,00" aria-describedby="checkInPaymentError"></div></label>
                <label class="booking-check-field booking-full"><input id="checkInPaymentReceived" type="checkbox"><span><strong>Pagamento efetuado</strong></span></label>
                <p id="checkInPaymentError" class="financial-allocation-error" role="alert"></p>
              </div>
            </fieldset>
            <fieldset class="financial-allocation-group">
              <legend>Pagamento no checkout</legend>
              <label class="booking-check-field"><input id="checkOutPaymentEnabled" type="checkbox"><span><strong>Agendar pagamento no checkout</strong><small>O valor e a data ficam definidos agora; a estrutura será escolhida no checkout.</small></span></label>
              <div class="booking-fields financial-allocation-fields" data-financial-fields="checkOutPayment" hidden>
                <label class="booking-field no-icon booking-full"><span>Valor alocado</span><div><input id="checkOutPaymentAmount" type="text" inputmode="decimal" placeholder="0,00" aria-describedby="checkOutPaymentError"></div></label>
                <label class="booking-check-field booking-full"><input id="checkOutPaymentReceived" type="checkbox"><span><strong>Pagamento efetuado</strong></span></label>
                <p id="checkOutPaymentError" class="financial-allocation-error" role="alert"></p>
              </div>
            </fieldset>
          </div>
          <div class="booking-fields financial-allocation-fields financial-current-payment" data-financial-fields="currentPayment">
            <label class="booking-field no-icon"><span>Valor do pagamento</span><div><input id="currentPaymentAmount" type="text" inputmode="decimal" value="0,00" placeholder="0,00" aria-describedby="currentPaymentError" readonly></div></label>
            <label class="booking-field no-icon"><span>Forma de pagamento <strong>*</strong></span><div><select id="currentPaymentMethod" aria-describedby="currentPaymentError"><option value="">Selecione</option><option value="PIX">Pix</option><option value="CREDIT_CARD">Cartão de crédito</option><option value="DEBIT_CARD">Cartão de débito</option><option value="CASH">Dinheiro</option><option value="BANK_TRANSFER">Transferência bancária</option><option value="BOOKING">Booking</option><option value="AIRBNB">Airbnb</option></select></div></label>
            <label class="booking-field no-icon" data-current-installment-fields hidden><span>Quantidade de parcelas</span><div><select id="currentPaymentInstallments">${Array.from({ length: 11 }, (_, index) => `<option value="${index + 2}">${index + 2}x</option>`).join("")}</select></div></label>
            <label class="booking-check-field booking-full"><input id="currentPaymentReceived" type="checkbox"><span><strong>Pagamento efetuado</strong></span></label>
            <p id="currentPaymentError" class="financial-allocation-error" role="alert"></p>
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
    const counts = {
        adultos: options.initialState?.counts?.adultos ?? 2,
        criancas: options.initialState?.counts?.criancas ?? 0,
        pets: options.initialState?.counts?.pets ?? 0,
    };
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
    restoreReservationFormState(container, options.initialState);
    setupGuestLookup(container, options);
    setupFinancialAllocation(container, options);

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

    ["#checkin", "#checkout", "#valorDiaria", "#desconto", "#downPaymentAmount", "#checkInPaymentAmount", "#checkOutPaymentAmount", "#currentPaymentMethod", "#downPaymentInstallments", "#currentPaymentInstallments"].forEach((selector) => {
        container.querySelector(selector).addEventListener("input", () => updatePreview(container));
        container.querySelector(selector).addEventListener("change", () => updatePreview(container));
    });

    loadRooms(container, options.initialState);
    container.querySelector("#new-reservation-form").addEventListener("submit", (event) => handleReservationSubmit(event, container, counts, options));
}

function setupFinancialAllocation(container, options) {
    ["downPayment", "checkInPayment", "checkOutPayment"].forEach((purpose) => {
        const enabledInput = container.querySelector(`#${purpose}Enabled`);
        enabledInput.addEventListener("change", () => {
            syncFinancialPurpose(container, purpose);
            updatePreview(container);
        });
    });
    ["#downPaymentMethod", "#currentPaymentMethod"].forEach((selector) => {
        container.querySelector(selector).addEventListener("change", () => {
            syncCardInstallments(container);
            updatePreview(container);
        });
    });
    ["#downPaymentReceived", "#checkInPaymentReceived", "#checkOutPaymentReceived", "#currentPaymentReceived"].forEach((selector) => {
        container.querySelector(selector).addEventListener("change", () => updatePreview(container));
    });
    ["#checkin", "#checkout"].forEach((selector) => {
        container.querySelector(selector).addEventListener("change", () => updateFinancialDates(container));
    });
    syncFinancialPurpose(container, "downPayment");
    syncFinancialPurpose(container, "checkInPayment");
    syncFinancialPurpose(container, "checkOutPayment");
    syncCardInstallments(container);
    if (!options.initialState?.financialIdempotencyKey) {
        container.dataset.financialIdempotencyKey = createReservationIdempotencyKey();
    } else {
        container.dataset.financialIdempotencyKey = options.initialState.financialIdempotencyKey;
    }
    updateFinancialDates(container);
}

function syncFinancialPurpose(container, purpose) {
    const enabled = checked(container, `#${purpose}Enabled`);
    const fields = container.querySelector(`[data-financial-fields="${purpose}"]`);
    fields.hidden = !enabled;
    fields.querySelectorAll("input, select").forEach((field) => {
        field.disabled = !enabled;
    });
}

function syncCardInstallments(container) {
    const downPaymentInstallmentField = container.querySelector("[data-installment-fields]");
    const downPaymentCardSelected = value(container, "#downPaymentMethod") === "CREDIT_CARD"
        && checked(container, "#downPaymentEnabled");
    downPaymentInstallmentField.hidden = !downPaymentCardSelected;
    downPaymentInstallmentField.querySelector("select").disabled = !downPaymentCardSelected;

    const currentPaymentInstallmentField = container.querySelector("[data-current-installment-fields]");
    const currentPaymentCardSelected = value(container, "#currentPaymentMethod") === "CREDIT_CARD"
        && !container.querySelector("#currentPaymentMethod").disabled;
    currentPaymentInstallmentField.hidden = !currentPaymentCardSelected;
    currentPaymentInstallmentField.querySelector("select").disabled = !currentPaymentCardSelected;
}

function updateFinancialDates(container) {
    const checkInInput = container.querySelector("#checkInPaymentAmount");
    const checkOutInput = container.querySelector("#checkOutPaymentAmount");
    checkInInput.closest(".financial-allocation-fields").querySelector("span").textContent =
        `Valor alocado · check-in ${formatDateLabel(value(container, "#checkin"))}`;
    checkOutInput.closest(".financial-allocation-fields").querySelector("span").textContent =
        `Valor alocado · checkout ${formatDateLabel(value(container, "#checkout"))}`;
    updatePreview(container);
}

function formatDateLabel(dateValue) {
    if (!dateValue) {
        return "data ainda não definida";
    }
    const date = new Date(`${dateValue}T12:00:00`);
    return Number.isNaN(date.getTime())
        ? "data inválida"
        : new Intl.DateTimeFormat("pt-BR").format(date);
}

async function loadRooms(container, reservationFormStateRecord) {
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
        restoreRadioValues(container, reservationFormStateRecord?.radioValueMap);

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

function setupGuestLookup(container, options) {
    const state = {
        guests: [],
        activeField: null,
        requestId: 0,
        selectedGuestId: normalizedRecordId(options.initialState?.selectedGuestId),
    };
    const nameInput = container.querySelector("#new-reservation-guest-name");
    const documentInput = container.querySelector("#new-reservation-guest-document");
    const ratingHistoryLoader = createGuestRatingHistoryLoader({
        findRatings: options.findRatingsByGuestId || findRatingsByGuestId,
        onLoading: () => renderGuestRatingHistoryLoading(container),
        onLoaded: (ratingSummaryList) => renderGuestRatingHistory(container, ratingSummaryList, options),
        onEmpty: () => renderGuestRatingHistory(container, [], options),
        onError: () => renderGuestRatingHistoryError(container),
        onClear: () => clearGuestRatingHistory(container),
    });
    if (state.selectedGuestId !== null) {
        container.dataset.selectedGuestId = String(state.selectedGuestId);
    }

    bindGuestLookupInput(container, state, ratingHistoryLoader, nameInput, "name");
    bindGuestLookupInput(container, state, ratingHistoryLoader, documentInput, "document");
    bindGuestLookupOptions(container, state, ratingHistoryLoader, "name");
    bindGuestLookupOptions(container, state, ratingHistoryLoader, "document");

    if (state.selectedGuestId !== null) {
        ratingHistoryLoader.load(state.selectedGuestId);
    }

    document.addEventListener("click", (event) => {
        if (!container.contains(event.target) || !event.target.closest(".guest-lookup-field")) {
            hideGuestLookupOptions(container);
        }
    });

}

function bindGuestLookupInput(container, state, ratingHistoryLoader, input, field) {
    input.addEventListener("focus", () => renderGuestLookupOptions(container, state, field, false));
    input.addEventListener("click", () => renderGuestLookupOptions(container, state, field, false));
    input.addEventListener("input", () => {
        state.selectedGuestId = null;
        delete container.dataset.selectedGuestId;
        ratingHistoryLoader.clear();
        if (field === "document") {
            maskCpf(input);
        }

        const length = field === "document"
            ? input.value.replace(/\D/g, "").length
            : input.value.trim().length;
        renderGuestLookupOptions(container, state, field, length % 3 === 0);
    });
}

export function bindGuestLookupOptions(container, state, ratingHistoryLoader, field) {
    const options = container.querySelector(`#new-reservation-guest-${field}-options`);
    const selectOption = (event) => {
        const optionButton = event.target.closest("[data-guest-id]");
        if (!optionButton || !options.contains(optionButton)) {
            return false;
        }
        return handleGuestLookupOptionSelection(
                event,
                container,
                state,
                ratingHistoryLoader,
                optionButton.dataset.guestId
        );
    };
    options.addEventListener("pointerdown", selectOption);
    options.addEventListener("click", (event) => {
        if (event.detail !== 0) {
            event.preventDefault();
            event.stopPropagation();
            return;
        }
        selectOption(event);
    });
}

async function renderGuestLookupOptions(container, state, field, shouldRequest) {
    state.activeField = field;
    const options = container.querySelector(`#new-reservation-guest-${field}-options`);
    const input = container.querySelector(field === "name" ? "#new-reservation-guest-name" : "#new-reservation-guest-document");
    if (shouldRequest) {
        const requestId = ++state.requestId;

        try {
            const response = field === "document"
                ? await findGuestsByDocumentNumber(input.value)
                : await findGuestsByName(input.value);
            if (requestId !== state.requestId) {
                return;
            }
            state.guests = response.data || [];
        } catch (error) {
            if (requestId !== state.requestId) {
                return;
            }
            state.guests = [];
        }
    }

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

    showGuestLookupOptions(container, field);
}

export function handleGuestLookupOptionSelection(
    event,
    container,
    state,
    ratingHistoryLoader,
    guestId,
) {
    event.preventDefault();
    event.stopPropagation();
    const guest = state.guests.find((guestItem) => String(guestItem.id) === String(guestId));
    selectGuest(container, state, ratingHistoryLoader, guest);
    return Boolean(guest);
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

function selectGuest(container, state, ratingHistoryLoader, guest) {
    if (!guest) {
        return;
    }

    const documentInput = container.querySelector("#new-reservation-guest-document");
    container.querySelector("#new-reservation-guest-name").value = guest.fullName || "";
    documentInput.value = guest.documentNumber || "";
    maskCpf(documentInput);
    state.selectedGuestId = normalizedRecordId(guest.id);
    if (state.selectedGuestId === null) {
        delete container.dataset.selectedGuestId;
    } else {
        container.dataset.selectedGuestId = String(state.selectedGuestId);
    }
    hideGuestLookupOptions(container);
    ratingHistoryLoader.load(state.selectedGuestId);
}

export function createGuestRatingHistoryLoader({
    findRatings,
    onLoading = () => {},
    onLoaded = () => {},
    onEmpty = () => {},
    onError = () => {},
    onClear = () => {},
}) {
    let requestId = 0;
    let abortController = null;

    function clear() {
        requestId += 1;
        abortController?.abort();
        abortController = null;
        onClear();
    }

    async function load(guestId) {
        const normalizedGuestId = normalizedRecordId(guestId);
        clear();
        if (normalizedGuestId === null) {
            return false;
        }

        const currentRequestId = requestId;
        abortController = typeof AbortController === "function" ? new AbortController() : null;
        onLoading();

        try {
            const response = await findRatings(
                    normalizedGuestId,
                    0,
                    GUEST_RATING_HISTORY_PAGE_SIZE,
                    abortController ? { signal: abortController.signal } : {}
            );
            if (currentRequestId !== requestId) {
                return false;
            }

            const ratingSummaryList = Array.isArray(response?.data?.ratingSummaryDTOList)
                ? response.data.ratingSummaryDTOList
                : [];
            if (ratingSummaryList.length === 0) {
                onEmpty();
                return true;
            }

            onLoaded(ratingSummaryList);
            return true;
        } catch (error) {
            if (currentRequestId !== requestId || error?.name === "AbortError") {
                return false;
            }
            onError();
            return false;
        }
    }

    return { load, clear };
}

export function buildGuestRatingHistoryMarkup(ratingSummaryList) {
    const hasRatings = ratingSummaryList.length > 0;
    return `
        <button class="guest-rating-history-toggle" type="button" aria-expanded="false" aria-controls="guest-rating-history-table">
            <span>Histórico de avaliações</span>
            <i class="ti ti-chevron-down" aria-hidden="true"></i>
        </button>
        ${hasRatings ? `<div id="guest-rating-history-table" class="guest-rating-history-table-wrap" hidden>
            <table class="guest-rating-history-table">
                <thead>
                    <tr>
                        <th scope="col">Reserva</th>
                        <th scope="col">Avaliada em</th>
                        ${GUEST_RATING_COLUMN_LIST.map(([, label]) => `<th scope="col">${label}</th>`).join("")}
                        <th scope="col">Observações</th>
                    </tr>
                </thead>
                <tbody>
                    ${ratingSummaryList.map(buildGuestRatingHistoryRowMarkup).join("")}
                </tbody>
            </table>
        </div>` : `<div id="guest-rating-history-table" class="guest-rating-history-empty" role="status" hidden>
            Nenhuma avaliação encontrada para este hóspede.
        </div>`}
    `;
}

function buildGuestRatingHistoryRowMarkup(ratingSummary) {
    const bookingId = normalizedRecordId(ratingSummary?.bookingId);
    const bookingLabel = bookingId === null ? "Reserva não identificada" : `Reserva #${bookingId}`;
    return `
        <tr>
            <td>${bookingId === null
                    ? `<span>${bookingLabel}</span>`
                    : `<a href="#" data-open-history-booking="${bookingId}" aria-label="Abrir ${bookingLabel}">${bookingLabel}</a>`}</td>
            <td><time datetime="${escapeHtml(ratingSummary?.evaluatedAt || "")}">${formatRatingDateTime(ratingSummary?.evaluatedAt)}</time></td>
            ${GUEST_RATING_COLUMN_LIST.map(([fieldName]) => `<td>${buildReadOnlyStarsMarkup(ratingSummary?.[fieldName])}</td>`).join("")}
            <td class="guest-rating-history-observations">${escapeHtml(ratingSummary?.observations || "Sem observações.")}</td>
        </tr>
    `;
}

function renderGuestRatingHistory(container, ratingSummaryList, options) {
    const history = container.querySelector("#guest-rating-history");
    history.innerHTML = buildGuestRatingHistoryMarkup(ratingSummaryList);
    const toggle = history.querySelector(".guest-rating-history-toggle");
    const historyContent = history.querySelector("#guest-rating-history-table");
    toggle.addEventListener("click", () => toggleGuestRatingHistory(toggle, historyContent));
    history.querySelectorAll("[data-open-history-booking]").forEach((bookingLink) => {
        bookingLink.addEventListener("click", (event) => {
            event.preventDefault();
            options.onOpenBooking?.(
                    bookingLink.dataset.openHistoryBooking,
                    captureReservationFormState(container)
            );
        });
    });
}

export function toggleGuestRatingHistory(toggle, table) {
    const expanded = toggle.getAttribute("aria-expanded") === "true";
    toggle.setAttribute("aria-expanded", String(!expanded));
    table.hidden = expanded;
    return !expanded;
}

function renderGuestRatingHistoryLoading(container) {
    container.querySelector("#guest-rating-history").innerHTML = `
        <span class="guest-rating-history-state" role="status">
            <i class="ti ti-loader-2 spinning" aria-hidden="true"></i> Carregando histórico de avaliações...
        </span>
    `;
}

function renderGuestRatingHistoryError(container) {
    container.querySelector("#guest-rating-history").innerHTML = `
        <span class="guest-rating-history-state guest-rating-history-error" role="alert">
            Não foi possível carregar o histórico de avaliações.
        </span>
    `;
}

function clearGuestRatingHistory(container) {
    container.querySelector("#guest-rating-history").innerHTML = "";
}

function captureReservationFormState(container) {
    const fieldValueMap = {};
    container.querySelectorAll("input[id], select[id], textarea[id]").forEach((field) => {
        fieldValueMap[field.id] = {
            value: field.value,
            checked: Boolean(field.checked),
        };
    });
    const radioValueMap = {};
    container.querySelectorAll('input[type="radio"]:checked').forEach((radio) => {
        radioValueMap[radio.name] = radio.value;
    });
    return {
        selectedGuestId: normalizedRecordId(container.dataset.selectedGuestId),
        financialIdempotencyKey: container.dataset.financialIdempotencyKey || null,
        counts: {
            adultos: Number(container.querySelector("#adultos")?.textContent) || 1,
            criancas: Number(container.querySelector("#criancas")?.textContent) || 0,
            pets: Number(container.querySelector("#pets")?.textContent) || 0,
        },
        fieldValueMap,
        radioValueMap,
    };
}

function restoreReservationFormState(container, reservationFormStateRecord) {
    if (!reservationFormStateRecord) {
        return;
    }
    Object.entries(reservationFormStateRecord.fieldValueMap || {}).forEach(([fieldId, fieldState]) => {
        const field = container.querySelector(`#${fieldId}`);
        if (!field) {
            return;
        }
        field.value = fieldState.value;
        if (field.type === "checkbox") {
            field.checked = fieldState.checked;
        }
    });
    restoreRadioValues(container, reservationFormStateRecord.radioValueMap);
    if (reservationFormStateRecord.financialIdempotencyKey) {
        container.dataset.financialIdempotencyKey = reservationFormStateRecord.financialIdempotencyKey;
    }
    Object.entries(reservationFormStateRecord.counts || {}).forEach(([counterId, count]) => {
        const counterValue = container.querySelector(`#${counterId}`);
        if (counterValue) {
            counterValue.textContent = String(count);
        }
    });
    updatePreview(container);
}

function restoreRadioValues(container, radioValueMap = {}) {
    Object.entries(radioValueMap || {}).forEach(([name, selectedValue]) => {
        container.querySelectorAll(`input[name="${name}"]`).forEach((radio) => {
            radio.checked = radio.value === selectedValue;
        });
    });
}

function normalizedRecordId(value) {
    const recordId = Number(value);
    return Number.isInteger(recordId) && recordId > 0 ? recordId : null;
}

function formatRatingDateTime(valueToFormat) {
    if (!valueToFormat) {
        return "Data indisponível";
    }
    const date = new Date(valueToFormat);
    if (Number.isNaN(date.getTime())) {
        return escapeHtml(valueToFormat);
    }
    return new Intl.DateTimeFormat("pt-BR", {
        dateStyle: "short",
        timeStyle: "short",
    }).format(date);
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

    const allocationSummary = calculateCurrentAllocationSummary(container);
    if (allocationSummary.state !== "complete" || allocationSummary.allocationError) {
        renderAllocationErrors(container, allocationSummary);
        showToast(container, allocationSummary.message, "ti-alert-circle");
        return;
    }

    const saveButton = container.querySelector("#saveBtn");
    saveButton.innerHTML = '<i class="ti ti-loader-2 spinning"></i> Salvando...';
    saveButton.disabled = true;

    try {
        const response = await createBookingFromForm(payload);
        applyAuthoritativeFinancialSummary(container, response.data?.financialTransactionPlan);
        const authoritativeBooking = response.data?.booking || response.data;
        rotateReservationIdempotencyKey(container);
        showToast(container, response.message || "Reserva confirmada com sucesso!", "ti-calendar-check");

        if (typeof options.onSaved === "function") {
            options.onSaved(authoritativeBooking, response.data?.financialTransactionPlan || null);
        }
    } catch (error) {
        const reconciliation = await reconcileUncertainReservationSubmission(
                container.dataset.financialIdempotencyKey,
                error
        );
        if (reconciliation.state === "committed") {
            applyAuthoritativeFinancialSummary(
                    container,
                    reconciliation.outcome.financialTransactionPlanSummaryDTO
                            || reconciliation.outcome.financialTransactionPlan
            );
            rotateReservationIdempotencyKey(container);
            showToast(container, "Reserva confirmada após reconciliação.", "ti-calendar-check");
            options.onSaved?.(
                    { id: reconciliation.outcome.bookingId },
                    reconciliation.outcome.financialTransactionPlanSummaryDTO
                            || reconciliation.outcome.financialTransactionPlan
            );
            return;
        }
        renderReservationCommandError(container, reconciliation.message);
        showToast(container, reconciliation.message, "ti-alert-circle");
    } finally {
        saveButton.innerHTML = '<i class="ti ti-check"></i> Confirmar reserva';
        saveButton.disabled = false;
    }
}

export async function reconcileUncertainReservationSubmission(idempotencyKey, originalError) {
    if (originalError?.status && originalError.status < 500 && originalError.status !== 408) {
        return { state: "unconfirmed", message: originalError.message || "Não foi possível salvar a reserva." };
    }
    if (!idempotencyKey) {
        return { state: "unconfirmed", message: originalError?.message || "Não foi possível confirmar a reserva." };
    }

    try {
        const response = await reconcileReservationCreation(idempotencyKey);
        const outcome = response?.data;
        if (outcome?.bookingId) {
            return { state: "committed", outcome };
        }
        return { state: "unconfirmed", message: "A reserva não foi confirmada. Revise os dados e tente novamente." };
    } catch (reconciliationError) {
        return {
            state: "unconfirmed",
            message: reconciliationError?.status === 404
                    ? "A reserva não foi confirmada. A mesma tentativa pode ser enviada novamente."
                    : "Não foi possível confirmar o resultado da reserva. A tentativa permanece disponível para recuperação.",
        };
    }
}

function rotateReservationIdempotencyKey(container) {
    container.dataset.financialIdempotencyKey = createReservationIdempotencyKey();
}

function applyAuthoritativeFinancialSummary(container, financialTransactionPlanSummary) {
    if (!financialTransactionPlanSummary) {
        return;
    }
    const totalAmount = Number(financialTransactionPlanSummary.totalAmount);
    const allocatedAmount = Number(financialTransactionPlanSummary.totalAmount)
            - Number(financialTransactionPlanSummary.outstandingAmount);
    const remainingAmount = Number(financialTransactionPlanSummary.outstandingAmount);
    if (Number.isFinite(totalAmount)) {
        container.querySelector("#allocationTotal").textContent = formatMoneyValue(totalAmount);
    }
    if (Number.isFinite(allocatedAmount)) {
        container.querySelector("#allocationAllocated").textContent = formatMoneyValue(allocatedAmount);
    }
    if (Number.isFinite(remainingAmount)) {
        container.querySelector("#allocationRemaining").textContent = formatMoneyValue(remainingAmount);
    }
    const summaryState = container.querySelector("#allocationSummaryState");
    summaryState.textContent = "Totais confirmados pelo backend.";
    summaryState.dataset.state = "complete";
}

function formatMoneyValue(amount) {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(amount);
}

function renderReservationCommandError(container, message) {
    const summaryState = container.querySelector("#allocationSummaryState");
    summaryState.textContent = message;
    summaryState.dataset.state = "error";
    summaryState.setAttribute("role", "alert");
}

function collectReservationPayload(container, counts) {
    const financialAllocationState = collectFinancialAllocationState(container);
    const allocationSummary = calculateCurrentAllocationSummary(container);
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
            dailyRate: numberValue(container, "#valorDiaria"),
            discount: numberValue(container, "#desconto"),
        },
        paymentAllocation: buildReservationFinancialAllocation({
            ...financialAllocationState,
            currentPayment: {
                enabled: allocationSummary.currentPaymentCents > 0,
                amountCents: allocationSummary.currentPaymentCents,
                method: value(container, "#currentPaymentMethod") || null,
                installmentsQuantity: Number(value(container, "#currentPaymentInstallments")) || null,
                received: checked(container, "#currentPaymentReceived"),
            },
        }),
        idempotencyKey: container.dataset.financialIdempotencyKey || createReservationIdempotencyKey(),
        origin: checkedValue(container, 'input[name="origem"]'),
        status: checkedValue(container, 'input[name="status"]'),
        specialRequests: value(container, "#specialRequests"),
        internalNotes: value(container, "#internalNotes"),
    };
}

function collectFinancialAllocationState(container) {
    return {
        currentPayment: {
            enabled: false,
            amountCents: null,
            method: null,
        },
        downPayment: {
            enabled: checked(container, "#downPaymentEnabled"),
            amountCents: toCents(value(container, "#downPaymentAmount")),
            received: checked(container, "#downPaymentReceived"),
            method: value(container, "#downPaymentMethod") || null,
            installmentsQuantity: Number(value(container, "#downPaymentInstallments")) || null,
        },
        checkInPayment: {
            enabled: checked(container, "#checkInPaymentEnabled"),
            amountCents: toCents(value(container, "#checkInPaymentAmount")),
            received: checked(container, "#checkInPaymentReceived"),
        },
        checkOutPayment: {
            enabled: checked(container, "#checkOutPaymentEnabled"),
            amountCents: toCents(value(container, "#checkOutPaymentAmount")),
            received: checked(container, "#checkOutPaymentReceived"),
        },
    };
}

function calculateCurrentAllocationSummary(container) {
    const totalCents = calculateReservationTotalCents(container);
    const financialAllocationState = collectFinancialAllocationState(container);
    const allocationCentsList = ["downPayment", "checkInPayment", "checkOutPayment"]
        .filter((purpose) => financialAllocationState[purpose].enabled)
        .map((purpose) => financialAllocationState[purpose].amountCents);
    const summary = calculateAllocationSummary(totalCents, allocationCentsList);
    const currentPaymentCents = summary.state === "incomplete" ? summary.remainingCents : 0;
    const currentPaymentError = currentPaymentCents > 0
        ? !value(container, "#currentPaymentMethod")
            ? "Selecione a forma de pagamento do valor pago no momento."
            : !checked(container, "#currentPaymentReceived")
                ? "Marque o pagamento no momento como efetuado."
                : null
        : null;
    const enabledPaymentWithoutConfirmation = [
        ["#downPaymentEnabled", "#downPaymentReceived", "sinal"],
        ["#checkInPaymentEnabled", "#checkInPaymentReceived", "check-in"],
        ["#checkOutPaymentEnabled", "#checkOutPaymentReceived", "checkout"],
    ].find(([enabledSelector, receivedSelector]) =>
        checked(container, enabledSelector) && !checked(container, receivedSelector)
    );
    const confirmationError = enabledPaymentWithoutConfirmation
        ? `Marque o pagamento de ${enabledPaymentWithoutConfirmation[2]} como efetuado.`
        : null;
    const allocationError = confirmationError || currentPaymentError;
    const currentPaymentComplete = currentPaymentCents > 0 && !allocationError;
    return {
        ...summary,
        state: allocationError ? "incomplete" : currentPaymentComplete ? "complete" : summary.state,
        currentPaymentCents,
        currentPaymentDisabled: summary.state === "complete",
        currentPaymentError,
        allocationError,
        confirmationErrorPurpose: enabledPaymentWithoutConfirmation?.[2] || null,
        message: allocationError
            ? allocationError
            : currentPaymentComplete
                ? "Alocação completa. O restante será pago agora."
                : summary.state === "complete"
                    ? "Alocação completa."
                    : summary.state === "excessive"
                        ? "A alocação excede o total da reserva."
                        : summary.state === "incomplete"
                            ? "O valor restante será pago no momento da reserva."
                            : "Informe datas, quarto e valores para calcular a alocação.",
    };
}

function calculateReservationTotalCents(container) {
    const checkin = value(container, "#checkin");
    const checkout = value(container, "#checkout");
    const dailyRateCents = toCents(value(container, "#valorDiaria"));
    const discountCents = toCents(value(container, "#desconto"));
    if (!checkin || !checkout || !Number.isSafeInteger(dailyRateCents) || !Number.isSafeInteger(discountCents)) {
        return null;
    }
    const nights = Math.round((new Date(checkout) - new Date(checkin)) / 86400000);
    return nights > 0 ? Math.max(0, nights * dailyRateCents - discountCents) : null;
}

function renderAllocationErrors(container, summary) {
    const state = container.querySelector("#allocationSummaryState");
    state.textContent = summary.message;
    state.dataset.state = summary.state;
    const currentPaymentError = container.querySelector("#currentPaymentError");
    if (currentPaymentError) {
        currentPaymentError.textContent = summary.currentPaymentError || "";
    }
    const purposeLabelMap = {
        downPayment: "sinal",
        checkInPayment: "check-in",
        checkOutPayment: "checkout",
    };
    ["downPayment", "checkInPayment", "checkOutPayment"].forEach((purpose) => {
        const input = container.querySelector(`#${purpose}Amount`);
        const error = container.querySelector(`#${purpose}Error`);
        if (!input || !error) {
            return;
        }
        const invalid = checked(container, `#${purpose}Enabled`)
                && !Number.isSafeInteger(toCents(input.value));
        const confirmationMissing = summary.confirmationErrorPurpose === purposeLabelMap[purpose];
        error.textContent = invalid
            ? "Informe um valor válido em centavos."
            : confirmationMissing
                ? `Marque o pagamento de ${purposeLabelMap[purpose]} como efetuado.`
                : "";
        input.setAttribute("aria-invalid", String(invalid));
    });
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
        const blocksRoom = ["UNCONFIRMED", "CONFIRMED", "IN_STAY"].includes(status);
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
    const dailyRateCents = toCents(value(container, "#valorDiaria"));
    const discountCents = toCents(value(container, "#desconto"));
    const alert = container.querySelector("#dateAlert");

    let nights = 0;
    if (checkin && checkout) {
        nights = Math.max(0, Math.round((new Date(checkout) - new Date(checkin)) / 86400000));
    }

    alert.classList.toggle("hidden", !(checkin && checkout && new Date(checkout) <= new Date(checkin)));

    const totalCents = nights > 0 && Number.isSafeInteger(dailyRateCents) && Number.isSafeInteger(discountCents)
        ? Math.max(0, nights * dailyRateCents - discountCents)
        : null;
    const totalText = Number.isSafeInteger(totalCents) && totalCents > 0 ? formatCents(totalCents) : "R$ -";

    container.querySelector("#previewNights").innerHTML = nights > 0 ? `${nights}<span>noite${nights > 1 ? "s" : ""}</span>` : '-<span>noites</span>';
    container.querySelector("#previewTotal").textContent = totalText;
    container.querySelector("#footerTotal").textContent = totalText;
    const allocationSummary = calculateCurrentAllocationSummary(container);
    container.querySelector("#allocationTotal").textContent = Number.isSafeInteger(allocationSummary.totalCents)
        ? formatCents(allocationSummary.totalCents)
        : "Indisponível";
    container.querySelector("#allocationAllocated").textContent = Number.isSafeInteger(allocationSummary.allocatedCents)
        ? formatCents(allocationSummary.allocatedCents)
        : "Indisponível";
    container.querySelector("#allocationRemaining").textContent = Number.isSafeInteger(allocationSummary.remainingCents)
        ? formatCents(allocationSummary.remainingCents)
        : "Indisponível";
    const currentPaymentAmount = container.querySelector("#currentPaymentAmount");
    currentPaymentAmount.value = Number.isSafeInteger(allocationSummary.currentPaymentCents)
        ? centsToDecimal(allocationSummary.currentPaymentCents).replace(".", ",")
        : "";
    currentPaymentAmount.dataset.amountCents = String(
        allocationSummary.currentPaymentCents || 0
    );
    currentPaymentAmount.disabled = allocationSummary.currentPaymentDisabled;
    container.querySelector("#currentPaymentMethod").disabled = allocationSummary.currentPaymentDisabled;
    container.querySelector("#currentPaymentReceived").disabled = allocationSummary.currentPaymentDisabled;
    container.querySelector("#currentPaymentError").textContent = allocationSummary.currentPaymentError || "";
    syncCardInstallments(container);
    const summaryState = container.querySelector("#allocationSummaryState");
    summaryState.textContent = allocationSummary.message;
    summaryState.dataset.state = allocationSummary.state;
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
