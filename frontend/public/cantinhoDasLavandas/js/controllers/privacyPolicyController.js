import { findCurrentPrivacyPolicy } from "../api.js?v=2026-07-28-versioned-privacy-policy";
import { normalizePrivacyPolicyResponse } from "../privacyPolicyDocument.js?v=2026-07-28-versioned-privacy-policy";

const EMPTY_POLICY_STATE = Object.freeze({
    id: null,
    version: null,
    title: "",
    content: null,
    contentHash: "",
    effectiveAt: "",
});

export function createPrivacyPolicyController(loadPrivacyPolicy = findCurrentPrivacyPolicy) {
    let policyState = {
        ...EMPTY_POLICY_STATE,
        loadState: "idle",
        submissionState: "idle",
        acknowledgedPolicyId: null,
        policyChanged: false,
        errorMessage: "",
    };
    let policyRequestSequence = 0;
    let activePolicyRequestPromise = null;
    const listenerSet = new Set();

    function getState() {
        return { ...policyState };
    }

    function subscribe(listener) {
        listenerSet.add(listener);
        listener(getState());

        return () => listenerSet.delete(listener);
    }

    async function load({ force = false } = {}) {
        if (activePolicyRequestPromise && !force) {
            return activePolicyRequestPromise;
        }

        const requestSequence = policyRequestSequence + 1;
        policyRequestSequence = requestSequence;
        updateState({
            ...EMPTY_POLICY_STATE,
            loadState: "loading",
            acknowledgedPolicyId: null,
            errorMessage: "",
        });

        const policyRequestPromise = resolvePolicyRequest(requestSequence);
        activePolicyRequestPromise = policyRequestPromise;
        return policyRequestPromise;
    }

    async function resolvePolicyRequest(requestSequence) {
        try {
            const loadedPolicy = normalizePrivacyPolicyResponse(await loadPrivacyPolicy());
            if (requestSequence !== policyRequestSequence) {
                return getState();
            }

            updateState({
                ...loadedPolicy,
                loadState: "ready",
                errorMessage: "",
            });
        } catch {
            if (requestSequence === policyRequestSequence) {
                updateState({
                    ...EMPTY_POLICY_STATE,
                    loadState: "unavailable",
                    acknowledgedPolicyId: null,
                    errorMessage: "Não foi possível carregar a política de privacidade vigente.",
                });
            }
        } finally {
            if (requestSequence === policyRequestSequence) {
                activePolicyRequestPromise = null;
            }
        }

        return getState();
    }

    function resetReservationJourney() {
        if (policyState.submissionState === "submitting") {
            return false;
        }

        updateState({
            submissionState: "idle",
            acknowledgedPolicyId: null,
            policyChanged: false,
        });
        return true;
    }

    function setAcknowledged(acknowledged) {
        const acknowledgedPolicyId = acknowledged && policyState.loadState === "ready"
            ? policyState.id
            : null;
        updateState({
            acknowledgedPolicyId,
            policyChanged: acknowledgedPolicyId ? false : policyState.policyChanged,
        });
    }

    function canSubmit() {
        return policyState.loadState === "ready"
            && policyState.submissionState === "idle"
            && policyState.acknowledgedPolicyId === policyState.id;
    }

    async function submit(submitCurrentPolicy) {
        if (!canSubmit()) {
            return { status: "blocked", data: null };
        }

        updateState({ submissionState: "submitting" });

        try {
            const submittedData = await submitCurrentPolicy(getState());
            updateState({ submissionState: "completed" });
            return { status: "success", data: submittedData };
        } catch (error) {
            if (error?.status === 409) {
                updateState({
                    submissionState: "idle",
                    acknowledgedPolicyId: null,
                    policyChanged: true,
                });
                await load({ force: true });
                return { status: "conflict", data: null };
            }

            updateState({ submissionState: "idle" });
            throw error;
        }
    }

    function updateState(update) {
        policyState = {
            ...policyState,
            ...update,
        };
        listenerSet.forEach((listener) => listener(getState()));
    }

    return Object.freeze({
        getState,
        subscribe,
        load,
        resetReservationJourney,
        setAcknowledged,
        canSubmit,
        submit,
    });
}

export function buildPrivacyAcceptancePayload(policyState) {
    return {
        privacyPolicyId: policyState.id,
        privacyAccepted: policyState.loadState === "ready"
            && policyState.id !== null
            && policyState.acknowledgedPolicyId === policyState.id,
    };
}

export const privacyPolicyController = createPrivacyPolicyController();
