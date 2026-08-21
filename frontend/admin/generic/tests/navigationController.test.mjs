import assert from "node:assert/strict";
import test from "node:test";

const { createNavigationController } = await import(
    "../js/controllers/navigationController.js?v=navigation-history-entries-tests"
);

function createEntry(name, rendered, params = {}, meta) {
    return {
        name,
        params,
        ...(meta === undefined ? {} : { meta }),
        render: (receivedParams) => rendered.push({ name, params: receivedParams }),
    };
}

function createHarness() {
    const rendered = [];
    const fallbackPage = createEntry("dashboard", rendered);

    return {
        rendered,
        fallbackPage,
        navigation: createNavigationController({ fallbackPage }),
    };
}

test("goTo appends dynamic entries and back restores the immediate predecessor", () => {
    const { navigation, rendered } = createHarness();

    navigation.reset(createEntry("finance", rendered));
    navigation.goTo(createEntry("transactionProfile", rendered, { transactionId: 25 }));
    navigation.goTo(createEntry("guestProfile", rendered, { guestId: 10 }));

    assert.deepEqual(navigation.current(), {
        name: "guestProfile",
        params: { guestId: 10 },
    });
    assert.equal(navigation.canGoBack(), true);

    navigation.back();

    assert.deepEqual(navigation.current(), {
        name: "transactionProfile",
        params: { transactionId: 25 },
    });
    assert.deepEqual(rendered.at(-1), {
        name: "transactionProfile",
        params: { transactionId: 25 },
    });
});

test("back at a non-fallback root uses the configured fallback without growing history", () => {
    const { navigation, rendered } = createHarness();

    navigation.reset(createEntry("guests", rendered));
    navigation.back();

    assert.deepEqual(navigation.current(), { name: "dashboard", params: {} });
    assert.equal(navigation.canGoBack(), false);
    assert.deepEqual(rendered.at(-1), { name: "dashboard", params: {} });
});

test("back at the fallback root is safe and keeps one history entry", () => {
    const { navigation, rendered } = createHarness();

    navigation.reset(createEntry("dashboard", rendered));
    navigation.back();

    assert.deepEqual(navigation.current(), { name: "dashboard", params: {} });
    assert.equal(navigation.canGoBack(), false);
    assert.equal(rendered.length, 2);
});

test("repeated back calls converge on the fallback and remain deterministic", () => {
    const { navigation, rendered } = createHarness();

    navigation.reset(createEntry("finance", rendered));
    navigation.goTo(createEntry("transactionProfile", rendered, { transactionId: 25 }));
    navigation.goTo(createEntry("guestProfile", rendered, { guestId: 10 }));

    for (let index = 0; index < 6; index += 1) {
        navigation.back();
    }

    assert.deepEqual(navigation.current(), { name: "dashboard", params: {} });
    assert.equal(navigation.canGoBack(), false);
    assert.deepEqual(rendered.slice(-3), [
        { name: "dashboard", params: {} },
        { name: "dashboard", params: {} },
        { name: "dashboard", params: {} },
    ]);
});

test("reset discards unrelated history and creates one new root", () => {
    const { navigation, rendered } = createHarness();

    navigation.reset(createEntry("dashboard", rendered));
    navigation.goTo(createEntry("transactionProfile", rendered, { transactionId: 25 }));
    navigation.goTo(createEntry("guestProfile", rendered, { guestId: 10 }));
    navigation.reset(createEntry("guests", rendered));

    assert.deepEqual(navigation.current(), { name: "guests", params: {} });
    assert.equal(navigation.canGoBack(), false);
});

test("replace changes the current page without adding a back step", () => {
    const { navigation, rendered } = createHarness();

    navigation.reset(createEntry("dashboard", rendered));
    navigation.goTo(createEntry("transactionProfile", rendered, { transactionId: 25 }));
    navigation.replace(createEntry("guestProfile", rendered, { guestId: 10 }));
    navigation.back();

    assert.deepEqual(navigation.current(), { name: "dashboard", params: {} });
});

test("page parameters and metadata are defensively copied", () => {
    const { navigation, rendered } = createHarness();
    const entry = createEntry(
        "guestProfile",
        rendered,
        { guestId: 10, context: { source: "transactionProfile" } },
        { relation: "guest" },
    );

    navigation.reset(createEntry("dashboard", rendered));
    navigation.goTo(entry);
    entry.params.context.source = "mutated";
    entry.meta.relation = "mutated";

    const current = navigation.current();
    current.params.context.source = "mutated-again";
    current.meta.relation = "mutated-again";

    assert.deepEqual(navigation.current(), {
        name: "guestProfile",
        params: { guestId: 10, context: { source: "transactionProfile" } },
        meta: { relation: "guest" },
    });
});

test("malformed entries do not corrupt the current history", () => {
    const { navigation } = createHarness();
    navigation.reset({
        name: "dashboard",
        params: {},
        render: () => {},
    });

    assert.throws(() => navigation.goTo(null), /Entrada de navegação inválida/);
    assert.throws(
        () => navigation.goTo({ name: "missing-renderer" }),
        /Entrada de navegação sem renderer válido/
    );
    assert.throws(
        () => navigation.goTo({ name: "" , render: () => {} }),
        /Entrada de navegação sem nome válido/
    );

    assert.deepEqual(navigation.current(), { name: "dashboard", params: {} });
});

test("a render failure leaves the existing history unchanged", () => {
    const rendered = [];
    const navigation = createNavigationController({
        fallbackPage: {
            name: "dashboard",
            params: {},
            render: () => rendered.push("dashboard"),
        },
    });

    navigation.reset({
        name: "dashboard",
        params: {},
        render: () => rendered.push("dashboard"),
    });

    assert.throws(
        () => navigation.goTo({
            name: "broken",
            params: {},
            render: () => { throw new Error("render failure"); },
        }),
        /render failure/
    );

    assert.deepEqual(navigation.current(), { name: "dashboard", params: {} });
    assert.deepEqual(rendered, ["dashboard"]);
});

test("a new controller starts with empty in-memory history after a simulated refresh", () => {
    const first = createHarness();
    first.navigation.reset(createEntry("finance", first.rendered));
    first.navigation.goTo(createEntry("transactionProfile", first.rendered, { transactionId: 25 }));

    const refreshed = createHarness();

    assert.equal(refreshed.navigation.current(), null);
    assert.equal(refreshed.navigation.canGoBack(), false);
    refreshed.navigation.back();
    assert.deepEqual(refreshed.navigation.current(), { name: "dashboard", params: {} });
});
