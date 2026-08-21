import { formatPrivacyPolicyEffectiveDate } from "../privacyPolicyDocument.js?v=2026-07-28-versioned-privacy-policy";

export function renderPoliticaPrivacidadeView(containerId) {
    const container = document.getElementById(containerId);

    if (!container) {
        return;
    }

    container.innerHTML = `
<div class="page active privacy-page" id="page-privacidade">
  <header class="privacy-hero">
    <div class="page-hero-eyebrow"><span></span>Privacidade e proteção de dados</div>
    <h1 id="privacy-policy-title" tabindex="-1">Carregando política…</h1>
    <p>Consulte o aviso vigente antes de enviar uma solicitação de reserva.</p>
    <div class="privacy-version" id="privacy-policy-version" aria-live="polite">Buscando a versão vigente…</div>
  </header>

  <article class="privacy-content" id="privacy-policy-content" aria-busy="true">
    <div class="privacy-load-state" id="privacy-policy-load-state" role="status" aria-live="polite" tabindex="-1">
      <p>Carregando a política de privacidade vigente…</p>
    </div>
  </article>

  <nav class="privacy-back-actions privacy-page-actions" aria-label="Navegação após a política de privacidade">
    <button type="button" onclick="goPage('reserva')">Fazer uma solicitação de reserva</button>
    <button type="button" class="secondary" onclick="goPage('home')">Voltar ao início</button>
  </nav>
</div>
    `;
}

export function renderPrivacyPolicyPageState(policyState, onRetry) {
    const title = document.getElementById("privacy-policy-title");
    const version = document.getElementById("privacy-policy-version");
    const content = document.getElementById("privacy-policy-content");

    if (!title || !version || !content) {
        return;
    }

    if (policyState.loadState === "loading" || policyState.loadState === "idle") {
        title.textContent = "Carregando política…";
        version.textContent = "Buscando a versão vigente…";
        content.setAttribute("aria-busy", "true");
        renderLoadState(content, "Carregando a política de privacidade vigente…");
        return;
    }

    if (policyState.loadState !== "ready") {
        title.textContent = "Política temporariamente indisponível";
        version.textContent = "A versão vigente não pôde ser confirmada.";
        content.setAttribute("aria-busy", "false");
        renderUnavailableState(content, onRetry);
        return;
    }

    title.textContent = policyState.title;
    version.textContent = `Versão ${policyState.version} · Vigente desde ${formatPrivacyPolicyEffectiveDate(policyState.effectiveAt)}`;
    content.setAttribute("aria-busy", "false");
    renderPrivacyPolicyDocument(content, policyState);
}

export function renderPrivacyPolicyDocument(container, policyState, documentReference = document) {
    const fragment = documentReference.createDocumentFragment();

    policyState.content.sections.forEach((section, index) => {
        const sectionElement = documentReference.createElement("section");
        sectionElement.className = "privacy-section";

        const sectionNumber = documentReference.createElement("div");
        sectionNumber.className = "privacy-section-number";
        sectionNumber.setAttribute("aria-hidden", "true");
        sectionNumber.textContent = String(index + 1).padStart(2, "0");

        const sectionContent = documentReference.createElement("div");
        const heading = documentReference.createElement("h2");
        heading.textContent = section.heading;
        sectionContent.append(heading);

        section.nodes.forEach((node) => {
            sectionContent.append(createPolicyNode(node, documentReference));
        });

        sectionElement.append(sectionNumber, sectionContent);
        fragment.append(sectionElement);
    });

    container.replaceChildren(fragment);
}

function createPolicyNode(node, documentReference) {
    if (node.type === "paragraph") {
        const paragraph = documentReference.createElement("p");
        paragraph.textContent = node.text;
        return paragraph;
    }

    if (node.type === "list") {
        const list = documentReference.createElement("ul");
        node.items.forEach((item) => {
            const listItem = documentReference.createElement("li");
            listItem.textContent = item;
            list.append(listItem);
        });
        return list;
    }

    const link = documentReference.createElement("a");
    link.className = "privacy-whatsapp";
    link.href = node.url;
    link.target = "_blank";
    link.rel = "noopener noreferrer";
    link.textContent = node.text;
    return link;
}

function renderLoadState(container, message) {
    const loadState = document.createElement("div");
    loadState.className = "privacy-load-state";
    loadState.id = "privacy-policy-load-state";
    loadState.setAttribute("role", "status");
    loadState.setAttribute("aria-live", "polite");
    loadState.setAttribute("tabindex", "-1");

    const paragraph = document.createElement("p");
    paragraph.textContent = message;
    loadState.append(paragraph);
    container.replaceChildren(loadState);
}

function renderUnavailableState(container, onRetry) {
    const loadState = document.createElement("div");
    loadState.className = "privacy-load-state privacy-load-state-error";
    loadState.id = "privacy-policy-load-state";
    loadState.setAttribute("role", "alert");
    loadState.setAttribute("tabindex", "-1");

    const message = document.createElement("p");
    message.textContent = "Não foi possível carregar a política vigente. Seus dados não serão enviados enquanto ela estiver indisponível.";

    const retryButton = document.createElement("button");
    retryButton.type = "button";
    retryButton.className = "privacy-retry-button";
    retryButton.textContent = "Tentar novamente";
    if (typeof onRetry === "function") {
        retryButton.addEventListener("click", onRetry);
    }

    loadState.append(message, retryButton);
    container.replaceChildren(loadState);
}
