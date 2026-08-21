const API_BASE_URL = globalThis.HOUSEHOST_API_BASE_URL || resolveApiBaseUrl();

export class ApiError extends Error {
    constructor(message, status) {
        super(message);
        this.name = "ApiError";
        this.status = status;
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

async function apiRequest(path, options = {}) {
    const body = options.body;
    const response = await fetch(apiUrl(path), {
        ...options,
        headers: {
            ...(body ? { "Content-Type": "application/json" } : {}),
            ...options.headers,
        },
    });

    return parseJsonResponse(response);
}

export function findPublicRooms() {
    return apiRequest("/public/rooms");
}

export function findCurrentPrivacyPolicy() {
    return apiRequest("/public/privacy-policy", {
        cache: "no-store",
    });
}

export function quotePublicBooking(quote) {
    return apiRequest("/public/quote", {
        method: "POST",
        body: JSON.stringify(quote),
    });
}

export function createPublicBooking(booking) {
    return apiRequest("/public/bookings", {
        method: "POST",
        body: JSON.stringify(booking),
    });
}

async function parseJsonResponse(response) {
    const text = await response.text();
    let payload = null;

    if (text) {
        try {
            payload = JSON.parse(text);
        } catch {
            throw new ApiError("O servidor retornou uma resposta inválida.", response.status);
        }
    }

    if (!response.ok || payload?.status === "error") {
        throw new ApiError(
                payload?.message || "Não foi possível comunicar com o servidor.",
                response.status
        );
    }

    return payload?.data ?? payload;
}
