import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const {
    buildCheckOutRatingControlsMarkup,
    collectCheckOutPayload,
    updateCheckOutRatingGroup,
    validateCheckOutRating,
} = await import("../js/views/checkOperationFormView.js?v=checkout-rating-stars-tests");

const checkOperationFormSource = fs.readFileSync(
    new URL("../js/views/checkOperationFormView.js", import.meta.url),
    "utf8"
);
const homeCssSource = fs.readFileSync(new URL("../css/home.css", import.meta.url), "utf8");
const checkInRenderSource = checkOperationFormSource.slice(
    checkOperationFormSource.indexOf("export function renderCheckInFormView"),
    checkOperationFormSource.indexOf("export function renderCheckOutFormView")
);
const checkOutRenderSource = checkOperationFormSource.slice(
    checkOperationFormSource.indexOf("export function renderCheckOutFormView"),
        checkOperationFormSource.indexOf("async function bindCheckInForm")
);
const checkOutFlowSource = checkOperationFormSource.slice(
    checkOperationFormSource.indexOf("async function bindCheckOutForm")
);

const completeRatingValueMap = {
    checkInProcedureScore: "1",
    checkOutProcedureScore: "2",
    accommodationCleanlinessScore: "3",
    teamCommunicationScore: "4",
    locationScore: "5",
    comfortScore: "4",
};

function createCheckoutContainer(ratingValueMap = completeRatingValueMap, options = {}) {
    const fieldValueMap = {
        bookingId: "42",
        actualCheckOutAt: "2026-08-12T11:30",
        extraCharges: "25.50",
        pendingAmount: "0",
        performedBy: "Recepção",
        notes: "Checkout concluído",
        ratingObservations: options.observations || "Atendimento excelente",
        status: options.status || "COMPLETED",
    };
    const checkboxFieldSet = new Set([
        "roomInspected",
        "keysReturned",
        "consumablesChecked",
        "pendingAmountPaid",
    ]);

    return {
        querySelector(selector) {
            const ratingSelectorMatch = selector.match(/^input\[name="([^"]+)"\]:checked$/);
            if (ratingSelectorMatch) {
                const selectedValue = ratingValueMap[ratingSelectorMatch[1]];
                return selectedValue == null ? null : { value: selectedValue };
            }

            const fieldName = selector.replace("#", "");
            return {
                checked: checkboxFieldSet.has(fieldName),
                value: fieldValueMap[fieldName] || "",
            };
        },
    };
}

test("six required empty star groups are rendered only by checkout", () => {
    const ratingMarkup = buildCheckOutRatingControlsMarkup();

    assert.doesNotMatch(checkInRenderSource, /Histórico e avaliação|checkout-rating|data-checkout-history-assessment/);
    assert.match(checkOutRenderSource, /data-checkout-history-assessment/);
    assert.match(checkOutRenderSource, /Histórico e avaliação/);
    assert.equal((ratingMarkup.match(/data-rating-group=/g) || []).length, 6);
    assert.equal((ratingMarkup.match(/type="radio"/g) || []).length, 30);
    assert.equal((ratingMarkup.match(/ checked/g) || []).length, 0);
    assert.match(ratingMarkup, /maxlength="4000"/);
    assert.doesNotMatch(ratingMarkup, /checkout-rating"|Sem avaliação/);
});

test("checkout payload sends the exact nested six-criterion rating", () => {
    const payload = collectCheckOutPayload(createCheckoutContainer());

    assert.equal(payload.bookingId, 42);
    assert.deepEqual(payload.rating, {
        checkInProcedureScore: 1,
        checkOutProcedureScore: 2,
        accommodationCleanlinessScore: 3,
        teamCommunicationScore: 4,
        locationScore: 5,
        comfortScore: 4,
        observations: "Atendimento excelente",
    });
    assert.equal(payload.extraCharges, 25.5);
    assert.equal(payload.pendingAmount, 0);
    ["stayCount", "totalSpent", "lastStayDate", "guestRating"].forEach(
        (historyMember) => assert.equal(Object.hasOwn(payload, historyMember), false)
    );

    const payloadWithoutRating = collectCheckOutPayload(
            createCheckoutContainer({}, { status: "PENDING" })
    );
    assert.equal(payloadWithoutRating.rating, null);
});

test("completed checkout validation identifies and names the first unanswered group", () => {
    assert.equal(validateCheckOutRating(null, "PENDING"), null);
    assert.equal(validateCheckOutRating(collectCheckOutPayload(createCheckoutContainer()).rating), null);

    const incompleteRating = collectCheckOutPayload(createCheckoutContainer()).rating;
    incompleteRating.accommodationCleanlinessScore = null;
    assert.deepEqual(validateCheckOutRating(incompleteRating), {
        fieldName: "accommodationCleanlinessScore",
        message: "Responda o critério limpeza da acomodação.",
    });

    const invalidRating = { ...incompleteRating, accommodationCleanlinessScore: 6 };
    assert.equal(validateCheckOutRating(invalidRating).fieldName, "accommodationCleanlinessScore");

    const longObservationRating = collectCheckOutPayload(createCheckoutContainer()).rating;
    longObservationRating.observations = "x".repeat(4001);
    assert.equal(validateCheckOutRating(longObservationRating).fieldName, "observations");
});

test("selecting star four fills four stars and exposes four of five", () => {
    const starOptionList = [1, 2, 3, 4, 5].map((score) => ({
        dataset: { score: String(score) },
        classList: {
            filled: false,
            toggle(_className, enabled) {
                this.filled = enabled;
            },
        },
    }));
    const ratingStatus = { textContent: "Não respondido" };
    const ratingGroup = {
        querySelectorAll: () => starOptionList,
        querySelector: () => ratingStatus,
    };

    updateCheckOutRatingGroup(ratingGroup, 4);

    assert.equal(starOptionList.filter((option) => option.classList.filled).length, 4);
    assert.equal(ratingStatus.textContent, "4 de 5");
});

test("star groups use native named radios with keyboard score names", () => {
    const ratingMarkup = buildCheckOutRatingControlsMarkup();
    Object.keys(completeRatingValueMap).forEach((fieldName) => {
        assert.equal((ratingMarkup.match(new RegExp(`name="${fieldName}"`, "g")) || []).length, 5);
    });
    [1, 2, 3, 4, 5].forEach((score) => {
        assert.match(ratingMarkup, new RegExp(`aria-label="${score} de 5"`));
    });
});

test("checkout preview reads backend guest history without calculating totals", () => {
    assert.match(checkOperationFormSource, /await findGuestById\(guestId\)/);
    assert.match(checkOperationFormSource, /guest\?\.stayCount/);
    assert.match(checkOperationFormSource, /guest\?\.lastStayDate/);
    assert.doesNotMatch(checkOperationFormSource, /guest\?\.rating|checkout-history-current-rating/);
    assert.doesNotMatch(checkOperationFormSource, /guest\?\.totalSpent|guest\.totalSpent/);
    assert.match(checkOperationFormSource, /requestId !== state\.historyRequestId/);
});

test("checkout prevents duplicate submission and preserves fields after failure", () => {
    assert.match(checkOperationFormSource, /if \(state\.submitting\) \{\s*return;/);
    assert.match(checkOperationFormSource, /state\.submitting = true;/);
    assert.match(checkOperationFormSource, /submitButton\.disabled = true;/);
    assert.match(checkOperationFormSource, /setAttribute\("aria-busy", "true"\)/);

    const failureSource = checkOutFlowSource.match(
        /catch \(error\) \{[\s\S]*?recoverCheckOutFinancialCommand\(state, error\)[\s\S]*?\} finally \{[\s\S]*?if \(!saved\) \{[\s\S]*?\n            \}/
    )?.[0];
    assert.ok(failureSource);
    assert.match(failureSource, /submitButton\.disabled = false/);
    assert.doesNotMatch(failureSource, /\.reset\(|setValue\(|\.value\s*=/);
});

test("checkout announces loading validation request and success outcomes", () => {
    assert.match(checkOutRenderSource, /id="checkout-form-announcement"[^>]*role="status"[^>]*aria-live="polite"/);
    assert.match(checkOperationFormSource, /setAnnouncement\(container, "Salvando checkout\."\)/);
    assert.match(checkOperationFormSource, /setAnnouncement\(container, "Checkout cadastrado com sucesso\."\)/);
    assert.match(checkOperationFormSource, /toast\.setAttribute\("role", icon === "ti-alert-circle" \? "alert" : "status"\)/);
    assert.match(checkOperationFormSource, /invalidControl\?\.focus\(\)/);
    assert.match(checkOperationFormSource, /setAnnouncement\(container, ratingValidation\.message\)/);
});

test("checkout rating controls collapse to one responsive column", () => {
    assert.match(homeCssSource, /\.checkout-rating-grid \{[\s\S]*?grid-template-columns: repeat\(2, minmax\(0, 1fr\)\)/);
    assert.match(homeCssSource, /\.checkout-rating-grid \{ grid-template-columns: 1fr; \}/);
    assert.match(homeCssSource, /\.checkout-star-option input:focus-visible \+ span/);
});

test("checkout rating cache version reaches the browser entry point", () => {
    const cacheVersion = "2026-08-20-ftp-checkout-materialization";
    const shellCacheVersion = "2026-08-17-financial-classification-removal";
    const operationsControllerSource = fs.readFileSync(
            new URL("../js/controllers/operationsController.js", import.meta.url),
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

    assert.match(operationsControllerSource, new RegExp(`checkOperationFormView\\.js\\?v=${cacheVersion}`));
    assert.match(uiControllerSource, new RegExp(`operationsController\\.js\\?v=${cacheVersion}`));
    assert.match(mainControllerSource, new RegExp(`UICOntroller\\.js\\?v=${shellCacheVersion}`));
    assert.match(indexSource, new RegExp(`main\\.js\\?v=${shellCacheVersion}`));
});
