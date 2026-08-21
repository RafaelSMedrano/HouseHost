const RATING_COLUMN_LIST = Object.freeze([
    ["checkInProcedureScore", "Check-in"],
    ["checkOutProcedureScore", "Checkout"],
    ["accommodationCleanlinessScore", "Limpeza"],
    ["teamCommunicationScore", "Comunicação"],
    ["locationScore", "Localização"],
    ["comfortScore", "Conforto"],
]);

export function renderRatingsLoadingView(containerId) {
    const container = document.getElementById(containerId);
    if (!container) {
        return false;
    }

    container.innerHTML = rootMarkup(`
        <div class="ratings-state" role="status" aria-live="polite">
            <i class="ti ti-loader-2 spinning" aria-hidden="true"></i>
            <p>Carregando avaliações...</p>
        </div>
    `);
    return true;
}

export function renderRatingsView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    if (!container) {
        return false;
    }

    const ratingSummaryList = newestFirstRatingSummaryList(
            options.pageData?.ratingSummaryDTOList
    );
    if (ratingSummaryList.length === 0) {
        container.innerHTML = rootMarkup(`
            <div class="ratings-state" role="status">
                <i class="ti ti-stars-off" aria-hidden="true"></i>
                <p>Nenhuma avaliação encontrada.</p>
            </div>
        `);
        return true;
    }

    const page = boundedInteger(options.pageData?.page, 0);
    const totalPages = boundedInteger(options.pageData?.totalPages, 1);
    const totalElements = boundedInteger(options.pageData?.totalElements, ratingSummaryList.length);
    container.innerHTML = rootMarkup(`
        <div class="ratings-table-region" role="region" aria-label="Avaliações de hospedagem" tabindex="0">
            <table class="ratings-table">
                <thead>
                    <tr>
                        <th scope="col">Hóspede</th>
                        <th scope="col">Reserva</th>
                        <th scope="col">Avaliada em</th>
                        ${RATING_COLUMN_LIST.map(([, label]) => `<th scope="col">${label}</th>`).join("")}
                        <th scope="col">Observações</th>
                    </tr>
                </thead>
                <tbody>
                    ${ratingSummaryList.map(buildRatingRowMarkup).join("")}
                </tbody>
            </table>
        </div>
        ${buildPaginationMarkup(page, totalPages, totalElements)}
    `);

    bindRatingsActions(container, options);
    return true;
}

export function renderRatingsErrorView(containerId) {
    const container = document.getElementById(containerId);
    if (!container) {
        return false;
    }

    container.innerHTML = rootMarkup(`
        <div class="ratings-state error-message" role="alert">
            <i class="ti ti-alert-circle" aria-hidden="true"></i>
            <p>Não foi possível carregar as avaliações.</p>
        </div>
    `);
    return true;
}

export function buildReadOnlyStarsMarkup(score) {
    const normalizedScore = boundedInteger(score, 0, 5);
    return `
        <span class="read-only-stars" aria-label="${normalizedScore} de 5">
            <span aria-hidden="true">${[1, 2, 3, 4, 5]
                    .map((position) => `<span class="read-only-star${position <= normalizedScore ? " filled" : ""}">★</span>`)
                    .join("")}</span>
            <span class="read-only-score">${normalizedScore} de 5</span>
        </span>
    `;
}

export function buildRatingRowMarkup(ratingSummary) {
    const bookingId = recordIdOrEmpty(ratingSummary?.bookingId);
    const guestId = recordIdOrEmpty(ratingSummary?.guestId);
    const guestName = escapeHtml(ratingSummary?.guestName || "Hóspede não identificado");
    const bookingLabel = bookingId
            ? `Reserva #${escapeHtml(bookingId)}`
            : "Reserva não identificada";
    const stayDates = formatStayDates(
            ratingSummary?.bookingCheckInDate,
            ratingSummary?.bookingCheckOutDate
    );

    return `
        <tr>
            <td>
                ${guestId
                        ? `<a class="ratings-related-link" href="#" data-open-guest="${escapeHtml(guestId)}" aria-label="Abrir perfil de ${guestName}">${guestName}</a>`
                        : `<span>${guestName}</span>`}
            </td>
            <td>
                ${bookingId
                        ? `<a class="ratings-related-link" href="#" data-open-booking="${escapeHtml(bookingId)}" aria-label="Abrir ${bookingLabel}">${bookingLabel}</a>`
                        : `<span>${bookingLabel}</span>`}
                <small>${stayDates}</small>
            </td>
            <td><time datetime="${escapeHtml(ratingSummary?.evaluatedAt || "")}">${formatDateTime(ratingSummary?.evaluatedAt)}</time></td>
            ${RATING_COLUMN_LIST.map(([fieldName]) => `<td>${buildReadOnlyStarsMarkup(ratingSummary?.[fieldName])}</td>`).join("")}
            <td class="ratings-observations">${escapeHtml(ratingSummary?.observations || "Sem observações.")}</td>
        </tr>
    `;
}

function bindRatingsActions(container, options) {
    container.querySelectorAll("[data-open-guest]").forEach((guestButton) => {
        guestButton.addEventListener("click", (event) => {
            event.preventDefault();
            options.onOpenGuest?.(guestButton.dataset.openGuest);
        });
    });
    container.querySelectorAll("[data-open-booking]").forEach((bookingButton) => {
        bookingButton.addEventListener("click", (event) => {
            event.preventDefault();
            options.onOpenBooking?.(bookingButton.dataset.openBooking);
        });
    });
    container.querySelectorAll("[data-ratings-page]").forEach((pageButton) => {
        pageButton.addEventListener("click", () => {
            options.onPageChange?.(Number(pageButton.dataset.ratingsPage));
        });
    });
}

function buildPaginationMarkup(page, totalPages, totalElements) {
    const currentPage = Math.min(page, Math.max(totalPages - 1, 0));
    return `
        <nav class="ratings-pagination" aria-label="Paginação de avaliações">
            <span>${totalElements} avaliação${totalElements === 1 ? "" : "ões"}</span>
            <div>
                <button type="button" data-ratings-page="${Math.max(currentPage - 1, 0)}" ${currentPage === 0 ? "disabled" : ""} aria-label="Página anterior">
                    <i class="ti ti-chevron-left" aria-hidden="true"></i>
                </button>
                <span>Página ${currentPage + 1} de ${Math.max(totalPages, 1)}</span>
                <button type="button" data-ratings-page="${Math.min(currentPage + 1, Math.max(totalPages - 1, 0))}" ${currentPage >= totalPages - 1 ? "disabled" : ""} aria-label="Próxima página">
                    <i class="ti ti-chevron-right" aria-hidden="true"></i>
                </button>
            </div>
        </nav>
    `;
}

function newestFirstRatingSummaryList(ratingSummaryList) {
    if (!Array.isArray(ratingSummaryList)) {
        return [];
    }
    return [...ratingSummaryList].sort((leftRatingSummary, rightRatingSummary) =>
        String(rightRatingSummary?.evaluatedAt || "").localeCompare(
                String(leftRatingSummary?.evaluatedAt || "")
        )
    );
}

function formatStayDates(checkInDate, checkOutDate) {
    if (!checkInDate && !checkOutDate) {
        return "Período indisponível";
    }
    return `${formatDate(checkInDate)} a ${formatDate(checkOutDate)}`;
}

function formatDateTime(value) {
    if (!value) {
        return "Data indisponível";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return escapeHtml(value);
    }
    return new Intl.DateTimeFormat("pt-BR", {
        dateStyle: "short",
        timeStyle: "short",
    }).format(date);
}

function formatDate(value) {
    if (!value) {
        return "-";
    }
    const [year, month, day] = String(value).split("-");
    return year && month && day ? `${day}/${month}/${year}` : escapeHtml(value);
}

function recordIdOrEmpty(value) {
    const recordId = Number(value);
    return Number.isSafeInteger(recordId) && recordId > 0 ? String(recordId) : "";
}

function boundedInteger(value, fallback, maximum = Number.MAX_SAFE_INTEGER) {
    const normalizedValue = Number(value);
    return Number.isSafeInteger(normalizedValue)
            && normalizedValue >= 0
            && normalizedValue <= maximum
        ? normalizedValue
        : fallback;
}

function rootMarkup(contentMarkup) {
    return `
        <section class="content ratings-page" aria-labelledby="ratings-root-title">
            <header class="ratings-heading">
                <div>
                    <span>Experiência de hospedagem</span>
                    <h2 id="ratings-root-title" tabindex="-1">Avaliações de hospedagem</h2>
                </div>
                <i class="ti ti-stars" aria-hidden="true"></i>
            </header>
            ${contentMarkup}
        </section>
    `;
}

function escapeHtml(value) {
    return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
}
