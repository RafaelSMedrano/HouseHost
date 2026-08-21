import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

import {
    buildBookingPayload,
    isValidTransactionalEmail,
    normalizeTransactionalEmail,
    validateGuestData,
} from "../js/controllers/publicInteractions.js?v=public-booking-email-tests";

test("public reservation view collects an accessible bounded transactional email", () => {
    const reservationViewSource = fs.readFileSync(
            "frontend/public/cantinhoDasLavandas/js/views/reservaView.js",
            "utf8"
    );

    assert.match(reservationViewSource, /<label class="fl" for="f-email">Email/);
    assert.match(reservationViewSource, /type="email"[^>]*id="f-email"[^>]*required[^>]*maxlength="255"/);
    assert.match(reservationViewSource, /id="f-email"[^>]*autocomplete="email"[^>]*aria-describedby="ferr-email email-transactional-hint"/);
    assert.match(reservationViewSource, /comunicações transacionais[^<]*nunca para marketing/);
    assert.match(reservationViewSource, /Solicitação recebida!/);
    assert.match(reservationViewSource, /WhatsApp informado para confirmar disponibilidade e próximos passos/);
    assert.equal(reservationViewSource.includes("Reserva confirmada"), false);
});

test("transactional email validation matches the backend boundary and normalization", () => {
    assert.equal(normalizeTransactionalEmail("  Guest.Name@Example.COM  "), "guest.name@example.com");
    assert.equal(isValidTransactionalEmail("  Guest.Name@Example.COM  "), true);
    assert.equal(isValidTransactionalEmail(""), false);
    assert.equal(isValidTransactionalEmail("guest@example"), false);
    assert.equal(isValidTransactionalEmail(`guest@${"a".repeat(246)}.com`), false);
    assert.equal(isValidTransactionalEmail("guest@example.com\nBcc:other@example.com"), false);
});

test("guest validation blocks invalid email and exposes accessible feedback", () => {
    const documentReference = createBookingDocument("email-invalido");
    globalThis.document = documentReference;

    assert.equal(validateGuestData(), false);
    assert.equal(documentReference.element("f-email").attributeMap.get("aria-invalid"), "true");
    assert.equal(documentReference.element("ferr-email").classList.has("show"), true);

    documentReference.element("f-email").value = "Guest@Example.COM";
    assert.equal(validateGuestData(), true);
    assert.equal(documentReference.element("f-email").attributeMap.get("aria-invalid"), "false");
    assert.equal(documentReference.element("ferr-email").classList.has("show"), false);
    delete globalThis.document;
});

test("public booking payload contains only the normalized guest email contract", () => {
    const documentReference = createBookingDocument("  Guest@Example.COM  ");
    globalThis.document = documentReference;

    const bookingPayload = buildBookingPayload({
        id: 42,
        acknowledgedPolicyId: 42,
    });

    assert.equal(bookingPayload.guest.email, "guest@example.com");
    assert.equal("ses" in bookingPayload, false);
    assert.equal("sender" in bookingPayload, false);
    assert.equal("managementRecipient" in bookingPayload, false);
    assert.equal("provider" in bookingPayload, false);
    delete globalThis.document;
});

function createBookingDocument(email) {
    const elementMap = new Map();
    const register = (id, value = "") => {
        const element = new FakeElement(value);
        elementMap.set(id, element);
        return element;
    };

    register("f-nome", "Ana");
    register("f-sob", "Lavanda");
    register("f-tel", "(11) 99999-9999");
    register("f-email", email);
    register("f-cid", "Cunha - SP");
    register("f-obs", "Berço");
    register("ferr-nome");
    register("ferr-sob");
    register("ferr-tel");
    register("ferr-email");
    register("f-pet", "0");
    const guestSelect = register("f-hsp");
    guestSelect.selectedIndex = 0;
    guestSelect.options = [{ dataset: { adults: "2", children: "0" } }];

    return {
        element: (id) => elementMap.get(id),
        getElementById: (id) => elementMap.get(id) || null,
    };
}

class FakeElement {
    constructor(value) {
        this.value = value;
        this.attributeMap = new Map();
        this.classSet = new Set();
        this.classList = {
            add: (className) => this.classSet.add(className),
            remove: (className) => this.classSet.delete(className),
            toggle: (className, force) => {
                if (force) {
                    this.classSet.add(className);
                } else {
                    this.classSet.delete(className);
                }
            },
            has: (className) => this.classSet.has(className),
        };
    }

    setAttribute(name, value) {
        this.attributeMap.set(name, value);
    }
}
