import assert from "node:assert/strict";
import test from "node:test";

globalThis.location = { protocol: "http:", hostname: "localhost", port: "8080" };

const {
    ApiError,
    createPublicBooking,
    findCurrentPrivacyPolicy,
} = await import("../js/api.js?v=versioned-privacy-policy-tests");

test("current policy request uses the public endpoint without browser cache", async () => {
    let requestedUrl;
    let requestedOptions;
    globalThis.fetch = async (url, options) => {
        requestedUrl = url;
        requestedOptions = options;
        return jsonResponse({
            status: "success",
            data: policyResponse(),
        });
    };

    const policy = await findCurrentPrivacyPolicy();

    assert.equal(requestedUrl, "/public/privacy-policy");
    assert.equal(requestedOptions.cache, "no-store");
    assert.deepEqual(policy, policyResponse());
});

test("policy API preserves a controlled 503 unavailable response", async () => {
    globalThis.fetch = async () => jsonResponse({
        status: "error",
        message: "controlled-503",
        data: null,
    }, 503);

    await assert.rejects(
            () => findCurrentPrivacyPolicy(),
            (error) => error instanceof ApiError
                && error.status === 503
                && error.message === "controlled-503"
    );
});

test("booking API preserves a controlled 409 policy conflict", async () => {
    globalThis.fetch = async () => jsonResponse({
        status: "error",
        message: "controlled-409",
        data: null,
    }, 409);

    await assert.rejects(
            () => createPublicBooking({ privacyPolicyId: 42, privacyAccepted: true }),
            (error) => error instanceof ApiError
                && error.status === 409
                && error.message === "controlled-409"
    );
});

test("booking adapter sends the supplied transient acceptance payload", async () => {
    let requestedUrl;
    let requestedMethod;
    let requestBody;
    globalThis.fetch = async (url, options) => {
        requestedUrl = url;
        requestedMethod = options.method;
        requestBody = JSON.parse(options.body);
        return jsonResponse({ status: "success", data: { bookingCode: "CL-123" } });
    };

    await createPublicBooking({
        roomId: 1,
        guest: {
            email: "hospede@example.com",
        },
        privacyPolicyId: 42,
        privacyAccepted: true,
    });

    assert.equal(requestedUrl, "/public/bookings");
    assert.equal(requestedMethod, "POST");
    assert.equal(requestBody.privacyPolicyId, 42);
    assert.equal(requestBody.privacyAccepted, true);
    assert.equal(requestBody.guest.email, "hospede@example.com");
    assert.equal("ses" in requestBody, false);
    assert.equal("sender" in requestBody, false);
    assert.equal("managementRecipient" in requestBody, false);
    assert.equal("privacyPolicyVersion" in requestBody, false);
    assert.equal("contentHash" in requestBody, false);
});

function jsonResponse(payload, status = 200) {
    return new Response(JSON.stringify(payload), {
        status,
        headers: { "Content-Type": "application/json" },
    });
}

function policyResponse() {
    return {
        id: 42,
        version: 3,
        title: "Política vigente",
        content: JSON.stringify({
            schemaVersion: 1,
            sections: [{
                heading: "Introdução",
                nodes: [{ type: "paragraph", text: "Conteúdo" }],
            }],
        }),
        contentHash: `sha256:${"a".repeat(64)}`,
        effectiveAt: "2026-07-28T00:00:00",
    };
}
