const API_BASE_URL = globalThis.HOUSEHOST_API_BASE_URL || resolveApiBaseUrl();
const AUTH_TOKEN_KEY = "househost_token";
const AUTH_USER_KEY = "househost_user";

export class ApiError extends Error {
    constructor(status, message, retryAfterSeconds = null) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}

function resolveApiBaseUrl() {
    const { protocol, hostname, port } = globalThis.location || {};
    const isLocalHost = hostname === "localhost" || hostname === "127.0.0.1";

    if (protocol === "file:" || (isLocalHost && port !== "8080")) {
        return "http://localhost:8080";
    }

    return "";
}

function apiUrl(path) {
    return `${API_BASE_URL}${path}`;
}

async function apiRequest(path, options = {}, requestOptions = {}) {
    const { auth = true } = requestOptions;
    const token = getAuthToken();
    const body = options.body;
    const headers = {
        ...(body && !(body instanceof FormData) ? { "Content-Type": "application/json" } : {}),
        ...(auth && token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
    };

    const response = await fetch(apiUrl(path), {
        ...options,
        headers,
    });

    return parseJsonResponse(response, {
        authenticatedRequest: auth,
        authToken: auth ? token : null,
    });
}

export function saveAuthSession(session) {
    if (!session?.token) {
        clearAuthSession();
        return;
    }

    localStorage.setItem(AUTH_TOKEN_KEY, session.token);
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(stripTokenData(session)));
}

export function clearAuthSession() {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    localStorage.removeItem(AUTH_USER_KEY);
}

export function getAuthToken() {
    return localStorage.getItem(AUTH_TOKEN_KEY);
}

export function getStoredUser() {
    if (!getAuthToken()) {
        clearAuthSession();
        return null;
    }
    const rawUser = localStorage.getItem(AUTH_USER_KEY);
    if (!rawUser) {
        return null;
    }

    try {
        return JSON.parse(rawUser);
    } catch (error) {
        clearAuthSession();
        return null;
    }
}

function stripTokenData(session) {
    const { token, tokenType, expiresIn, ...user } = session;
    return user;
}

export async function login(email, password) {
    return apiRequest("/auth/login", {
        method: "POST",
        body: JSON.stringify({
            email: email,
            password: password,
        }),
    }, { auth: false });
}

export async function registration(username, password, email, role, photoUrl) {
    return apiRequest("/auth/registration", {
        method: "POST",
        body: JSON.stringify({
            username: username,
            password: password,
            email: email,
            role: role,
            photoUrl: photoUrl,
        }),
    }, { auth: false });
}

export function updateUserPhoto(id, photoUrl) {
    return apiRequest(`/auth/users/${id}/photo`, {
        method: "PUT",
        body: JSON.stringify({
            photoUrl: photoUrl,
        }),
    });
}

export function updateUserProfile(id, profile) {
    return apiRequest(`/auth/users/${id}`, {
        method: "PUT",
        body: JSON.stringify(profile),
    });
}

export function findQuickAccessUsers() {
    return apiRequest("/auth/users/quick-access", {}, { auth: false });
}

export function createGuest(guest) {
    return apiRequest("/guests/register", {
        method: "POST",
        body: JSON.stringify(guest),
    });
}

export function updateGuest(id, guest) {
    return apiRequest(`/guests/${id}`, {
        method: "PUT",
        body: JSON.stringify(guest),
    });
}

export function findGuestById(id, masked = true) {
    return apiRequest(`/guests/${id}?masked=${masked}`);
}

export function findGuestByIdForEdit(id) {
    return apiRequest(`/guests/${id}/edit`);
}

export function revealGuestContact(id) {
    return apiRequest(`/guests/${id}/contact`);
}

export function findAllGuests(masked = true) {
    return apiRequest(`/guests?masked=${masked}`);
}

export function findGuestsByName(name) {
    return apiRequest(`/guests/search/name?value=${encodeURIComponent(name)}`);
}

export function findGuestsByDocumentNumber(documentNumber) {
    return apiRequest(`/guests/search/document?value=${encodeURIComponent(documentNumber)}`);
}

export function findGuestsByEmail(email) {
    return apiRequest(`/guests/search/email?value=${encodeURIComponent(email)}`);
}

export function findGuestsByPhone(phone) {
    return apiRequest(`/guests/search/phone?value=${encodeURIComponent(phone)}`);
}

export function findGuestsByCity(city) {
    return apiRequest(`/guests/search/city?value=${encodeURIComponent(city)}`);
}

export function deleteGuest(id) {
    return apiRequest(`/guests/${id}`, {
        method: "DELETE",
    });
}

export function createBookingFromForm(booking) {
    return apiRequest("/bookings/form", {
        method: "POST",
        body: JSON.stringify(booking),
    });
}

export function findAllBookings() {
    return apiRequest("/bookings");
}

export function findBookingById(id) {
    return apiRequest(`/bookings/${id}`);
}

export function updateBooking(id, booking) {
    return apiRequest(`/bookings/${id}`, {
        method: "PUT",
        body: JSON.stringify(booking),
    });
}

export function deleteBooking(id) {
    return apiRequest(`/bookings/${id}`, {
        method: "DELETE",
    });
}

export function findMetricsSummary() {
    return apiRequest("/metrics/summary");
}

export function findAllFinancialTransactions() {
    return apiRequest("/financial-transactions");
}

export function findFinancialTransactionById(id) {
    return apiRequest(`/financial-transactions/${id}`);
}

export function settleFinancialTransaction(id) {
    return apiRequest(`/financial-transactions/${id}/settle`, {
        method: "PUT",
    });
}

export function deleteFinancialTransaction(id) {
    return apiRequest(`/financial-transactions/${id}`, {
        method: "DELETE",
    });
}

export function findAllCashiers() {
    return apiRequest("/cashiers");
}

export function findCashierEntriesByCashierId(cashierId) {
    return apiRequest(`/cashier-entries/cashier/${cashierId}`);
}

export function findCashierExpensesByCashierId(cashierId) {
    return apiRequest(`/cashier-expenses/cashier/${cashierId}`);
}

export function findAllRooms() {
    return apiRequest("/rooms");
}

export function findRoomById(id) {
    return apiRequest(`/rooms/${id}`);
}

export function createRoom(room) {
    return apiRequest("/rooms", {
        method: "POST",
        body: JSON.stringify(room),
    });
}

export function updateRoom(id, room) {
    return apiRequest(`/rooms/${id}`, {
        method: "PUT",
        body: JSON.stringify(room),
    });
}

export function deleteRoom(id) {
    return apiRequest(`/rooms/${id}`, {
        method: "DELETE",
    });
}

export function findAllCheckIns() {
    return apiRequest("/check-ins");
}

export function createCheckIn(checkIn) {
    return apiRequest("/check-ins", {
        method: "POST",
        body: JSON.stringify(checkIn),
    });
}

export function updateCheckIn(id, checkIn) {
    return apiRequest(`/check-ins/${id}`, {
        method: "PUT",
        body: JSON.stringify(checkIn),
    });
}

export function deleteCheckIn(id) {
    return apiRequest(`/check-ins/${id}`, {
        method: "DELETE",
    });
}

export function findAllCheckOuts() {
    return apiRequest("/check-outs");
}

export function createCheckOut(checkOut) {
    return apiRequest("/check-outs", {
        method: "POST",
        body: JSON.stringify(checkOut),
    });
}

export function updateCheckOut(id, checkOut) {
    return apiRequest(`/check-outs/${id}`, {
        method: "PUT",
        body: JSON.stringify(checkOut),
    });
}

export function deleteCheckOut(id) {
    return apiRequest(`/check-outs/${id}`, {
        method: "DELETE",
    });
}

export function createSupplier(supplier) {
    return apiRequest("/suppliers", { method: "POST", body: JSON.stringify(supplier) });
}

export function findAllSuppliers(filters = {}) {
    const searchParameters = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && String(value).trim() !== "") {
            searchParameters.set(key, value);
        }
    });
    const query = searchParameters.toString();
    return apiRequest(`/suppliers${query ? `?${query}` : ""}`);
}

export function findSupplierById(id) {
    return apiRequest(`/suppliers/${id}`);
}

export function updateSupplier(id, supplier) {
    return apiRequest(`/suppliers/${id}`, { method: "PUT", body: JSON.stringify(supplier) });
}

export function changeSupplierStatus(id, status) {
    return apiRequest(`/suppliers/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
    });
}

export function reviewSupplierRelationship(supplierId, relationshipId, review) {
    return apiRequest(`/suppliers/${supplierId}/relationships/${relationshipId}/review`, {
        method: "POST",
        body: JSON.stringify(review),
    });
}

export function findAllDataProcessingOperations(filters = {}) {
    const searchParameters = new URLSearchParams();
    if (filters.status) {
        searchParameters.set("status", filters.status);
    }
    const query = searchParameters.toString();
    return apiRequest(`/data-processing-operations${query ? `?${query}` : ""}`);
}

export function findDataProcessingOperationById(operationId) {
    return apiRequest(`/data-processing-operations/${encodePathIdentifier(operationId)}`);
}

export function findLegalBasisAssessmentsByOperation(operationId) {
    return apiRequest(
            `/data-processing-operations/${encodePathIdentifier(operationId)}/legal-basis-assessments`
    );
}

export function findLegalBasisAssessmentById(assessmentId) {
    return apiRequest(`/legal-basis-assessments/${encodePathIdentifier(assessmentId)}`);
}

export function createLegalBasisAssessmentDraft(operationId, assessment) {
    return apiRequest(`/data-processing-operations/${encodePathIdentifier(operationId)}/legal-basis-assessments`, {
        method: "POST",
        body: JSON.stringify(assessment),
    });
}

export function updateLegalBasisAssessmentDraft(assessmentId, assessment) {
    return apiRequest(`/legal-basis-assessments/${encodePathIdentifier(assessmentId)}`, {
        method: "PUT",
        body: JSON.stringify(assessment),
    });
}

export function submitLegalBasisAssessment(assessmentId) {
    return apiRequest(`/legal-basis-assessments/${encodePathIdentifier(assessmentId)}/submit`, { method: "POST" });
}

export function approveLegalBasisAssessment(assessmentId) {
    return apiRequest(`/legal-basis-assessments/${encodePathIdentifier(assessmentId)}/approve`, { method: "POST" });
}

export function rejectLegalBasisAssessment(assessmentId, reason) {
    return apiRequest(`/legal-basis-assessments/${encodePathIdentifier(assessmentId)}/reject`, {
        method: "POST",
        body: JSON.stringify({ reason }),
    });
}

export function createLegalBasisAssessmentRevision(assessmentId) {
    return apiRequest(`/legal-basis-assessments/${encodePathIdentifier(assessmentId)}/revisions`, { method: "POST" });
}

function encodePathIdentifier(identifier) {
    return encodeURIComponent(String(identifier));
}

async function parseJsonResponse(response, { authenticatedRequest = true, authToken = null } = {}) {
    const text = await response.text();
    const payload = parseResponsePayload(text);
    const retryAfterSeconds = parseRetryAfterSeconds(response.headers.get("Retry-After"));

    if (response.status === 401) {
        const ownsCurrentSession = authenticatedRequest
                && Boolean(authToken)
                && getAuthToken() === authToken;

        if (!authenticatedRequest || ownsCurrentSession) {
            clearAuthSession();
        }
        if (ownsCurrentSession) {
            globalThis.dispatchEvent?.(new Event("househost:session-expired"));
        }
        throw new ApiError(
                response.status,
                payload?.message || "Sessao expirada. Faca login novamente.",
                retryAfterSeconds
        );
    }

    if (!response.ok) {
        throw new ApiError(
                response.status,
                payload?.message || "Erro ao comunicar com o servidor.",
                retryAfterSeconds
        );
    }

    return payload;
}

function parseResponsePayload(text) {
    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch (error) {
        return null;
    }
}

function parseRetryAfterSeconds(retryAfterHeader) {
    const normalizedRetryAfter = retryAfterHeader?.trim();
    if (!normalizedRetryAfter || !/^\d+$/.test(normalizedRetryAfter)) {
        return null;
    }

    const retryAfterSeconds = Number(normalizedRetryAfter);
    return Number.isSafeInteger(retryAfterSeconds) ? retryAfterSeconds : null;
}
