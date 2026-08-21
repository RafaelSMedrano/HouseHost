import { createGuest, deleteGuest, findGuestByIdForEdit, updateGuest } from "../api.js?v=2026-08-11-api-log-transport";
import { guestStatusBadgeClass, guestStatusLabel, normalizeGuestStatus } from "../guestStatus.js?v=2026-08-12-guest-status";

const states = ["", "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"];

// Renderiza o formulario completo no container da SPA; centraliza o HTML para a tela ser recriada ao navegar.
export function renderGuestFormView(containerId, options = {}) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    const guestId = options.guestId || null;
    const isEdit = Boolean(guestId);

    container.innerHTML = `
<div class="main guest-form-main">
  <div class="guest-form-content">
    <div class="guest-profile-preview">
      <div id="guest-preview-avatar" class="guest-preview-avatar">?</div>
      <div class="guest-preview-info">
        <div id="guest-preview-name" class="guest-preview-name empty">Nome do hospede aparece aqui</div>
        <div id="guest-preview-sub" class="guest-preview-sub">Preencha os campos para visualizar o perfil</div>
        <div id="guest-preview-badges" class="guest-preview-badges"></div>
      </div>
    </div>

    <form id="guest-form" class="guest-form">
      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-lav"><i class="ti ti-user"></i></span><div><strong>Identificacao</strong><small>Dados pessoais do hospede</small></div></div>
        <div class="guest-fields">
          ${field("fullName", "Nome completo", "text", "Ex: Maria Fernanda Costa", "ti-user", true, "wide")}
          ${field("documentNumber", "CPF", "text", "000.000.000-00", "ti-id-badge")}
          ${field("birthDate", "Data de nascimento", "date", "", "ti-calendar")}
          ${selectField("gender", "Genero", ["", "Feminino", "Masculino", "Nao binario", "Outro"], "Prefiro nao informar")}
          ${selectField("guestType", "Tipo", ["REGULAR", "VIP", "NOVO"], null, { REGULAR: "Regular", VIP: "VIP", NOVO: "Novo" })}
        </div>
      </section>

      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-sage"><i class="ti ti-device-mobile"></i></span><div><strong>Contato</strong><small>Telefone, email e localizacao</small></div></div>
        <div class="guest-fields">
          ${field("phone", "Telefone / WhatsApp", "tel", "(00) 00000-0000", "ti-device-mobile", true)}
          ${field("email", "Email", "email", "hospede@email.com", "ti-mail")}
          ${field("city", "Cidade", "text", "Ex: Curitiba", "ti-map-pin")}
          ${selectField("state", "Estado", states, "Selecione")}
          ${field("address", "Endereco completo", "text", "Rua, numero, bairro, CEP", "ti-home", false, "wide")}
        </div>
      </section>

      <section class="guest-section" data-guest-origin-channel>
        <div class="guest-section-head"><span class="guest-section-icon icon-sage"><i class="ti ti-antenna"></i></span><div><strong>Origem & Canal</strong><small>Como o hospede chegou ate nos</small></div></div>
        <div class="guest-fields">
          ${selectField("originChannel", "Canal de origem", ["Direto / Telefone", "WhatsApp", "Instagram", "Booking", "Airbnb", "Indicacao", "Google", "Outros"], null, {}, "wide")}
        </div>
      </section>

      <section class="guest-section" data-guest-care-fields>
        <div class="guest-section-head"><span class="guest-section-icon icon-amb"><i class="ti ti-heart"></i></span><div><strong>Preferências e restrições</strong><small>Informações de cuidado para a hospedagem</small></div></div>
        <div class="guest-fields guest-care-fields">
          ${textareaField("preferencesAndRestrictions", "Preferências e restrições", "Preferências alimentares, de acomodação ou outras restrições", "guest-care-help")}
          ${textareaField("accessibilityNeeds", "Necessidades de acessibilidade", "Descreva necessidades de mobilidade, comunicação ou acesso", "guest-care-help")}
          <small id="guest-care-help" class="guest-care-help">Campos opcionais, com até 4.000 caracteres cada. Evite registrar informações que não sejam necessárias para a hospedagem.</small>
        </div>
      </section>

      <section class="guest-section" data-guest-internal-notes>
        <div class="guest-section-head"><span class="guest-section-icon icon-rose"><i class="ti ti-notes"></i></span><div><strong>Observações internas</strong><small>Visível apenas para a equipe</small></div></div>
        <div class="guest-internal-notes">
          <textarea id="guest-notes" aria-label="Observações internas" placeholder="Pedidos habituais, situações especiais, histórico relevante..."></textarea>
        </div>
      </section>

      <section id="guest-danger-zone" class="guest-danger-zone ${isEdit && options.canDelete ? "" : "hidden"}">
        <div><strong>Excluir hospede</strong><span>Remove permanentemente o cadastro deste hospede.</span></div>
        <button id="guest-delete" class="guest-delete-button" type="button"><i class="ti ti-trash"></i> Excluir hospede</button>
      </section>
    </form>
  </div>

  <div class="guest-form-footer">
    <div><span id="guest-footer-mode">${isEdit ? "Modo edicao" : "Novo cadastro"}</span><strong id="guest-footer-status">Ainda nao salvo</strong></div>
    <div class="footer-actions">
      <button id="guest-cancel" class="btn-cancel" type="button"><i class="ti ti-x"></i> Cancelar</button>
      <button id="guest-save" class="btn-save" type="submit" form="guest-form"><i class="ti ti-check"></i> Salvar hospede</button>
    </div>
  </div>

  <div id="guest-toast" class="booking-toast" role="status" aria-live="polite" aria-atomic="true"><i class="ti ti-check"></i><span></span></div>
</div>
    `;

    bindGuestForm(container, { ...options, guestId });
}

// Liga eventos do formulario depois do HTML existir; separa renderizacao de comportamento para evitar handlers soltos.
function bindGuestForm(container, options) {
    // Estado local da tela: guarda dados que nao vivem em um input simples.
    // O status apenas explica o ciclo de vida server-owned e nunca vira um controle ou payload.
    const state = { status: "INACTIVE", guestId: options.guestId || null };
    const form = container.querySelector("#guest-form");

    form.addEventListener("input", () => updatePreview(container, state));
    container.querySelector("#guest-documentNumber").addEventListener("input", (event) => maskCpf(event.target));
    container.querySelector("#guest-phone").addEventListener("input", (event) => maskPhone(event.target));
    container.querySelector("#guest-cancel").addEventListener("click", () => {
        if (typeof options.onCancel === "function") {
            options.onCancel();
        }
    });
    container.querySelector("#guest-delete")?.addEventListener("click", () => handleDelete(container, options, state));
    // No submit, passamos explicitamente:
    // event: evento nativo do navegador; container: area onde procurar os campos;
    // options: callbacks vindos do controller; state: memoria temporaria desta tela.
    form.addEventListener("submit", (event) => handleSubmit(event, container, options, state));

    if (state.guestId) {
        loadGuest(container, state);
    }

    updatePreview(container, state);
}

// Carrega um hospede existente no modo edicao; permite reaproveitar o mesmo formulario para POST e PUT.
async function loadGuest(container, state) {
    try {
        const response = await findGuestByIdForEdit(state.guestId);
        const guest = response.data;
        setFormValue(container, "fullName", guest.fullName);
        setFormValue(container, "documentNumber", guest.documentNumber);
        setFormValue(container, "birthDate", guest.birthDate);
        setFormValue(container, "gender", guest.gender);
        setFormValue(container, "guestType", guest.guestType || "REGULAR");
        state.status = normalizeGuestStatus(guest.status);
        setFormValue(container, "phone", guest.phone);
        setFormValue(container, "email", guest.email);
        setFormValue(container, "city", guest.city);
        setFormValue(container, "state", guest.state);
        setFormValue(container, "address", guest.address);
        setFormValue(container, "preferencesAndRestrictions", guest.preferencesAndRestrictions);
        setFormValue(container, "accessibilityNeeds", guest.accessibilityNeeds);
        setFormValue(container, "originChannel", guest.originChannel);
        container.querySelector("#guest-notes").value = guest.notes || "";
        updatePreview(container, state);
        container.querySelector("#guest-footer-status").textContent = `ID #${String(guest.id).padStart(3, "0")}`;
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
    }
}

// Trata o submit real do formulario; decide entre criar e atualizar conforme exista guestId no estado.
async function handleSubmit(event, container, options, state) {
    // Impede o submit tradicional do HTML, que recarregaria a pagina e quebraria o fluxo SPA.
    event.preventDefault();

    // Transforma os campos editaveis em um objeto pronto para enviar ao backend.
    const payload = collectGuestPayload(container, state);
    if (!payload.fullName || !payload.phone) {
        showToast(container, "Preencha nome completo e telefone.", "ti-alert-circle");
        return;
    }

    const saveButton = container.querySelector("#guest-save");
    saveButton.disabled = true;
    saveButton.innerHTML = '<i class="ti ti-loader-2 spinning"></i> Salvando...';

    try {
        const response = state.guestId
            ? await updateGuest(state.guestId, payload)
            : await createGuest(payload);
        state.guestId = response.data?.id || state.guestId;
        container.querySelector("#guest-footer-mode").textContent = "Cadastro salvo";
        container.querySelector("#guest-footer-status").textContent = `ID #${String(state.guestId).padStart(3, "0")}`;
        showToast(container, response.message || "Hospede salvo com sucesso.", "ti-user-check");

        if (typeof options.onSaved === "function") {
            options.onSaved(response.data);
        }
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
    } finally {
        saveButton.disabled = false;
        saveButton.innerHTML = '<i class="ti ti-check"></i> Salvar hospede';
    }
}

// Remove um hospede salvo; fica separado do submit porque usa outro verbo HTTP e exige confirmacao.
async function handleDelete(container, options, state) {
    if (!state.guestId || !window.confirm("Excluir este hospede permanentemente?")) {
        return;
    }

    try {
        await deleteGuest(state.guestId);
        showToast(container, "Hospede excluido com sucesso.", "ti-trash");
        if (typeof options.onDeleted === "function") {
            options.onDeleted();
        }
    } catch (error) {
        showToast(container, error.message, "ti-alert-circle");
    }
}

// Monta o payload no formato esperado pelo backend; isola a conversao dos campos visuais para DTO.
export function collectGuestPayload(container, state) {
    return {
        fullName: value(container, "fullName"),
        documentNumber: value(container, "documentNumber"),
        birthDate: value(container, "birthDate") || null,
        gender: value(container, "gender"),
        guestType: value(container, "guestType") || "REGULAR",
        phone: value(container, "phone"),
        email: value(container, "email"),
        city: value(container, "city"),
        state: value(container, "state"),
        address: value(container, "address"),
        preferencesAndRestrictions: value(container, "preferencesAndRestrictions"),
        accessibilityNeeds: value(container, "accessibilityNeeds"),
        originChannel: value(container, "originChannel"),
        notes: container.querySelector("#guest-notes").value.trim(),
    };
}

// Atualiza o card de pre-visualizacao enquanto o usuario digita; ajuda a conferir identidade e marcadores.
function updatePreview(container, state) {
    const name = value(container, "fullName");
    const email = value(container, "email");
    const city = value(container, "city");
    const uf = value(container, "state");
    const type = value(container, "guestType");
    const avatar = container.querySelector("#guest-preview-avatar");
    const previewName = container.querySelector("#guest-preview-name");
    const badges = [];

    avatar.textContent = initialsFor(name);
    previewName.textContent = name || "Nome do hospede aparece aqui";
    previewName.classList.toggle("empty", !name);
    container.querySelector("#guest-preview-sub").textContent = [email, city && uf ? `${city} - ${uf}` : city || uf].filter(Boolean).join(" · ") || "Preencha os campos para visualizar o perfil";

    if (type === "VIP") badges.push('<span class="guest-preview-badge vip">VIP</span>');
    if (type === "NOVO") badges.push('<span class="guest-preview-badge new">Novo</span>');
    badges.push(`<span class="guest-preview-badge ${guestStatusBadgeClass(state.status)}">${guestStatusLabel(state.status)}</span>`);
    container.querySelector("#guest-preview-badges").innerHTML = badges.join("");
}

// Gera um campo de texto/numero/data padronizado; reduz repeticao no HTML das secoes do formulario.
function field(id, label, type, placeholder, icon, required = false, extraClass = "") {
    return `
      <label class="guest-field ${extraClass}">
        <span>${label}${required ? " *" : ""}</span>
        <div><i class="ti ${icon}"></i><input id="guest-${id}" type="${type}" placeholder="${placeholder}" ${required ? "required" : ""}></div>
      </label>
    `;
}

function textareaField(id, label, placeholder, describedBy) {
    return `
      <label class="guest-field wide guest-care-field">
        <span>${label}</span>
        <div><textarea id="guest-${id}" placeholder="${placeholder}" maxlength="4000" aria-describedby="${describedBy}"></textarea></div>
      </label>
    `;
}

// Gera selects padronizados; permite trocar labels exibidos sem mudar os valores enviados ao backend.
function selectField(id, label, options, placeholder = null, labels = {}, extraClass = "") {
    return `
      <label class="guest-field no-icon ${extraClass}">
        <span>${label}</span>
        <div><select id="guest-${id}">
          ${options.map((option, index) => `<option value="${escapeHtml(option)}">${index === 0 && placeholder ? placeholder : escapeHtml(labels[option] || option)}</option>`).join("")}
        </select></div>
      </label>
    `;
}

// Le o valor textual de um campo pelo id logico; evita repetir seletores e normalizacao trim.
function value(container, id) {
    return container.querySelector(`#guest-${id}`)?.value.trim() || "";
}

// Preenche campos textuais vindos da API no modo edicao, preservando quebras de linha.
function setFormValue(container, id, valueToSet) {
    const element = container.querySelector(`#guest-${id}`);
    if (!element) return;
    element.value = valueToSet ?? "";
}

// Mostra feedback temporario dentro da tela; evita depender de alert para respostas de salvamento.
function showToast(container, message, icon) {
    const toast = container.querySelector("#guest-toast");
    toast.setAttribute("role", icon === "ti-alert-circle" ? "alert" : "status");
    toast.querySelector("span").textContent = message;
    toast.querySelector("i").className = `ti ${icon}`;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2800);
}

// Calcula iniciais do nome para o avatar textual; mantem a preview util sem precisar de imagem.
function initialsFor(name) {
    return name
        ? name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0].toUpperCase()).join("")
        : "?";
}

// Aplica mascara visual de CPF no input; melhora digitacao sem alterar a responsabilidade de validar no backend.
function maskCpf(input) {
    let valueToMask = input.value.replace(/\D/g, "").substring(0, 11);
    valueToMask = valueToMask.replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d)/, "$1.$2")
        .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
    input.value = valueToMask;
}

// Aplica mascara visual de telefone brasileiro; melhora leitura mantendo o valor como texto simples.
function maskPhone(input) {
    let valueToMask = input.value.replace(/\D/g, "").substring(0, 11);
    if (valueToMask.length <= 10) {
        valueToMask = valueToMask.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{4})(\d)/, "$1-$2");
    } else {
        valueToMask = valueToMask.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{5})(\d)/, "$1-$2");
    }
    input.value = valueToMask;
}

// Escapa texto usado em HTML gerado por template; protege chips e options contra markup acidental.
function escapeHtml(valueToEscape) {
    return String(valueToEscape || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
