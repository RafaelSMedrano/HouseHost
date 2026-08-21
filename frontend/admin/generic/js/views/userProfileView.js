import { updateUserPhoto, updateUserProfile } from "../api.js?v=2026-08-11-api-log-transport";

export function renderUserProfileView(containerId, user = {}, options = {}) {
    const container = document.getElementById(containerId);
    const username = user.username || user.name || "Usuario";
    const role = roleLabel(user.role);
    const email = user.email || "";
    const photoUrl = user.photoUrl || "";
    const accountId = user.id ? `#${user.id}` : "#--";
    const initials = initialsFor(username);
    const firstName = firstNameFor(username);
    const lastName = lastNameFor(username);
    const canManageUsers = Boolean(options.canManageUsers);

    container.innerHTML = `
  <div class="content user-profile-page">
    <div class="up-top-actions">
      <button class="dashboard-back-btn user-profile-back" type="button" aria-label="Voltar"><i class="ti ti-arrow-left" aria-hidden="true"></i> Voltar</button>
      <div class="up-actions">
        <button class="up-btn-ghost" type="button" data-up-cancel><i class="ti ti-x"></i> Cancelar</button>
        <button class="up-btn-save" type="button" data-up-save><i class="ti ti-check"></i> Salvar alterações</button>
      </div>
    </div>

    <div class="up-page">
      <section class="up-hero-card">
        <div class="up-hero-av-wrap">
          <button class="up-hero-av" type="button" data-up-upload>
            <span data-up-hero-initials class="${photoUrl ? "up-hidden" : ""}">${escapeHtml(initials)}</span>
            <img data-up-hero-img src="${escapeHtml(photoUrl)}" alt="" class="${photoUrl ? "show" : ""}">
          </button>
          <button class="up-hero-av-edit" type="button" data-up-upload title="Alterar foto"><i class="ti ti-camera"></i></button>
        </div>
        <div class="up-hero-info">
          <div class="up-hero-role">Perfil do usuário</div>
          <div class="up-hero-name" data-up-hero-name>${escapeHtml(username)}</div>
          <div class="up-hero-email" data-up-hero-email>${escapeHtml(email || "E-mail não informado")}</div>
          <div class="up-hero-tags">
            <span class="up-hero-tag active"><span class="up-live-dot"></span> Conta ativa</span>
            <span class="up-hero-tag"><i class="ti ti-shield-check"></i> ${escapeHtml(role)}</span>
            <span class="up-hero-tag"><i class="ti ti-hash"></i> ${escapeHtml(accountId)}</span>
          </div>
        </div>
        <div class="up-hero-stats">
          <div>
            <div class="up-hs-id">${escapeHtml(accountId)}<span>id</span></div>
            <div class="up-hs-since">Membro do<br>sistema</div>
          </div>
        </div>
      </section>

      <section class="up-section-card">
        <div class="up-section-head">
          <div class="up-sec-icon up-si-lav"><i class="ti ti-user-circle"></i></div>
          <div><div class="up-sec-title">Resumo da conta</div><div class="up-sec-desc">Visão geral do seu perfil no sistema</div></div>
        </div>
        <div class="up-section-body">
          <div class="up-summary-grid">
            ${summaryCell("Cargo", role, "Acesso do usuário")}
            ${summaryCell("Identificador", accountId, "Usuário logado", true)}
            ${summaryCell("Status", `<span class="up-sg-dot"></span>Ativa`, "Conta verificada")}
            ${summaryCell("Foto", photoUrl ? "Enviada" : "Pendente", photoUrl ? "Imagem cadastrada" : "Não enviada")}
          </div>
        </div>
      </section>

      <section class="up-section-card">
        <div class="up-section-head">
          <div class="up-sec-icon up-si-sage"><i class="ti ti-user"></i></div>
          <div><div class="up-sec-title">Informações pessoais</div><div class="up-sec-desc">Seus dados de identificação no sistema</div></div>
        </div>
        <div class="up-section-body">
          <div class="up-form-grid">
            ${inputField("Nome", "up-name", firstName, "ti-user")}
            ${inputField("Sobrenome", "up-lastname", lastName, "ti-user")}
            ${inputField("Nome de exibição", "up-display", username, "ti-at", "Aparece na barra lateral e em notificações internas.")}
            ${selectField("Cargo / Função", "up-role", role, ["Chefe Executivo Organizacional (CEO)", "Diretor Geral de Tecnologia (CTO)", "Administrador", "Gerente", "Recepção", "Governança"], "Apenas administradores podem alterar cargos.", !canManageUsers)}
          </div>
        </div>
      </section>

      <section class="up-section-card">
        <div class="up-section-head">
          <div class="up-sec-icon up-si-lav"><i class="ti ti-mail"></i></div>
          <div><div class="up-sec-title">Contato</div><div class="up-sec-desc">E-mail e telefone da conta</div></div>
        </div>
        <div class="up-section-body">
          <div class="up-form-grid">
            ${inputField("E-mail", "up-email", email, "ti-mail", "Usado para login e notificações do sistema.", "email")}
            ${inputField("Telefone / WhatsApp", "up-phone", user.phone || "", "ti-device-mobile", "", "tel", "(00) 00000-0000")}
          </div>
        </div>
      </section>

      <section class="up-section-card">
        <div class="up-section-head">
          <div class="up-sec-icon up-si-amb"><i class="ti ti-photo"></i></div>
          <div><div class="up-sec-title">Foto de perfil</div><div class="up-sec-desc">Imagem exibida no sistema</div></div>
        </div>
        <div class="up-section-body">
          <div class="up-avatar-upload-wrap">
            <button class="up-av-preview" type="button" data-up-upload>
              <span data-up-av-initials class="${photoUrl ? "up-hidden" : ""}">${escapeHtml(initials)}</span>
              <img data-up-av-img src="${escapeHtml(photoUrl)}" alt="" class="${photoUrl ? "show" : ""}">
              <span class="up-av-preview-overlay"><i class="ti ti-camera"></i></span>
            </button>
            <div class="up-av-upload-info">
              <div class="up-av-upload-title">${photoUrl ? "Foto cadastrada" : "Nenhuma foto enviada"}</div>
              <div class="up-av-upload-desc">Envie uma foto para personalizar seu perfil. Formatos aceitos: JPG, PNG, WEBP. Tamanho máximo: 2MB.</div>
              <div class="up-av-upload-actions">
                <button type="button" class="up-btn-upload" data-up-upload><i class="ti ti-upload"></i> Enviar foto</button>
                <button type="button" class="up-btn-rm-av ${photoUrl ? "show" : ""}" data-up-remove-photo><i class="ti ti-trash"></i> Remover</button>
              </div>
            </div>
          </div>
          <input type="file" data-up-file accept="image/*" hidden>
        </div>
      </section>

      <section class="up-section-card">
        <div class="up-section-head">
          <div class="up-sec-icon up-si-rose"><i class="ti ti-lock"></i></div>
          <div><div class="up-sec-title">Segurança</div><div class="up-sec-desc">Senha e autenticação da conta</div></div>
        </div>
        <div class="up-section-body">
          <div class="up-form-grid">
            ${passwordField("Senha atual", "up-pass-current", "Senha atual")}
            ${passwordField("Nova senha", "up-pass-new", "Mínimo 8 caracteres", true)}
            ${passwordField("Confirmar nova senha", "up-pass-confirm", "Repita a nova senha")}
          </div>
          <div class="up-sep"></div>
          <div class="up-inline-action">
            <div><strong>Autenticação em dois fatores</strong><span>Adicione uma camada extra de segurança à sua conta.</span></div>
            <button class="up-btn-ghost" type="button" data-up-toast="2FA em breve"><i class="ti ti-shield"></i> Configurar 2FA</button>
          </div>
        </div>
      </section>

      <section class="up-section-card">
        <div class="up-section-head">
          <div class="up-sec-icon up-si-lav"><i class="ti ti-shield-check"></i></div>
          <div><div class="up-sec-title">Permissões</div><div class="up-sec-desc">Nível de acesso às funcionalidades do sistema</div></div>
        </div>
        <div class="up-section-body">
          <div class="up-perm-list">
            ${permissionRows(options)}
          </div>
        </div>
      </section>

      <section class="up-section-card">
        <div class="up-section-head">
          <div class="up-sec-icon up-si-sage"><i class="ti ti-history"></i></div>
          <div><div class="up-sec-title">Atividade recente</div><div class="up-sec-desc">Últimas ações realizadas no sistema</div></div>
        </div>
        <div class="up-section-body">
          <div class="up-activity-list">
            ${activityRow("ti-login", "Login realizado", "Sessão atual", "Hoje", "up-ai-sage")}
            ${activityRow("ti-user-check", "Perfil acessado", "Visualização do perfil de usuário", "Agora", "up-ai-lav")}
            ${activityRow("ti-pencil", "Perfil atualizado", "Última alteração local", "Sistema", "up-ai-amb")}
          </div>
        </div>
      </section>

      <section class="up-section-card">
        <div class="up-section-head">
          <div class="up-sec-icon up-si-amb"><i class="ti ti-settings"></i></div>
          <div><div class="up-sec-title">Preferências do sistema</div><div class="up-sec-desc">Configurações de interface e notificações</div></div>
        </div>
        <div class="up-section-body">
          <div class="up-form-grid">
            ${selectField("Idioma", "up-language", "Português (Brasil)", ["Português (Brasil)", "English (US)", "Español"])}
            ${selectField("Fuso horário", "up-timezone", "América/São_Paulo (UTC-3)", ["América/São_Paulo (UTC-3)", "América/Manaus (UTC-4)", "América/Noronha (UTC-2)"])}
            ${selectField("Formato de data", "up-date-format", "DD/MM/AAAA", ["DD/MM/AAAA", "MM/DD/AAAA", "AAAA-MM-DD"])}
            ${selectField("Notificações por e-mail", "up-notifications", "Todas as atividades", ["Todas as atividades", "Apenas reservas", "Apenas check-ins", "Desativadas"])}
          </div>
        </div>
      </section>

      <section class="up-danger-zone">
        <div><strong><i class="ti ti-alert-triangle"></i> Encerrar sessão em todos os dispositivos</strong><span>Desconecta sua conta de todos os navegadores e dispositivos onde você estiver logado.</span></div>
        <button class="up-btn-danger" type="button" data-up-toast="Sessões encerradas"><i class="ti ti-logout"></i> Encerrar todas as sessões</button>
      </section>
    </div>

    <div class="up-footer">
      <div><span>Usuário logado</span><strong>${escapeHtml(username)} · ${escapeHtml(role)} · ${escapeHtml(accountId)}</strong></div>
      <div class="up-actions">
        <button class="up-btn-ghost" type="button" data-up-cancel><i class="ti ti-x"></i> Cancelar</button>
        <button class="up-btn-save" type="button" data-up-save><i class="ti ti-check"></i> Salvar alterações</button>
      </div>
    </div>

    <div class="up-toast" data-up-toast-box><i class="ti ti-check"></i><span></span></div>
  </div>
    `;

    wireUserProfile(container, options, { username, initials, userId: user.id, user });
}

function wireUserProfile(container, options, initialState) {
    let hasPhoto = Boolean(container.querySelector("[data-up-hero-img]")?.getAttribute("src"));
    const fileInput = container.querySelector("[data-up-file]");

    container.querySelector(".user-profile-back").addEventListener("click", () => options.onBack?.());
    container.querySelectorAll("[data-up-cancel]").forEach((button) => button.addEventListener("click", () => showToast(container, "Alterações descartadas", "ti-x")));
    container.querySelectorAll("[data-up-save]").forEach((button) => button.addEventListener("click", () => saveProfile(container, button, initialState)));
    container.querySelectorAll("[data-up-upload]").forEach((button) => button.addEventListener("click", () => fileInput.click()));
    container.querySelector("[data-up-remove-photo]").addEventListener("click", () => {
        hasPhoto = false;
        removePhoto(container, initialState);
    });
    container.querySelectorAll("[data-up-toast]").forEach((button) => button.addEventListener("click", () => showToast(container, button.dataset.upToast || "Pronto")));

    ["up-name", "up-lastname", "up-email"].forEach((id) => {
        container.querySelector(`#${id}`)?.addEventListener("input", () => updatePreview(container, hasPhoto, initialState.username));
    });

    container.querySelector("#up-phone")?.addEventListener("input", (event) => {
        event.target.value = maskPhone(event.target.value);
    });

    container.querySelector("#up-pass-new")?.addEventListener("input", (event) => updatePasswordStrength(container, event.target.value));
    container.querySelectorAll("[data-up-toggle-pass]").forEach((button) => {
        button.addEventListener("click", () => togglePassword(container, button.dataset.upTogglePass, button));
    });

    fileInput.addEventListener("change", () => {
        if (!fileInput.files || !fileInput.files[0]) {
            return;
        }

        const file = fileInput.files[0];
        if (file.size > 2 * 1024 * 1024) {
            showToast(container, "Foto muito grande (máx 2MB)", "ti-alert-circle");
            return;
        }

        const reader = new FileReader();
        reader.onload = async (event) => {
            const src = event.target.result;
            hasPhoto = true;
            applyPhoto(container, src);
            container.querySelector(".up-av-upload-title").textContent = file.name;
            container.querySelector(".up-av-upload-desc").textContent = `${(file.size / 1024).toFixed(0)} KB · Foto enviada com sucesso`;
            container.querySelector("[data-up-remove-photo]").classList.add("show");

            if (!initialState.userId) {
                showToast(container, "Foto adicionada ao perfil", "ti-photo");
                return;
            }

            try {
                const response = await updateUserPhoto(initialState.userId, src);
                if (response.status !== "success") {
                    throw new Error(response.message || "Nao foi possivel salvar a foto.");
                }

                initialState.user.photoUrl = src;
                updateSidebarPhoto(src);
                showToast(container, "Foto salva com sucesso", "ti-photo");
            } catch (error) {
                showToast(container, error.message || "Nao foi possivel salvar a foto.", "ti-alert-circle");
            }
        };
        reader.readAsDataURL(file);
    });
}

function summaryCell(label, value, sub, large = false) {
    return `
      <div class="up-sg-cell">
        <div class="up-sg-label">${escapeHtml(label)}</div>
        <div class="up-sg-val ${large ? "" : "sm"}">${value}</div>
        <div class="up-sg-sub">${escapeHtml(sub)}</div>
      </div>
    `;
}

function inputField(label, id, value, icon, hint = "", type = "text", placeholder = "") {
    return `
      <label class="up-field">
        <span class="up-field-label">${escapeHtml(label)}</span>
        <div class="up-input-wrap"><i class="ti ${icon}"></i><input type="${escapeHtml(type)}" id="${escapeHtml(id)}" value="${escapeHtml(value)}" placeholder="${escapeHtml(placeholder)}"></div>
        ${hint ? `<small>${escapeHtml(hint)}</small>` : ""}
      </label>
    `;
}

function selectField(label, id, value, options, hint = "", disabled = false) {
    return `
      <label class="up-field">
        <span class="up-field-label">${escapeHtml(label)}</span>
        <div class="up-input-wrap no-icon"><select id="${escapeHtml(id)}" ${disabled ? "disabled" : ""}>${options.map((option) => `<option ${option === value ? "selected" : ""}>${escapeHtml(option)}</option>`).join("")}</select></div>
        ${hint ? `<small>${escapeHtml(hint)}</small>` : ""}
      </label>
    `;
}

function passwordField(label, id, placeholder, strength = false) {
    return `
      <label class="up-field">
        <span class="up-field-label">${escapeHtml(label)}</span>
        <div class="up-input-wrap"><i class="ti ti-lock"></i><input type="password" id="${escapeHtml(id)}" placeholder="${escapeHtml(placeholder)}"><button type="button" class="up-pass-toggle" data-up-toggle-pass="${escapeHtml(id)}"><i class="ti ti-eye"></i></button></div>
        ${strength ? `<div class="up-pass-strength"><div><span data-up-pass-bar></span></div><small data-up-pass-label></small></div>` : ""}
      </label>
    `;
}

function permissionRows(options) {
    const operationalStatus = options.canManageOperationalData
        ? options.canDeleteOperationalData ? "Acesso total" : "Sem exclusão"
        : "Somente consulta";

    return [
        permissionRow("Reservas", "Consultar, criar e atualizar reservas", operationalStatus),
        permissionRow("Hóspedes", "Consultar, cadastrar e atualizar hóspedes", options.canManageOperationalData ? operationalStatus : "Sem acesso"),
        permissionRow("Quartos", "Consultar quartos, tarifas e disponibilidade", operationalStatus),
        permissionRow("Financeiro", "Visualizar e registrar pagamentos e relatórios", options.canAccessFinance ? "Permitido" : "Sem acesso"),
        permissionRow("Usuários do sistema", "Criar usuários e gerenciar cargos", options.canManageUsers ? "Permitido" : "Próprio perfil"),
    ].join("");
}

function permissionRow(name, desc, status) {
    return `<div class="up-perm-item"><div><strong>${escapeHtml(name)}</strong><span>${escapeHtml(desc)}</span></div><em>${escapeHtml(status)}</em></div>`;
}

function activityRow(icon, title, time, badge, className) {
    return `<div class="up-act-row"><div class="up-act-icon ${className}"><i class="ti ${icon}"></i></div><div><strong>${escapeHtml(title)}</strong><span>${escapeHtml(time)}</span></div><em>${escapeHtml(badge)}</em></div>`;
}

function updatePreview(container, hasPhoto, fallbackName) {
    const name = container.querySelector("#up-name")?.value.trim() || "";
    const lastname = container.querySelector("#up-lastname")?.value.trim() || "";
    const email = container.querySelector("#up-email")?.value.trim() || "";
    const fullName = [name, lastname].filter(Boolean).join(" ") || fallbackName;
    const initials = initialsFor(fullName);

    container.querySelector("[data-up-hero-name]").textContent = fullName;
    container.querySelector("[data-up-hero-email]").textContent = email || "E-mail não informado";

    if (!hasPhoto) {
        container.querySelector("[data-up-hero-initials]").textContent = initials;
        container.querySelector("[data-up-av-initials]").textContent = initials;
    }
}

function applyPhoto(container, src) {
    container.querySelectorAll("[data-up-hero-img], [data-up-av-img]").forEach((image) => {
        image.src = src;
        image.classList.add("show");
    });
    container.querySelectorAll("[data-up-hero-initials], [data-up-av-initials]").forEach((item) => {
        item.classList.add("up-hidden");
    });
}

async function removePhoto(container, initialState) {
    const initials = initialState.initials;
    container.querySelectorAll("[data-up-hero-img], [data-up-av-img]").forEach((image) => {
        image.removeAttribute("src");
        image.classList.remove("show");
    });
    container.querySelectorAll("[data-up-hero-initials], [data-up-av-initials]").forEach((item) => {
        item.textContent = initials;
        item.classList.remove("up-hidden");
    });
    container.querySelector(".up-av-upload-title").textContent = "Nenhuma foto enviada";
    container.querySelector(".up-av-upload-desc").textContent = "Envie uma foto para personalizar seu perfil. Formatos aceitos: JPG, PNG, WEBP. Tamanho máximo: 2MB.";
    container.querySelector("[data-up-remove-photo]").classList.remove("show");

    if (!initialState.userId) {
        showToast(container, "Foto removida", "ti-trash");
        return;
    }

    try {
        const response = await updateUserPhoto(initialState.userId, "");
        if (response.status !== "success") {
            throw new Error(response.message || "Nao foi possivel remover a foto.");
        }

        initialState.user.photoUrl = "";
        updateSidebarInitials(initials);
        showToast(container, "Foto removida", "ti-trash");
    } catch (error) {
        showToast(container, error.message || "Nao foi possivel remover a foto.", "ti-alert-circle");
    }
}

function updateSidebarPhoto(src) {
    const avatar = document.querySelector(".sidebar-user .avatar");
    if (!avatar) {
        return;
    }

    avatar.innerHTML = `<img src="${escapeHtml(src)}" alt="">`;
}

function updateSidebarInitials(initials) {
    const avatar = document.querySelector(".sidebar-user .avatar");
    if (!avatar) {
        return;
    }

    avatar.textContent = initials;
}

function togglePassword(container, id, button) {
    const input = container.querySelector(`#${id}`);
    const isPassword = input.type === "password";
    input.type = isPassword ? "text" : "password";
    button.querySelector("i").className = isPassword ? "ti ti-eye-off" : "ti ti-eye";
}

function updatePasswordStrength(container, value) {
    const wrap = container.querySelector(".up-pass-strength");
    const bar = container.querySelector("[data-up-pass-bar]");
    const label = container.querySelector("[data-up-pass-label]");
    if (!value) {
        wrap.classList.remove("show");
        return;
    }

    wrap.classList.add("show");
    let score = 0;
    if (value.length >= 8) score++;
    if (/[A-Z]/.test(value)) score++;
    if (/[0-9]/.test(value)) score++;
    if (/[^A-Za-z0-9]/.test(value)) score++;

    const levels = [
        { pct: "25%", className: "weak", text: "Fraca" },
        { pct: "50%", className: "fair", text: "Razoável" },
        { pct: "75%", className: "good", text: "Boa" },
        { pct: "100%", className: "strong", text: "Forte" },
    ];
    const level = levels[Math.max(score - 1, 0)];
    bar.style.width = level.pct;
    bar.className = level.className;
    label.textContent = `Força: ${level.text}`;
}

async function saveProfile(container, button, initialState) {
    const userId = Number(container.querySelector(".up-hs-id")?.textContent.replace(/\D/g, "")) || null;
    const payload = profilePayload(container);

    if (!userId) {
        showToast(container, "Usuario invalido.", "ti-alert-circle");
        return;
    }

    if (!payload.username || !payload.email) {
        showToast(container, "Preencha nome e email.", "ti-alert-circle");
        return;
    }

    if (payload.newPassword && payload.newPassword !== value(container, "#up-pass-confirm")) {
        showToast(container, "A confirmação da senha não confere.", "ti-alert-circle");
        return;
    }

    const original = button.innerHTML;
    button.innerHTML = `<i class="ti ti-loader-2 spin"></i> Salvando...`;
    button.disabled = true;

    try {
        const response = await updateUserProfile(userId, payload);
        if (response.status !== "success") {
            throw new Error(response.message || "Nao foi possivel salvar o perfil.");
        }

        const updated = response.data || {};
        Object.assign(initialState.user, updated);
        updateSidebarIdentity(updated);
        button.innerHTML = `<i class="ti ti-check"></i> Salvo!`;
        showToast(container, "Perfil atualizado com sucesso", "ti-user-check");
        setTimeout(() => {
            button.innerHTML = original;
            button.disabled = false;
        }, 1600);
    } catch (error) {
        button.innerHTML = original;
        button.disabled = false;
        showToast(container, error.message || "Nao foi possivel salvar o perfil.", "ti-alert-circle");
    }
}

function profilePayload(container) {
    const firstName = value(container, "#up-name");
    const lastName = value(container, "#up-lastname");
    const displayName = value(container, "#up-display");
    const username = displayName || [firstName, lastName].filter(Boolean).join(" ");

    return {
        username,
        email: value(container, "#up-email"),
        phone: value(container, "#up-phone"),
        role: roleValueFor(value(container, "#up-role")),
        currentPassword: value(container, "#up-pass-current"),
        newPassword: value(container, "#up-pass-new"),
    };
}

function roleValueFor(label) {
    const values = {
        "Chefe Executivo Organizacional (CEO)": "CEO",
        "Diretor Geral de Tecnologia (CTO)": "CTO",
        "Administrador": "ADMIN",
        "Gerente": "MANAGER",
        "Recepção": "RECEPTION",
        "Governança": "HOUSEKEEPING",
    };

    return values[label] || label;
}

function updateSidebarIdentity(user) {
    const name = document.querySelector(".sidebar-user .user-name");
    const role = document.querySelector(".sidebar-user .user-role");

    if (name) {
        name.textContent = user.username || "Usuário";
    }

    if (role) {
        role.textContent = roleLabel(user.role);
    }
}

function value(container, selector) {
    return container.querySelector(selector)?.value.trim() || "";
}

function showToast(container, message, icon = "ti-check") {
    const toast = container.querySelector("[data-up-toast-box]");
    toast.querySelector("span").textContent = message;
    toast.querySelector("i").className = `ti ${icon}`;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2600);
}

function maskPhone(value) {
    const digits = value.replace(/\D/g, "").slice(0, 11);
    if (digits.length <= 10) {
        return digits.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{4})(\d)/, "$1-$2");
    }
    return digits.replace(/(\d{2})(\d)/, "($1) $2").replace(/(\d{5})(\d)/, "$1-$2");
}

function firstNameFor(username) {
    return String(username || "").trim().split(/\s+/)[0] || username || "";
}

function lastNameFor(username) {
    const parts = String(username || "").trim().split(/\s+/).filter(Boolean);
    return parts.length > 1 ? parts.slice(1).join(" ") : "";
}

function roleLabel(role) {
    const labels = {
        CEO: "Chefe Executivo Organizacional (CEO)",
        CTO: "Diretor Geral de Tecnologia (CTO)",
        ADMIN: "Administrador",
        MANAGER: "Gerente",
        RECEPTION: "Recepção",
        HOUSEKEEPING: "Governança",
    };

    return labels[role] || role || "Recepção";
}

function initialsFor(username) {
    return String(username || "")
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("") || "HH";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
