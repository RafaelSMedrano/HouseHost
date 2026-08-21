import assert from "node:assert/strict";
import test from "node:test";

import {
    formatPrivacyPolicyEffectiveDate,
    normalizePrivacyPolicyResponse,
    parsePrivacyPolicyContent,
} from "../js/privacyPolicyDocument.js";
import { renderPrivacyPolicyDocument } from "../js/views/politicaPrivacidadeView.js";

test("controlled document preserves semantic sections, paragraphs, lists and safe links", () => {
    const policy = normalizePrivacyPolicyResponse(policyResponse());
    const documentReference = new FakeDocument();
    const container = documentReference.createElement("article");

    renderPrivacyPolicyDocument(container, policy, documentReference);

    assert.deepEqual(findTagList(container), ["SECTION", "DIV", "DIV", "H2", "P", "UL", "LI", "LI", "A"]);
    const link = findFirstTag(container, "A");
    assert.equal(link.href.startsWith("https://example.com/privacidade"), true);
    assert.equal(link.target, "_blank");
    assert.equal(link.rel, "noopener noreferrer");
});

test("script-like backend text remains inert text when rendered", () => {
    const scriptLikeText = '<img src=x onerror="globalThis.compromised=true">';
    const policy = normalizePrivacyPolicyResponse(policyResponse({
        nodes: [{ type: "paragraph", text: scriptLikeText }],
    }));
    const documentReference = new FakeDocument();
    const container = documentReference.createElement("article");

    renderPrivacyPolicyDocument(container, policy, documentReference);

    assert.equal(findFirstTag(container, "P").textContent, scriptLikeText);
    assert.equal(findFirstTag(container, "IMG"), null);
});

test("unsupported nodes, fields and unsafe links fail closed", () => {
    assert.throws(
            () => parsePrivacyPolicyContent(contentWith([{ type: "html", text: "<b>texto</b>" }])),
            /tipo de conteúdo não suportado/
    );
    assert.throws(
            () => parsePrivacyPolicyContent(JSON.stringify({ schemaVersion: 1, sections: [], extra: true })),
            /campo não suportado/
    );
    assert.throws(
            () => parsePrivacyPolicyContent(contentWith([{ type: "link", text: "Abrir", url: "javascript:alert(1)" }])),
            /link não permitido/
    );
    assert.throws(
            () => normalizePrivacyPolicyResponse({
                ...policyResponse(),
                contentHash: "sha256:invalid",
            }),
            /integridade da política é inválida/
    );
});

test("effective date uses its calendar date without timezone displacement", () => {
    assert.equal(
            formatPrivacyPolicyEffectiveDate("2026-07-26T00:00:00", "pt-BR"),
            "26 de julho de 2026"
    );
    assert.throws(() => formatPrivacyPolicyEffectiveDate("2026-02-30T00:00:00"), /data de vigência/);
});

function policyResponse({ nodes } = {}) {
    return {
        id: 7,
        version: 2,
        title: "Política carregada do servidor",
        content: contentWith(nodes || [
            { type: "paragraph", text: "Parágrafo" },
            { type: "list", items: ["Primeiro", "Segundo"] },
            { type: "link", text: "Direitos", url: "https://example.com/privacidade" },
        ]),
        contentHash: `sha256:${"b".repeat(64)}`,
        effectiveAt: "2026-07-26T00:00:00",
    };
}

function contentWith(nodes) {
    return JSON.stringify({
        schemaVersion: 1,
        sections: [{ heading: "Seção", nodes }],
    });
}

function findTagList(root) {
    const tagList = [];
    visitChildren(root, (node) => {
        if (node.tagName && node.tagName !== "FRAGMENT") {
            tagList.push(node.tagName);
        }
    });
    return tagList;
}

function findFirstTag(root, tagName) {
    let matchingNode = null;
    visitChildren(root, (node) => {
        if (!matchingNode && node.tagName === tagName) {
            matchingNode = node;
        }
    });
    return matchingNode;
}

function visitChildren(node, visitor) {
    node.children.forEach((child) => {
        visitor(child);
        visitChildren(child, visitor);
    });
}

class FakeDocument {
    createDocumentFragment() {
        return new FakeNode("FRAGMENT");
    }

    createElement(tagName) {
        return new FakeNode(tagName.toUpperCase());
    }
}

class FakeNode {
    constructor(tagName) {
        this.tagName = tagName;
        this.children = [];
        this.attributes = new Map();
        this.textContent = "";
        this.className = "";
        this.href = "";
        this.target = "";
        this.rel = "";
    }

    append(...nodeList) {
        nodeList.forEach((node) => {
            if (node.tagName === "FRAGMENT") {
                this.children.push(...node.children);
            } else {
                this.children.push(node);
            }
        });
    }

    replaceChildren(...nodeList) {
        this.children = [];
        this.append(...nodeList);
    }

    setAttribute(name, value) {
        this.attributes.set(name, value);
    }
}
