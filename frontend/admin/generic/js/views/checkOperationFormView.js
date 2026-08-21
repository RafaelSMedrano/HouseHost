import {
    createCheckIn,
    createCheckOut,
    findAllBookings,
    findFinancialTransactionPlanByBookingId,
    findGuestById,
    findScheduledFinancialComponent,
    reconcileFinancialReplacement,
} from "../api.js?v=2026-08-20-ftp-checkout-materialization";
import { calculateInstallmentPreview, createReservationIdempotencyKey, formatCents, toCents } from "../financialAllocation.js?v=2026-08-20-ftp-checkout-materialization";

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
        <button class="dashboard-back-btn" type="button" id="checkin-cancel" aria-label="Voltar"><i class="ti ti-arrow-left" aria-hidden="true"></i> Voltar</button>
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

      <section id="checkin-financial-section" class="checkin-financial-section" aria-labelledby="checkin-financial-title">
        <div class="checkin-financial-heading">
          <div>
            <span>Pagamento agendado</span>
            <h2 id="checkin-financial-title">Pagamento no check-in</h2>
          </div>
          <i class="ti ti-credit-card" aria-hidden="true"></i>
        </div>
        <div id="checkin-financial-state" role="status" aria-live="polite" aria-atomic="true">Carregando pagamento agendado...</div>
      </section>

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
  <div id="checkin-form-toast" class="booking-toast" role="status" aria-live="polite" aria-atomic="true"><i class="ti ti-check"></i><span></span></div>
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
        <button class="dashboard-back-btn" type="button" id="checkout-cancel" aria-label="Voltar"><i class="ti ti-arrow-left" aria-hidden="true"></i> Voltar</button>
      </div>

      <div class="check-form-grid">
        ${selectField("bookingId", "Reserva em hospedagem", "ti-bed")}
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

      <section id="checkout-financial-section" class="checkin-financial-section checkout-financial-section" aria-labelledby="checkout-financial-title">
        <div class="checkin-financial-heading">
          <div>
            <span>Pagamento agendado</span>
            <h2 id="checkout-financial-title">Pagamento no checkout</h2>
          </div>
          <i class="ti ti-credit-card" aria-hidden="true"></i>
        </div>
        <div id="checkout-financial-state" role="status" aria-live="polite" aria-atomic="true">Selecione uma reserva para consultar o pagamento agendado.</div>
      </section>

      <div class="check-form-switches">
        ${switchField("roomInspected", "Quarto inspecionado")}
        ${switchField("keysReturned", "Chaves devolvidas")}
        ${switchField("consumablesChecked", "Consumo conferido")}
        ${switchField("pendingAmountPaid", "Pendência paga")}
      </div>

      <section class="check-history-assessment" data-checkout-history-assessment aria-labelledby="checkout-history-title">
        <div class="check-history-header">
          <div>
            <span>Conclusão da hospedagem</span>
            <h2 id="checkout-history-title">Histórico e avaliação</h2>
          </div>
          <i class="ti ti-chart-bar" aria-hidden="true"></i>
        </div>

        <div id="checkout-history-context" class="check-history-context" role="status" aria-live="polite" aria-atomic="true">
          <strong id="checkout-history-guest">Selecione uma reserva</strong>
          <small id="checkout-history-booking">O histórico do hóspede será carregado sem alterar os valores mantidos pelo backend.</small>
        </div>

        <div class="check-history-stats" aria-label="Histórico atual do hóspede">
          <div><span>Hospedagens anteriores</span><strong id="checkout-history-stays">-</strong></div>
          <div><span>Última estadia</span><strong id="checkout-history-last-stay">-</strong></div>
        </div>

        ${buildCheckOutRatingControlsMarkup()}
      </section>

      ${textareaField("notes", "Observações")}

      <p id="checkout-form-announcement" class="check-form-announcement" role="status" aria-live="polite" aria-atomic="true">Carregando reservas em hospedagem.</p>

      <div class="check-form-footer">
        <button class="dashboard-back-btn" type="button" id="checkout-cancel-footer">Cancelar</button>
        <button id="checkout-submit" class="btn btn-primary" type="submit"><i class="ti ti-device-floppy"></i> Salvar checkout</button>
      </div>
    </form>
  </div>
  <div id="checkout-form-toast" class="booking-toast" role="status" aria-live="polite" aria-atomic="true"><i class="ti ti-check"></i><span></span></div>
</div>`;

    bindCheckOutForm(container, options);
}

async function bindCheckInForm(container, options) {
    const state = {
        financialRequestId: 0,
        financialPlanId: null,
        scheduledFinancialComponent: null,
        paymentCommandIdempotencyKey: createReservationIdempotencyKey(),
        financialLoadState: "none",
        submitting: false,
    };
    const cancel = () => options.onCancel?.();
    container.querySelector("#checkin-cancel").addEventListener("click", cancel);
    container.querySelector("#checkin-cancel-footer").addEventListener("click", cancel);
    setValue(container, "status", "COMPLETED");

    try {
        const bookingsResponse = await findAllBookings();
        fillBookingSelect(container, bookingsResponse.data || []);
        prefillCheckInForm(container, options);
    } catch (error) {
        showToast(container, "checkin", error.message || "Não foi possível carregar opções.", "ti-alert-circle");
    }

    container.querySelector("#bookingId").addEventListener("change", (event) => {
        const option = event.target.selectedOptions[0];
        if (option?.dataset.checkout) {
        }
        if (option?.dataset.adults) setValue(container, "adults", option.dataset.adults);
        if (option?.dataset.children) setValue(container, "children", option.dataset.children);
        if (option?.dataset.pets) setValue(container, "pets", option.dataset.pets);
        updateCheckInBookingPreview(container, option);
        loadCheckInFinancialState(container, state, option?.value);
    });

    if (options.bookingId) {
        loadCheckInFinancialState(container, state, options.bookingId);
    } else {
        state.financialLoadState = "none";
        renderCheckInFinancialState(container, { state: "none", message: "Selecione uma reserva para consultar o pagamento agendado." });
    }

    container.querySelector("#checkin-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        if (state.submitting) {
            return;
        }
        const payload = collectCheckInPayload(container, state);
        const financialValidation = validateCheckInFinancialSubmission(state, container);
        if (financialValidation) {
            renderCheckInFinancialError(container, financialValidation);
            showToast(container, "checkin", financialValidation, "ti-alert-circle");
            return;
        }

        const submitButton = container.querySelector('#checkin-form button[type="submit"]');
        state.submitting = true;
        submitButton.disabled = true;
        submitButton.innerHTML = '<i class="ti ti-loader-2 spinning"></i> Salvando check-in...';
        container.querySelector("#checkin-form").setAttribute("aria-busy", "true");
        try {
            const response = await createCheckIn(payload);
            if (response.data?.paymentMaterialization) {
                renderCheckInFinancialSuccess(container, response.data.paymentMaterialization);
            }
            state.paymentCommandIdempotencyKey = createReservationIdempotencyKey();
            showToast(container, "checkin", "Check-in cadastrado com sucesso.", "ti-check");
            setTimeout(() => options.onSaved?.(), 500);
        } catch (error) {
            const recovery = await recoverCheckInFinancialCommand(state, error);
            if (recovery.state === "committed") {
                renderCheckInFinancialSuccess(container, recovery.outcome);
                state.paymentCommandIdempotencyKey = createReservationIdempotencyKey();
                showToast(container, "checkin", "Check-in confirmado após reconciliação.", "ti-check");
                setTimeout(() => options.onSaved?.(), 500);
            } else {
                renderCheckInFinancialError(container, recovery.message);
                showToast(container, "checkin", recovery.message, "ti-alert-circle");
            }
        } finally {
            container.querySelector("#checkin-form").setAttribute("aria-busy", "false");
            state.submitting = false;
            submitButton.disabled = false;
            submitButton.innerHTML = '<i class="ti ti-device-floppy"></i> Salvar check-in';
        }
    });
}

async function loadCheckInFinancialState(container, state, bookingId) {
    const requestId = ++state.financialRequestId;
    state.financialPlanId = null;
    state.scheduledFinancialComponent = null;
    if (!bookingId) {
        state.financialLoadState = "none";
        renderCheckInFinancialState(container, { state: "none", message: "Selecione uma reserva para consultar o pagamento agendado." });
        return;
    }

    state.financialLoadState = "loading";
    renderCheckInFinancialState(container, { state: "loading", message: "Carregando pagamento agendado..." });
    try {
        const planResponse = await findFinancialTransactionPlanByBookingId(bookingId);
        if (requestId !== state.financialRequestId) {
            return;
        }
        const financialTransactionPlanSummary = planResponse.data;
        if (!financialTransactionPlanSummary?.id) {
            state.financialLoadState = "none";
            renderCheckInFinancialState(container, { state: "none", message: "Nenhum pagamento FTP está agendado para este check-in." });
            return;
        }
        state.financialPlanId = financialTransactionPlanSummary.id;
        const scheduledResponse = await findScheduledFinancialComponent(
                state.financialPlanId,
                "PLAN_CHECK_IN_PAYMENT"
        );
        if (requestId !== state.financialRequestId) {
            return;
        }
        if (!scheduledResponse.data) {
            state.financialLoadState = "none";
            renderCheckInFinancialState(container, { state: "none", message: "Nenhum pagamento FTP está agendado para este check-in." });
            return;
        }
        state.scheduledFinancialComponent = scheduledResponse.data;
        state.financialLoadState = isEligibleScheduledComponent(scheduledResponse.data) ? "eligible" : "completed";
        renderCheckInFinancialState(container, {
            state: state.financialLoadState,
            component: scheduledResponse.data,
        });
    } catch (error) {
        if (requestId !== state.financialRequestId) {
            return;
        }
        state.financialLoadState = error.status === 409 ? "stale" : "failure";
        renderCheckInFinancialState(container, {
            state: state.financialLoadState,
            message: error.status === 409
                    ? "O pagamento agendado mudou. Selecione a reserva novamente para atualizar."
                    : error.message || "Não foi possível carregar o pagamento agendado.",
        });
    }
}

function renderCheckInFinancialState(container, financialState) {
    const stateContainer = container.querySelector("#checkin-financial-state");
    if (!stateContainer) {
        return;
    }
    stateContainer.dataset.state = financialState.state;
    if (financialState.state !== "eligible") {
        stateContainer.innerHTML = `<p class="checkin-financial-message" role="${financialState.state === "failure" || financialState.state === "stale" ? "alert" : "status"}">${escapeHtml(financialState.message || "")}</p>`;
        return;
    }

    const component = financialState.component;
    stateContainer.innerHTML = `
      <div class="checkin-financial-authoritative">
        <div><span>Finalidade</span><strong>Pagamento no check-in</strong></div>
        <div><span>Valor agendado</span><strong>${escapeHtml(formatMoney(component.amount))}</strong></div>
        <div><span>Vencimento</span><strong>${escapeHtml(formatDate(component.dueDate))}</strong></div>
        <div><span>Status</span><strong>${escapeHtml(financialStatusLabel(component.status))}</strong></div>
      </div>
      <div class="checkin-financial-controls">
        <label class="check-field"><span>Forma de pagamento</span><div><select id="checkinPaymentMethod"><option value="">Selecione</option><option value="PIX">Pix</option><option value="CREDIT_CARD">Cartão de crédito</option><option value="DEBIT_CARD">Cartão de débito</option><option value="CASH">Dinheiro</option><option value="BANK_TRANSFER">Transferência bancária</option><option value="BOOKING">Booking</option><option value="AIRBNB">Airbnb</option></select></div></label>
        <fieldset class="checkin-payment-structure" aria-describedby="checkin-payment-structure-error">
          <legend>Estrutura do pagamento</legend>
          <label><input type="radio" name="checkinPaymentStructure" value="SIMPLE" checked> Pagamento à vista</label>
          <label><input type="radio" name="checkinPaymentStructure" value="INSTALLMENT"> Pagamento parcelado</label>
        </fieldset>
        <label id="checkin-installments-field" class="check-field" hidden><span>Quantidade de parcelas</span><div><select id="checkinInstallments">${Array.from({ length: 11 }, (_, index) => `<option value="${index + 2}">${index + 2}x</option>`).join("")}</select></div><div id="checkin-installment-preview" class="checkin-installment-preview" aria-live="polite"></div></label>
        <p id="checkin-payment-structure-error" class="checkin-financial-error" role="alert"></p>
        <label class="check-switch checkin-financial-confirm"><span>Confirmo a materialização do pagamento agendado</span><input id="checkinFinancialConfirmation" type="checkbox"></label>
      </div>
    `;
    bindCheckInFinancialControls(container, component);
}

function bindCheckInFinancialControls(container, component) {
    const update = () => {
        const installmentSelected = checkedValue(container, "checkinPaymentStructure") === "INSTALLMENT";
        const installmentField = container.querySelector("#checkin-installments-field");
        installmentField.hidden = !installmentSelected;
        const amountCents = toCents(String(component.amount));
        const previewList = calculateInstallmentPreview(
                amountCents,
                Number(value(container, "checkinInstallments")),
                component.dueDate
        );
        container.querySelector("#checkin-installment-preview").innerHTML = installmentSelected
                ? previewList.map((installment) => `<span>${installment.number}ª: ${formatCents(installment.amountCents)}</span>`).join("")
                : "";
    };
    container.querySelectorAll('input[name="checkinPaymentStructure"]').forEach((input) => input.addEventListener("change", update));
    container.querySelector("#checkinInstallments").addEventListener("change", update);
    update();
}

function validateCheckInFinancialSubmission(state, container) {
    if (["loading", "failure", "stale"].includes(state.financialLoadState)) {
        return "Atualize o pagamento agendado antes de salvar o check-in.";
    }
    if (!isEligibleScheduledComponent(state.scheduledFinancialComponent)) {
        return null;
    }
    if (!value(container, "checkinPaymentMethod")) {
        return "Selecione a forma de pagamento do check-in.";
    }
    if (!checked(container, "checkinFinancialConfirmation")) {
        return "Confirme a finalidade, o valor e a estrutura do pagamento agendado.";
    }
    if (checkedValue(container, "checkinPaymentStructure") === "INSTALLMENT") {
        const installmentsQuantity = Number(value(container, "checkinInstallments"));
        if (!Number.isInteger(installmentsQuantity) || installmentsQuantity < 2 || installmentsQuantity > 12) {
            return "A quantidade de parcelas deve estar entre 2 e 12.";
        }
    }
    return null;
}

function buildCheckInPaymentMaterialization(state, container) {
    if (!isEligibleScheduledComponent(state.scheduledFinancialComponent)) {
        return null;
    }
    const structure = checkedValue(container, "checkinPaymentStructure");
    return {
        structure,
        method: value(container, "checkinPaymentMethod"),
        installmentsQuantity: structure === "INSTALLMENT" ? Number(value(container, "checkinInstallments")) : null,
        idempotencyKey: state.paymentCommandIdempotencyKey,
    };
}

async function recoverCheckInFinancialCommand(state, originalError) {
    if (!state.financialPlanId || !state.paymentCommandIdempotencyKey
            || (originalError?.status && originalError.status < 500 && originalError.status !== 408)) {
        return { state: "unconfirmed", message: originalError?.message || "Não foi possível salvar o check-in." };
    }
    try {
        const response = await reconcileFinancialReplacement(state.financialPlanId, state.paymentCommandIdempotencyKey);
        return response.data ? { state: "committed", outcome: response.data } : {
            state: "unconfirmed",
            message: "O resultado do check-in ainda não foi confirmado. Tente novamente com a mesma operação.",
        };
    } catch (error) {
        return {
            state: "unconfirmed",
            message: error.status === 409
                    ? "O pagamento agendado mudou. Atualize a reserva antes de tentar novamente."
                    : "Não foi possível confirmar o resultado do check-in. A operação permanece disponível para recuperação.",
        };
    }
}

function renderCheckInFinancialSuccess(container, outcome) {
    const component = outcome?.definitiveComponent;
    const financialTransactionPlan = outcome?.financialTransactionPlan;
    const stateContainer = container.querySelector("#checkin-financial-state");
    stateContainer.dataset.state = "success";
    stateContainer.innerHTML = `<p class="checkin-financial-message">Pagamento definitivo criado: ${escapeHtml(formatMoney(component?.amount))} · ${escapeHtml(financialStatusLabel(component?.status))}. FTP atualizado: ${escapeHtml(financialPlanStatusLabel(financialTransactionPlan?.status))}.</p>`;
}

function renderCheckInFinancialError(container, message) {
    const stateContainer = container.querySelector("#checkin-financial-state");
    stateContainer.dataset.state = "failure";
    stateContainer.setAttribute("role", "alert");
    const error = stateContainer.querySelector(".checkin-financial-error, .checkin-financial-message");
    if (error) {
        error.textContent = message;
    } else {
        stateContainer.innerHTML = `<p class="checkin-financial-message" role="alert">${escapeHtml(message)}</p>`;
    }
}

function isEligibleScheduledComponent(component) {
    return Boolean(component?.id)
            && ["WAITING", "ON_TIME", "ACTIVE"].includes(String(component.status || "").toUpperCase());
}

function checkedValue(container, name) {
    return container.querySelector(`input[name="${name}"]:checked`)?.value || "";
}

function financialStatusLabel(status) {
    return {
        WAITING: "Agendado",
        ON_TIME: "Em dia",
        LATE: "Em atraso",
        PAID: "Pago",
        SETTLED: "Liquidado",
        CANCELED: "Cancelado",
    }[String(status || "").toUpperCase()] || "Indisponível";
}

function financialPlanStatusLabel(status) {
    return {
        ACTIVE: "Ativo",
        PARTIALLY_SETTLED: "Parcialmente pago",
        OVERDUE: "Em atraso",
        SETTLED: "Liquidado",
        CANCELED: "Cancelado",
    }[String(status || "").toUpperCase()] || "Indisponível";
}

function formatMoney(amount) {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(Number(amount) || 0);
}

async function bindCheckOutForm(container, options) {
    const state = {
        historyRequestId: 0,
        financialRequestId: 0,
        financialPlanId: null,
        scheduledFinancialComponent: null,
        paymentCommandIdempotencyKey: createReservationIdempotencyKey(),
        financialLoadState: "none",
        submitting: false,
    };
    const cancel = () => options.onCancel?.();
    container.querySelector("#checkout-cancel").addEventListener("click", cancel);
    container.querySelector("#checkout-cancel-footer").addEventListener("click", cancel);
    setValue(container, "actualCheckOutAt", toInputDateTime(new Date()));
    setValue(container, "status", "COMPLETED");
    bindCheckOutRatingControls(container);
    syncCheckOutRatingVisibility(container);

    container.querySelector("#status").addEventListener("change", () => {
        syncCheckOutRatingVisibility(container);
    });

    container.querySelector("#bookingId").addEventListener("change", (event) => {
        const option = event.target.selectedOptions[0];
        updateCheckOutHistoryPreview(container, option, state);
        loadCheckOutFinancialState(container, state, option?.value);
    });

    try {
        const bookingsResponse = await findAllBookings();
        const bookingList = bookingsResponse.data || [];
        fillCheckOutBookingSelect(container, bookingList);
        prefillCheckOutForm(container, options);
        setAnnouncement(container, `${bookingList.filter((booking) => booking.status === "IN_STAY").length} reservas em hospedagem carregadas.`);
    } catch (error) {
        setAnnouncement(container, "Não foi possível carregar as reservas em hospedagem.");
        showToast(container, "checkout", error.message || "Não foi possível carregar reservas.", "ti-alert-circle");
    }

    if (options.bookingId) {
        loadCheckOutFinancialState(container, state, options.bookingId);
    }

    container.querySelector("#checkout-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        if (state.submitting) {
            return;
        }

        const payload = collectCheckOutPayload(container, state);
        const financialValidation = validateCheckOutFinancialSubmission(state, container);
        if (financialValidation) {
            renderCheckOutFinancialError(container, financialValidation);
            showToast(container, "checkout", financialValidation, "ti-alert-circle");
            return;
        }
        const ratingValidation = validateCheckOutRating(payload.rating, payload.status);

        if (ratingValidation) {
            const invalidControl = container.querySelector(
                    ratingValidation.fieldName === "observations"
                            ? "#ratingObservations"
                            : `input[name="${ratingValidation.fieldName}"]`
            );
            invalidControl?.closest("fieldset, label")?.setAttribute("aria-invalid", "true");
            invalidControl?.focus();
            setAnnouncement(container, ratingValidation.message);
            showToast(container, "checkout", ratingValidation.message, "ti-alert-circle");
            return;
        }

        clearCheckOutRatingErrors(container);
        const submitButton = container.querySelector("#checkout-submit");
        state.submitting = true;
        submitButton.disabled = true;
        submitButton.innerHTML = '<i class="ti ti-loader-2 spinning"></i> Salvando checkout...';
        container.querySelector("#checkout-form").setAttribute("aria-busy", "true");
        setAnnouncement(container, "Salvando checkout.");
        let saved = false;
        try {
            const response = await createCheckOut(payload);
            if (response.data?.paymentMaterialization) {
                renderCheckOutFinancialSuccess(container, response.data.paymentMaterialization);
            }
            saved = true;
            state.paymentCommandIdempotencyKey = createReservationIdempotencyKey();
            setAnnouncement(container, "Checkout cadastrado com sucesso.");
            showToast(container, "checkout", "Checkout cadastrado com sucesso.", "ti-check");
            setTimeout(() => options.onSaved?.(), 500);
        } catch (error) {
            const recovery = await recoverCheckOutFinancialCommand(state, error);
            if (recovery.state === "committed") {
                saved = true;
                renderCheckOutFinancialSuccess(container, recovery.outcome);
                state.paymentCommandIdempotencyKey = createReservationIdempotencyKey();
                setAnnouncement(container, "Checkout confirmado após reconciliação.");
                showToast(container, "checkout", "Checkout confirmado após reconciliação.", "ti-check");
                setTimeout(() => options.onSaved?.(), 500);
            } else {
                setAnnouncement(container, recovery.message);
                showToast(container, "checkout", recovery.message, "ti-alert-circle");
            }
        } finally {
            container.querySelector("#checkout-form").setAttribute("aria-busy", "false");
            if (!saved) {
                state.submitting = false;
                submitButton.disabled = false;
                submitButton.innerHTML = '<i class="ti ti-device-floppy"></i> Salvar checkout';
            }
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

}

function prefillCheckOutForm(container, options) {
    if (options.bookingId) {
        setValue(container, "bookingId", String(options.bookingId));
        container.querySelector("#bookingId").dispatchEvent(new Event("change"));
    }
}

export function collectCheckInPayload(container, state = null) {
    return {
        bookingId: numberOrNull(value(container, "bookingId")),
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
        paymentMaterialization: state ? buildCheckInPaymentMaterialization(state, container) : null,
        status: value(container, "status"),
    };
}

export function collectCheckOutPayload(container, state = null) {
    const status = value(container, "status");
    return {
        bookingId: numberOrNull(value(container, "bookingId")),
        actualCheckOutAt: value(container, "actualCheckOutAt") || null,
        roomInspected: checked(container, "roomInspected"),
        keysReturned: checked(container, "keysReturned"),
        consumablesChecked: checked(container, "consumablesChecked"),
        pendingAmountPaid: checked(container, "pendingAmountPaid"),
        extraCharges: numberOrNull(value(container, "extraCharges")),
        pendingAmount: numberOrNull(value(container, "pendingAmount")),
        performedBy: value(container, "performedBy"),
        notes: value(container, "notes"),
        rating: status === "COMPLETED" ? collectCheckOutRating(container) : null,
        paymentMaterialization: state ? buildCheckOutPaymentMaterialization(state, container) : null,
        status,
    };
}

export function validateCheckOutRating(rating, status = "COMPLETED") {
    if (status !== "COMPLETED") {
        return null;
    }

    for (const criterion of CHECK_OUT_RATING_CRITERION_LIST) {
        const score = rating?.[criterion.fieldName];
        if (!Number.isInteger(score) || score < 1 || score > 5) {
            return {
                fieldName: criterion.fieldName,
                message: `Responda o critério ${criterion.label}.`,
            };
        }
    }

    if ((rating.observations || "").length > 4000) {
        return {
            fieldName: "observations",
            message: "As observações da avaliação devem ter no máximo 4.000 caracteres.",
        };
    }

    return null;
}

function fillBookingSelect(container, bookings) {
    const select = container.querySelector("#bookingId");
    select.innerHTML = `<option value="">Selecionar reserva</option>` + bookings
        .filter((booking) => booking.status !== "CANCELED")
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

function fillCheckOutBookingSelect(container, bookings) {
    const select = container.querySelector("#bookingId");
    select.innerHTML = `<option value="">Selecione uma reserva em hospedagem</option>` + bookings
        .filter((booking) => booking.status === "IN_STAY")
        .map((booking) => `
          <option value="${escapeHtml(booking.id)}" data-guest-id="${escapeHtml(booking.guestId || "")}" data-guest="${escapeHtml(booking.guestName || "")}" data-room="${escapeHtml(booking.roomNumber || "")}" data-checkin="${escapeHtml(booking.checkInDate || "")}" data-checkout="${escapeHtml(booking.checkOutDate || "")}">
            #${escapeHtml(booking.id)} · ${escapeHtml(booking.guestName || "-")} · Qto ${escapeHtml(booking.roomNumber || "-")} · saída ${formatDate(booking.checkOutDate)}
          </option>
        `).join("");
}

async function updateCheckOutHistoryPreview(container, option, state) {
    const requestId = ++state.historyRequestId;
    const guestName = option?.dataset.guest || "-";
    const guestId = Number(option?.dataset.guestId);

    if (!option?.value) {
        setCheckOutHistoryContext(container, "Selecione uma reserva", "O histórico do hóspede será carregado sem alterar os valores mantidos pelo backend.");
        setCheckOutHistoryStats(container, null);
        return;
    }

    setCheckOutHistoryContext(
        container,
        guestName,
        [
            `Reserva #${option.value}`,
            option.dataset.room ? `Qto ${option.dataset.room}` : null,
            option.dataset.checkin && option.dataset.checkout
                ? `${formatFullDate(option.dataset.checkin)} a ${formatFullDate(option.dataset.checkout)}`
                : null,
        ].filter(Boolean).join(" · ")
    );
    setCheckOutHistoryStats(container, null, "Carregando...");

    if (!Number.isInteger(guestId) || guestId <= 0) {
        setCheckOutHistoryStats(container, null, "Indisponível");
        setAnnouncement(container, "A reserva selecionada não possui um hóspede válido para consulta de histórico.");
        return;
    }

    try {
        const guestResponse = await findGuestById(guestId);
        if (requestId !== state.historyRequestId) {
            return;
        }

        setCheckOutHistoryStats(container, guestResponse.data || {});
        setAnnouncement(container, `Histórico de ${guestName} carregado.`);
    } catch (error) {
        if (requestId !== state.historyRequestId) {
            return;
        }

        setCheckOutHistoryStats(container, null, "Indisponível");
        setAnnouncement(container, "Não foi possível carregar o histórico do hóspede.");
        showToast(container, "checkout", error.message || "Não foi possível carregar o histórico do hóspede.", "ti-alert-circle");
    }
}

async function loadCheckOutFinancialState(container, state, bookingId) {
    const requestId = ++state.financialRequestId;
    state.financialPlanId = null;
    state.scheduledFinancialComponent = null;
    if (!bookingId) {
        state.financialLoadState = "none";
        renderCheckOutFinancialState(container, { state: "none", message: "Selecione uma reserva para consultar o pagamento agendado." });
        return;
    }

    state.financialLoadState = "loading";
    renderCheckOutFinancialState(container, { state: "loading", message: "Carregando pagamento agendado..." });
    try {
        const planResponse = await findFinancialTransactionPlanByBookingId(bookingId);
        if (requestId !== state.financialRequestId) {
            return;
        }
        const planSummary = planResponse.data;
        if (!planSummary?.id) {
            state.financialLoadState = "none";
            renderCheckOutFinancialState(container, { state: "none", message: "Nenhum pagamento FTP está agendado para este checkout." });
            return;
        }
        state.financialPlanId = planSummary.id;
        const scheduledResponse = await findScheduledFinancialComponent(
                state.financialPlanId,
                "PLAN_CHECK_OUT_PAYMENT"
        );
        if (requestId !== state.financialRequestId) {
            return;
        }
        if (!scheduledResponse.data) {
            state.financialLoadState = "none";
            renderCheckOutFinancialState(container, { state: "none", message: "Nenhum pagamento FTP está agendado para este checkout." });
            return;
        }
        state.scheduledFinancialComponent = scheduledResponse.data;
        state.financialLoadState = isEligibleScheduledComponent(scheduledResponse.data) ? "eligible" : "completed";
        renderCheckOutFinancialState(container, {
            state: state.financialLoadState,
            component: scheduledResponse.data,
        });
    } catch (error) {
        if (requestId !== state.financialRequestId) {
            return;
        }
        state.financialLoadState = error.status === 409 ? "stale" : "failure";
        renderCheckOutFinancialState(container, {
            state: state.financialLoadState,
            message: error.status === 409
                    ? "O pagamento agendado mudou. Selecione a reserva novamente para atualizar."
                    : error.message || "Não foi possível carregar o pagamento agendado.",
        });
    }
}

function renderCheckOutFinancialState(container, financialState) {
    const stateContainer = container.querySelector("#checkout-financial-state");
    if (!stateContainer) {
        return;
    }
    stateContainer.dataset.state = financialState.state;
    if (financialState.state !== "eligible") {
        stateContainer.innerHTML = `<p class="checkin-financial-message" role="${financialState.state === "failure" || financialState.state === "stale" ? "alert" : "status"}">${escapeHtml(financialState.message || "")}</p>`;
        return;
    }

    const component = financialState.component;
    stateContainer.innerHTML = `
      <div class="checkin-financial-authoritative">
        <div><span>Finalidade</span><strong>Pagamento no checkout</strong></div>
        <div><span>Valor agendado</span><strong>${escapeHtml(formatMoney(component.amount))}</strong></div>
        <div><span>Vencimento</span><strong>${escapeHtml(formatDate(component.dueDate))}</strong></div>
        <div><span>Status</span><strong>${escapeHtml(financialStatusLabel(component.status))}</strong></div>
      </div>
      <div class="checkin-financial-controls">
        <label class="check-field"><span>Forma de pagamento</span><div><select id="checkoutPaymentMethod"><option value="">Selecione</option><option value="PIX">Pix</option><option value="CREDIT_CARD">Cartão de crédito</option><option value="DEBIT_CARD">Cartão de débito</option><option value="CASH">Dinheiro</option><option value="BANK_TRANSFER">Transferência bancária</option><option value="BOOKING">Booking</option><option value="AIRBNB">Airbnb</option></select></div></label>
        <fieldset class="checkin-payment-structure" aria-describedby="checkout-payment-structure-error">
          <legend>Estrutura do pagamento</legend>
          <label><input type="radio" name="checkoutPaymentStructure" value="SIMPLE" checked> Pagamento à vista</label>
          <label><input type="radio" name="checkoutPaymentStructure" value="INSTALLMENT"> Pagamento parcelado</label>
        </fieldset>
        <label id="checkout-installments-field" class="check-field" hidden><span>Quantidade de parcelas</span><div><select id="checkoutInstallments">${Array.from({ length: 11 }, (_, index) => `<option value="${index + 2}">${index + 2}x</option>`).join("")}</select></div><div id="checkout-installment-preview" class="checkin-installment-preview" aria-live="polite"></div></label>
        <p id="checkout-payment-structure-error" class="checkin-financial-error" role="alert"></p>
        <label class="check-switch checkin-financial-confirm"><span>Confirmo a materialização do pagamento agendado</span><input id="checkoutFinancialConfirmation" type="checkbox"></label>
      </div>
    `;
    bindCheckOutFinancialControls(container, component);
}

function bindCheckOutFinancialControls(container, component) {
    const update = () => {
        const installmentSelected = checkedValue(container, "checkoutPaymentStructure") === "INSTALLMENT";
        const installmentField = container.querySelector("#checkout-installments-field");
        installmentField.hidden = !installmentSelected;
        const previewList = calculateInstallmentPreview(
                toCents(String(component.amount)),
                Number(value(container, "checkoutInstallments")),
                component.dueDate
        );
        container.querySelector("#checkout-installment-preview").innerHTML = installmentSelected
                ? previewList.map((installment) => `<span>${installment.number}ª: ${formatCents(installment.amountCents)}</span>`).join("")
                : "";
    };
    container.querySelectorAll('input[name="checkoutPaymentStructure"]').forEach((input) => input.addEventListener("change", update));
    container.querySelector("#checkoutInstallments").addEventListener("change", update);
    update();
}

function validateCheckOutFinancialSubmission(state, container) {
    if (["loading", "failure", "stale"].includes(state.financialLoadState)) {
        return "Atualize o pagamento agendado antes de salvar o checkout.";
    }
    if (!isEligibleScheduledComponent(state.scheduledFinancialComponent)) {
        return null;
    }
    if (!value(container, "checkoutPaymentMethod")) {
        return "Selecione a forma de pagamento do checkout.";
    }
    if (!checked(container, "checkoutFinancialConfirmation")) {
        return "Confirme a finalidade, o valor e a estrutura do pagamento agendado.";
    }
    if (checkedValue(container, "checkoutPaymentStructure") === "INSTALLMENT") {
        const installmentsQuantity = Number(value(container, "checkoutInstallments"));
        if (!Number.isInteger(installmentsQuantity) || installmentsQuantity < 2 || installmentsQuantity > 12) {
            return "A quantidade de parcelas deve estar entre 2 e 12.";
        }
    }
    return null;
}

function buildCheckOutPaymentMaterialization(state, container) {
    if (!isEligibleScheduledComponent(state.scheduledFinancialComponent)) {
        return null;
    }
    const structure = checkedValue(container, "checkoutPaymentStructure");
    return {
        structure,
        method: value(container, "checkoutPaymentMethod"),
        installmentsQuantity: structure === "INSTALLMENT" ? Number(value(container, "checkoutInstallments")) : null,
        idempotencyKey: state.paymentCommandIdempotencyKey,
    };
}

async function recoverCheckOutFinancialCommand(state, originalError) {
    if (!state.financialPlanId || !state.paymentCommandIdempotencyKey
            || (originalError?.status && originalError.status < 500 && originalError.status !== 408)) {
        return { state: "unconfirmed", message: originalError?.message || "Não foi possível salvar o checkout." };
    }
    try {
        const response = await reconcileFinancialReplacement(state.financialPlanId, state.paymentCommandIdempotencyKey);
        return response.data ? { state: "committed", outcome: response.data } : {
            state: "unconfirmed",
            message: "O resultado do checkout ainda não foi confirmado. Tente novamente com a mesma operação.",
        };
    } catch (error) {
        return {
            state: "unconfirmed",
            message: error.status === 409
                    ? "O pagamento agendado mudou. Atualize a reserva antes de tentar novamente."
                    : "Não foi possível confirmar o resultado do checkout. A operação permanece disponível para recuperação.",
        };
    }
}

function renderCheckOutFinancialSuccess(container, outcome) {
    const component = outcome?.definitiveComponent;
    const financialTransactionPlan = outcome?.financialTransactionPlan;
    const stateContainer = container.querySelector("#checkout-financial-state");
    stateContainer.dataset.state = "success";
    stateContainer.innerHTML = `<p class="checkin-financial-message">Pagamento definitivo criado: ${escapeHtml(formatMoney(component?.amount))} · ${escapeHtml(financialStatusLabel(component?.status))}. FTP atualizado: ${escapeHtml(financialPlanStatusLabel(financialTransactionPlan?.status))}.</p>`;
}

function renderCheckOutFinancialError(container, message) {
    const stateContainer = container.querySelector("#checkout-financial-state");
    stateContainer.dataset.state = "failure";
    stateContainer.innerHTML = `<p class="checkin-financial-message" role="alert">${escapeHtml(message)}</p>`;
}

function setCheckOutHistoryContext(container, guestName, bookingDescription) {
    container.querySelector("#checkout-history-guest").textContent = guestName;
    container.querySelector("#checkout-history-booking").textContent = bookingDescription;
}

function setCheckOutHistoryStats(container, guest, fallback = "-") {
    container.querySelector("#checkout-history-stays").textContent = guest?.stayCount ?? fallback;
    container.querySelector("#checkout-history-last-stay").textContent = guest?.lastStayDate
        ? formatFullDate(guest.lastStayDate)
        : fallback;
}

const CHECK_OUT_RATING_CRITERION_LIST = Object.freeze([
    { fieldName: "checkInProcedureScore", label: "procedimento de check-in" },
    { fieldName: "checkOutProcedureScore", label: "procedimento de checkout" },
    { fieldName: "accommodationCleanlinessScore", label: "limpeza da acomodação" },
    { fieldName: "teamCommunicationScore", label: "comunicação da equipe" },
    { fieldName: "locationScore", label: "localização" },
    { fieldName: "comfortScore", label: "conforto" },
]);

export function buildCheckOutRatingControlsMarkup() {
    return `
        <section id="checkout-rating-section" class="checkout-rating-section" aria-labelledby="checkout-rating-title">
          <div class="checkout-rating-heading">
            <h3 id="checkout-rating-title">Avaliação do serviço</h3>
            <p>Obrigatória para concluir o checkout. Selecione de 1 a 5 estrelas em cada critério.</p>
          </div>
          <div class="checkout-rating-grid">
            ${CHECK_OUT_RATING_CRITERION_LIST.map(buildRatingGroupMarkup).join("")}
          </div>
          <label class="check-field checkout-rating-observations">
            <span>Observações da avaliação <small>(opcional)</small></span>
            <div><textarea id="ratingObservations" rows="4" maxlength="4000"></textarea></div>
          </label>
        </section>
    `;
}

function buildRatingGroupMarkup({ fieldName, label }) {
    const labelId = `${fieldName}-label`;
    const statusId = `${fieldName}-status`;
    return `
        <fieldset class="checkout-rating-group" data-rating-group="${fieldName}" aria-describedby="${statusId}">
          <legend id="${labelId}">${capitalize(label)}</legend>
          <div class="checkout-star-options">
            ${[1, 2, 3, 4, 5].map((score) => `
              <label class="checkout-star-option" data-score="${score}">
                <input type="radio" name="${fieldName}" value="${score}" aria-label="${score} de 5">
                <span aria-hidden="true">★</span>
              </label>
            `).join("")}
          </div>
          <output id="${statusId}" class="checkout-rating-status">Não respondido</output>
        </fieldset>
    `;
}

function bindCheckOutRatingControls(container) {
    container.querySelectorAll("[data-rating-group]").forEach((ratingGroup) => {
        ratingGroup.querySelectorAll('input[type="radio"]').forEach((radioInput) => {
            radioInput.addEventListener("change", () => {
                updateCheckOutRatingGroup(ratingGroup, Number(radioInput.value));
                ratingGroup.setAttribute("aria-invalid", "false");
            });
        });
    });
}

export function updateCheckOutRatingGroup(ratingGroup, selectedScore) {
    ratingGroup.querySelectorAll("[data-score]").forEach((starOption) => {
        starOption.classList.toggle("filled", Number(starOption.dataset.score) <= selectedScore);
    });
    const ratingStatus = ratingGroup.querySelector(".checkout-rating-status");
    if (ratingStatus) {
        ratingStatus.textContent = `${selectedScore} de 5`;
    }
}

function syncCheckOutRatingVisibility(container) {
    const ratingSection = container.querySelector("#checkout-rating-section");
    const completed = value(container, "status") === "COMPLETED";
    ratingSection.hidden = !completed;
    ratingSection.querySelectorAll("input, textarea").forEach((ratingControl) => {
        ratingControl.disabled = !completed;
    });
    if (!completed) {
        clearCheckOutRatingErrors(container);
    }
}

function collectCheckOutRating(container) {
    const rating = {};
    CHECK_OUT_RATING_CRITERION_LIST.forEach(({ fieldName }) => {
        const selectedScore = container.querySelector(`input[name="${fieldName}"]:checked`)?.value;
        rating[fieldName] = numberOrNull(selectedScore);
    });
    rating.observations = value(container, "ratingObservations") || null;
    return rating;
}

function clearCheckOutRatingErrors(container) {
    container.querySelectorAll("[data-rating-group], .checkout-rating-observations").forEach(
            (ratingControl) => ratingControl.setAttribute("aria-invalid", "false")
    );
}

function capitalize(text) {
    return text.charAt(0).toUpperCase() + text.slice(1);
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

function formatFullDate(date) {
    if (!date) return "-";
    const [year, month, day] = String(date).split("-");
    return year && month && day ? `${day}/${month}/${year}` : date;
}

function setAnnouncement(container, message) {
    const announcement = container.querySelector("#checkout-form-announcement");
    if (announcement) {
        announcement.textContent = message;
    }
}

function showToast(container, prefix, message, icon) {
    const toast = container.querySelector(`#${prefix}-form-toast`);
    toast.setAttribute("role", icon === "ti-alert-circle" ? "alert" : "status");
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
