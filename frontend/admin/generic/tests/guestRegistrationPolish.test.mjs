import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const {
    guestStatusBadgeClass,
    guestStatusLabel,
    normalizeGuestStatus,
} = await import("../js/guestStatus.js?v=guest-registration-polish-tests");
const { collectGuestPayload } = await import(
    "../js/views/guestFormView.js?v=guest-registration-polish-tests"
);

const guestFormSource = fs.readFileSync(
    new URL("../js/views/guestFormView.js", import.meta.url),
    "utf8"
);
const guestsSource = fs.readFileSync(
    new URL("../js/views/guestsView.js", import.meta.url),
    "utf8"
);
const guestProfileSource = fs.readFileSync(
    new URL("../js/views/guestProfileView.js", import.meta.url),
    "utf8"
);
const checkOperationFormSource = fs.readFileSync(
    new URL("../js/views/checkOperationFormView.js", import.meta.url),
    "utf8"
);
const homeCssSource = fs.readFileSync(
    new URL("../css/home.css", import.meta.url),
    "utf8"
);

test("guest registration has no editable status control", () => {
    assert.doesNotMatch(guestFormSource, /selectField\("status"/);
    assert.doesNotMatch(guestFormSource, /setFormValue\(container, "status"/);
    assert.match(guestFormSource, /status: "INACTIVE"/);
});

test("guest create and update payload never submit lifecycle status", () => {
    const fieldValueMap = {
        fullName: "Maria Lavanda",
        phone: "(35) 99999-9999",
        preferencesAndRestrictions: "Sem lactose\nQuarto silencioso",
        accessibilityNeeds: "Acesso sem degraus\nSinalização visível",
        status: "WITH_CONFIRMED_BOOKING",
    };
    const container = {
        querySelector(selector) {
            if (selector === "#guest-notes") {
                return { value: "Equipe" };
            }

            const fieldName = selector.replace("#guest-", "");
            return {
                checked: false,
                value: fieldValueMap[fieldName] || "",
            };
        },
    };

    const payload = collectGuestPayload(container, {
        status: "WITH_CONFIRMED_BOOKING",
    });

    assert.equal(payload.fullName, "Maria Lavanda");
    assert.equal(payload.phone, "(35) 99999-9999");
    assert.equal(
        payload.preferencesAndRestrictions,
        "Sem lactose\nQuarto silencioso"
    );
    assert.equal(
        payload.accessibilityNeeds,
        "Acesso sem degraus\nSinalização visível"
    );
    assert.equal(Object.hasOwn(payload, "status"), false);
});

test("guest registration contains no operational history or assessment input", () => {
    ["stayCount", "totalSpent", "lastStayDate", "data-rating", "guest-rating"].forEach(
        (historyMember) => assert.equal(guestFormSource.includes(historyMember), false)
    );

    const payload = collectGuestPayload({
        querySelector(selector) {
            if (selector === "#guest-notes") {
                return { value: "" };
            }
            return { value: "" };
        },
    }, { status: "INACTIVE" });

    ["stayCount", "totalSpent", "lastStayDate", "rating"].forEach(
        (historyMember) => assert.equal(Object.hasOwn(payload, historyMember), false)
    );
});

test("guest care block exposes exactly two accessible multiline fields", () => {
    const careSectionSource = guestFormSource.match(
        /<section class="guest-section" data-guest-care-fields>[\s\S]*?<\/section>/
    )?.[0];

    assert.ok(careSectionSource);
    assert.equal((careSectionSource.match(/textareaField\(/g) || []).length, 2);
    assert.match(careSectionSource, /"preferencesAndRestrictions", "Preferências e restrições"/);
    assert.match(careSectionSource, /"accessibilityNeeds", "Necessidades de acessibilidade"/);
    assert.doesNotMatch(careSectionSource, /selectField|switchField|type="checkbox"|<button/);
    assert.match(guestFormSource, /maxlength="4000" aria-describedby="\$\{describedBy\}"/);
});

test("guest care edit loading and payload use only the current textual contract", () => {
    assert.match(
        guestFormSource,
        /setFormValue\(container, "preferencesAndRestrictions", guest\.preferencesAndRestrictions\)/
    );
    assert.match(
        guestFormSource,
        /setFormValue\(container, "accessibilityNeeds", guest\.accessibilityNeeds\)/
    );

    [
        "travelsWithPets",
        "petType",
        "needsAccessibility",
        "favoriteRoom",
        "guest-pref-input",
        "guest-pref-add",
        "guest-pref-chips",
    ].forEach((obsoleteMember) => {
        assert.equal(guestFormSource.includes(obsoleteMember), false);
        assert.equal(guestProfileSource.includes(obsoleteMember), false);
    });
    assert.doesNotMatch(guestFormSource, /\bpreferences:/);
    assert.doesNotMatch(guestProfileSource, /guest\.preferences\b/);
});

test("guest care feedback is accessible and failed save does not clear textareas", () => {
    assert.match(
        guestFormSource,
        /id="guest-toast"[^>]*role="status"[^>]*aria-live="polite"[^>]*aria-atomic="true"/
    );
    assert.match(
        guestFormSource,
        /toast\.setAttribute\("role", icon === "ti-alert-circle" \? "alert" : "status"\)/
    );

    const saveFailureSource = guestFormSource.match(
        /catch \(error\) \{\s*showToast\(container, error\.message, "ti-alert-circle"\);\s*\} finally/
    )?.[0];
    assert.ok(saveFailureSource);
    assert.doesNotMatch(saveFailureSource, /\.value\s*=|setFormValue|reset\(/);
});

test("guest profile renders both care strings and preserves line breaks", () => {
    assert.match(guestProfileSource, /guest\.preferencesAndRestrictions/);
    assert.match(guestProfileSource, /guest\.accessibilityNeeds/);

    assert.match(homeCssSource, /\.guest-profile-care p[\s\S]*?white-space: pre-wrap/);
    assert.match(homeCssSource, /\.guest-care-fields \{ grid-template-columns: 1fr; \}/);
});

test("internal notes use one visible title and a symmetric accessible textarea", () => {
    const notesSectionSource = guestFormSource.match(
        /<section class="guest-section" data-guest-internal-notes>[\s\S]*?<\/section>/
    )?.[0];

    assert.ok(notesSectionSource);
    assert.equal((notesSectionSource.match(/Observações internas/g) || []).length, 2);
    assert.match(notesSectionSource, /<strong>Observações internas<\/strong>/);
    assert.match(notesSectionSource, /class="guest-internal-notes"/);
    assert.match(notesSectionSource, /id="guest-notes" aria-label="Observações internas"/);
    assert.doesNotMatch(notesSectionSource, /<span>Anotações?<\/span>|<label/);
    assert.match(homeCssSource, /\.guest-internal-notes \{[\s\S]*?width: 100%;[\s\S]*?padding: 18px;[\s\S]*?box-sizing: border-box;/);
    assert.match(homeCssSource, /\.guest-internal-notes textarea \{[\s\S]*?width: 100%;[\s\S]*?box-sizing: border-box;[\s\S]*?resize: vertical;/);
    assert.match(homeCssSource, /\.guest-internal-notes textarea:focus-visible \{[\s\S]*?box-shadow:/);
    assert.match(homeCssSource, /\.guest-internal-notes \{ padding: 16px; \}/);
});

test("polished guest flow requirements remain integrated", () => {
    const careSectionSource = guestFormSource.match(
        /<section class="guest-section" data-guest-care-fields>[\s\S]*?<\/section>/
    )?.[0];

    assert.doesNotMatch(guestFormSource, /selectField\("status"/);
    assert.equal((careSectionSource?.match(/textareaField\(/g) || []).length, 2);
    assert.doesNotMatch(guestFormSource, /stayCount|totalSpent|lastStayDate|guest-rating/);
    assert.doesNotMatch(guestFormSource, /Histórico e avaliação/);
    assert.doesNotMatch(guestProfileSource, /guest\.rating|formatRating|Avaliacao atual/);
    assert.match(
        checkOperationFormSource,
        /renderCheckOutFormView[\s\S]*?data-checkout-history-assessment[\s\S]*?Histórico e avaliação/
    );
});

test("guest and checkout payloads match their revised contracts", async () => {
    const { collectCheckOutPayload } = await import(
        "../js/views/checkOperationFormView.js?v=guest-registration-polish-contract-tests"
    );
    const fieldValueMap = {
        bookingId: "42",
        actualCheckOutAt: "2026-08-12T11:30",
        extraCharges: "25.5",
        pendingAmount: "0",
        performedBy: "Equipe",
        notes: "Chaves devolvidas",
        status: "COMPLETED",
    };
    const ratingValueMap = {
        checkInProcedureScore: "5",
        checkOutProcedureScore: "5",
        accommodationCleanlinessScore: "5",
        teamCommunicationScore: "5",
        locationScore: "5",
        comfortScore: "5",
    };
    const checkoutContainer = {
        querySelector(selector) {
            const ratingSelectorMatch = selector.match(/^input\[name="([^"]+)"\]:checked$/);
            if (ratingSelectorMatch) {
                return { value: ratingValueMap[ratingSelectorMatch[1]] || "" };
            }
            const fieldName = selector.replace("#", "");
            return {
                checked: ["roomInspected", "keysReturned"].includes(fieldName),
                value: fieldValueMap[fieldName] || "",
            };
        },
    };

    const checkoutPayload = collectCheckOutPayload(checkoutContainer);
    assert.deepEqual(Object.keys(checkoutPayload), [
        "bookingId",
        "actualCheckOutAt",
        "roomInspected",
        "keysReturned",
        "consumablesChecked",
        "pendingAmountPaid",
        "extraCharges",
        "pendingAmount",
        "performedBy",
        "notes",
        "rating",
        "paymentMaterialization",
        "status",
    ]);
    assert.deepEqual(checkoutPayload.rating, {
        checkInProcedureScore: 5,
        checkOutProcedureScore: 5,
        accommodationCleanlinessScore: 5,
        teamCommunicationScore: 5,
        locationScore: 5,
        comfortScore: 5,
        observations: null,
    });

    const guestPayload = collectGuestPayload({
        querySelector(selector) {
            return { value: selector === "#guest-notes" ? "Somente equipe" : "" };
        },
    }, { status: "IN_STAY" });
    assert.deepEqual(Object.keys(guestPayload), [
        "fullName",
        "documentNumber",
        "birthDate",
        "gender",
        "guestType",
        "phone",
        "email",
        "city",
        "state",
        "address",
        "preferencesAndRestrictions",
        "accessibilityNeeds",
        "originChannel",
        "notes",
    ]);
});

test("origin channel precedes care fields and contains no guest referral field", () => {
    const originSectionStart = guestFormSource.indexOf("data-guest-origin-channel");
    const careSectionStart = guestFormSource.indexOf("data-guest-care-fields");
    const originSectionSource = guestFormSource.match(
        /<section class="guest-section" data-guest-origin-channel>[\s\S]*?<\/section>/
    )?.[0];

    assert.ok(originSectionSource);
    assert.ok(originSectionStart >= 0 && originSectionStart < careSectionStart);
    assert.equal((originSectionSource.match(/selectField\(/g) || []).length, 1);
    assert.match(originSectionSource, /"originChannel", "Canal de origem"/);
    assert.doesNotMatch(originSectionSource, /referredBy|Indicado por/);
    assert.doesNotMatch(guestFormSource, /referredBy|Indicado por/);
    assert.doesNotMatch(guestProfileSource, /guest\.referredBy|Indicacao/);
});

test("guest form and rating cleanup use their current cache versions", () => {
    const guestFormCacheVersion = "2026-08-12-guest-origin-channel";
    const ratingCleanupCacheVersion = "2026-08-13-checkout-rating-stars";
    const guestHistoryCacheVersion = "2026-08-13-guest-rating-history";
    const shellCacheVersion = "2026-08-17-financial-classification-removal";
    const guestControllerSource = fs.readFileSync(
        new URL("../js/controllers/guestController.js", import.meta.url),
        "utf8"
    );
    const uiControllerSource = fs.readFileSync(
        new URL("../js/controllers/UICOntroller.js", import.meta.url),
        "utf8"
    );
    const mainControllerSource = fs.readFileSync(
        new URL("../js/controllers/main.js", import.meta.url),
        "utf8"
    );
    const indexSource = fs.readFileSync(new URL("../index.html", import.meta.url), "utf8");

    assert.match(guestControllerSource, new RegExp(`guestFormView\\.js\\?v=${guestFormCacheVersion}`));
    assert.match(guestControllerSource, new RegExp(`guestProfileView\\.js\\?v=${ratingCleanupCacheVersion}`));
    assert.match(uiControllerSource, new RegExp(`guestController\\.js\\?v=${guestHistoryCacheVersion}`));
    assert.match(mainControllerSource, new RegExp(`UICOntroller\\.js\\?v=${shellCacheVersion}`));
    assert.match(indexSource, new RegExp(`main\\.js\\?v=${shellCacheVersion}`));
});

test("guest status presentation uses the exact four authoritative values", () => {
    const expectedPresentationMap = {
        WITH_UNCONFIRMED_BOOKING: ["Com reserva não confirmada", "status-unconfirmed"],
        WITH_CONFIRMED_BOOKING: ["Com reserva confirmada", "status-confirmed"],
        IN_STAY: ["Em estadia", "status-stay"],
        INACTIVE: ["Inativo", "status-inactive"],
    };

    Object.entries(expectedPresentationMap).forEach(([status, presentation]) => {
        assert.equal(normalizeGuestStatus(status), status);
        assert.equal(guestStatusLabel(status), presentation[0]);
        assert.equal(guestStatusBadgeClass(status), presentation[1]);
    });
});

test("legacy response aliases are normalized only for presentation", () => {
    assert.equal(normalizeGuestStatus("IN_BOOKING"), "WITH_UNCONFIRMED_BOOKING");
    assert.equal(normalizeGuestStatus("COM_RESERVA"), "WITH_UNCONFIRMED_BOOKING");
    assert.equal(normalizeGuestStatus("EM_ESTADIA"), "IN_STAY");
    assert.equal(normalizeGuestStatus("GOT_CHECKOUT"), "INACTIVE");
});

test("guest list filters both booking states and trusts backend status", () => {
    assert.match(guestsSource, /value="WITH_UNCONFIRMED_BOOKING"/);
    assert.match(guestsSource, /value="WITH_CONFIRMED_BOOKING"/);
    assert.match(guestsSource, /value="IN_STAY"/);
    assert.match(guestsSource, /value="INACTIVE"/);
    assert.match(guestsSource, /guestStatusBadge\(guest\.status\)/);
    assert.match(guestsSource, /normalizeGuestStatus\(guest\.status\) === state\.status/);
    assert.doesNotMatch(guestsSource, /resolveGuestDisplayStatus|hasBookingWithStatus/);
});

test("guest profile trusts backend status instead of inferring it from bookings", () => {
    assert.match(guestProfileSource, /guestStatusBadge\(guest\.status\)/);
    assert.doesNotMatch(
        guestProfileSource,
        /activeBookingsInStay\.length > 0 \? "IN_STAY"/
    );
});
