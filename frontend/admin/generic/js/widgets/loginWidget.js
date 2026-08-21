import { ApiError, findQuickAccessUsers, login, saveAuthSession } from "../api.js?v=2026-08-11-api-log-transport"; /*  O usuário pode estar na casa dele com o navegador guardando um arquivo antigo em cache por horas, dias ou até mais, dependendo das regras de cache do servidor/
                                                       navegador.

                                                       Então, se ele já tinha baixado:

                                                       api.js?v=2026-05-16-02

                                                       e você altera o conteúdo do api.js, mas mantém a mesma URL, o navegador dele pode continuar usando o antigo.

                                                       Quando você troca para:

                                                       api.js?v=2026-05-17-01

                                                       você força o navegador a enxergar como outro arquivo.
                                                       */

const INVALID_CREDENTIALS_MESSAGE = "E-mail ou senha incorretos. Verifique suas credenciais e tente novamente.";
const LOGIN_RESTRICTION_MESSAGE = "Muitas tentativas de acesso. Aguarde para tentar novamente.";
const SERVICE_UNAVAILABLE_MESSAGE = "O acesso está temporariamente indisponível. Tente novamente em alguns instantes.";
const CONNECTION_ERROR_MESSAGE = "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.";
const RETRY_AFTER_FALLBACK_SECONDS = 60;

export function renderLoginWidget(containerId, options = {}) {
    const container = document.getElementById(containerId);
    const errorMessage = options.errorMessage || "";
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
              <div class="alert ${errorMessage ? "" : "hidden"}" id="alert" role="status" aria-live="polite" aria-atomic="true" tabindex="-1">
                <i class="ti ti-alert-circle"></i>
                <span>${escapeHtml(errorMessage || INVALID_CREDENTIALS_MESSAGE)}</span>
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
    let requestInFlight = false;
    let restrictionCountdownActive = false;
    let restrictionCountdownInterval = null;
    let loginSucceeded = false;

    toggleButton.onclick = () => {
        const isPassword = passwordInput.type === "password";
        passwordInput.type = isPassword ? "text" : "password";
        eyeIcon.className = isPassword ? "ti ti-eye-off" : "ti ti-eye";
        toggleButton.setAttribute("aria-label", isPassword ? "Ocultar senha" : "Mostrar senha");
    };

    loadQuickAccessProfiles(quickAccessProfiles, emailInput, passwordInput);

    loginForm.onsubmit = async (event) => {
        event.preventDefault();

        if (requestInFlight || restrictionCountdownActive) {
            return;
        }

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        if (!email || !password) {
            alertText.innerText = "Preencha todos os campos.";
            alert.classList.remove("hidden");
            return;
        }

        requestInFlight = true;
        setLoginButtonState(loginButton, true, `<i class="ti ti-loader-2 spin"></i> Verificando...`);
        alert.classList.add("hidden");
        successAlert.classList.add("hidden");

        try {
            const response = await login(email, password);

            if (response.status === "success") {
                loginSucceeded = true;
                loginButton.innerHTML = `<i class="ti ti-check"></i> Acesso liberado!`;
                saveAuthSession(response.data);
                onLoginSuccess(response.data);
            } else {
                showLoginAlert(alert, alertText, INVALID_CREDENTIALS_MESSAGE);
                passwordInput.value = "";
                passwordInput.focus();
            }
        } catch (error) {
            if (error instanceof ApiError && error.status === 401) {
                passwordInput.value = "";
                showLoginAlert(alert, alertText, INVALID_CREDENTIALS_MESSAGE);
                passwordInput.focus();
            } else if (error instanceof ApiError && error.status === 429) {
                passwordInput.value = "";
                const retryAfterSeconds = error.retryAfterSeconds ?? RETRY_AFTER_FALLBACK_SECONDS;
                restrictionCountdownInterval = startRestrictionCountdown(
                        retryAfterSeconds,
                        container,
                        alert,
                        alertText,
                        loginButton,
                        () => {
                            restrictionCountdownActive = false;
                            restrictionCountdownInterval = null;
                        }
                );
                restrictionCountdownActive = restrictionCountdownInterval !== null;
            } else if (error instanceof ApiError && error.status === 503) {
                showLoginAlert(alert, alertText, SERVICE_UNAVAILABLE_MESSAGE);
            } else {
                showLoginAlert(alert, alertText, CONNECTION_ERROR_MESSAGE);
            }
        } finally {
            requestInFlight = false;
            if (!restrictionCountdownActive && !loginSucceeded) {
                setLoginButtonState(loginButton, false, defaultLoginButtonContent());
            }
        }
    };
}

function startRestrictionCountdown(
        retryAfterSeconds,
        container,
        alert,
        alertText,
        loginButton,
        onCountdownComplete
) {
    const countdownEndsAt = Date.now() + (retryAfterSeconds * 1000);
    let lastAnnouncedRemainingSeconds = null;

    const updateCountdown = () => {
        if (!container.isConnected) {
            clearInterval(restrictionCountdownInterval);
            onCountdownComplete();
            return;
        }

        const remainingSeconds = Math.max(0, Math.ceil((countdownEndsAt - Date.now()) / 1000));
        if (remainingSeconds === 0) {
            clearInterval(restrictionCountdownInterval);
            showLoginAlert(alert, alertText, "Você já pode tentar entrar novamente.");
            setLoginButtonState(loginButton, false, defaultLoginButtonContent());
            onCountdownComplete();
            return;
        }

        if (remainingSeconds === lastAnnouncedRemainingSeconds) {
            return;
        }

        lastAnnouncedRemainingSeconds = remainingSeconds;

        showLoginAlert(
                alert,
                alertText,
                `${LOGIN_RESTRICTION_MESSAGE} Nova tentativa em ${remainingSeconds}s.`
        );
        setLoginButtonState(
                loginButton,
                true,
                `<i class="ti ti-clock"></i> Aguarde ${remainingSeconds}s`
        );
    };

    let restrictionCountdownInterval = null;
    updateCountdown();
    alert.focus();

    if (retryAfterSeconds <= 0) {
        return null;
    }

    restrictionCountdownInterval = setInterval(updateCountdown, 250);
    return restrictionCountdownInterval;
}

function showLoginAlert(alert, alertText, message) {
    alertText.innerText = message;
    alert.classList.remove("hidden");
}

function setLoginButtonState(loginButton, disabled, content) {
    loginButton.disabled = disabled;
    loginButton.innerHTML = content;
}

function defaultLoginButtonContent() {
    return `<i class="ti ti-login"></i> Entrar no painel`;
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
        CEO: "CEO", CTO: "CTO", ADMIN: "Admin", MANAGER: "Gerente",
        RECEPTION: "Recepção", HOUSEKEEPING: "Governança",
    };
    return labels[role] || role || "Usuário";
}

function fullRoleLabel(role) {
    const labels = {
        CEO: "Chefe Executivo Organizacional (CEO)",
        CTO: "Diretor Geral de Tecnologia (CTO)",
        ADMIN: "Administrador", MANAGER: "Gerente",
        RECEPTION: "Recepção", HOUSEKEEPING: "Governança",
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
