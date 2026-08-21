import assert from "node:assert/strict";
import test from "node:test";

globalThis.localStorage = { getItem: () => null, setItem() {}, removeItem() {} };
const apiModule = await import("../js/api.js?v=supplier-tests");
const permissionModule = await import("../js/permissions.js?v=supplier-tests");

test("supplier view is restricted to administrator roles", () => {
    assert.equal(permissionModule.canAccessView("CEO", "suppliers"), true);
    assert.equal(permissionModule.canAccessView("CTO", "suppliers"), true);
    assert.equal(permissionModule.canAccessView("ADMIN", "suppliers"), true);
    assert.equal(permissionModule.canAccessView("MANAGER", "suppliers"), false);
    assert.equal(permissionModule.canAccessView("RECEPTION", "suppliers"), false);
});

test("supplier filters are safely URL encoded", async () => {
    let requestedUrl = "";
    globalThis.fetch = async (url) => {
        requestedUrl = url;
        return new Response(JSON.stringify({ status: "success", data: [] }), { status: 200 });
    };
    await apiModule.findAllSuppliers({ name: "AWS Brasil & Dados", role: "OPERATOR" });
    assert.equal(requestedUrl.includes("name=AWS+Brasil+%26+Dados"), true);
    assert.equal(requestedUrl.includes("role=OPERATOR"), true);
});
