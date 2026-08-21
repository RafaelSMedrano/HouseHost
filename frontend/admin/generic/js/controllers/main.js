import { getStoredUser } from "../api.js?v=2026-08-11-api-log-transport";
import { renderLoginWidget } from "../widgets/loginWidget.js?v=2026-07-26-session-expiration";
import { renderMetricsResumeWidget } from "../widgets/metricsResumeWidget.js?v=2026-06-06-pousada-logo";
import { renderRegistrationWidget } from "../widgets/registrationWidget.js?v=2026-06-14-role-access";
import { initializeAdministrativeLogging } from "../loggerBootstrap.js?v=2026-08-11-api-log-transport";
import { startUIController } from "./UICOntroller.js?v=2026-08-17-financial-classification-removal";

const loggingBootstrap = initializeAdministrativeLogging();

document.addEventListener("DOMContentLoaded", () => {
    loggingBootstrap.logApplicationStarted();

    const mainContainer = document.getElementById("main-container");
    let handlingSessionExpiration = false;

    globalThis.addEventListener("househost:session-expired", () => {
        if (handlingSessionExpiration) {
            return;
        }
        handlingSessionExpiration = true;
        renderAuthLayout();
        renderLoginPanel({
            errorMessage: "Sua sessão expirou. Faça login novamente.",
        });
    });

    const storedUser = getStoredUser();
    if (storedUser) {
        startUIController("main-container", storedUser);
    } else {
        renderAuthLayout();
        renderLoginPanel();
    }

    function renderAuthLayout() {
        mainContainer.className = "login-screen";
        mainContainer.innerHTML = `
            <div id="metrics-resume-widget"></div>
            <div id="auth-panel" class="auth-panel"></div>
        `;
        renderMetricsResumeWidget("metrics-resume-widget");
    }

    function renderLoginPanel(options = {}) {
        const authPanel = document.getElementById("auth-panel");
        authPanel.innerHTML = `
            <div id="login-widget"></div>
            <div class="login-secondary-action">
                <a href="#" id="register-link" class="register-link"><strong>Registrar</strong> novo usuário</a>
            </div>
        `;

        renderLoginWidget("login-widget", {
            ...options,
            onLoginSuccess: (user) => {
                handlingSessionExpiration = false;
                startUIController("main-container", user);
            },
        });
    }

    function renderRegistrationPanel() {
        renderRegistrationWidget("auth-panel", {
            onBackToLogin: () => renderLoginPanel(),
            onRegistrationSuccess: () => {
                renderLoginPanel({
                    successMessage: "Usuário cadastrado com sucesso. Faça login para entrar no painel.",
                });
            },
        });
    }

    mainContainer.addEventListener("click", (event) => {
        if (event.target.closest("#register-link")) {
            event.preventDefault();
            renderRegistrationPanel();
        }
    });

});
