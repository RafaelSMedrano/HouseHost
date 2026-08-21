import { renderRoomFormView } from "../views/roomFormView.js?v=2026-06-14-role-access";
import { renderRoomsView } from "../views/roomsView.js?v=2026-06-14-role-access";

export function createRoomController({
    permissions,
    renderDashboardTopbar,
    navigation,
    views = {},
}) {
    const renderRoomForm = views.renderRoomFormView || renderRoomFormView;
    const renderRooms = views.renderRoomsView || renderRoomsView;

    function openRoomsPanel() {
        navigation.reset({
            name: "rooms",
            params: {},
            render: () => renderRoomsPanel(),
        });
    }

    function openRoomFormPanel(roomId = null) {
        const normalizedRoomId = optionalRecordId(roomId);
        if (roomId !== null && normalizedRoomId === null) {
            return;
        }

        navigation.goTo({
            name: "roomForm",
            params: normalizedRoomId === null ? {} : { roomId: normalizedRoomId },
            render: () => renderRoomFormPanel(normalizedRoomId),
        });
    }

    function renderRoomsPanel() {
        renderDashboardTopbar("Quartos");
        renderRooms("main-pannel-container", {
            canManage: permissions.canManageOperationalData,
            onNewRoom: () => openRoomFormPanel(),
            onEditRoom: (roomId) => openRoomFormPanel(roomId),
        });
    }

    function renderRoomFormPanel(roomId = null) {
        renderDashboardTopbar(roomId ? "Editar quarto" : "Novo quarto");
        renderRoomForm("main-pannel-container", {
            roomId,
            canDelete: permissions.canDeleteOperationalData,
            onCancel: () => navigation.back(),
            onSaved: () => navigation.back(),
            onDeleted: () => navigation.back(),
        });
    }

    return {
        openRoomsPanel,
        openRoomFormPanel,
        renderRoomsPanel,
        renderRoomFormPanel,
    };
}

function optionalRecordId(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }

    const id = Number(value);
    return Number.isInteger(id) && id > 0 ? id : null;
}
