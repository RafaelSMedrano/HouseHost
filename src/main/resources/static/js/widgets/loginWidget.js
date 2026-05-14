import { renderHomeView } from "../views/homeView.js";
import { login } from "../api.js";

export function renderLoginWidget(containerId) {
    const container = document.getElementById(containerId);

    container.innerHTML = `
        <div class="auth">
            <input type="text" id="lw-username" placeholder="Username">
            <input type="password" id="lw-password" placeholder="Password">
            <button id="lw-login-btn">Login</button>
        </div>
    `;

    document.getElementById("lw-login-btn").onclick = async () => {
        const username = document.getElementById("lw-username").value;
        const password = document.getElementById("lw-password").value;
        const responseDiv = document.getElementById("lw-response");

        if (!username || !password) {
            responseDiv.innerText = "Preencha todos os campos.";
            return;
        }

        try {
            const response = await login(username, password);

            if (response.status === "success") {
                const mainContainer = document.getElementById("main-container");
                mainContainer.innerHTML = "";

                renderHomeView("main-container", response.data.username);
            } else {
                responseDiv.innerText = response.message;
            }
        } catch (err) {
            console.error(err);
            responseDiv.innerText = "Erro ao conectar com o servidor.";
        }
    };
}
