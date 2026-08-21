import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

const storageMap = new Map([["househost_token", "ratings-test-token"]]);
globalThis.localStorage = {
    getItem: (key) => storageMap.get(key) ?? null,
    setItem: (key, value) => storageMap.set(key, value),
    removeItem: (key) => storageMap.delete(key),
};
globalThis.location = { protocol: "http:", hostname: "localhost", port: "8080" };

const { findAllRatings, findRatingsByGuestId } = await import(
    "../js/api.js?v=ratings-navigation-api-tests"
);
const { permissionsFor } = await import(
    "../js/permissions.js?v=ratings-navigation-api-tests"
);
const { createRatingController } = await import(
    "../js/controllers/ratingController.js?v=ratings-navigation-api-tests"
);
const { createNavigationController } = await import(
    "../js/controllers/navigationController.js?v=ratings-navigation-api-tests"
);
const { renderSidebarWidget } = await import(
    "../js/widgets/sidebarWidget.js?v=ratings-navigation-api-tests"
);
const ratingsViewModule = await import(
    "../js/views/ratingsView.js?v=ratings-navigation-api-tests"
);

test("ratings API uses bounded encoded identifiers and pagination only", async () => {
    const requestedUrlList = [];
    globalThis.fetch = async (url) => {
        requestedUrlList.push(url);
        return new Response(
                JSON.stringify({ status: "success", data: { ratingSummaryDTOList: [] } }),
                { status: 200 }
        );
    };

    await findAllRatings("1", "20", { observations: "private-observation" });
    await findRatingsByGuestId("7", "2", "10", {
        observations: "private-observation",
    });

    assert.equal(requestedUrlList[0].endsWith("/ratings?page=1&size=20"), true);
    assert.equal(
            requestedUrlList[1].endsWith("/ratings/guest/7?page=2&size=10"),
            true
    );
    assert.equal(requestedUrlList.some((url) => url.includes("private-observation")), false);
    assert.throws(
            () => findRatingsByGuestId("7/../guests", 0, 20),
            /guestId inválido/
    );
    assert.throws(
            () => findAllRatings("1&observations=private", 20),
            /page inválido/
    );

    const apiSource = fs.readFileSync(new URL("../js/api.js", import.meta.url), "utf8");
    assert.match(apiSource, /ratings\/guest\/\$\{encodeURIComponent\(normalizedGuestId\)\}/);
    assert.match(apiSource, /page=\$\{encodeURIComponent\(normalizedPage\)\}/);
    assert.match(apiSource, /size=\$\{encodeURIComponent\(normalizedSize\)\}/);
});

test("ratings permission matches operational roles and excludes housekeeping", () => {
    for (const role of ["CEO", "CTO", "ADMIN", "MANAGER", "RECEPTION"]) {
        assert.equal(permissionsFor(role).canAccessView("ratings"), true, role);
    }
    assert.equal(permissionsFor("HOUSEKEEPING").canAccessView("ratings"), false);
});

test("sidebar renders an accessible ratings root only for authorized roles", () => {
    const originalDocument = globalThis.document;
    const container = {
        innerHTML: "",
        querySelectorAll: () => [],
        querySelector: () => null,
    };
    globalThis.document = { getElementById: () => container };

    try {
        renderSidebarWidget("sidebar", { name: "Ana", role: "RECEPTION" });
        assert.match(
                container.innerHTML,
                /<button[^>]*data-view="ratings"[^>]*>[^<]*<i[^>]*aria-hidden="true"[^>]*><\/i> Avaliações/s
        );

        renderSidebarWidget("sidebar", { name: "Bia", role: "HOUSEKEEPING" });
        assert.equal(container.innerHTML.includes('data-view="ratings"'), false);
    } finally {
        globalThis.document = originalDocument;
    }
});

test("ratings root loads lazily and related profiles preserve it as predecessor", async () => {
    const renderedPageNameList = [];
    let ratingsViewOptions;
    let dependencyLoadCount = 0;
    const requestedPageList = [];
    const navigation = createNavigationController({
        fallbackPage: {
            name: "dashboard",
            params: {},
            render: () => renderedPageNameList.push("dashboard"),
        },
    });
    const ratingController = createRatingController({
        navigation,
        permissions: permissionsFor("RECEPTION"),
        renderDashboardTopbar: () => renderedPageNameList.push("ratingsTopbar"),
        renderGuestProfilePanel: (guestId) =>
            renderedPageNameList.push(`guest:${guestId}`),
        renderReservationProfilePanel: (bookingId) =>
            renderedPageNameList.push(`booking:${bookingId}`),
        loadDependencies: async () => {
            dependencyLoadCount += 1;
            return {
                findAllRatings: async (page) => {
                    requestedPageList.push(page);
                    return {
                        data: { ratingSummaryDTOList: [{ bookingId: 42, guestId: 7 }] },
                    };
                },
                renderRatingsLoadingView: () => renderedPageNameList.push("loading"),
                renderRatingsView: (_containerId, options) => {
                    ratingsViewOptions = options;
                    renderedPageNameList.push("ratings");
                },
                renderRatingsErrorView: () => renderedPageNameList.push("error"),
            };
        },
    });

    assert.equal(dependencyLoadCount, 0);
    assert.equal(ratingController.openRatingsPanel(), true);
    await waitForAsyncRender();
    assert.equal(dependencyLoadCount, 1);
    assert.deepEqual(navigation.current(), {
        name: "ratings",
        params: { page: 0, size: 20 },
    });
    assert.deepEqual(requestedPageList, [0]);

    assert.equal(ratingsViewOptions.onPageChange(2), true);
    await waitForAsyncRender();
    assert.deepEqual(navigation.current(), {
        name: "ratings",
        params: { page: 2, size: 20 },
    });
    assert.deepEqual(requestedPageList, [0, 2]);

    assert.equal(ratingsViewOptions.onOpenGuest(7), true);
    assert.deepEqual(navigation.current(), {
        name: "guestProfile",
        params: { guestId: 7 },
    });
    navigation.back();
    assert.deepEqual(navigation.current(), {
        name: "ratings",
        params: { page: 2, size: 20 },
    });

    await waitForAsyncRender();
    assert.equal(ratingsViewOptions.onOpenBooking(42), true);
    assert.deepEqual(navigation.current(), {
        name: "reservationProfile",
        params: { bookingId: 42 },
    });
    navigation.back();
    assert.deepEqual(navigation.current(), {
        name: "ratings",
        params: { page: 2, size: 20 },
    });
});

test("ratings controller refuses an unauthorized root before loading dependencies", () => {
    let dependencyLoadCount = 0;
    const navigation = createNavigationController();
    const ratingController = createRatingController({
        navigation,
        permissions: permissionsFor("HOUSEKEEPING"),
        renderDashboardTopbar() {},
        renderGuestProfilePanel() {},
        renderReservationProfilePanel() {},
        loadDependencies: async () => {
            dependencyLoadCount += 1;
            return {};
        },
    });

    assert.equal(ratingController.openRatingsPanel(), false);
    assert.equal(navigation.current(), null);
    assert.equal(dependencyLoadCount, 0);
});

test("ratings root view exposes accessible loading empty and error states", () => {
    const originalDocument = globalThis.document;
    const container = { innerHTML: "" };
    globalThis.document = { getElementById: () => container };

    try {
        ratingsViewModule.renderRatingsLoadingView("main");
        assert.match(container.innerHTML, /aria-labelledby="ratings-root-title"/);
        assert.match(container.innerHTML, /role="status" aria-live="polite"/);

        ratingsViewModule.renderRatingsView("main", {
            pageData: { ratingSummaryDTOList: [] },
        });
        assert.match(container.innerHTML, /Nenhuma avaliação encontrada/);

        ratingsViewModule.renderRatingsErrorView("main");
        assert.match(container.innerHTML, /role="alert"/);
    } finally {
        globalThis.document = originalDocument;
    }
});

test("ratings cache-busting chain reaches the browser entry point", () => {
    const shellCacheVersion = "2026-08-17-financial-classification-removal";
    const indexSource = fs.readFileSync(new URL("../index.html", import.meta.url), "utf8");
    const mainSource = fs.readFileSync(
            new URL("../js/controllers/main.js", import.meta.url),
            "utf8"
    );
    const uiControllerSource = fs.readFileSync(
            new URL("../js/controllers/UICOntroller.js", import.meta.url),
            "utf8"
    );

    assert.match(indexSource, new RegExp(`main\\.js\\?v=${shellCacheVersion}`));
    assert.match(mainSource, new RegExp(`UICOntroller\\.js\\?v=${shellCacheVersion}`));
    assert.match(uiControllerSource, /ratingController\.js\?v=2026-08-13-ratings-list/);
    assert.match(uiControllerSource, /sidebarWidget\.js\?v=2026-08-13-ratings-navigation/);
    assert.match(uiControllerSource, /permissions\.js\?v=2026-08-13-ratings-navigation/);
});

async function waitForAsyncRender() {
    await new Promise((resolve) => setImmediate(resolve));
}
