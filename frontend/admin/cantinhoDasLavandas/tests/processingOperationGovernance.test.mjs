import assert from "node:assert/strict";
import fs from "node:fs";
import test from "node:test";

globalThis.localStorage = { getItem: () => null, setItem() {}, removeItem() {} };
globalThis.location = { protocol: "http:", hostname: "localhost", port: "8080" };

const apiModule = await import("../js/api.js?v=processing-governance-tests");
const permissionModule = await import("../js/permissions.js?v=processing-governance-tests");
const operationListModule = await import("../js/views/dataProcessingOperationsView.js?v=processing-governance-tests");
const operationProfileModule = await import("../js/views/dataProcessingOperationProfileView.js?v=processing-governance-tests");
const assessmentListModule = await import("../js/views/legalBasisAssessmentsView.js?v=processing-governance-tests");
const assessmentProfileModule = await import("../js/views/legalBasisAssessmentProfileView.js?v=processing-governance-tests");

test("processing-operation governance is restricted to administrators", () => {
    assert.equal(permissionModule.canAccessView("CEO", "processingOperations"), true);
    assert.equal(permissionModule.canAccessView("CTO", "processingOperations"), true);
    assert.equal(permissionModule.canAccessView("ADMIN", "processingOperations"), true);
    assert.equal(permissionModule.canAccessView("MANAGER", "processingOperations"), false);
    assert.equal(permissionModule.canAccessView("RECEPTION", "processingOperations"), false);
});

test("processing-operation API safely encodes filters and identifiers", async () => {
    const requestedUrlList = [];
    globalThis.fetch = async (url) => {
        requestedUrlList.push(url);
        return new Response(JSON.stringify({ status: "success", data: [] }), { status: 200 });
    };

    await apiModule.findAllDataProcessingOperations({ status: "ACTIVE & REVIEW" });
    await apiModule.findDataProcessingOperationById("1/../../audit-events");
    await apiModule.findLegalBasisAssessmentsByOperation("2/../suppliers");
    await apiModule.findLegalBasisAssessmentById("3/../auth/users");

    assert.equal(requestedUrlList[0].includes("status=ACTIVE+%26+REVIEW"), true);
    assert.equal(requestedUrlList[1].endsWith("/1%2F..%2F..%2Faudit-events"), true);
    assert.equal(requestedUrlList[2].endsWith("/2%2F..%2Fsuppliers/legal-basis-assessments"), true);
    assert.equal(requestedUrlList[3].endsWith("/3%2F..%2Fauth%2Fusers"), true);
});

test("legal-basis lifecycle API uses encoded identifiers and expected methods", async () => {
    const requestList = [];
    globalThis.fetch = async (url, options = {}) => {
        requestList.push({ url, method: options.method || "GET", body: options.body });
        return new Response(JSON.stringify({ status: "success", data: { id: 9 } }), { status: 200 });
    };

    await apiModule.createLegalBasisAssessmentDraft("1/../audit", { purpose: "Reserva" });
    await apiModule.updateLegalBasisAssessmentDraft("2/../users", { purpose: "Reserva" });
    await apiModule.submitLegalBasisAssessment(3);
    await apiModule.approveLegalBasisAssessment(4);
    await apiModule.rejectLegalBasisAssessment(5, "Evidência insuficiente");
    await apiModule.createLegalBasisAssessmentRevision(6);

    assert.equal(requestList[0].url.includes("/1%2F..%2Faudit/legal-basis-assessments"), true);
    assert.deepEqual(requestList.map((request) => request.method), ["POST", "PUT", "POST", "POST", "POST", "POST"]);
    assert.equal(JSON.parse(requestList[4].body).reason, "Evidência insuficiente");
});

test("search and governance filters use only concise operation summaries", () => {
    const operationList = [
        operation({ operationName: "Gestão de reservas", operationCode: "BOOKING_MANAGEMENT", legalBasisReadiness: "DRAFT" }),
        operation({ operationName: "Marketing por WhatsApp", operationCode: "WHATSAPP_MARKETING", status: "INACTIVE", legalBasisReadiness: "NOT_ASSESSED", legalBasisAssessmentList: [] }),
    ];

    assert.equal(operationListModule.filterDataProcessingOperations(operationList, { search: "reservas" }).length, 1);
    assert.equal(operationListModule.filterDataProcessingOperations(operationList, { status: "INACTIVE" }).length, 1);
    assert.equal(operationListModule.filterDataProcessingOperations(operationList, { readiness: "DRAFT", work: "PENDING" }).length, 1);
    assert.equal(operationListModule.filterDataProcessingOperations(operationList, { work: "REJECTED" }).length, 0);
});

test("backend text is escaped in list and profile markup", () => {
    const maliciousText = '<img src=x onerror="globalThis.compromised=true">';
    const operationValue = operation({ operationName: maliciousText, description: maliciousText, legalBasis: maliciousText });
    const rowMarkup = operationListModule.buildOperationListRowMarkup(operationValue);
    const profileMarkup = operationProfileModule.buildOperationProfileMarkup(operationValue, [{
        id: 4,
        purpose: maliciousText,
        legalBasis: "LEGITIMATE_INTEREST",
        status: "REJECTED",
        assessmentVersion: 1,
        current: true,
    }]);

    assert.equal(rowMarkup.includes("<img src=x"), false);
    assert.equal(rowMarkup.includes("privacy-operation-card"), false);
    assert.equal(rowMarkup.includes("privacy-operation-row"), true);
    assert.equal(profileMarkup.includes("<img src=x"), false);
    assert.equal(profileMarkup.includes("&lt;img"), true);
    assert.equal(profileMarkup.includes("privacy-inventory-panel"), false);
    assert.equal(profileMarkup.includes("privacy-version-card"), false);
    assert.equal(profileMarkup.includes("privacy-inventory-row"), true);
    assert.equal(profileMarkup.includes("privacy-version-row"), true);
    assert.equal(profileMarkup.includes("não é evidência de aprovação"), true);
});

test("assessment history is grouped by purpose and ordered by newest version", () => {
    const purposeGroupList = operationProfileModule.groupAssessmentVersions([
        { id: 1, purpose: "Executar hospedagem", assessmentVersion: 1 },
        { id: 3, purpose: "Cumprir obrigação fiscal", assessmentVersion: 1 },
        { id: 2, purpose: "executar hospedagem", assessmentVersion: 2 },
    ]);

    assert.equal(purposeGroupList.length, 2);
    assert.deepEqual(purposeGroupList[0].assessmentList.map((assessment) => assessment.id), [2, 1]);
});

test("assessment inventory is flattened and filtered from concise operation summaries", () => {
    const assessmentList = assessmentListModule.flattenLegalBasisAssessments([
        operation(),
        operation({
            id: 2,
            operationName: "Gestão financeira",
            operationCode: "FINANCIAL_MANAGEMENT",
            legalBasisAssessmentList: [{ id: 8, purpose: "Cumprir obrigação fiscal", legalBasis: "LEGAL_OR_REGULATORY_OBLIGATION", status: "APPROVED", assessmentVersion: 2, current: true }],
        }),
    ]);

    assert.equal(assessmentList.length, 2);
    assert.equal(assessmentList[1].operationName, "Gestão financeira");
    assert.equal(assessmentListModule.filterLegalBasisAssessments(assessmentList, { search: "fiscal" }).length, 1);
    assert.equal(assessmentListModule.filterLegalBasisAssessments(assessmentList, { status: "DRAFT" }).length, 1);
    assert.equal(assessmentListModule.filterLegalBasisAssessments(assessmentList, { versionState: "CURRENT" }).length, 2);
});

test("assessment list and profile escape narratives and use line presentation", () => {
    const maliciousText = '<svg onload="globalThis.compromised=true">';
    const rowMarkup = assessmentListModule.buildLegalBasisAssessmentRowMarkup({
        id: 1,
        purpose: maliciousText,
        legalBasis: "CONSENT",
        operationName: maliciousText,
        operationCode: "OPERATION",
        status: "DRAFT",
        assessmentVersion: 1,
        current: true,
    });
    const profileMarkup = assessmentProfileModule.buildLegalBasisAssessmentProfileMarkup({
        id: 1,
        processingOperationId: 1,
        purpose: maliciousText,
        legalBasis: "CONSENT",
        lgpdReference: "Lei nº 13.709/2018, art. 7º, I",
        justification: maliciousText,
        status: "DRAFT",
        assessmentVersion: 1,
        sensitiveData: false,
    }, operation());

    assert.equal(rowMarkup.includes("<svg"), false);
    assert.equal(rowMarkup.includes("privacy-assessment-row"), true);
    assert.equal(profileMarkup.includes("<svg"), false);
    assert.equal(profileMarkup.includes("privacy-inventory-row"), true);
    assert.equal(profileMarkup.includes("Referência na LGPD"), true);
    assert.equal(profileMarkup.includes("Lei nº 13.709/2018, art. 7º, I"), true);
});

test("LGPD reference and concrete external obligation remain distinct", () => {
    const profileMarkup = assessmentProfileModule.buildLegalBasisAssessmentProfileMarkup({
        id: 2,
        processingOperationId: 2,
        purpose: "Cumprir obrigação fiscal",
        legalBasis: "LEGAL_OR_REGULATORY_OBLIGATION",
        lgpdReference: "Lei nº 13.709/2018, art. 7º, II",
        legalReference: "Norma fiscal aplicável",
        legalObligationDescription: "Emitir o documento exigido",
        status: "DRAFT",
        assessmentVersion: 1,
        sensitiveData: false,
    }, operation());

    assert.equal(profileMarkup.includes("Referência na LGPD"), true);
    assert.equal(profileMarkup.includes("Norma concreta da obrigação"), true);
    assert.equal(profileMarkup.includes("Norma fiscal aplicável"), true);
});

test("operation profile exposes working assessment actions without next-step placeholder", () => {
    const markup = operationProfileModule.buildOperationProfileMarkup(operation(), operation().legalBasisAssessmentList, {
        assessmentNavigationAvailable: true,
    });
    assert.equal(markup.includes("Inspecionar avaliação"), true);
    assert.equal(markup.includes("Nova avaliação"), true);
    assert.equal(markup.includes("próxima etapa"), false);
    const marketingMarkup = operationProfileModule.buildOperationProfileMarkup(operation({
        operationCode: "WHATSAPP_MARKETING",
    }), []);
    assert.equal(marketingMarkup.includes("Nova avaliação"), false);
});

test("governance views do not persist or log operation narratives", () => {
    const sourceList = [
        fs.readFileSync(new URL("../js/views/dataProcessingOperationsView.js", import.meta.url), "utf8"),
        fs.readFileSync(new URL("../js/views/dataProcessingOperationProfileView.js", import.meta.url), "utf8"),
        fs.readFileSync(new URL("../js/views/legalBasisAssessmentsView.js", import.meta.url), "utf8"),
        fs.readFileSync(new URL("../js/views/legalBasisAssessmentProfileView.js", import.meta.url), "utf8"),
        fs.readFileSync(new URL("../js/views/legalBasisAssessmentFormView.js", import.meta.url), "utf8"),
    ];
    sourceList.forEach((source) => {
        assert.equal(source.includes("localStorage"), false);
        assert.equal(source.includes("sessionStorage"), false);
        assert.equal(source.includes("console."), false);
    });
});

test("supplier and processing governance pages keep their own vertical scroll", () => {
    const homeCssSource = fs.readFileSync(new URL("../css/home.css", import.meta.url), "utf8");
    assert.match(homeCssSource, /\.main\.supplier-main\s*\{[^}]*overflow-y:\s*auto/s);
    assert.match(homeCssSource, /\.main\.privacy-governance-main\s*\{[^}]*overflow-y:\s*auto/s);
});

test("supplier pages and only the processing profile use a vertical flow", () => {
    const homeCssSource = fs.readFileSync(new URL("../css/home.css", import.meta.url), "utf8");
    const listSource = fs.readFileSync(new URL("../js/views/dataProcessingOperationsView.js", import.meta.url), "utf8");
    assert.match(homeCssSource, /Vertical reading flow for privacy-governance modules/);
    assert.match(homeCssSource, /\.main\.supplier-main \.supplier-detail-groups/);
    assert.match(homeCssSource, /\.main\.privacy-governance-profile-main \.privacy-inventory-row-content/);
    assert.equal(listSource.includes('<div class="privacy-operation-list-header"'), true);
});

function operation(overrides = {}) {
    return {
        id: 1,
        operationName: "Operação",
        operationCode: "OPERATION",
        description: "Descrição",
        purpose: "Finalidade",
        legalBasis: "RESUMO_LEGADO",
        dataSubjectCategories: "Titulares",
        personalDataCategories: "Dados",
        dataSource: "Fonte",
        processingActions: "Ações",
        internalAccessRoles: "Administradores",
        externalRecipients: "Operadores",
        internationalTransfer: false,
        retentionPeriod: "Critério",
        deletionMethod: "Eliminação",
        securityMeasures: "Segurança",
        responsibleArea: "Privacidade",
        systemName: "HouseHost",
        status: "ACTIVE",
        legalBasisReadiness: "DRAFT",
        legalBasisAssessmentList: [{
            id: 1,
            purpose: "Finalidade",
            legalBasis: "CONTRACT_OR_PRE_CONTRACT",
            status: "DRAFT",
            assessmentVersion: 1,
            current: true,
        }],
        ...overrides,
    };
}
