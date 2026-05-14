import { renderLoginWidget } from "../widgets/loginWidget.js";
import { renderRegistrationWidget } from "../widgets/registrationWidget.js";

document.addEventListener("DOMContentLoaded", () => {
    console.log("HouseHost iniciado.");

    const mainContainer = document.getElementById("main-container");
    mainContainer.className = "";

    mainContainer.innerHTML = `
        <div id="login-widget"></div>
        <div class="auth" id="action-area">
            <button id="register-btn">Register</button>
        </div>
        <div id="lw-response" class="response"></div>
    `;

    renderLoginWidget("login-widget");

    document.getElementById("register-btn").onclick = () => {
        mainContainer.innerHTML = "";
        renderRegistrationWidget("main-container");
    };
});
