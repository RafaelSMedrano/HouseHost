const API_BASE_URL = globalThis.HOUSEHOST_API_BASE_URL || resolveApiBaseUrl();
const AUTH_TOKEN_KEY = "househost_token";
const AUTH_USER_KEY = "househost_user";

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

    return parseJsonResponse(response);
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
    return apiRequest("/guests", {
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

export function findGuestById(id) {
    return apiRequest(`/guests/${id}`);
}

export function findAllGuests() {
    return apiRequest("/guests");
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

export function findAllStays() {
    return apiRequest("/stays");
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

async function parseJsonResponse(response) {
    const text = await response.text();
    const payload = text ? JSON.parse(text) : null;

    if (response.status === 401) {
        clearAuthSession();
        throw new Error(payload?.message || "Sessao expirada. Faca login novamente.");
    }

    if (!response.ok) {
        throw new Error(payload?.message || "Erro ao comunicar com o servidor.");
    }

    return payload;
}
