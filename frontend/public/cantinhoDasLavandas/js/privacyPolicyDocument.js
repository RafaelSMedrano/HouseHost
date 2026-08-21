const ROOT_FIELD_SET = new Set(["schemaVersion", "sections"]);
const SECTION_FIELD_SET = new Set(["heading", "nodes"]);
const NODE_FIELD_SET_MAP = new Map([
    ["paragraph", new Set(["type", "text"])],
    ["list", new Set(["type", "items"])],
    ["link", new Set(["type", "text", "url"])],
]);

export function normalizePrivacyPolicyResponse(policyResponse) {
    requireObject(policyResponse, "A política retornada");

    const id = requirePositiveInteger(policyResponse.id, "O identificador da política");
    const version = requirePositiveInteger(policyResponse.version, "A versão da política");
    const title = requireText(policyResponse.title, "O título da política");
    const contentHash = requireContentHash(policyResponse.contentHash);
    const effectiveAt = requireEffectiveDate(policyResponse.effectiveAt);

    return Object.freeze({
        id,
        version,
        title,
        content: parsePrivacyPolicyContent(policyResponse.content),
        contentHash,
        effectiveAt,
    });
}

export function parsePrivacyPolicyContent(content) {
    if (typeof content !== "string" || !content.trim()) {
        throw new Error("O conteúdo da política está indisponível.");
    }

    let documentContent;
    try {
        documentContent = JSON.parse(content);
    } catch {
        throw new Error("O conteúdo da política possui formato inválido.");
    }

    requireObject(documentContent, "O documento da política");
    requireOnlyFields(documentContent, ROOT_FIELD_SET);

    if (documentContent.schemaVersion !== 1) {
        throw new Error("O formato da política não é suportado.");
    }
    if (!Array.isArray(documentContent.sections) || documentContent.sections.length === 0) {
        throw new Error("A política não possui seções disponíveis.");
    }

    const sectionList = documentContent.sections.map(normalizeSection);
    return Object.freeze({
        schemaVersion: 1,
        sections: Object.freeze(sectionList),
    });
}

export function formatPrivacyPolicyEffectiveDate(effectiveAt, locale = "pt-BR") {
    const normalizedEffectiveAt = requireEffectiveDate(effectiveAt);
    const [year, month, day] = normalizedEffectiveAt.slice(0, 10).split("-").map(Number);
    const effectiveDate = new Date(Date.UTC(year, month - 1, day));

    return new Intl.DateTimeFormat(locale, {
        day: "numeric",
        month: "long",
        year: "numeric",
        timeZone: "UTC",
    }).format(effectiveDate);
}

function normalizeSection(section) {
    requireObject(section, "Cada seção da política");
    requireOnlyFields(section, SECTION_FIELD_SET);

    if (!Array.isArray(section.nodes) || section.nodes.length === 0) {
        throw new Error("Uma seção da política está sem conteúdo.");
    }

    return Object.freeze({
        heading: requireText(section.heading, "O título da seção"),
        nodes: Object.freeze(section.nodes.map(normalizeNode)),
    });
}

function normalizeNode(node) {
    requireObject(node, "Cada bloco da política");

    const allowedFieldSet = NODE_FIELD_SET_MAP.get(node.type);
    if (!allowedFieldSet) {
        throw new Error("A política contém um tipo de conteúdo não suportado.");
    }
    requireOnlyFields(node, allowedFieldSet);

    if (node.type === "paragraph") {
        return Object.freeze({
            type: "paragraph",
            text: requireText(node.text, "O parágrafo da política"),
        });
    }

    if (node.type === "list") {
        if (!Array.isArray(node.items) || node.items.length === 0) {
            throw new Error("Uma lista da política está sem itens.");
        }
        return Object.freeze({
            type: "list",
            items: Object.freeze(node.items.map((item) => requireText(item, "O item da política"))),
        });
    }

    return Object.freeze({
        type: "link",
        text: requireText(node.text, "O texto do link da política"),
        url: requireSafeHttpUrl(node.url),
    });
}

function requireSafeHttpUrl(value) {
    const url = requireText(value, "O endereço do link da política");
    let parsedUrl;

    try {
        parsedUrl = new URL(url);
    } catch {
        throw new Error("A política contém um link inválido.");
    }

    if (!["http:", "https:"].includes(parsedUrl.protocol) || !parsedUrl.hostname) {
        throw new Error("A política contém um link não permitido.");
    }

    return parsedUrl.href;
}

function requireEffectiveDate(value) {
    const effectiveAt = requireText(value, "A data de vigência da política");
    const dateMatch = /^(\d{4})-(\d{2})-(\d{2})(?:T.*)?$/.exec(effectiveAt);

    if (!dateMatch) {
        throw new Error("A data de vigência da política é inválida.");
    }

    const [, year, month, day] = dateMatch;
    const date = new Date(Date.UTC(Number(year), Number(month) - 1, Number(day)));
    const matchesCalendarDate = date.getUTCFullYear() === Number(year)
        && date.getUTCMonth() === Number(month) - 1
        && date.getUTCDate() === Number(day);

    if (!matchesCalendarDate) {
        throw new Error("A data de vigência da política é inválida.");
    }

    return effectiveAt;
}

function requireContentHash(value) {
    const contentHash = requireText(value, "A identificação de integridade da política");
    if (!/^sha256:[a-f0-9]{64}$/.test(contentHash)) {
        throw new Error("A identificação de integridade da política é inválida.");
    }

    return contentHash;
}

function requirePositiveInteger(value, label) {
    if (!Number.isSafeInteger(value) || value <= 0) {
        throw new Error(`${label} é inválido.`);
    }

    return value;
}

function requireText(value, label) {
    if (typeof value !== "string" || !value.trim()) {
        throw new Error(`${label} está indisponível.`);
    }

    return value;
}

function requireObject(value, label) {
    if (!value || typeof value !== "object" || Array.isArray(value)) {
        throw new Error(`${label} possui formato inválido.`);
    }
}

function requireOnlyFields(value, allowedFieldSet) {
    Object.keys(value).forEach((field) => {
        if (!allowedFieldSet.has(field)) {
            throw new Error("A política contém um campo não suportado.");
        }
    });
}
