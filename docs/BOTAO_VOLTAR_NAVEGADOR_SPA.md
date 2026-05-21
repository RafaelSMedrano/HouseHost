# Botao Voltar do Navegador em SPA

Este documento explica como fazer o botao de voltar do navegador funcionar em uma aplicacao frontend simples feita com JavaScript, sem framework.

O contexto aqui e o Cantinho Das Lavandas, onde o frontend e estatico e renderiza telas trocando o conteudo do `#main-container`.

## Problema

Hoje a aplicacao muda de tela usando JavaScript.

Exemplo simplificado:

```js
mainContainer.innerHTML = "";
renderRegistrationWidget("main-container");
```

Isso troca o que aparece na tela, mas nao avisa o navegador que uma "pagina" nova foi aberta.

Para o navegador, a URL continua a mesma:

```text
http://50.17.103.188/
```

Entao, quando o usuario clica no botao voltar do navegador, o browser nao sabe que precisa voltar do registro para o login. Ele so conhece o historico real de URLs visitadas.

## Conceito: SPA

SPA significa Single Page Application.

Em uma SPA, o servidor entrega uma pagina principal:

```text
index.html
```

Depois disso, o JavaScript controla a troca de telas.

Exemplo:

```text
Login
Registro
Dashboard
Reservas
Estadias
```

Todas podem estar dentro da mesma pagina HTML, com o JavaScript renderizando componentes diferentes.

No nosso caso:

```text
index.html
        |
        v
main.js
        |
        v
renderLoginWidget()
renderRegistrationWidget()
renderHomeView()
```

## O Que o Navegador Entende Como Historico

O navegador entende historico por URLs.

Exemplo tradicional:

```text
/login
/register
/dashboard
```

Quando o usuario navega de `/login` para `/register`, o navegador registra essa mudanca.

Mas se o JavaScript apenas troca HTML internamente, sem mudar URL, o navegador nao registra nada.

Exemplo atual:

```text
URL continua /
Tela muda de login para registro
Historico do navegador nao muda
```

Resultado:

```text
Botao voltar nao sabe voltar para login.
```

## Solucao Geral

Para o botao voltar funcionar, precisamos sincronizar duas coisas:

```text
Estado visual da aplicacao
URL/historico do navegador
```

Quando a tela muda para registro, a URL tambem precisa indicar isso.

Exemplo:

```text
Login:    /
Registro: /register
```

Ou:

```text
Login:    /
Registro: /#register
```

Depois, quando o usuario clicar em voltar, o JavaScript precisa ouvir o evento do navegador e renderizar a tela correta.

## Duas Abordagens

Existem duas formas principais.

### 1. History API

Usa URLs limpas:

```text
/register
/dashboard
/bookings
```

Exemplo:

```js
history.pushState({ view: "register" }, "", "/register");
```

E escuta:

```js
window.addEventListener("popstate", (event) => {
    renderRoute(event.state?.view || "login");
});
```

Vantagens:

- URLs mais bonitas;
- mais parecido com sites profissionais;
- melhor para compartilhar links.

Desvantagens:

- exige configuracao correta no Nginx;
- rotas frontend podem conflitar com rotas backend;
- precisa separar claramente "rota de tela" e "rota de API".

### 2. Hash Routing

Usa a parte da URL depois do `#`.

Exemplo:

```text
/
/#register
/#dashboard
```

No navegador, tudo depois do `#` e chamado de hash.

Exemplo:

```js
window.location.hash = "register";
```

E escuta:

```js
window.addEventListener("hashchange", renderCurrentHash);
```

Vantagens:

- simples;
- nao exige mexer no Nginx;
- funciona bem para SPA pequena;
- o servidor sempre recebe apenas `/`.

Desvantagens:

- URLs menos elegantes;
- `/#register` parece menos profissional que `/register`;
- se o app crescer muito, pode valer migrar para History API.

## Recomendacao Para o Projeto Agora

Para o Cantinho Das Lavandas neste momento, a recomendacao e usar Hash Routing.

Motivos:

- o frontend e JavaScript puro;
- temos poucas telas no momento;
- queremos fazer o botao voltar funcionar sem mexer no Nginx;
- a configuracao atual do Nginx ainda encaminha rotas inexistentes para o Spring Boot;
- `/#register` evita conflito com endpoints como `/auth/login`, `/rooms`, `/bookings`.

Fluxo recomendado:

```text
Login:    http://50.17.103.188/
Registro: http://50.17.103.188/#register
```

Quando o usuario clicar em voltar:

```text
/#register -> /
```

O JavaScript percebe a mudanca do hash e renderiza login.

## Como Funciona Hash Routing

Considere esta URL:

```text
http://50.17.103.188/#register
```

Ela tem duas partes:

```text
http://50.17.103.188/
```

Essa parte vai para o servidor.

```text
#register
```

Essa parte fica no navegador.

O servidor Nginx nao recebe o hash.

Isso significa que, mesmo se a URL no navegador for:

```text
http://50.17.103.188/#register
```

o Nginx recebe:

```text
GET /
```

E entrega:

```text
index.html
```

Depois o JavaScript le:

```js
window.location.hash
```

E decide qual tela renderizar.

## Exemplo de Implementacao

No `main.js`, podemos centralizar a navegacao.

Ideia:

```js
import { renderLoginWidget } from "../widgets/loginWidget.js";
import { renderRegistrationWidget } from "../widgets/registrationWidget.js";

function renderCurrentRoute() {
    const mainContainer = document.getElementById("main-container");
    const route = window.location.hash.replace("#", "") || "login";

    mainContainer.className = "login-screen";

    if (route === "register") {
        renderRegistrationWidget("main-container");
        return;
    }

    renderLoginScreen(mainContainer);
}

function renderLoginScreen(mainContainer) {
    mainContainer.innerHTML = `
        <div id="login-widget"></div>
        <div class="login-secondary-action">
            <a href="#register" id="register-link" class="register-link">
                <strong>Registrar</strong> novo usuário
            </a>
        </div>
    `;

    renderLoginWidget("login-widget");
}

document.addEventListener("DOMContentLoaded", () => {
    renderCurrentRoute();
    window.addEventListener("hashchange", renderCurrentRoute);
});
```

Agora, em vez de interceptar clique e chamar diretamente `renderRegistrationWidget`, o link pode apontar para:

```html
<a href="#register">Registrar novo usuário</a>
```

Ao clicar:

```text
URL muda para /#register
hashchange dispara
renderCurrentRoute() roda
tela de registro aparece
```

Ao clicar no botao voltar:

```text
URL muda de /#register para /
hashchange dispara
renderCurrentRoute() roda
tela de login aparece
```

## Como Voltar do Registro Para Login

Na tela de registro, o link "Voltar ao login" pode ser:

```html
<a href="#" id="back-login-link">Voltar ao login</a>
```

Ou:

```html
<a href="#login" id="back-login-link">Voltar ao login</a>
```

Se usarmos `#login`, a URL fica:

```text
/#login
```

Se usarmos `#`, a URL volta para:

```text
/
```

Para este projeto, podemos tratar `""` e `"login"` como login:

```js
const route = window.location.hash.replace("#", "") || "login";

if (route === "register") {
    renderRegistrationWidget("main-container");
} else {
    renderLoginScreen(mainContainer);
}
```

## Mensagem de Sucesso Apos Registro

Um detalhe: se o cadastro for concluido com sucesso, queremos voltar para o login e mostrar:

```text
Usuário cadastrado com sucesso. Faça login para entrar no painel.
```

Com hash routing, ha algumas formas.

### Opcao Simples: Passar Estado em Memoria

Criar uma variavel em `main.js`:

```js
let pendingLoginMessage = "";
```

Quando o registro der sucesso:

```js
pendingLoginMessage = "Usuário cadastrado com sucesso. Faça login para entrar no painel.";
window.location.hash = "";
```

Quando renderizar login:

```js
renderLoginWidget("login-widget", {
    successMessage: pendingLoginMessage
});
pendingLoginMessage = "";
```

Vantagem:

- simples.

Desvantagem:

- se der refresh, a mensagem some.

### Opcao Com Session Storage

Guardar temporariamente no navegador:

```js
sessionStorage.setItem(
    "loginSuccessMessage",
    "Usuário cadastrado com sucesso. Faça login para entrar no painel."
);

window.location.hash = "";
```

Ao renderizar login:

```js
const successMessage = sessionStorage.getItem("loginSuccessMessage") || "";
sessionStorage.removeItem("loginSuccessMessage");

renderLoginWidget("login-widget", { successMessage });
```

Vantagem:

- sobrevive a reload da pagina;
- ainda e simples.

Para o nosso projeto, `sessionStorage` e uma boa opcao.

## Impacto no Nginx

Com hash routing, nao precisa mudar o Nginx.

Porque:

```text
/#register
```

chega no servidor como:

```text
/
```

O hash nunca vai para o servidor.

Isso evita conflito com a configuracao atual:

```nginx
location / {
    try_files $uri $uri/ @spring;
}
```

Com History API, a URL seria:

```text
/register
```

Nesse caso, o Nginx receberia:

```text
GET /register
```

E precisaria saber que `/register` e rota do frontend, nao endpoint do backend.

Por isso History API exigiria uma configuracao mais cuidadosa.

## Como Seria Com History API

Se quisermos URLs limpas no futuro, o frontend usaria:

```js
history.pushState({ view: "register" }, "", "/register");
```

E:

```js
window.addEventListener("popstate", (event) => {
    renderRoute(event.state?.view || routeFromPath());
});
```

O Nginx precisaria separar rotas de API e rotas de tela.

Exemplo:

```nginx
location /auth/ {
    proxy_pass http://localhost:8080/auth/;
}

location /rooms {
    proxy_pass http://localhost:8080/rooms;
}

location /bookings {
    proxy_pass http://localhost:8080/bookings;
}

location /stays {
    proxy_pass http://localhost:8080/stays;
}

location / {
    try_files $uri $uri/ /index.html;
}
```

Assim:

```text
/register
vai para index.html

/auth/login
vai para Spring Boot
```

Essa abordagem e melhor para um frontend maior, mas e mais trabalhosa agora.

## Cuidados

### Evitar Renderizacao Duplicada

Se varios lugares chamarem `renderLoginWidget` diretamente, o controle de historico fica espalhado.

Melhor:

```text
main.js controla rotas
widgets apenas renderizam seu conteudo
```

### Widgets Nao Devem Decidir Rotas Globais

O `loginWidget` deve renderizar login.

Ele nao deveria decidir que existe cadastro.

Isso permite usar o widget em outro lugar sem link de registro.

Exemplo:

```text
loginWidget.js
somente formulario de login

main.js
decide se a tela tem link de registro
```

### Registro Tambem Nao Deve Quebrar Historico

Quando o cadastro termina, ele nao deve apenas trocar HTML.

Ele deve voltar para a rota de login:

```js
window.location.hash = "";
```

Ou:

```js
window.location.hash = "login";
```

Assim o historico e a tela continuam sincronizados.

## Checklist de Implementacao

Para implementar Hash Routing:

1. Criar uma funcao central `renderCurrentRoute()` em `main.js`.

2. Ler a rota atual com:

```js
window.location.hash.replace("#", "") || "login"
```

3. Renderizar `registrationWidget` quando a rota for `register`.

4. Renderizar login para qualquer outra rota.

5. Trocar o link de registro para:

```html
<a href="#register">Registrar novo usuário</a>
```

6. Adicionar:

```js
window.addEventListener("hashchange", renderCurrentRoute);
```

7. No sucesso do cadastro, guardar mensagem no `sessionStorage`.

8. No sucesso do cadastro, mudar rota para login:

```js
window.location.hash = "";
```

9. Ao renderizar login, ler e remover a mensagem do `sessionStorage`.

10. Testar:

```text
abrir /
clicar Registrar
ver /#register
clicar voltar do navegador
voltar para /
clicar Registrar de novo
cadastrar usuario
voltar para login com mensagem de sucesso
```

## Resumo

O botao voltar nao funciona automaticamente porque a aplicacao troca telas com JavaScript sem mudar a URL.

Para corrigir, precisamos sincronizar tela e URL.

Para agora, a melhor solucao e Hash Routing:

```text
Login:    /
Registro: /#register
```

Isso faz o botao voltar funcionar sem mudar Nginx.

No futuro, se quisermos URLs limpas como `/register`, podemos migrar para History API e ajustar o Nginx para separar rotas de frontend e rotas de API.
