import { createRoom, deleteRoom, findRoomById, updateRoom } from "../api.js?v=2026-05-18-rooms";

const roomTypes = [
    { value: "SINGLE", label: "Individual" },
    { value: "DOUBLE", label: "Duplo" },
    { value: "SUITE", label: "Suíte" },
    { value: "FAMILY", label: "Família" },
    { value: "STANDARD", label: "Standard" },
];

const roomStatuses = [
    { value: "AVAILABLE", label: "Disponível" },
    { value: "OCCUPIED", label: "Ocupado" },
    { value: "MAINTENANCE", label: "Manutenção" },
    { value: "INACTIVE", label: "Inativo" },
];

export function renderRoomFormView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    const roomId = options.roomId || null;
    const isEdit = Boolean(roomId);

    container.innerHTML = `
<div class="main guest-form-main room-form-main">
  <div class="guest-form-content room-form-content">
    <div class="room-form-preview">
      <div id="room-preview-number" class="room-form-number">--</div>
      <div class="room-form-preview-info">
        <div id="room-preview-type" class="room-form-title empty">Novo quarto</div>
        <div id="room-preview-sub" class="room-form-sub">Preencha os dados para cadastrar no sistema</div>
        <div id="room-preview-badge" class="room-form-badge available">Disponível</div>
      </div>
    </div>

    <form id="room-form" class="guest-form room-form">
      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-lav"><i class="ti ti-door"></i></span><div><strong>Identificação</strong><small>Dados principais do quarto</small></div></div>
        <div class="guest-fields">
          ${field("roomNumber", "Número do quarto", "text", "Ex: 01, 102 ou Chalé 3", "ti-door", true)}
          ${selectField("type", "Tipo", roomTypes)}
          ${field("capacity", "Capacidade", "number", "2", "ti-users", true)}
          ${field("dailyRate", "Diária (R$)", "number", "350,00", "ti-currency-real", true)}
        </div>
      </section>

      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-sage"><i class="ti ti-circle-check"></i></span><div><strong>Status operacional</strong><small>Define se o quarto pode ser usado em reservas</small></div></div>
        <div class="guest-fields">
          ${selectField("status", "Status", roomStatuses)}
        </div>
      </section>

      <section id="room-danger-zone" class="guest-danger-zone ${isEdit && options.canDelete ? "" : "hidden"}">
        <div><strong>Excluir quarto</strong><span>Remove permanentemente o cadastro deste quarto.</span></div>
        <button id="room-delete" class="guest-delete-button" type="button"><i class="ti ti-trash"></i> Excluir quarto</button>
      </section>
    </form>
  </div>

  <div class="guest-form-footer">
    <div><span id="room-footer-mode">${isEdit ? "Modo edicao" : "Novo cadastro"}</span><strong id="room-footer-status">Ainda nao salvo</strong></div>
    <div class="footer-actions">
      <button id="room-cancel" class="btn-cancel" type="button"><i class="ti ti-x"></i> Cancelar</button>
      <button id="room-save" class="btn-save" type="submit" form="room-form"><i class="ti ti-check"></i> Salvar quarto</button>
    </div>
  </div>

  <div id="room-toast" class="booking-toast"><i class="ti ti-check"></i><span></span></div>
</div>
    `;

    bindRoomForm(container, { ...options, roomId });
}

function bindRoomForm(container, options) {
    const state = { roomId: options.roomId || null };
    const form = container.querySelector("#room-form");

    form.addEventListener("input", () => updatePreview(container));
    form.addEventListener("change", () => updatePreview(container));
    form.addEventListener("submit", (event) => handleSubmit(event, container, options, state));

    container.querySelector("#room-cancel").addEventListener("click", () => {
        if (typeof options.onCancel === "function") {
            options.onCancel();
        }
    });

    container.querySelector("#room-delete")?.addEventListener("click", () => handleDelete(container, options, state));

    if (state.roomId) {
        loadRoom(container, state);
    }

    updatePreview(container);
}

async function loadRoom(container, state) {
    try {
        const response = await findRoomById(state.roomId);
        const room = response.data;

        setFormValue(container, "roomNumber", room.roomNumber);
        setFormValue(container, "type", room.type);
        setFormValue(container, "capacity", room.capacity);
        setFormValue(container, "dailyRate", room.dailyRate);
        setFormValue(container, "status", room.status || "AVAILABLE");
        container.querySelector("#room-footer-status").textContent = `ID #${String(room.id).padStart(3, "0")}`;
        updatePreview(container);
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
    }
}

async function handleSubmit(event, container, options, state) {
    event.preventDefault();

    const payload = collectPayload(container);
    if (!payload.roomNumber || !payload.type || !payload.capacity) {
        showToast(container, "Preencha número, tipo e capacidade.", "ti-alert-circle");
        return;
    }

    const saveButton = container.querySelector("#room-save");
    saveButton.disabled = true;
    saveButton.innerHTML = '<i class="ti ti-loader-2 spinning"></i> Salvando...';

    try {
        const response = state.roomId
            ? await updateRoom(state.roomId, payload)
            : await createRoom(payload);

        state.roomId = response.data?.id || state.roomId;
        container.querySelector("#room-footer-mode").textContent = "Cadastro salvo";
        container.querySelector("#room-footer-status").textContent = `ID #${String(state.roomId).padStart(3, "0")}`;
        showToast(container, response.message || "Quarto salvo com sucesso.", "ti-door");

        if (typeof options.onSaved === "function") {
            options.onSaved(response.data);
        }
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
    } finally {
        saveButton.disabled = false;
        saveButton.innerHTML = '<i class="ti ti-check"></i> Salvar quarto';
    }
}

async function handleDelete(container, options, state) {
    if (!state.roomId || !window.confirm("Excluir este quarto permanentemente?")) {
        return;
    }

    try {
        await deleteRoom(state.roomId);
        showToast(container, "Quarto excluido com sucesso.", "ti-trash");
        if (typeof options.onDeleted === "function") {
            options.onDeleted();
        }
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
    }
}

function collectPayload(container) {
    return {
        roomNumber: value(container, "roomNumber"),
        type: value(container, "type"),
        capacity: numberValue(container, "capacity"),
        dailyRate: decimalValue(container, "dailyRate"),
        status: value(container, "status") || "AVAILABLE",
    };
}

function updatePreview(container) {
    const number = value(container, "roomNumber");
    const type = selectedLabel(container, "type") || "Novo quarto";
    const capacity = numberValue(container, "capacity");
    const dailyRate = decimalValue(container, "dailyRate");
    const status = value(container, "status") || "AVAILABLE";
    const statusLabel = selectedLabel(container, "status") || "Disponível";
    const badge = container.querySelector("#room-preview-badge");

    container.querySelector("#room-preview-number").textContent = number || "--";
    container.querySelector("#room-preview-type").textContent = type;
    container.querySelector("#room-preview-type").classList.toggle("empty", !number);
    container.querySelector("#room-preview-sub").textContent = [
        capacity ? `${capacity} pessoa${capacity === 1 ? "" : "s"}` : "",
        dailyRate ? formatCurrency(dailyRate) : "",
    ].filter(Boolean).join(" · ") || "Preencha os dados para cadastrar no sistema";

    badge.textContent = statusLabel;
    badge.className = `room-form-badge ${status.toLowerCase()}`;
}

function field(id, label, type, placeholder, icon, required = false) {
    const numberAttrs = id === "capacity" ? 'min="1" step="1"' : 'min="0" step="0.01"';

    return `
      <label class="guest-field">
        <span>${label}${required ? " *" : ""}</span>
        <div><i class="ti ${icon}"></i><input id="room-${id}" type="${type}" placeholder="${placeholder}" ${type === "number" ? numberAttrs : ""} ${required ? "required" : ""}></div>
      </label>
    `;
}

function selectField(id, label, options) {
    return `
      <label class="guest-field no-icon">
        <span>${label}</span>
        <div><select id="room-${id}">
          ${options.map((option) => `<option value="${escapeHtml(option.value)}">${escapeHtml(option.label)}</option>`).join("")}
        </select></div>
      </label>
    `;
}

function value(container, id) {
    return container.querySelector(`#room-${id}`)?.value.trim() || "";
}

function numberValue(container, id) {
    const rawValue = value(container, id).replace(",", ".");
    return rawValue === "" ? null : Number(rawValue);
}

function decimalValue(container, id) {
    const valueToConvert = numberValue(container, id);
    return valueToConvert === null ? 0 : valueToConvert;
}

function selectedLabel(container, id) {
    const select = container.querySelector(`#room-${id}`);
    return select?.selectedOptions?.[0]?.textContent || "";
}

function setFormValue(container, id, valueToSet) {
    const element = container.querySelector(`#room-${id}`);
    if (element) {
        element.value = valueToSet ?? "";
    }
}

function formatCurrency(value) {
    return new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL",
    }).format(value);
}

function showToast(container, message, icon) {
    const toast = container.querySelector("#room-toast");
    toast.querySelector("span").textContent = message;
    toast.querySelector("i").className = `ti ${icon}`;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2800);
}

function escapeHtml(valueToEscape) {
    return String(valueToEscape || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
