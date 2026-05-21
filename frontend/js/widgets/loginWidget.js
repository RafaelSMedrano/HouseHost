import { findQuickAccessUsers, login } from "../api.js?v=2026-05-20-login-real-data"; /*  O usuário pode estar na casa dele com o navegador guardando um arquivo antigo em cache por horas, dias ou até mais, dependendo das regras de cache do servidor/
                                                       navegador.

                                                       Então, se ele já tinha baixado:

                                                       api.js?v=2026-05-16-02

                                                       e você altera o conteúdo do api.js, mas mantém a mesma URL, o navegador dele pode continuar usando o antigo.

                                                       Quando você troca para:

                                                       api.js?v=2026-05-17-01

                                                       você força o navegador a enxergar como outro arquivo.
                                                       */

export function renderLoginWidget(containerId, options = {}) {
    const container = document.getElementById(containerId);
    const successMessage = options.successMessage || "";
    const onLoginSuccess = options.onLoginSuccess || (() => {});

    container.innerHTML = `
        <div class="right-inner">

              <div class="form-header">
                <div class="form-eyebrow">Acesso restrito</div>
                <h1 class="form-title">Entre na<br><em>sua conta</em></h1>
                <p class="form-sub">Utilize suas credenciais de acesso ao painel de gestão.</p>
              </div>

              <!-- Alerta de erro (oculto por padrão) -->
              <div class="alert hidden" id="alert">
                <i class="ti ti-alert-circle"></i>
                <span>E-mail ou senha incorretos. Verifique suas credenciais e tente novamente.</span>
              </div>

              <div class="alert alert-success ${successMessage ? "" : "hidden"}" id="success-alert">
                <i class="ti ti-circle-check"></i>
                <span>${successMessage}</span>
              </div>

              <!-- Formulário -->
              <form id="loginForm">

                <div class="field">
                  <label class="field-label">E-mail</label>
                  <div class="input-wrap">
                    <i class="ti ti-mail"></i>
                    <input
                      class="field-input"
                      type="email"
                      id="email"
                      placeholder="seu@email.com"
                      autocomplete="email"
                      required
                    >
                  </div>
                </div>

                <div class="field">
                  <div class="field-meta">
                    <label class="field-label" style="margin:0" for="password">Senha</label>
                    <a href="#" class="forgot">Esqueci minha senha</a>
                  </div>
                  <div class="input-wrap">
                    <i class="ti ti-lock"></i>
                    <input
                      class="field-input"
                      type="password"
                      id="password"
                      placeholder="••••••••"
                      autocomplete="current-password"
                      required
                    >
                    <button type="button" class="toggle-pass" id="toggleBtn" aria-label="Mostrar senha">
                      <i class="ti ti-eye" id="eyeIcon"></i>
                    </button>
                  </div>
                </div>

                <label class="remember">
                  <input type="checkbox" id="remember">
                  <div class="check-box"></div>
                  <span class="remember-label">Manter-me conectado</span>
                </label>

                <button type="submit" class="btn-login" id="login-btn">
                  <i class="ti ti-login"></i>
                  Entrar no painel
                </button>

              </form>

              <!-- Acesso rápido por perfil -->
              <div class="divider">
                <div class="divider-line"></div>
                <span class="divider-text">ou acesse como</span>
                <div class="divider-line"></div>
              </div>

              <div class="profiles" id="quick-access-profiles">
                <div class="profile-empty">Carregando usuários...</div>
              </div>

        </div>
    `;

    const loginForm = document.getElementById("loginForm");
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const alert = document.getElementById("alert");
    const alertText = alert.querySelector("span");
    const successAlert = document.getElementById("success-alert");
    const loginButton = document.getElementById("login-btn");
    const toggleButton = document.getElementById("toggleBtn");
    const eyeIcon = document.getElementById("eyeIcon");
    const quickAccessProfiles = document.getElementById("quick-access-profiles");

    toggleButton.onclick = () => {
        const isPassword = passwordInput.type === "password";
        passwordInput.type = isPassword ? "text" : "password";
        eyeIcon.className = isPassword ? "ti ti-eye-off" : "ti ti-eye";
        toggleButton.setAttribute("aria-label", isPassword ? "Ocultar senha" : "Mostrar senha");
    };

    loadQuickAccessProfiles(quickAccessProfiles, emailInput, passwordInput);

    loginForm.onsubmit = async (event) => {
        event.preventDefault();

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        if (!email || !password) {
            alertText.innerText = "Preencha todos os campos.";
            alert.classList.remove("hidden");
            return;
        }

        loginButton.disabled = true;
        loginButton.innerHTML = `<i class="ti ti-loader-2 spin"></i> Verificando...`;
        alert.classList.add("hidden");
        successAlert.classList.add("hidden");

        try {
            const response = await login(email, password);

            if (response.status === "success") {
                loginButton.innerHTML = `<i class="ti ti-check"></i> Acesso liberado!`;
                onLoginSuccess(response.data);
            } else {
                loginButton.disabled = false;
                loginButton.innerHTML = `<i class="ti ti-login"></i> Entrar no painel`;
                alertText.innerText = response.message || "E-mail ou senha incorretos. Verifique suas credenciais e tente novamente.";
                alert.classList.remove("hidden");
            }
        } catch (err) {
            console.error(err);
            loginButton.disabled = false;
            loginButton.innerHTML = `<i class="ti ti-login"></i> Entrar no painel`;
            alertText.innerText = "Erro ao conectar com o servidor.";
            alert.classList.remove("hidden");
        }
    };
}

async function loadQuickAccessProfiles(container, emailInput, passwordInput) {
    try {
        const response = await findQuickAccessUsers();
        const users = Array.isArray(response.data) ? response.data : [];

        if (users.length === 0) {
            container.innerHTML = `<div class="profile-empty">Nenhum usuário cadastrado.</div>`;
            return;
        }

        container.innerHTML = users.map((user, index) => quickAccessButtonTemplate(user, index)).join("");
        container.querySelectorAll(".profile-btn").forEach((button) => {
            button.onclick = () => {
                emailInput.value = button.dataset.email || "";
                passwordInput.value = "";
                passwordInput.focus();
            };
        });
    } catch (error) {
        console.error("Erro ao carregar usuarios de acesso rapido.", error);
        container.innerHTML = `<div class="profile-empty">Não foi possível carregar usuários.</div>`;
    }
}

function quickAccessButtonTemplate(user, index) {
    const username = user.username || user.email || "Usuário";
    const email = user.email || "";
    const photoUrl = user.photoUrl || "";
    const avatarClass = ["av-a", "av-b", "av-c"][index % 3];
    const avatarContent = photoUrl
            ? `<img src="${escapeHtml(photoUrl)}" alt="">`
            : escapeHtml(initialsFor(username));

    return `
        <button class="profile-btn" type="button" data-email="${escapeHtml(email)}">
          <div class="profile-avatar ${avatarClass}">${avatarContent}</div>
          <div class="profile-name">${escapeHtml(username)}</div>
          <div class="profile-role" title="${escapeHtml(fullRoleLabel(user.role))}">${escapeHtml(shortRoleLabel(user.role))}</div>
        </button>
    `;
}

function initialsFor(name) {
    return String(name || "U")
            .trim()
            .split(/\s+/)
            .slice(0, 2)
            .map((part) => part.charAt(0).toUpperCase())
            .join("") || "U";
}

function shortRoleLabel(role) {
    const labels = {
        CEO: "CEO",
        CTO: "CTO",
        ADMIN: "Admin",
        MANAGER: "Gerente",
        RECEPTION: "Recepção",
        HOUSEKEEPING: "Governança",
    };
    return labels[role] || role || "Usuário";
}

function fullRoleLabel(role) {
    const labels = {
        CEO: "Chefe Executivo Organizacional (CEO)",
        CTO: "Diretor Geral de Tecnologia (CTO)",
        ADMIN: "Administrador",
        MANAGER: "Gerente",
        RECEPTION: "Recepção",
        HOUSEKEEPING: "Governança",
    };
    return labels[role] || role || "Usuário";
}

function escapeHtml(value) {
    return String(value ?? "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
}
