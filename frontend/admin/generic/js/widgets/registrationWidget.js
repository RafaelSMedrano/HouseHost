import { registration } from "../api.js?v=2026-08-11-api-log-transport";

export function renderRegistrationWidget(containerId, options = {}) {
    const mainContainer = document.getElementById(containerId);
    const onBackToLogin = options.onBackToLogin || (() => {});
    const onRegistrationSuccess = options.onRegistrationSuccess || (() => {});

    mainContainer.innerHTML = `
        <div class="right-inner">
            <div class="form-header">
                <div class="form-eyebrow">Novo acesso</div>
                <h1 class="form-title">Registrar<br><em>usuário</em></h1>
                <p class="form-sub">Crie uma credencial administrativa para acessar o painel de gestão.</p>
            </div>

            <div class="alert hidden" id="registration-alert">
                <i class="ti ti-alert-circle"></i>
                <span>Preencha todos os campos.</span>
            </div>

            <form id="registrationForm">
                <div class="field">
                    <label class="field-label" for="rw-username">Usuário</label>
                    <div class="input-wrap">
                        <i class="ti ti-user"></i>
                        <input
                            class="field-input"
                            type="text"
                            id="rw-username"
                            placeholder="Nome de usuário"
                            autocomplete="username"
                            required
                        >
                    </div>
                </div>

                <div class="field">
                    <label class="field-label" for="rw-email">E-mail</label>
                    <div class="input-wrap">
                        <i class="ti ti-mail"></i>
                        <input
                            class="field-input"
                            type="email"
                            id="rw-email"
                            placeholder="seu@email.com"
                            autocomplete="email"
                            required
                        >
                    </div>
                </div>

                <div class="field">
                    <label class="field-label" for="rw-role">Cargo</label>
                    <div class="input-wrap">
                        <i class="ti ti-id-badge-2"></i>
                        <select class="field-input" id="rw-role" required disabled>
                            <option value="RECEPTION" selected>Recepção</option>
                        </select>
                    </div>
                    <small>Cadastros públicos recebem o perfil de recepção.</small>
                </div>

                <div class="field">
                    <label class="field-label" for="rw-password">Senha</label>
                    <div class="input-wrap">
                        <i class="ti ti-lock"></i>
                        <input
                            class="field-input"
                            type="password"
                            id="rw-password"
                            placeholder="••••••••"
                            autocomplete="new-password"
                            required
                        >
                        <button type="button" class="toggle-pass" id="registration-toggle" aria-label="Mostrar senha">
                            <i class="ti ti-eye" id="registration-eye"></i>
                        </button>
                    </div>
                </div>

                <div class="field">
                    <label class="field-label" for="rw-photo-url">Foto</label>
                    <div class="input-wrap">
                        <i class="ti ti-photo"></i>
                        <input
                            class="field-input"
                            type="url"
                            id="rw-photo-url"
                            placeholder="URL da foto de perfil"
                            autocomplete="url"
                        >
                    </div>
                </div>

                <button type="submit" class="btn-login" id="registration-btn">
                    <i class="ti ti-user-plus"></i>
                    Registrar usuário
                </button>
            </form>

            <div class="right-footer">
                Já tem acesso? <a href="#" id="back-login-link">Voltar ao login</a>
            </div>
        </div>
    `;

    const form = document.getElementById("registrationForm");
    const usernameInput = document.getElementById("rw-username");
    const emailInput = document.getElementById("rw-email");
    const roleInput = document.getElementById("rw-role");
    const passwordInput = document.getElementById("rw-password");
    const photoUrlInput = document.getElementById("rw-photo-url");
    const alert = document.getElementById("registration-alert");
    const alertText = alert.querySelector("span");
    const submitButton = document.getElementById("registration-btn");
    const toggleButton = document.getElementById("registration-toggle");
    const eyeIcon = document.getElementById("registration-eye");

    toggleButton.onclick = () => {
        const isPassword = passwordInput.type === "password";
        passwordInput.type = isPassword ? "text" : "password";
        eyeIcon.className = isPassword ? "ti ti-eye-off" : "ti ti-eye";
        toggleButton.setAttribute("aria-label", isPassword ? "Ocultar senha" : "Mostrar senha");
    };

    document.getElementById("back-login-link").onclick = (event) => {
        event.preventDefault();
        onBackToLogin();
    };

    form.onsubmit = async (event) => {
        event.preventDefault();

        const username = usernameInput.value.trim();
        const email = emailInput.value.trim();
        const role = roleInput.value;
        const password = passwordInput.value;
        const photoUrl = photoUrlInput.value.trim();

        if (!username || !password || !email || !role) {
            alertText.innerText = "Preencha todos os campos.";
            alert.classList.remove("hidden");
            return;
        }

        submitButton.disabled = true;
        submitButton.innerHTML = `<i class="ti ti-loader-2 spin"></i> Registrando...`;
        alert.classList.add("hidden");

        try {
            const response = await registration(username, password, email, role, photoUrl);

            if (response.status === "success") {
                onRegistrationSuccess(response.data);
            } else {
                submitButton.disabled = false;
                submitButton.innerHTML = `<i class="ti ti-user-plus"></i> Registrar usuário`;
                alertText.innerText = response.message || "Nao foi possivel registrar o usuario.";
                alert.classList.remove("hidden");
            }
        } catch (err) {
            console.error(err);
            submitButton.disabled = false;
            submitButton.innerHTML = `<i class="ti ti-user-plus"></i> Registrar usuário`;
            alertText.innerText = "Erro ao conectar com o servidor.";
            alert.classList.remove("hidden");
        }
    };
}
