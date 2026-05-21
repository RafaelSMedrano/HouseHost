import { renderLoginWidget } from "../widgets/loginWidget.js?v=2026-05-20-login-real-data";
import { renderMetricsResumeWidget } from "../widgets/metricsResumeWidget.js?v=2026-05-20-logo-jpeg";
import { renderRegistrationWidget } from "../widgets/registrationWidget.js?v=2026-05-20-exec-role-labels";
import { startUIController } from "./UICOntroller.js?v=2026-05-20-logo-jpeg";

document.addEventListener("DOMContentLoaded", () => {
    console.log("HouseHost iniciado.");

    const mainContainer = document.getElementById("main-container");

    renderAuthLayout();
    renderLoginPanel();

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
