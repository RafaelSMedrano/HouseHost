/**
 * Creates the in-memory navigation history used by the administrative SPA.
 *
 * The controller receives complete page entries when navigation happens. It
 * intentionally does not know the application's controllers, views or DOM.
 */
export function createNavigationController({ fallbackPage = null, onRendered = null } = {}) {
    const navigationHistory = [];

    /**
     * Abre uma nova página e registra a página atual como predecessora.
     *
     * Objetivo: representar uma navegação normal para uma lista, perfil ou
     * formulário relacionado.
     * Como funciona: valida a entrada, renderiza a página e só então adiciona
     * a entrada ao final do array privado. Se a renderização falhar, o
     * histórico anterior permanece intacto.
     * Quando usar: ao clicar em um registro relacionado, abrir um perfil ou
     * iniciar um formulário a partir da tela atual.
     * Onde chamar: controllers de domínio que recebem esta instância por
     * injeção do UICOntroller; views não chamam este método diretamente.
     */
    function goTo(entry) {
        const safeEntry = normalizeEntry(entry);
        renderEntry(safeEntry);
        navigationHistory.push(safeEntry);
        return current();
    }

    /**
     * Retorna para a entrada imediatamente anterior do histórico.
     *
     * Objetivo: implementar o comportamento do botão “Voltar”.
     * Como funciona: renderiza a entrada anterior antes de removê-la da
     * posição atual. Se não existir predecessor, renderiza a página fallback
     * configurada e reinicia o array com ela.
     * Quando usar: quando o usuário solicitar retorno em qualquer perfil,
     * formulário ou tela relacionada.
     * Onde chamar: o callback onBack fornecido às views deve delegar para este
     * método; controllers não devem escolher manualmente a lista de retorno.
     */
    function back() {
        if (navigationHistory.length > 1) {
            const previousEntry = navigationHistory[navigationHistory.length - 2];
            renderEntry(previousEntry);
            navigationHistory.pop();
            return current();
        }

        if (!fallbackPage) {
            return current();
        }

        const safeFallback = normalizeEntry(fallbackPage);

        if (navigationHistory.length === 1 && samePage(navigationHistory[0], safeFallback)) {
            renderEntry(navigationHistory[0]);
            return current();
        }

        renderEntry(safeFallback);
        navigationHistory.length = 0;
        navigationHistory.push(safeFallback);
        return current();
    }

    /**
     * Substitui a página atual sem criar uma nova etapa de retorno.
     *
     * Objetivo: evitar que uma tela temporária continue aparecendo no
     * histórico depois de uma operação que produziu uma tela definitiva.
     * Como funciona: valida e renderiza a nova entrada, depois troca somente
     * o último elemento do array privado.
     * Quando usar: principalmente após salvar uma edição, quando o fluxo deve
     * permanecer no perfil atualizado em vez de voltar ao formulário salvo.
     * Onde chamar: controller responsável pelo formulário, após confirmar o
     * sucesso; não usar para abrir uma nova relação navegável.
     */
    function replace(entry) {
        const safeEntry = normalizeEntry(entry);
        renderEntry(safeEntry);

        if (navigationHistory.length === 0) {
            navigationHistory.push(safeEntry);
        } else {
            navigationHistory[navigationHistory.length - 1] = safeEntry;
        }

        return current();
    }

    /**
     * Inicia um fluxo independente de navegação.
     *
     * Objetivo: estabelecer uma nova raiz, descartando detalhes de um fluxo
     * anterior.
     * Como funciona: valida e renderiza a entrada, limpa o array e armazena
     * somente essa nova página como raiz.
     * Quando usar: ao selecionar uma página primária na sidebar ou iniciar
     * explicitamente uma área administrativa diferente.
     * Onde chamar: UICOntroller ou sidebarController, nunca como substituto
     * de goTo para abrir perfis relacionados.
     */
    function reset(entry) {
        const safeEntry = normalizeEntry(entry);
        renderEntry(safeEntry);
        navigationHistory.length = 0;
        navigationHistory.push(safeEntry);
        return current();
    }

    /**
     * Retorna uma cópia da página atualmente visível.
     *
     * Objetivo: permitir que a camada de composição ou testes consultem a
     * página atual sem expor o array e os objetos internos do histórico.
     * Como funciona: copia nome, parâmetros e metadados da última entrada;
     * a função render não é exposta.
     * Quando usar: para diagnóstico, testes ou decisões de composição que
     * dependam da página atual.
     * Onde chamar: UICOntroller e testes; não usar para implementar o botão
     * Voltar, que deve chamar back().
     */
    function current() {
        const entry = navigationHistory[navigationHistory.length - 1];
        return entry ? publicEntry(entry) : null;
    }

    /**
     * Informa se existe uma página anterior na navegação atual.
     *
     * Objetivo: permitir que a interface saiba se há um retorno real antes da
     * raiz atual.
     * Como funciona: verifica se o array possui mais de uma entrada.
     * Quando usar: para habilitar, desabilitar ou ajustar a apresentação do
     * controle de retorno, sem substituir a execução de back().
     * Onde chamar: UICOntroller, topbar e testes.
     */
    function canGoBack() {
        return navigationHistory.length > 1;
    }

    /**
     * Valida e copia uma entrada criada por um controller de domínio.
     *
     * Objetivo: garantir que toda página tenha identidade e renderer válidos.
     * Como funciona: exige um objeto com name e render executável e copia os
     * parâmetros e metadados antes de armazená-los.
     * Quando usar: internamente antes de goTo, replace, reset ou back.
     * Onde fica: dentro do navigationController para proteger o histórico.
     */
    function normalizeEntry(entry) {
        if (!entry || typeof entry !== "object") {
            throw new Error("Entrada de navegação inválida");
        }

        if (typeof entry.name !== "string" || entry.name.trim() === "") {
            throw new Error("Entrada de navegação sem nome válido");
        }

        if (typeof entry.render !== "function") {
            throw new Error(`Entrada de navegação sem renderer válido: ${entry.name}`);
        }

        return {
            name: entry.name,
            params: cloneValue(entry.params ?? {}),
            render: entry.render,
            ...(entry.meta === undefined ? {} : { meta: cloneValue(entry.meta) }),
        };
    }

    /**
     * Renderiza uma entrada sem entregar referências internas ao renderer.
     * O renderer recebe uma cópia dos parâmetros e pode ignorá-los quando a
     * função já fechou sobre o contexto da página.
     */
    function renderEntry(entry) {
        entry.render(cloneValue(entry.params));
        if (typeof onRendered === "function") {
            onRendered(publicEntry(entry));
        }
    }

    function samePage(first, second) {
        return first.name === second.name
            && JSON.stringify(first.params) === JSON.stringify(second.params);
    }

    return {
        goTo,
        back,
        replace,
        reset,
        current,
        canGoBack,
    };
}

/**
 * Cria a representação pública de uma entrada sem expor o renderer.
 */
function publicEntry(entry) {
    return {
        name: entry.name,
        params: cloneValue(entry.params),
        ...(entry.meta === undefined ? {} : { meta: cloneValue(entry.meta) }),
    };
}

/**
 * Copia recursivamente arrays e objetos simples usados nos parâmetros e
 * metadados de navegação.
 */
function cloneValue(value) {
    if (Array.isArray(value)) {
        return value.map((item) => cloneValue(item));
    }

    if (value && typeof value === "object") {
        return Object.fromEntries(
            Object.entries(value).map(([key, item]) => [key, cloneValue(item)])
        );
    }

    return value;
}
