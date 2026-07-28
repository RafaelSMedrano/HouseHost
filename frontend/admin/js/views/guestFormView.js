import { createGuest, deleteGuest, findGuestByIdForEdit, updateGuest } from "../api.js?v=2026-06-15-contact-reveal";

const states = ["", "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"];
const quickPreferences = ["Sem gluten", "Vegano", "Vegetariano", "Sem lactose", "Silencio apos 22h", "Vista p/ jardim", "Andar terreo", "Travesseiro extra"];
const starLabels = ["Sem avaliacao", "Ruim", "Regular", "Bom", "Muito bom", "Excelente"];

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
          ${selectField("status", "Status", ["IN_BOOKING", "IN_STAY", "GOT_CHECKOUT"], null, { IN_BOOKING: "Com reserva", IN_STAY: "Em estadia", GOT_CHECKOUT: "Com check out" })}
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

      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-amb"><i class="ti ti-heart"></i></span><div><strong>Preferencias & Restricoes</strong><small>Alimentacao, acomodacao e necessidades especiais</small></div></div>
        <div class="guest-preference-input">
          <label class="guest-field wide"><span>Preferencias e restricoes</span><div><i class="ti ti-tag"></i><input id="guest-pref-input" type="text" placeholder="Digite uma preferencia"></div></label>
          <button id="guest-pref-add" class="guest-icon-button" type="button" title="Adicionar preferencia"><i class="ti ti-plus"></i></button>
        </div>
        <div class="guest-quick-prefs">${quickPreferences.map((preference) => `<button class="guest-pref-suggestion" type="button" data-pref="${escapeHtml(preference)}">${escapeHtml(preference)}</button>`).join("")}</div>
        <div id="guest-pref-chips" class="guest-pref-chips"></div>
        <div class="guest-fields compact">
          ${switchField("travelsWithPets", "Viaja com pets?")}
          ${field("petType", "Tipo de pet", "text", "Ex: cachorro pequeno porte", "ti-paw")}
          ${switchField("needsAccessibility", "Necessidade de acessibilidade?")}
          ${selectField("favoriteRoom", "Quarto favorito", ["", "Suite Lavanda", "Suite Salvia", "Chale Bergamota", "Standard"], "Sem preferencia")}
        </div>
      </section>

      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-lav"><i class="ti ti-chart-bar"></i></span><div><strong>Historico & Avaliacao</strong><small>Dados de hospedagem e satisfacao</small></div></div>
        <div class="guest-fields cols3">
          ${field("stayCount", "Numero de estadias", "number", "0", "ti-calendar-check")}
          ${field("totalSpent", "Total gasto (R$)", "number", "0,00", "ti-currency-real")}
          ${field("lastStayDate", "Ultima estadia", "date", "", "ti-calendar-event")}
        </div>
        <div class="guest-rating-row">
          <span>Avaliacao do hospede</span>
          <div id="guest-rating-stars" class="guest-rating-stars">
            ${[1, 2, 3, 4, 5].map((value) => `<button type="button" data-rating="${value}"><i class="ti ti-star"></i></button>`).join("")}
          </div>
          <strong id="guest-rating-label">Sem avaliacao</strong>
        </div>
      </section>

      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-sage"><i class="ti ti-antenna"></i></span><div><strong>Origem & Canal</strong><small>Como o hospede chegou ate nos</small></div></div>
        <div class="guest-fields">
          ${selectField("originChannel", "Canal de origem", ["Direto / Telefone", "WhatsApp", "Instagram", "Booking", "Airbnb", "Indicacao", "Google", "Outros"])}
          ${field("referredBy", "Indicado por", "text", "Nome de quem indicou", "ti-users")}
        </div>
      </section>

      <section class="guest-section">
        <div class="guest-section-head"><span class="guest-section-icon icon-rose"><i class="ti ti-notes"></i></span><div><strong>Observacoes internas</strong><small>Visivel apenas para a equipe</small></div></div>
        <label class="guest-field wide no-icon"><span>Anotacoes</span><div><textarea id="guest-notes" placeholder="Pedidos habituais, situacoes especiais, historico relevante..."></textarea></div></label>
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

  <div id="guest-toast" class="booking-toast"><i class="ti ti-check"></i><span></span></div>
</div>
    `;

    bindGuestForm(container, { ...options, guestId });
}

// Liga eventos do formulario depois do HTML existir; separa renderizacao de comportamento para evitar handlers soltos.
function bindGuestForm(container, options) {
    // Estado local da tela: guarda dados que nao vivem em um input simples.
    // preferences vem dos chips, rating vem das estrelas e guestId decide se o submit cria ou atualiza.
    const state = { preferences: [], rating: 0, guestId: options.guestId || null };
    const form = container.querySelector("#guest-form");

    form.addEventListener("input", () => updatePreview(container, state));
    container.querySelector("#guest-documentNumber").addEventListener("input", (event) => maskCpf(event.target));
    container.querySelector("#guest-phone").addEventListener("input", (event) => maskPhone(event.target));
    container.querySelector("#guest-pref-add").addEventListener("click", () => addPreference(container, state));
    container.querySelector("#guest-pref-input").addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            addPreference(container, state);
        }
    });
    container.querySelectorAll("[data-pref]").forEach((button) => {
        button.addEventListener("click", () => addPreferenceValue(container, state, button.dataset.pref));
    });
    container.querySelectorAll("[data-rating]").forEach((button) => {
        button.addEventListener("click", () => {
            state.rating = Number(button.dataset.rating);
            renderRating(container, state.rating);
            updatePreview(container, state);
        });
    });
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
        setFormValue(container, "status", normalizeGuestStatus(guest.status));
        setFormValue(container, "phone", guest.phone);
        setFormValue(container, "email", guest.email);
        setFormValue(container, "city", guest.city);
        setFormValue(container, "state", guest.state);
        setFormValue(container, "address", guest.address);
        setFormValue(container, "travelsWithPets", guest.travelsWithPets);
        setFormValue(container, "petType", guest.petType);
        setFormValue(container, "needsAccessibility", guest.needsAccessibility);
        setFormValue(container, "favoriteRoom", guest.favoriteRoom);
        setFormValue(container, "stayCount", guest.stayCount);
        setFormValue(container, "totalSpent", guest.totalSpent);
        setFormValue(container, "lastStayDate", guest.lastStayDate);
        setFormValue(container, "originChannel", guest.originChannel);
        setFormValue(container, "referredBy", guest.referredBy);
        container.querySelector("#guest-notes").value = guest.notes || "";
        state.preferences = guest.preferences || [];
        state.rating = guest.rating || 0;
        renderPreferences(container, state);
        renderRating(container, state.rating);
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

    // Transforma inputs, checkboxes, estrelas e chips em um objeto pronto para enviar ao backend.
    const payload = collectPayload(container, state);
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
function collectPayload(container, state) {
    return {
        fullName: value(container, "fullName"),
        documentNumber: value(container, "documentNumber"),
        birthDate: value(container, "birthDate") || null,
        gender: value(container, "gender"),
        guestType: value(container, "guestType") || "REGULAR",
        status: value(container, "status") || "IN_BOOKING",
        phone: value(container, "phone"),
        email: value(container, "email"),
        city: value(container, "city"),
        state: value(container, "state"),
        address: value(container, "address"),
        travelsWithPets: checked(container, "travelsWithPets"),
        petType: value(container, "petType"),
        needsAccessibility: checked(container, "needsAccessibility"),
        favoriteRoom: value(container, "favoriteRoom"),
        stayCount: numberValue(container, "stayCount"),
        totalSpent: numberValue(container, "totalSpent"),
        lastStayDate: value(container, "lastStayDate") || null,
        rating: state.rating,
        originChannel: value(container, "originChannel"),
        referredBy: value(container, "referredBy"),
        notes: container.querySelector("#guest-notes").value.trim(),
        preferences: state.preferences,
    };
}

// Atualiza o card de pre-visualizacao enquanto o usuario digita; ajuda a conferir identidade e marcadores.
function updatePreview(container, state) {
    const name = value(container, "fullName");
    const email = value(container, "email");
    const city = value(container, "city");
    const uf = value(container, "state");
    const type = value(container, "guestType");
    const status = value(container, "status");
    const travelsWithPets = checked(container, "travelsWithPets");
    const avatar = container.querySelector("#guest-preview-avatar");
    const previewName = container.querySelector("#guest-preview-name");
    const badges = [];

    avatar.textContent = initialsFor(name);
    previewName.textContent = name || "Nome do hospede aparece aqui";
    previewName.classList.toggle("empty", !name);
    container.querySelector("#guest-preview-sub").textContent = [email, city && uf ? `${city} - ${uf}` : city || uf].filter(Boolean).join(" · ") || "Preencha os campos para visualizar o perfil";

    if (type === "VIP") badges.push('<span class="guest-preview-badge vip">VIP</span>');
    if (type === "NOVO") badges.push('<span class="guest-preview-badge new">Novo</span>');
    if (status) badges.push(`<span class="guest-preview-badge regular">${guestStatusLabel(status)}</span>`);
    if (travelsWithPets) badges.push('<span class="guest-preview-badge pet">Pet</span>');
    if (state.rating > 0) badges.push(`<span class="guest-preview-badge rating">${state.rating} estrelas</span>`);
    container.querySelector("#guest-preview-badges").innerHTML = badges.join("");
}

function guestStatusLabel(status) {
    return {
        IN_BOOKING: "Com reserva",
        IN_STAY: "Em estadia",
        GOT_CHECKOUT: "Com check out",
    }[normalizeGuestStatus(status)] || status;
}

function normalizeGuestStatus(status) {
    const normalized = String(status || "IN_BOOKING").toUpperCase();
    const aliases = {
        COM_RESERVA: "IN_BOOKING",
        EM_ESTADIA: "IN_STAY",
        COM_CHECK_OUT: "GOT_CHECKOUT",
    };
    return aliases[normalized] || normalized;
}

// Adiciona a preferencia digitada no input; limpa o campo para facilitar adicionar varias em sequencia.
function addPreference(container, state) {
    const input = container.querySelector("#guest-pref-input");
    addPreferenceValue(container, state, input.value);
    input.value = "";
}

// Adiciona uma preferencia vinda do input ou de sugestao rapida; evita valores vazios e duplicados.
function addPreferenceValue(container, state, rawValue) {
    const preference = String(rawValue || "").trim();
    if (!preference || state.preferences.includes(preference)) {
        return;
    }

    state.preferences.push(preference);
    renderPreferences(container, state);
}

// Redesenha os chips de preferencias; recria tambem os botoes de remover porque o HTML foi substituido.
function renderPreferences(container, state) {
    container.querySelector("#guest-pref-chips").innerHTML = state.preferences.map((preference, index) => `
      <span class="guest-pref-chip">${escapeHtml(preference)}<button type="button" data-remove-pref="${index}"><i class="ti ti-x"></i></button></span>
    `).join("");
    container.querySelectorAll("[data-remove-pref]").forEach((button) => {
        button.addEventListener("click", () => {
            state.preferences.splice(Number(button.dataset.removePref), 1);
            renderPreferences(container, state);
        });
    });
}

// Reflete visualmente a nota selecionada; mantem estrelas e label sincronizadas com o estado.
function renderRating(container, rating) {
    container.querySelectorAll("[data-rating]").forEach((button) => {
        const active = Number(button.dataset.rating) <= rating;
        button.classList.toggle("active", active);
        button.querySelector("i").className = active ? "ti ti-star-filled" : "ti ti-star";
    });
    container.querySelector("#guest-rating-label").textContent = starLabels[rating] || starLabels[0];
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

// Gera selects padronizados; permite trocar labels exibidos sem mudar os valores enviados ao backend.
function selectField(id, label, options, placeholder = null, labels = {}) {
    return `
      <label class="guest-field no-icon">
        <span>${label}</span>
        <div><select id="guest-${id}">
          ${options.map((option, index) => `<option value="${escapeHtml(option)}">${index === 0 && placeholder ? placeholder : escapeHtml(labels[option] || option)}</option>`).join("")}
        </select></div>
      </label>
    `;
}

// Gera campos booleanos como checkbox; usado para flags simples do cadastro de hospede.
function switchField(id, label) {
    return `
      <label class="guest-switch-field">
        <span>${label}</span>
        <input id="guest-${id}" type="checkbox">
      </label>
    `;
}

// Le o valor textual de um campo pelo id logico; evita repetir seletores e normalizacao trim.
function value(container, id) {
    return container.querySelector(`#guest-${id}`)?.value.trim() || "";
}

// Le checkboxes pelo id logico; deixa claro quando o payload espera boolean.
function checked(container, id) {
    return Boolean(container.querySelector(`#guest-${id}`)?.checked);
}

// Converte campos numericos vazios para null; evita enviar zero quando o usuario nao informou valor.
function numberValue(container, id) {
    const rawValue = value(container, id);
    return rawValue === "" ? null : Number(rawValue);
}

// Preenche campos vindos da API no modo edicao; trata checkbox diferente de inputs/selects.
function setFormValue(container, id, valueToSet) {
    const element = container.querySelector(`#guest-${id}`);
    if (!element) return;
    if (element.type === "checkbox") {
        element.checked = Boolean(valueToSet);
        return;
    }
    element.value = valueToSet ?? "";
}

// Mostra feedback temporario dentro da tela; evita depender de alert para respostas de salvamento.
function showToast(container, message, icon) {
    const toast = container.querySelector("#guest-toast");
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
