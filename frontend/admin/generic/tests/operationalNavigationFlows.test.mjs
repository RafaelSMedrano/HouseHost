import assert from "node:assert/strict";
import test from "node:test";

const { createNavigationController } = await import(
    "../js/controllers/navigationController.js?v=operational-navigation-flow-tests"
);
const { createRoomController } = await import(
    "../js/controllers/roomController.js?v=operational-navigation-flow-tests"
);
const { createOperationsController } = await import(
    "../js/controllers/operationsController.js?v=operational-navigation-flow-tests"
);
const { createSupplierController } = await import(
    "../js/controllers/supplierController.js?v=operational-navigation-flow-tests"
);
const { createPrivacyController } = await import(
    "../js/controllers/privacyController.js?v=operational-navigation-flow-tests"
);

function createNavigationHarness() {
    const rendered = [];
    const navigation = createNavigationController({
        fallbackPage: {
            name: "dashboard",
            params: {},
            render: () => rendered.push("dashboard"),
        },
    });
    return { navigation, rendered };
}

function permissions(allowed = true) {
    return {
        canManageOperationalData: true,
        canDeleteOperationalData: true,
        canAccessView: () => allowed,
    };
}

test("room forms return to the room list after cancel, save or delete", () => {
    const { navigation } = createNavigationHarness();
    let listOptions;
    let formOptions;
    const controller = createRoomController({
        navigation,
        permissions: permissions(),
        renderDashboardTopbar() {},
        views: {
            renderRoomsView: (_container, options) => { listOptions = options; },
            renderRoomFormView: (_container, options) => { formOptions = options; },
        },
    });

    controller.openRoomsPanel();
    listOptions.onEditRoom(4);
    assert.deepEqual(navigation.current(), { name: "roomForm", params: { roomId: 4 } });
    formOptions.onCancel();
    assert.deepEqual(navigation.current(), { name: "rooms", params: {} });

    listOptions.onNewRoom();
    formOptions.onSaved({ id: 8 });
    assert.deepEqual(navigation.current(), { name: "rooms", params: {} });

    listOptions.onEditRoom(4);
    formOptions.onDeleted();
    assert.deepEqual(navigation.current(), { name: "rooms", params: {} });
});

test("check-in and checkout forms preserve the screen that launched them", () => {
    const { navigation } = createNavigationHarness();
    let checkInFormOptions;
    let checkOutFormOptions;
    const controller = createOperationsController({
        navigation,
        renderDashboardTopbar() {},
        views: {
            renderCheckInView() {},
            renderCheckOutView() {},
            renderRoomTimelineView() {},
            renderCheckInFormView: (_container, options) => { checkInFormOptions = options; },
            renderCheckOutFormView: (_container, options) => { checkOutFormOptions = options; },
        },
    });

    controller.openTimelinePanel();
    controller.openCheckInFormPanel({ bookingId: 7 });
    checkInFormOptions.onCancel();
    assert.deepEqual(navigation.current(), { name: "timeline", params: {} });

    controller.openCheckOutPanel();
    controller.openCheckOutFormPanel({ bookingId: 9 });
    checkOutFormOptions.onSaved();
    assert.deepEqual(navigation.current(), { name: "checkout", params: {} });
});

test("supplier profile edit cancel and save restore the same predecessor without duplicates", () => {
    const { navigation } = createNavigationHarness();
    let listOptions;
    let profileOptions;
    let formOptions;
    const controller = createSupplierController({
        navigation,
        permissions: permissions(),
        renderDashboardTopbar() {},
        renderDashboardPanel() {},
        views: {
            renderSuppliersView: (_container, options) => { listOptions = options; },
            renderSupplierProfileView: (_container, options) => { profileOptions = options; },
            renderSupplierFormView: (_container, options) => { formOptions = options; },
        },
    });

    controller.openSuppliersPanel();
    listOptions.onOpenSupplier(15);
    profileOptions.onEdit(15);
    formOptions.onCancel();
    assert.deepEqual(navigation.current(), { name: "supplierProfile", params: { supplierId: 15 } });

    profileOptions.onEdit(15);
    formOptions.onSaved({ id: 15 });
    assert.deepEqual(navigation.current(), { name: "supplierProfile", params: { supplierId: 15 } });
    profileOptions.onBack();
    assert.deepEqual(navigation.current(), { name: "suppliers", params: {} });
});

test("a newly created supplier replaces its form with the resulting profile", () => {
    const { navigation } = createNavigationHarness();
    let listOptions;
    let profileOptions;
    let formOptions;
    const controller = createSupplierController({
        navigation,
        permissions: permissions(),
        renderDashboardTopbar() {},
        renderDashboardPanel() {},
        views: {
            renderSuppliersView: (_container, options) => { listOptions = options; },
            renderSupplierProfileView: (_container, options) => { profileOptions = options; },
            renderSupplierFormView: (_container, options) => { formOptions = options; },
        },
    });

    controller.openSuppliersPanel();
    listOptions.onNewSupplier();
    formOptions.onSaved({ id: 16 });
    assert.deepEqual(navigation.current(), { name: "supplierProfile", params: { supplierId: 16 } });
    profileOptions.onBack();
    assert.deepEqual(navigation.current(), { name: "suppliers", params: {} });
});

test("operation and assessment profiles preserve both relationship directions", () => {
    const { navigation } = createNavigationHarness();
    const captured = {};
    const controller = createPrivacyController({
        navigation,
        permissions: permissions(),
        renderDashboardTopbar() {},
        renderDashboardPanel() {},
        views: privacyViewSpies(captured),
    });

    controller.openDataProcessingOperationsPanel();
    captured.operations.onOpenOperation(3);
    captured.operationProfile.onOpenAssessment(21);
    assert.deepEqual(navigation.current(), {
        name: "assessmentProfile",
        params: { assessmentId: 21, origin: { type: "operation", operationId: 3 } },
    });
    captured.assessmentProfile.onBack();
    assert.deepEqual(navigation.current(), { name: "operationProfile", params: { operationId: 3 } });

    captured.operationProfile.onOpenAssessment(21);
    captured.assessmentProfile.onOpenOperation(3);
    assert.deepEqual(navigation.current(), { name: "operationProfile", params: { operationId: 3 } });
    captured.operationProfile.onBack();
    assert.equal(navigation.current().name, "assessmentProfile");
});

test("assessment edit and creation remove obsolete forms from history", () => {
    const { navigation } = createNavigationHarness();
    const captured = {};
    const controller = createPrivacyController({
        navigation,
        permissions: permissions(),
        renderDashboardTopbar() {},
        renderDashboardPanel() {},
        views: privacyViewSpies(captured),
    });

    controller.openDataProcessingOperationsPanel();
    captured.operations.onOpenOperation(3);
    captured.operationProfile.onOpenAssessment(21);
    captured.assessmentProfile.onEdit(21, 3);
    captured.assessmentForm.onSaved({ id: 21, processingOperationId: 3 });
    assert.equal(navigation.current().name, "assessmentProfile");
    captured.assessmentProfile.onBack();
    assert.equal(navigation.current().name, "operationProfile");

    captured.operationProfile.onNewAssessment(3);
    captured.assessmentForm.onSaved({ id: 22, processingOperationId: 3 });
    assert.deepEqual(navigation.current(), {
        name: "assessmentProfile",
        params: { assessmentId: 22, origin: { type: "operation", operationId: 3 } },
    });
    captured.assessmentProfile.onBack();
    assert.equal(navigation.current().name, "operationProfile");
});

test("a new assessment revision replaces its form and keeps the prior assessment", () => {
    const { navigation } = createNavigationHarness();
    const captured = {};
    const controller = createPrivacyController({
        navigation,
        permissions: permissions(),
        renderDashboardTopbar() {},
        renderDashboardPanel() {},
        views: privacyViewSpies(captured),
    });

    controller.openDataProcessingOperationsPanel();
    captured.operations.onOpenOperation(3);
    captured.operationProfile.onOpenAssessment(21);
    captured.assessmentProfile.onEdit(22, 3);
    captured.assessmentForm.onSaved({ id: 22, processingOperationId: 3 });
    assert.equal(navigation.current().params.assessmentId, 22);
    captured.assessmentProfile.onBack();
    assert.equal(navigation.current().params.assessmentId, 21);
});

test("assessment list remains the predecessor of profiles opened from it", () => {
    const { navigation } = createNavigationHarness();
    const captured = {};
    const controller = createPrivacyController({
        navigation,
        permissions: permissions(),
        renderDashboardTopbar() {},
        renderDashboardPanel() {},
        views: privacyViewSpies(captured),
    });

    controller.openDataProcessingOperationsPanel();
    captured.operations.onOpenAssessments();
    captured.assessments.onOpenAssessment(25);
    captured.assessmentProfile.onBack();
    assert.deepEqual(navigation.current(), { name: "assessments", params: {} });
});

test("permission failures reset to dashboard without retaining rejected entries", () => {
    for (const createController of [
        ({ navigation, renderDashboardPanel }) => createSupplierController({
            navigation,
            renderDashboardPanel,
            renderDashboardTopbar() {},
            permissions: permissions(false),
        }),
        ({ navigation, renderDashboardPanel }) => createPrivacyController({
            navigation,
            renderDashboardPanel,
            renderDashboardTopbar() {},
            permissions: permissions(false),
        }),
    ]) {
        const { navigation, rendered } = createNavigationHarness();
        const controller = createController({
            navigation,
            renderDashboardPanel: () => rendered.push("authorized-dashboard"),
        });

        if (controller.openSuppliersPanel) {
            controller.openSuppliersPanel();
            controller.openSupplierProfilePanel(5);
        } else {
            controller.openDataProcessingOperationsPanel();
            controller.openOperationProfilePanel(5);
        }

        assert.deepEqual(navigation.current(), { name: "dashboard", params: {} });
        assert.equal(navigation.canGoBack(), false);
        assert.equal(rendered.includes("authorized-dashboard"), true);
    }
});

test("malformed record identifiers do not create navigation entries", () => {
    const roomHarness = createNavigationHarness();
    const roomController = createRoomController({
        navigation: roomHarness.navigation,
        permissions: permissions(),
        renderDashboardTopbar() {},
        views: {
            renderRoomsView() {},
            renderRoomFormView() {},
        },
    });
    roomController.openRoomsPanel();
    roomController.openRoomFormPanel("not-a-room");
    assert.deepEqual(roomHarness.navigation.current(), { name: "rooms", params: {} });

    const privacyHarness = createNavigationHarness();
    const privacyController = createPrivacyController({
        navigation: privacyHarness.navigation,
        permissions: permissions(),
        renderDashboardTopbar() {},
        renderDashboardPanel() {},
        views: privacyViewSpies({}),
    });
    privacyController.openDataProcessingOperationsPanel();
    privacyController.openOperationProfilePanel(0);
    privacyController.openAssessmentProfilePanel("invalid");
    assert.deepEqual(privacyHarness.navigation.current(), { name: "processingOperations", params: {} });
});

function privacyViewSpies(captured) {
    return {
        renderDataProcessingOperationsView: (_container, options) => { captured.operations = options; },
        renderDataProcessingOperationProfileView: (_container, options) => { captured.operationProfile = options; },
        renderLegalBasisAssessmentsView: (_container, options) => { captured.assessments = options; },
        renderLegalBasisAssessmentProfileView: (_container, options) => { captured.assessmentProfile = options; },
        renderLegalBasisAssessmentFormView: (_container, options) => { captured.assessmentForm = options; },
    };
}
