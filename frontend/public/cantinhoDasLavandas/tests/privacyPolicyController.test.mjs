import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

import {
    buildPrivacyAcceptancePayload,
    createPrivacyPolicyController,
} from "../js/controllers/privacyPolicyController.js";
import {
    focusReservationPrivacyFeedback,
    renderReservationPrivacyPolicyState,
} from "../js/controllers/publicInteractions.js";

test("loading failure blocks acknowledgement until a successful retry", async () => {
    let attempt = 0;
    const controller = createPrivacyPolicyController(async () => {
        attempt += 1;
        if (attempt === 1) {
            throw new Error("internal detail must not leak");
        }
        return policyResponse(2, 2);
    });

    const failedState = await controller.load();
    controller.setAcknowledged(true);

    assert.equal(failedState.loadState, "unavailable");
    assert.equal(failedState.errorMessage.includes("internal detail"), false);
    assert.equal(controller.canSubmit(), false);

    const readyState = await controller.load({ force: true });
    assert.equal(readyState.loadState, "ready");
    assert.equal(readyState.id, 2);
    assert.equal(readyState.contentHash.startsWith("sha256:"), true);
    assert.equal(controller.canSubmit(), false);

    controller.setAcknowledged(true);
    assert.equal(controller.canSubmit(), true);
    assert.deepEqual(buildPrivacyAcceptancePayload(controller.getState()), {
        privacyPolicyId: 2,
        privacyAccepted: true,
    });
});

test("concurrent submission attempts execute only one booking request", async () => {
    const controller = createPrivacyPolicyController(async () => policyResponse(3, 3));
    await controller.load();
    controller.setAcknowledged(true);

    let finishSubmission;
    let submissionCount = 0;
    const firstSubmission = controller.submit(async () => {
        submissionCount += 1;
        return new Promise((resolve) => {
            finishSubmission = resolve;
        });
    });
    assert.equal(controller.resetReservationJourney(), false);
    const secondSubmission = await controller.submit(async () => {
        submissionCount += 1;
        return { bookingCode: "duplicate" };
    });

    assert.equal(secondSubmission.status, "blocked");
    assert.equal(submissionCount, 1);

    finishSubmission({ bookingCode: "CL-001" });
    const firstResult = await firstSubmission;
    assert.equal(firstResult.status, "success");
    assert.equal(controller.getState().submissionState, "completed");
    assert.equal(controller.resetReservationJourney(), true);
    assert.equal(controller.getState().submissionState, "idle");
});

test("a slower obsolete policy response cannot replace the latest retry", async () => {
    let finishFirstRequest;
    let requestCount = 0;
    const controller = createPrivacyPolicyController(async () => {
        requestCount += 1;
        if (requestCount === 1) {
            return new Promise((resolve) => {
                finishFirstRequest = resolve;
            });
        }
        return policyResponse(8, 8);
    });

    const firstRequest = controller.load();
    const latestState = await controller.load({ force: true });
    finishFirstRequest(policyResponse(7, 7));
    await firstRequest;

    assert.equal(latestState.id, 8);
    assert.equal(controller.getState().id, 8);
});

test("409 reloads the policy and clears acknowledgement before resubmission", async () => {
    const policyResponseList = [policyResponse(4, 4), policyResponse(5, 5)];
    const controller = createPrivacyPolicyController(async () => policyResponseList.shift());

    await controller.load();
    controller.setAcknowledged(true);
    const conflictResult = await controller.submit(async () => {
        const error = new Error("updated");
        error.status = 409;
        throw error;
    });

    assert.equal(conflictResult.status, "conflict");
    assert.equal(controller.getState().id, 5);
    assert.equal(controller.getState().policyChanged, true);
    assert.equal(controller.getState().acknowledgedPolicyId, null);
    assert.equal(controller.canSubmit(), false);

    const blockedRetry = await controller.submit(async () => ({ bookingCode: "must-not-run" }));
    assert.equal(blockedRetry.status, "blocked");

    controller.setAcknowledged(true);
    const successfulRetry = await controller.submit(async (policyState) => ({
        bookingCode: "CL-NEW",
        policyId: policyState.id,
    }));
    assert.equal(successfulRetry.status, "success");
    assert.equal(successfulRetry.data.policyId, 5);
});

test("a 409 followed by policy reload failure remains unavailable and blocked", async () => {
    let requestCount = 0;
    const controller = createPrivacyPolicyController(async () => {
        requestCount += 1;
        if (requestCount === 1) {
            return policyResponse(6, 6);
        }
        throw new Error("offline");
    });

    await controller.load();
    controller.setAcknowledged(true);
    const conflictResult = await controller.submit(async () => {
        const error = new Error("updated");
        error.status = 409;
        throw error;
    });

    assert.equal(conflictResult.status, "conflict");
    assert.equal(controller.getState().loadState, "unavailable");
    assert.equal(controller.getState().policyChanged, true);
    assert.equal(controller.canSubmit(), false);
});

test("reservation DOM states block submission, expose retry and preserve filled fields after conflict", async () => {
    const policyResponseList = [policyResponse(9, 9), policyResponse(10, 10)];
    const controller = createPrivacyPolicyController(async () => policyResponseList.shift());
    const documentReference = createReservationDocument();
    globalThis.document = documentReference;

    const loadingState = { ...controller.getState(), loadState: "loading" };
    renderReservationPrivacyPolicyState(loadingState, controller);
    assert.equal(documentReference.element("f-terms").disabled, true);
    assert.equal(documentReference.element("reservation-submit").disabled, true);
    assert.equal(documentReference.element("reservation-policy-retry").hidden, true);

    const unavailableState = {
        ...loadingState,
        loadState: "unavailable",
        errorMessage: "Não foi possível carregar a política de privacidade vigente.",
    };
    renderReservationPrivacyPolicyState(unavailableState, controller);
    assert.equal(documentReference.element("reservation-policy-retry").hidden, false);

    await controller.load();
    renderReservationPrivacyPolicyState(controller.getState(), controller);
    assert.equal(documentReference.element("f-terms").disabled, false);
    assert.equal(documentReference.element("reservation-submit").disabled, true);

    controller.setAcknowledged(true);
    renderReservationPrivacyPolicyState(controller.getState(), controller);
    assert.equal(documentReference.element("reservation-submit").disabled, false);

    await controller.submit(async () => {
        const error = new Error("updated");
        error.status = 409;
        throw error;
    });
    renderReservationPrivacyPolicyState(controller.getState(), controller);

    assert.equal(documentReference.element("f-nome").value, "Ana");
    assert.equal(documentReference.element("f-tel").value, "(11) 99999-9999");
    assert.equal(documentReference.element("f-obs").value, "Berço");
    assert.equal(documentReference.element("f-terms").checked, false);
    assert.equal(documentReference.element("reservation-submit").disabled, true);
    assert.equal(documentReference.element("reservation-policy-details").open, true);
    assert.match(documentReference.element("reservation-policy-status").textContent, /versão 10/);

    focusReservationPrivacyFeedback();
    assert.equal(documentReference.element("reservation-policy-status").focused, true);
    delete globalThis.document;
});

test("frontend policy flow has no browser persistence, console logging or client policy version", () => {
    const sourceFileList = findJavaScriptFileList("frontend/public/cantinhoDasLavandas/js");
    const source = sourceFileList.map((file) => fs.readFileSync(file, "utf8")).join("\n");

    assert.equal(/localStorage|sessionStorage|console\./.test(source), false);
    assert.equal(source.includes("privacyPolicyVersion"), false);
    assert.equal(source.includes("privacyPolicyId"), true);
});

test("reservation acknowledgement starts disabled and exposes accessible status and retry controls", () => {
    const reservationViewSource = fs.readFileSync(
            "frontend/public/cantinhoDasLavandas/js/views/reservaView.js",
            "utf8"
    );
    const policyViewSource = fs.readFileSync(
            "frontend/public/cantinhoDasLavandas/js/views/politicaPrivacidadeView.js",
            "utf8"
    );

    assert.match(reservationViewSource, /type="checkbox" id="f-terms"[^>]*disabled/);
    assert.match(reservationViewSource, /id="reservation-submit"[^>]*disabled/);
    assert.match(reservationViewSource, /id="reservation-policy-status"[^>]*aria-live="polite"/);
    assert.match(reservationViewSource, /id="reservation-policy-retry"[^>]*type="button"/);
    assert.equal(reservationViewSource.includes("display:none"), false);
    assert.equal(policyViewSource.includes("67.277.911/0001-31"), false);
    assert.equal(policyViewSource.includes("Versão 2"), false);
    assert.equal(policyViewSource.includes("O Refúgio Cantinho das Lavandas respeita sua privacidade"), false);
});

function policyResponse(id, version) {
    return {
        id,
        version,
        title: `Política ${version}`,
        content: JSON.stringify({
            schemaVersion: 1,
            sections: [{
                heading: "Introdução",
                nodes: [{ type: "paragraph", text: "Conteúdo vigente" }],
            }],
        }),
        contentHash: `sha256:${String(version).repeat(64).slice(0, 64)}`,
        effectiveAt: "2026-07-28T00:00:00",
    };
}

function findJavaScriptFileList(directory) {
    return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
        const path = `${directory}/${entry.name}`;
        if (entry.isDirectory()) {
            return findJavaScriptFileList(path);
        }
        return entry.isFile() && entry.name.endsWith(".js") ? [path] : [];
    });
}

function createReservationDocument() {
    const documentReference = new FakeDocument();
    [
        "reservation-policy-status",
        "reservation-policy-retry",
        "reservation-policy-details",
        "reservation-policy-summary",
        "reservation-policy-document",
        "f-terms",
        "termsLbl",
        "reservation-submit",
        "f-nome",
        "f-email",
        "f-tel",
        "f-obs",
    ].forEach((id) => documentReference.register(id));
    documentReference.element("f-nome").value = "Ana";
    documentReference.element("f-email").value = "ana@example.com";
    documentReference.element("f-tel").value = "(11) 99999-9999";
    documentReference.element("f-obs").value = "Berço";
    return documentReference;
}

class FakeDocument {
    constructor() {
        this.elementMap = new Map();
    }

    register(id) {
        const element = new FakeElement("DIV");
        element.id = id;
        this.elementMap.set(id, element);
    }

    element(id) {
        return this.elementMap.get(id);
    }

    getElementById(id) {
        return this.element(id) || null;
    }

    createDocumentFragment() {
        return new FakeElement("FRAGMENT");
    }

    createElement(tagName) {
        return new FakeElement(tagName.toUpperCase());
    }
}

class FakeElement {
    constructor(tagName) {
        this.tagName = tagName;
        this.children = [];
        this.attributeMap = new Map();
        this.className = "";
        this.textContent = "";
        this.hidden = false;
        this.open = false;
        this.checked = false;
        this.disabled = false;
        this.focused = false;
        this.value = "";
        this.classList = {
            toggle: () => {},
        };
    }

    append(...elementList) {
        elementList.forEach((element) => {
            if (element.tagName === "FRAGMENT") {
                this.children.push(...element.children);
            } else {
                this.children.push(element);
            }
        });
    }

    replaceChildren(...elementList) {
        this.children = [];
        this.append(...elementList);
    }

    setAttribute(name, value) {
        this.attributeMap.set(name, value);
    }

    focus() {
        this.focused = true;
    }
}
