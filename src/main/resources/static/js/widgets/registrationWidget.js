import { renderLoginWidget } from "./loginWidget.js";
import { registration } from "../api.js";

export function renderRegistrationWidget(containerId) {
    const mainContainer = document.getElementById(containerId);

    mainContainer.innerHTML = `
        <div id="lw-response" class="response"></div>
        <div id="login-widget" class="auth">
            <input type="text" id="lw-username" placeholder="Username">
            <input type="email" id="lw-email" placeholder="Email">
            <input type="password" id="lw-password" placeholder="Password">
            <button id="lw-registration-btn">Register</button>
        </div>
    `;

    document.getElementById("lw-registration-btn").onclick = async () => {
        const username = document.getElementById("lw-username").value;
        const password = document.getElementById("lw-password").value;
        const email = document.getElementById("lw-email").value;
        const responseDiv = document.getElementById("lw-response");

        if (!username || !password || !email) {
            responseDiv.innerText = "Preencha todos os campos.";
            return;
        }

        try {
            const response = await registration(username, password, email);

            if (response.status === "success") {
                responseDiv.innerHTML = `
                    <div style="text-align: center;">
                        Conta criada com sucesso. Faca login para entrar no HouseHost.
                    </div>
                `;
                renderLoginWidget("login-widget");
            } else {
                responseDiv.innerText = response.message;
            }
        } catch (err) {
            console.error(err);
            responseDiv.innerText = "Erro ao conectar com o servidor.";
        }
    };
}
