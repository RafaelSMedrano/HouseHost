const API_BASE_URL = globalThis.HOUSEHOST_API_BASE_URL || resolveApiBaseUrl();


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

export async function login(email, password) {
    const response = await fetch(apiUrl("/auth/login"), {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: email,
            password: password
        })
    });

    const text = await response.text();
    return JSON.parse(text);
}

export async function registration(username, password, email, role, photoUrl) {
    const response = await fetch(apiUrl("/auth/registration"), {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password,
            email: email,
            role: role,
            photoUrl: photoUrl
        })
    });

    const text = await response.text();
    return JSON.parse(text);
}

export async function updateUserPhoto(id, photoUrl) {
    const response = await fetch(apiUrl(`/auth/users/${id}/photo`), {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            photoUrl: photoUrl
        })
    });

    return parseJsonResponse(response);
}

export async function updateUserProfile(id, profile) {
    const response = await fetch(apiUrl(`/auth/users/${id}`), {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(profile)
    });

    return parseJsonResponse(response);
}

export async function findQuickAccessUsers() {
    const response = await fetch(apiUrl("/auth/users/quick-access"));
    return parseJsonResponse(response);
}

export async function createGuest(guest) {
    const response = await fetch(apiUrl("/guests"), {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(guest)
    });

    return parseJsonResponse(response);
}

export async function updateGuest(id, guest) {
    const response = await fetch(apiUrl(`/guests/${id}`), {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(guest)
    });

    return parseJsonResponse(response);
}

export async function findGuestById(id) {
    const response = await fetch(apiUrl(`/guests/${id}`));
    return parseJsonResponse(response);
}

export async function findAllGuests() {
    const response = await fetch(apiUrl("/guests"));
    return parseJsonResponse(response);
}

export async function deleteGuest(id) {
    const response = await fetch(apiUrl(`/guests/${id}`), {
        method: "DELETE"
    });

    return parseJsonResponse(response);
}

export async function createBookingFromForm(booking) {
    const response = await fetch(apiUrl("/bookings/form"), {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(booking)
    });

    return parseJsonResponse(response);
}

export async function findAllBookings() {
    const response = await fetch(apiUrl("/bookings"));
    return parseJsonResponse(response);
}

export async function findBookingById(id) {
    const response = await fetch(apiUrl(`/bookings/${id}`));
    return parseJsonResponse(response);
}

export async function updateBooking(id, booking) {
    const response = await fetch(apiUrl(`/bookings/${id}`), {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(booking)
    });

    return parseJsonResponse(response);
}

export async function deleteBooking(id) {
    const response = await fetch(apiUrl(`/bookings/${id}`), {
        method: "DELETE"
    });

    return parseJsonResponse(response);
}

export async function findMetricsSummary() {
    const response = await fetch(apiUrl("/metrics/summary"));
    return parseJsonResponse(response);
}

export async function findAllFinancialTransactions() {
    const response = await fetch(apiUrl("/financial-transactions"));
    return parseJsonResponse(response);
}

export async function settleFinancialTransaction(id) {
    const response = await fetch(apiUrl(`/financial-transactions/${id}/settle`), {
        method: "PUT"
    });

    return parseJsonResponse(response);
}

export async function deleteFinancialTransaction(id) {
    const response = await fetch(apiUrl(`/financial-transactions/${id}`), {
        method: "DELETE"
    });

    return parseJsonResponse(response);
}

export async function findAllCashiers() {
    const response = await fetch(apiUrl("/cashiers"));
    return parseJsonResponse(response);
}

export async function findCashierEntriesByCashierId(cashierId) {
    const response = await fetch(apiUrl(`/cashier-entries/cashier/${cashierId}`));
    return parseJsonResponse(response);
}

export async function findCashierExpensesByCashierId(cashierId) {
    const response = await fetch(apiUrl(`/cashier-expenses/cashier/${cashierId}`));
    return parseJsonResponse(response);
}

export async function findAllStays() {
    const response = await fetch(apiUrl("/stays"));
    return parseJsonResponse(response);
}

export async function findAllRooms() {
    const response = await fetch(apiUrl("/rooms"));
    return parseJsonResponse(response);
}

export async function findRoomById(id) {
    const response = await fetch(apiUrl(`/rooms/${id}`));
    return parseJsonResponse(response);
}

export async function createRoom(room) {
    const response = await fetch(apiUrl("/rooms"), {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(room)
    });

    return parseJsonResponse(response);
}

export async function updateRoom(id, room) {
    const response = await fetch(apiUrl(`/rooms/${id}`), {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(room)
    });

    return parseJsonResponse(response);
}

export async function deleteRoom(id) {
    const response = await fetch(apiUrl(`/rooms/${id}`), {
        method: "DELETE"
    });

    return parseJsonResponse(response);
}

export async function findAllCheckIns() {
    const response = await fetch(apiUrl("/check-ins"));
    return parseJsonResponse(response);
}

export async function createCheckIn(checkIn) {
    const response = await fetch(apiUrl("/check-ins"), {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(checkIn)
    });

    return parseJsonResponse(response);
}

export async function updateCheckIn(id, checkIn) {
    const response = await fetch(apiUrl(`/check-ins/${id}`), {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(checkIn)
    });

    return parseJsonResponse(response);
}

export async function deleteCheckIn(id) {
    const response = await fetch(apiUrl(`/check-ins/${id}`), {
        method: "DELETE"
    });

    return parseJsonResponse(response);
}

export async function findAllCheckOuts() {
    const response = await fetch(apiUrl("/check-outs"));
    return parseJsonResponse(response);
}

export async function createCheckOut(checkOut) {
    const response = await fetch(apiUrl("/check-outs"), {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(checkOut)
    });

    return parseJsonResponse(response);
}

export async function updateCheckOut(id, checkOut) {
    const response = await fetch(apiUrl(`/check-outs/${id}`), {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(checkOut)
    });

    return parseJsonResponse(response);
}

export async function deleteCheckOut(id) {
    const response = await fetch(apiUrl(`/check-outs/${id}`), {
        method: "DELETE"
    });

    return parseJsonResponse(response);
}

async function parseJsonResponse(response) {
    const text = await response.text();
    const payload = text ? JSON.parse(text) : null;

    if (!response.ok) {
        throw new Error(payload?.message || "Erro ao comunicar com o servidor.");
    }

    return payload;
}
