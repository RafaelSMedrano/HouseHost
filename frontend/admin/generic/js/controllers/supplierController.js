import { renderSupplierFormView } from "../views/supplierFormView.js?v=2026-08-10-navigation-accessibility";
import { renderSupplierProfileView } from "../views/supplierProfileView.js?v=2026-08-10-navigation-accessibility";
import { renderSuppliersView } from "../views/suppliersView.js?v=2026-07-26-vertical-governance-flow";

export function createSupplierController({
    permissions,
    renderDashboardTopbar,
    navigation,
    renderDashboardPanel,
    views = {},
}) {
    const renderSupplierForm = views.renderSupplierFormView || renderSupplierFormView;
    const renderSupplierProfile = views.renderSupplierProfileView || renderSupplierProfileView;
    const renderSuppliers = views.renderSuppliersView || renderSuppliersView;

    function allowed() {
        return permissions.canAccessView("suppliers");
    }

    function dashboardEntry() {
        return {
            name: "dashboard",
            params: {},
            render: () => renderDashboardPanel(),
        };
    }

    function resetToDashboard() {
        navigation.reset(dashboardEntry());
    }

    function navigateIfAllowed(method, entry) {
        if (!allowed()) {
            resetToDashboard();
            return false;
        }

        navigation[method](entry);
        return true;
    }

    function openSuppliersPanel() {
        navigateIfAllowed("reset", {
            name: "suppliers",
            params: {},
            render: () => renderSuppliersPanel(),
        });
    }

    function openSupplierFormPanel(supplierId = null) {
        const normalizedSupplierId = optionalRecordId(supplierId);
        if (supplierId !== null && normalizedSupplierId === null) {
            return false;
        }

        return navigateIfAllowed("goTo", {
            name: "supplierForm",
            params: normalizedSupplierId === null ? {} : { supplierId: normalizedSupplierId },
            render: () => renderSupplierFormPanel(normalizedSupplierId),
        });
    }

    function openSupplierProfilePanel(supplierId) {
        const normalizedSupplierId = requiredRecordId(supplierId);
        if (normalizedSupplierId === null) {
            return false;
        }

        return navigateIfAllowed("goTo", {
            name: "supplierProfile",
            params: { supplierId: normalizedSupplierId },
            render: () => renderSupplierProfilePanel(normalizedSupplierId),
        });
    }

    function renderSuppliersPanel() {
        if (!allowed()) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar("Fornecedores");
        renderSuppliers("main-pannel-container", {
            onNewSupplier: () => openSupplierFormPanel(),
            onOpenSupplier: (supplierId) => openSupplierProfilePanel(supplierId),
        });
    }

    function renderSupplierFormPanel(supplierId = null) {
        if (!allowed()) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar(supplierId ? "Editar fornecedor" : "Novo fornecedor");
        renderSupplierForm("main-pannel-container", {
            supplierId,
            onCancel: () => navigation.back(),
            onSaved: (supplier) => handleSupplierSaved(supplierId, supplier),
        });
    }

    function renderSupplierProfilePanel(supplierId) {
        if (!allowed()) {
            renderDashboardPanel();
            return;
        }
        renderDashboardTopbar("Detalhes do fornecedor");
        renderSupplierProfile("main-pannel-container", {
            supplierId,
            onBack: () => navigation.back(),
            onEdit: (id) => openSupplierFormPanel(id),
        });
    }

    function handleSupplierSaved(existingSupplierId, supplier) {
        if (existingSupplierId !== null) {
            navigation.back();
            return;
        }

        const supplierId = requiredRecordId(supplier?.id);
        if (supplierId === null) {
            navigation.back();
            return;
        }

        navigation.replace({
            name: "supplierProfile",
            params: { supplierId },
            render: () => renderSupplierProfilePanel(supplierId),
        });
    }

    return {
        openSuppliersPanel,
        openSupplierFormPanel,
        openSupplierProfilePanel,
        renderSuppliersPanel,
        renderSupplierFormPanel,
        renderSupplierProfilePanel,
    };
}

function optionalRecordId(value) {
    return value === null || value === undefined || value === ""
        ? null
        : requiredRecordId(value);
}

function requiredRecordId(value) {
    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : null;
}
