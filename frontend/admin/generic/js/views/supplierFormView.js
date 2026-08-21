import { createSupplier, findSupplierById, updateSupplier } from "../api.js?v=2026-08-11-api-log-transport";

const ROLE_OPTIONS = ["OPERATOR", "SUB_OPERATOR", "INDEPENDENT_CONTROLLER", "JOINT_CONTROLLER", "RECIPIENT", "NO_PERSONAL_DATA"];
const RELATIONSHIP_TEXT_FIELDS = ["description", "purpose", "personalDataCategories", "dataSubjectCategories", "processingActions", "roleAssessment", "storageLocations", "transferMechanism", "retentionCriteria", "deletionOrReturnProcedure", "securityMeasures", "incidentNotificationChannel", "incidentNotificationExpectation", "subOperatorInformation", "contractReference", "responsibilitySummary", "assessmentNotes", "dataDispositionNotes"];

export function renderSupplierFormView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    container.innerHTML = `<main class="main supplier-main"><form class="supplier-page supplier-form" data-supplier-form>
      <header class="supplier-header"><div><span>Inventário interno</span><h1>${options.supplierId ? "Editar" : "Novo"} fornecedor</h1><p>Cadastre a empresa uma vez e detalhe cada serviço separadamente.</p></div><button class="dashboard-back-btn" type="button" data-cancel aria-label="Voltar">Voltar</button></header>
      <div class="supplier-feedback" role="status" aria-live="polite"></div>
      <section class="supplier-panel"><h2>Identidade do fornecedor</h2><div class="supplier-field-grid">
        ${input("officialName", "Nome oficial *", true)}${input("tradeName", "Nome fantasia")}${input("registrationIdentifier", "CNPJ ou registro")}${input("countryOfEstablishment", "País *", true, "Brasil")}${input("website", "Site", false, "", "url")}${input("businessContact", "Contato comercial")}${input("privacyContact", "Canal de privacidade")}${input("incidentContact", "Canal de incidentes")}${input("internalOwnerUserId", "ID do responsável interno", false, "", "number")}
        <label>Situação<select name="status"><option value="ACTIVE">Ativo</option><option value="INACTIVE">Inativo</option></select></label>
      </div></section>
      <div data-relationship-list></div>
      <button class="dashboard-back-btn supplier-add-relationship" type="button" data-add-relationship><i class="ti ti-plus"></i>Adicionar serviço/relação</button>
      <footer class="supplier-form-actions"><button class="dashboard-back-btn" type="button" data-cancel>Cancelar</button><button class="btn btn-primary" type="submit" data-save-supplier><i class="ti ti-check"></i>Salvar fornecedor</button></footer>
    </form></main>`;
    const relationshipList = container.querySelector("[data-relationship-list]");
    addRelationship(relationshipList);
    container.querySelector("[data-add-relationship]").onclick = () => addRelationship(relationshipList);
    container.querySelectorAll("[data-cancel]").forEach((button) => button.onclick = () => options.onCancel?.());
    container.querySelector("[data-supplier-form]").onsubmit = (event) => submitSupplier(event, container, options);
    if (options.supplierId) loadSupplier(container, options.supplierId);
}

function addRelationship(container, relationship = {}) {
    const index = container.children.length;
    const section = document.createElement("section");
    section.className = "supplier-panel supplier-relationship";
    section.dataset.relationshipId = relationship.id || "";
    section.innerHTML = `<div class="supplier-relationship-head"><div><span>Relação ${index + 1}</span><h2>Serviço e tratamento de dados</h2></div><button type="button" class="supplier-remove" aria-label="Remover esta relação"><i class="ti ti-trash"></i></button></div>
      <p class="supplier-role-help" data-role-help>Escolha o papel conforme o comportamento real neste serviço.</p>
      <div class="supplier-field-grid">${input("serviceName", "Nome do serviço *", true)}${select("role", "Papel LGPD *", ROLE_OPTIONS)}${textarea("description", "Descrição operacional")}${textarea("purpose", "Finalidade")}${textarea("personalDataCategories", "Categorias de dados pessoais", true)}${textarea("dataSubjectCategories", "Categorias de titulares", true)}${textarea("processingActions", "Ações de tratamento", true)}${textarea("roleAssessment", "Justificativa do papel LGPD", true)}${textarea("storageLocations", "Localização dos dados", true)}
        <label class="supplier-check"><input name="internationalTransfer" type="checkbox">Há transferência internacional</label>${textarea("transferMechanism", "Mecanismo de transferência")}${textarea("retentionCriteria", "Critério de retenção", true)}${textarea("deletionOrReturnProcedure", "Eliminação ou devolução", true)}${textarea("securityMeasures", "Medidas de segurança", true)}${input("incidentNotificationChannel", "Canal de incidentes")}${textarea("incidentNotificationExpectation", "Prazo/expectativa de notificação")}${textarea("subOperatorInformation", "Suboperadores")}${select("contractStatus", "Situação contratual", ["NOT_REVIEWED", "ABSENT", "UNDER_REVIEW", "ACTIVE", "EXPIRED", "NOT_APPLICABLE"])}${input("contractReference", "Referência do contrato")}${input("contractStartDate", "Início do contrato", false, "", "date")}${input("contractEndDate", "Fim/renovação", false, "", "date")}${textarea("responsibilitySummary", "Responsabilidades", true)}${select("riskLevel", "Risco", ["LOW", "MEDIUM", "HIGH"])}${select("governanceStatus", "Governança", ["DRAFT", "PENDING", "BLOCKED", "INACTIVE"])}${textarea("assessmentNotes", "Notas da avaliação")}${input("nextReviewDate", "Próxima revisão", false, "", "date")}${input("endedAt", "Término do serviço", false, "", "date")}${select("dataDispositionStatus", "Destino dos dados", ["NOT_APPLICABLE", "PENDING", "RETURNED", "DELETED", "RETAINED_WITH_JUSTIFICATION"])}${textarea("dataDispositionNotes", "Observação/justificativa do destino")}</div>`;
    container.appendChild(section);
    fillRelationship(section, relationship);
    section.querySelector("[name=role]").onchange = () => applyRoleBehavior(section);
    section.querySelector(".supplier-remove").onclick = () => {
        if (container.children.length > 1 && !section.dataset.relationshipId) section.remove();
    };
    applyRoleBehavior(section);
}

async function loadSupplier(container, supplierId) {
    const feedback = container.querySelector(".supplier-feedback");
    try {
        const response = await findSupplierById(supplierId);
        const supplier = response.data;
        ["officialName", "tradeName", "registrationIdentifier", "countryOfEstablishment", "website", "businessContact", "privacyContact", "incidentContact", "internalOwnerUserId", "status"].forEach((name) => setValue(container, name, supplier[name]));
        const relationshipList = container.querySelector("[data-relationship-list]");
        relationshipList.innerHTML = "";
        (supplier.relationshipList || []).forEach((relationship) => addRelationship(relationshipList, relationship));
        if (!relationshipList.children.length) addRelationship(relationshipList);
    } catch (error) { feedback.textContent = error.message || "Não foi possível carregar o fornecedor."; }
}

async function submitSupplier(event, container, options) {
    event.preventDefault();
    const feedback = container.querySelector(".supplier-feedback");
    const saveButton = container.querySelector("[data-save-supplier]");
    const payload = collectSupplier(container);
    if (!payload.officialName || !payload.countryOfEstablishment || !payload.relationshipList.length) { feedback.textContent = "Preencha a identidade e ao menos uma relação."; return; }
    const incompleteRelationship = payload.relationshipList.find((relationship) => !relationship.serviceName || !relationship.role);
    if (incompleteRelationship) { feedback.textContent = "Informe o serviço e o papel LGPD de todas as relações."; return; }
    saveButton.disabled = true; saveButton.innerHTML = '<i class="ti ti-loader-2"></i>Salvando...';
    try {
        const response = options.supplierId ? await updateSupplier(options.supplierId, payload) : await createSupplier(payload);
        feedback.textContent = response.message || "Fornecedor salvo com sucesso.";
        options.onSaved?.(response.data);
    } catch (error) { feedback.textContent = error.message || "Não foi possível salvar o fornecedor."; }
    finally { saveButton.disabled = false; saveButton.innerHTML = '<i class="ti ti-check"></i>Salvar fornecedor'; }
}

function collectSupplier(container) {
    const form = container.querySelector("[data-supplier-form]");
    const relationshipList = [...container.querySelectorAll(".supplier-relationship")].map(collectRelationship);
    return { officialName:value(form,"officialName"), tradeName:value(form,"tradeName"), registrationIdentifier:value(form,"registrationIdentifier"), countryOfEstablishment:value(form,"countryOfEstablishment"), website:value(form,"website"), businessContact:value(form,"businessContact"), privacyContact:value(form,"privacyContact"), incidentContact:value(form,"incidentContact"), internalOwnerUserId:numberValue(form,"internalOwnerUserId"), status:value(form,"status"), relationshipList };
}
function collectRelationship(section) { const relationship = { id: section.dataset.relationshipId ? Number(section.dataset.relationshipId) : null, serviceName:value(section,"serviceName"), role:value(section,"role"), internationalTransfer:section.querySelector("[name=internationalTransfer]").checked, contractStatus:value(section,"contractStatus"), contractStartDate:value(section,"contractStartDate"), contractEndDate:value(section,"contractEndDate"), riskLevel:value(section,"riskLevel"), governanceStatus:value(section,"governanceStatus"), nextReviewDate:value(section,"nextReviewDate"), endedAt:value(section,"endedAt"), dataDispositionStatus:value(section,"dataDispositionStatus") }; RELATIONSHIP_TEXT_FIELDS.forEach((name) => relationship[name] = value(section,name)); return relationship; }
function fillRelationship(section, relationship) { Object.entries(relationship).forEach(([name, fieldValue]) => { const field=section.querySelector(`[name="${name}"]`); if (!field) return; if (field.type === "checkbox") field.checked=Boolean(fieldValue); else field.value=fieldValue ?? ""; }); }
function applyRoleBehavior(section) { const noData=section.querySelector("[name=role]").value === "NO_PERSONAL_DATA"; ["personalDataCategories","dataSubjectCategories","processingActions"].forEach((name) => { const field=section.querySelector(`[name=${name}]`); field.disabled=noData; field.required=!noData; if(noData) field.value=""; }); section.querySelector("[data-role-help]").textContent=noData ? "Esta relação declara que o serviço não trata dados pessoais." : "Descreva as evidências específicas deste serviço."; }
function input(name,labelText,required=false,fieldValue="",type="text") { return `<label>${labelText}<input name="${name}" type="${type}" value="${fieldValue}" ${required?"required":""}></label>`; }
function textarea(name,labelText,required=false) { return `<label>${labelText}<textarea name="${name}" ${required?"required":""}></textarea></label>`; }
function select(name,labelText,optionList) { return `<label>${labelText}<select name="${name}">${optionList.map((item)=>`<option value="${item}">${label(item)}</option>`).join("")}</select></label>`; }
function label(value) { return ({OPERATOR:"Operador",SUB_OPERATOR:"Suboperador",INDEPENDENT_CONTROLLER:"Controlador independente",JOINT_CONTROLLER:"Controlador conjunto",RECIPIENT:"Destinatário",NO_PERSONAL_DATA:"Sem dados pessoais",NOT_REVIEWED:"Não avaliado",ABSENT:"Ausente",UNDER_REVIEW:"Em avaliação",ACTIVE:"Ativo",EXPIRED:"Expirado",NOT_APPLICABLE:"Não aplicável",LOW:"Baixo",MEDIUM:"Médio",HIGH:"Alto",DRAFT:"Rascunho",PENDING:"Pendente",BLOCKED:"Bloqueado",INACTIVE:"Inativo",RETURNED:"Devolvidos",DELETED:"Eliminados",RETAINED_WITH_JUSTIFICATION:"Retidos com justificativa"})[value] || value; }
function value(container,name) { return container.querySelector(`[name="${name}"]`)?.value.trim() || null; }
function numberValue(container,name) { const result=value(container,name); return result ? Number(result) : null; }
function setValue(container,name,fieldValue) { const field=container.querySelector(`[name="${name}"]`); if(field) field.value=fieldValue ?? ""; }
