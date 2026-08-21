import { createPublicBooking, findPublicRooms, quotePublicBooking } from "../api.js?v=2026-07-28-versioned-privacy-policy";
import {
    buildPrivacyAcceptancePayload,
    privacyPolicyController,
} from "./privacyPolicyController.js?v=2026-07-28-versioned-privacy-policy";
import {
    renderPrivacyPolicyDocument,
    renderPrivacyPolicyPageState,
} from "../views/politicaPrivacidadeView.js?v=2026-07-28-versioned-privacy-policy";
import { formatPrivacyPolicyEffectiveDate } from "../privacyPolicyDocument.js?v=2026-07-28-versioned-privacy-policy";

let reservationState = {
    roomId: null,
    quarto: "",
    rate: 0,
    ci: "",
    co: "",
    nights: 0,
    hsp: "2 adultos",
    total: 0,
    available: true,
};

let starsAnimation = null;
let quoteTimer = null;
let quoteRequestId = 0;
let privacyPolicyUnsubscribe = null;

export function installPublicGlobals(navigate) {
    window.goPage = navigate;
    window.toggleSidebar = toggleSidebar;
    window.closeSidebar = closeSidebar;
    window.toggleFaq = toggleFaq;
    window.submitContact = submitContact;
    window.rfInit = rfInit;
    window.pickRoom = pickRoom;
    window.updateSum = updateSum;
    window.rfGo = rfGo;
    window.toggleT = toggleT;
    window.retryPrivacyPolicy = retryPrivacyPolicy;
    window.finalizar = finalizar;
    window.showToast = showToast;
    window.mF = maskPhone;
    window.validateReservationEmail = validateReservationEmail;
}

export function initCursor() {
    const cursor = document.getElementById("cursor");
    const ring = document.getElementById("cursorRing");

    if (!cursor || !ring || cursor.dataset.bound === "true") {
        return;
    }

    cursor.dataset.bound = "true";
    document.addEventListener("mousemove", (event) => {
        cursor.style.left = `${event.clientX}px`;
        cursor.style.top = `${event.clientY}px`;
        ring.style.left = `${event.clientX}px`;
        ring.style.top = `${event.clientY}px`;
    });
}

export function initPageInteractions(pageId) {
    ensurePrivacyPolicySubscription();

    if (pageId === "home") {
        initStars();
    }

    if (pageId === "reserva") {
        rfInit();
        initReservationDates();
    }

    if (pageId === "privacidade") {
        renderPrivacyPolicyPageState(privacyPolicyController.getState(), retryPrivacyPolicy);
        privacyPolicyController.load({ force: true });
    }
}

export function isReservationSubmissionInProgress() {
    return privacyPolicyController.getState().submissionState === "submitting";
}

function ensurePrivacyPolicySubscription() {
    if (privacyPolicyUnsubscribe) {
        return;
    }

    privacyPolicyUnsubscribe = privacyPolicyController.subscribe((policyState) => {
        renderPrivacyPolicyPageState(policyState, retryPrivacyPolicy);
        renderReservationPrivacyPolicyState(policyState);
    });
}

export function renderReservationPrivacyPolicyState(
        policyState,
        policyController = privacyPolicyController
) {
    const status = document.getElementById("reservation-policy-status");
    const retryButton = document.getElementById("reservation-policy-retry");
    const details = document.getElementById("reservation-policy-details");
    const summary = document.getElementById("reservation-policy-summary");
    const documentContainer = document.getElementById("reservation-policy-document");
    const checkbox = document.getElementById("f-terms");
    const acknowledgement = document.getElementById("termsLbl");
    const submitButton = document.getElementById("reservation-submit");

    if (!status || !retryButton || !details || !summary || !documentContainer
            || !checkbox || !acknowledgement || !submitButton) {
        return;
    }

    const policyReady = policyState.loadState === "ready";
    const submissionLocked = ["submitting", "completed"].includes(policyState.submissionState);
    checkbox.checked = policyReady && policyState.acknowledgedPolicyId === policyState.id;
    checkbox.disabled = !policyReady || submissionLocked;
    acknowledgement.setAttribute("aria-disabled", String(checkbox.disabled));
    acknowledgement.classList.toggle("is-disabled", checkbox.disabled);
    acknowledgement.classList.toggle("is-checked", checkbox.checked);
    submitButton.disabled = !policyController.canSubmit();
    submitButton.textContent = policyState.submissionState === "submitting"
        ? "Enviando…"
        : "✓ Enviar solicitação";

    retryButton.hidden = policyState.loadState !== "unavailable";

    if (!policyReady) {
        details.hidden = true;
        documentContainer.replaceChildren();
        status.textContent = policyState.loadState === "unavailable"
            ? `${policyState.errorMessage} Tente novamente para continuar.`
            : "Carregando a política de privacidade vigente…";
        return;
    }

    const effectiveDate = formatPrivacyPolicyEffectiveDate(policyState.effectiveAt);
    details.hidden = false;
    summary.textContent = `Ler ${policyState.title} — versão ${policyState.version}, vigente desde ${effectiveDate}`;
    renderPrivacyPolicyDocument(documentContainer, policyState);

    if (policyState.policyChanged) {
        details.open = true;
        status.textContent = `A política foi atualizada para a versão ${policyState.version}. Leia a nova versão e confirme novamente.`;
        return;
    }

    if (policyState.submissionState === "submitting") {
        status.textContent = "Enviando sua solicitação com a política vigente confirmada…";
        return;
    }

    status.textContent = `Política vigente carregada: versão ${policyState.version}, desde ${effectiveDate}.`;
}

async function retryPrivacyPolicy() {
    const policyState = await privacyPolicyController.load({ force: true });
    const policyTitle = document.getElementById("privacy-policy-title");

    if (policyTitle) {
        const focusTarget = policyState.loadState === "ready"
            ? policyTitle
            : document.getElementById("privacy-policy-load-state");
        focusTarget?.focus();
        return;
    }

    if (policyState.loadState === "ready" && policyState.policyChanged) {
        openReservationPrivacyPolicy();
    }
    focusReservationPrivacyFeedback();
}

function openReservationPrivacyPolicy() {
    const details = document.getElementById("reservation-policy-details");
    if (details && !details.hidden) {
        details.open = true;
    }
}

export function focusReservationPrivacyFeedback() {
    document.getElementById("reservation-policy-status")?.focus();
}

function initStars() {
    const canvas = document.getElementById("starsCanvas");

    if (!canvas) {
        return;
    }

    if (starsAnimation) {
        cancelAnimationFrame(starsAnimation);
        starsAnimation = null;
    }

    const context = canvas.getContext("2d");
    const resize = () => {
        canvas.width = canvas.offsetWidth;
        canvas.height = canvas.offsetHeight;
    };
    const stars = Array.from({ length: 120 }, () => ({
        x: Math.random(),
        y: Math.random() * 0.6,
        r: Math.random() * 1.4 + 0.3,
        a: Math.random(),
        speed: Math.random() * 0.02 + 0.005,
    }));

    resize();
    window.addEventListener("resize", resize, { passive: true });

    function draw() {
        context.clearRect(0, 0, canvas.width, canvas.height);
        stars.forEach((star) => {
            star.a += star.speed;
            if (star.a > 1) {
                star.a -= 1;
            }

            const alpha = 0.2 + 0.6 * Math.abs(Math.sin(star.a * Math.PI));
            context.beginPath();
            context.arc(star.x * canvas.width, star.y * canvas.height, star.r, 0, Math.PI * 2);
            context.fillStyle = `rgba(255,248,230,${alpha})`;
            context.fill();
        });
        starsAnimation = requestAnimationFrame(draw);
    }

    draw();
}

function toggleSidebar() {
    document.getElementById("sidebar")?.classList.toggle("open");
}

function closeSidebar() {
    document.getElementById("sidebar")?.classList.remove("open");
}

function toggleFaq(question) {
    const item = question.closest(".faq-item");
    const isOpen = item.classList.contains("open");

    document.querySelectorAll(".faq-item").forEach((faqItem) => {
        faqItem.classList.remove("open");
        faqItem.querySelector(".faq-a").style.maxHeight = "0";
    });

    if (!isOpen) {
        item.classList.add("open");
        item.querySelector(".faq-a").style.maxHeight = "400px";
    }
}

function submitContact(event) {
    event.preventDefault();

    const button = event.target.querySelector(".cf-submit");
    button.textContent = "Enviando...";
    button.disabled = true;

    setTimeout(() => {
        button.textContent = "Enviar mensagem";
        button.disabled = false;
        showToast("Mensagem enviada com sucesso!");
        event.target.reset();
    }, 1400);
}

async function rfInit() {
    privacyPolicyController.resetReservationJourney();
    renderReservationPrivacyPolicyState(privacyPolicyController.getState());
    privacyPolicyController.load({ force: true });

    if (!reservationState.quarto) {
        reservationState.quarto = "Casa privativa";
        reservationState.rate = 580;
    }

    try {
        const rooms = await findPublicRooms();
        const room = rooms?.[0];
        if (room) {
            reservationState.roomId = room.id;
            reservationState.quarto = room.name || "Casa privativa";
            reservationState.rate = Number(room.baseNightlyRate) || reservationState.rate;
        }
    } catch (error) {
        showToast(error.message);
    }

    rfGo(1, true);
    updateSum();
}

function pickRoom(input) {
    document.querySelectorAll(".rp-card").forEach((card) => card.classList.remove("picked"));
    input.closest(".rp-card").classList.add("picked");
    reservationState.quarto = input.value;
    reservationState.rate = Number(input.dataset.price);
    document.getElementById("ferr-quarto")?.classList.remove("show");
    updateSum();
}

function updateSum() {
    const checkIn = document.getElementById("f-ci")?.value;
    const checkOut = document.getElementById("f-co")?.value;
    const guests = document.getElementById("f-hsp");

    reservationState.ci = checkIn || "";
    reservationState.co = checkOut || "";
    reservationState.nights = checkIn && checkOut
        ? Math.max(0, Math.round((new Date(checkOut) - new Date(checkIn)) / 86400000))
        : 0;
    reservationState.hsp = guests?.options[guests.selectedIndex]?.textContent || "2 adultos";
    reservationState.total = reservationState.rate * reservationState.nights;

    renderReservationSummary();
    scheduleQuote();
}

function renderReservationSummary() {
    setText("s-quarto", reservationState.quarto || "Casa privativa");
    setText("s-cap", reservationState.rate ? `R$ ${reservationState.rate}/noite` : "—");
    setText("s-ci", formatDate(reservationState.ci));
    setText("s-co", formatDate(reservationState.co));
    setText("s-noites", reservationState.nights > 0 ? `${reservationState.nights} noite${reservationState.nights > 1 ? "s" : ""}` : "—");
    setText("s-hsp", reservationState.hsp);
    setText("s-rate", reservationState.rate ? `R$ ${reservationState.rate.toLocaleString("pt-BR")}` : "—");
    setText("s-total", reservationState.total > 0 ? `R$ ${reservationState.total.toLocaleString("pt-BR")}` : "R$ —");
}

async function rfGo(step, reset = false) {
    if (!reset && !(await canMoveToStep(step))) {
        return;
    }

    document.querySelectorAll(".rf-step").forEach((element, index) => {
        element.classList.toggle("active", index === step - 1);
    });
    updateProgress(step);
    document.querySelector(".reserva-form-panel")?.scrollTo({ top: 0 });
}

async function canMoveToStep(step) {
    if (step === 2 && !reservationState.quarto) {
        document.getElementById("ferr-quarto")?.classList.add("show");
        return false;
    }

    if (step === 3) {
        const checkIn = document.getElementById("f-ci")?.value;
        const checkOut = document.getElementById("f-co")?.value;
        document.getElementById("ferr-ci")?.classList.toggle("show", !checkIn);
        document.getElementById("ferr-co")?.classList.toggle("show", !checkOut);

        if (!checkIn || !checkOut) {
            return false;
        }

        if (reservationState.nights <= 0) {
            showToast("Check-out deve ser após o check-in");
            return false;
        }

        await refreshQuote();

        if (!reservationState.available) {
            showToast("Casa indisponível no período informado");
            return false;
        }
    }

    if (step === 4) {
        return validateGuestData();
    }

    return true;
}

export function normalizeTransactionalEmail(value) {
    return String(value || "").trim().toLowerCase();
}

export function isValidTransactionalEmail(value) {
    const normalizedEmail = normalizeTransactionalEmail(value);
    return normalizedEmail.length > 0
        && normalizedEmail.length <= 255
        && !normalizedEmail.includes("\r")
        && !normalizedEmail.includes("\n")
        && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail);
}

export function validateReservationEmail() {
    const emailInput = document.getElementById("f-email");
    const emailValid = isValidTransactionalEmail(emailInput?.value || "");

    emailInput?.setAttribute("aria-invalid", String(!emailValid));
    document.getElementById("ferr-email")?.classList.toggle("show", !emailValid);
    return emailValid;
}

export function validateGuestData() {
    const validName = (value) => {
        const normalizedValue = value.trim().replace(/\s+/g, " ");
        return normalizedValue.length >= 2
            && normalizedValue.length <= 80
            && /^[\p{L}][\p{L} .'-]*$/u.test(normalizedValue);
    };
    const validPhone = (value) => {
        const digits = value.replace(/\D/g, "");
        return digits.length === 10 || digits.length === 11;
    };
    const fields = [
        ["f-nome", "ferr-nome", validName],
        ["f-sob", "ferr-sob", validName],
        ["f-tel", "ferr-tel", validPhone],
        ["f-email", "ferr-email", isValidTransactionalEmail],
    ];
    let valid = true;

    fields.forEach(([fieldId, errorId, validator]) => {
        const fieldValid = validator(document.getElementById(fieldId)?.value || "");
        document.getElementById(errorId)?.classList.toggle("show", !fieldValid);
        document.getElementById(fieldId)?.setAttribute("aria-invalid", String(!fieldValid));
        valid = valid && fieldValid;
    });

    if (!valid) {
        showToast("Preencha todos os campos obrigatórios");
    }

    return valid;
}

function updateProgress(step) {
    for (let index = 1; index <= 5; index += 1) {
        const dot = document.getElementById(`pd${index}`);
        if (!dot) {
            continue;
        }

        dot.classList.remove("active", "done");
        if (index < step) {
            dot.classList.add("done");
        }
        if (index === step) {
            dot.classList.add("active");
        }
    }
}

function toggleT() {
    const checkbox = document.getElementById("f-terms");

    if (!checkbox) {
        return;
    }

    privacyPolicyController.setAcknowledged(checkbox.checked);

    if (checkbox.checked) {
        document.getElementById("ferr-terms")?.classList.remove("show");
    }
}

async function finalizar() {
    const policyState = privacyPolicyController.getState();

    if (policyState.submissionState === "submitting" || policyState.submissionState === "completed") {
        return;
    }

    if (!validateGuestData()) {
        await rfGo(3, true);
        document.querySelector('[aria-invalid="true"]')?.focus();
        return;
    }

    if (policyState.loadState !== "ready") {
        showToast("A política de privacidade precisa estar disponível antes do envio");
        focusReservationPrivacyFeedback();
        return;
    }

    if (!privacyPolicyController.canSubmit()) {
        document.getElementById("ferr-terms")?.classList.add("show");
        showToast("Leia e confirme a política vigente para enviar");
        document.getElementById("f-terms")?.focus();
        return;
    }

    try {
        const submissionResult = await privacyPolicyController.submit(async (currentPolicyState) => {
            const bookingPayload = buildBookingPayload(currentPolicyState);
            const confirmationSnapshot = buildReservationConfirmationSnapshot(bookingPayload);
            const quote = await refreshQuote();

            if (!quote?.available) {
                throw new Error("Casa indisponível no período informado");
            }

            const booking = await createPublicBooking(bookingPayload);
            return {
                booking,
                confirmationSnapshot: {
                    ...confirmationSnapshot,
                    nights: Number(quote.nights) || confirmationSnapshot.nights,
                    total: Number(quote.total) || confirmationSnapshot.total,
                },
            };
        });

        if (submissionResult.status === "blocked") {
            return;
        }

        if (submissionResult.status === "conflict") {
            showToast("A política foi atualizada. Leia a nova versão antes de continuar.");
            openReservationPrivacyPolicy();
            focusReservationPrivacyFeedback();
            return;
        }

        const { booking, confirmationSnapshot } = submissionResult.data;
        setText("cf-nome", confirmationSnapshot.firstName);
        setText("cf-code", booking.bookingCode);
        setText("cf-quarto", confirmationSnapshot.quarto);
        setText("cf-ci", formatDate(confirmationSnapshot.checkIn));
        setText("cf-co", formatDate(confirmationSnapshot.checkOut));
        setText("cf-noites", `${confirmationSnapshot.nights} noite${confirmationSnapshot.nights > 1 ? "s" : ""}`);
        setText("cf-hsp", confirmationSnapshot.guests);
        setText("cf-total", `R$ ${Number(booking.total || confirmationSnapshot.total).toLocaleString("pt-BR")}`);
        await rfGo(5);
        showToast("Solicitação recebida!");
    } catch (error) {
        showToast(error.message);
    }
}

function initReservationDates() {
    const today = new Date();
    const checkIn = new Date(today);
    const checkOut = new Date(today);
    checkIn.setDate(checkIn.getDate() + 1);
    checkOut.setDate(checkOut.getDate() + 3);

    const checkInInput = document.getElementById("f-ci");
    const checkOutInput = document.getElementById("f-co");

    if (checkInInput) {
        checkInInput.min = inputDate(checkIn);
        checkInInput.value = inputDate(checkIn);
    }
    if (checkOutInput) {
        checkOutInput.min = inputDate(checkOut);
        checkOutInput.value = inputDate(checkOut);
    }

    updateSum();
}

function scheduleQuote() {
    clearTimeout(quoteTimer);

    if (!reservationState.ci || !reservationState.co || reservationState.nights <= 0) {
        return;
    }

    quoteTimer = setTimeout(() => {
        refreshQuote().catch((error) => showToast(error.message));
    }, 350);
}

async function refreshQuote() {
    if (!reservationState.ci || !reservationState.co || reservationState.nights <= 0) {
        return null;
    }

    const requestId = quoteRequestId + 1;
    quoteRequestId = requestId;
    const composition = selectedGuestComposition();

    const quote = await quotePublicBooking({
        roomId: reservationState.roomId,
        checkIn: reservationState.ci,
        checkOut: reservationState.co,
        adults: composition.adults,
        children: composition.children,
        pets: selectedPetCount(),
    });

    if (requestId !== quoteRequestId) {
        return quote;
    }

    reservationState.roomId = quote.roomId || reservationState.roomId;
    reservationState.rate = Number(quote.nightlyRate) || reservationState.rate;
    reservationState.nights = Number(quote.nights) || reservationState.nights;
    reservationState.total = Number(quote.total) || 0;
    reservationState.available = Boolean(quote.available);
    renderReservationSummary();
    return quote;
}

export function buildBookingPayload(policyState) {
    const composition = selectedGuestComposition();
    return {
        roomId: reservationState.roomId,
        checkIn: reservationState.ci,
        checkOut: reservationState.co,
        adults: composition.adults,
        children: composition.children,
        pets: selectedPetCount(),
        ...buildPrivacyAcceptancePayload(policyState),
        termsVersion: "2026-06-04-public-pre-reserva",
        guest: {
            firstName: document.getElementById("f-nome")?.value || "",
            lastName: document.getElementById("f-sob")?.value || "",
            email: normalizeTransactionalEmail(document.getElementById("f-email")?.value || ""),
            phone: document.getElementById("f-tel")?.value || "",
            city: document.getElementById("f-cid")?.value || "",
        },
        notes: document.getElementById("f-obs")?.value || "",
    };
}

function buildReservationConfirmationSnapshot(bookingPayload) {
    return {
        firstName: bookingPayload.guest.firstName,
        quarto: reservationState.quarto,
        checkIn: bookingPayload.checkIn,
        checkOut: bookingPayload.checkOut,
        nights: reservationState.nights,
        guests: reservationState.hsp,
        total: reservationState.total,
    };
}

function selectedGuestComposition() {
    const guestSelect = document.getElementById("f-hsp");
    const selectedOption = guestSelect?.options[guestSelect.selectedIndex];
    return {
        adults: Number(selectedOption?.dataset.adults || 2),
        children: Number(selectedOption?.dataset.children || 0),
    };
}

function selectedPetCount() {
    return Number(document.getElementById("f-pet")?.value || 0);
}

function maskPhone(element) {
    let value = element.value.replace(/\D/g, "").substring(0, 11);
    value = value.length <= 10
        ? value.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{4})(\d)/, "$1-$2")
        : value.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{5})(\d)/, "$1-$2");
    element.value = value;
}

function showToast(message) {
    const toast = document.getElementById("toast");

    if (!toast) {
        return;
    }

    setText("toastMsg", message);
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 3200);
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value;
    }
}

function formatDate(value) {
    if (!value) {
        return "—";
    }

    const [year, month, day] = value.split("-");
    return `${day}/${month}/${year}`;
}

function inputDate(date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}
