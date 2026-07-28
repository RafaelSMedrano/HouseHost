import { changeSupplierStatus, findSupplierById, reviewSupplierRelationship } from "../api.js?v=2026-07-26-supplier-management";

export function renderSupplierProfileView(containerId, options = {}) {
    const container = document.getElementById(containerId);
    container.innerHTML = `<main class="main supplier-main"><div class="supplier-page"><div class="supplier-feedback" role="status" aria-live="polite">Carregando fornecedor...</div><div data-supplier-profile></div></div></main>`;
    loadProfile(container, options);
}

async function loadProfile(container, options) {
    try {
        const response = await findSupplierById(options.supplierId);
        renderProfile(container, response.data, options);
    } catch (error) { container.querySelector(".supplier-feedback").textContent = error.message || "Não foi possível carregar o fornecedor."; }
}

function renderProfile(container, supplier, options) {
    const feedback = container.querySelector(".supplier-feedback");
    feedback.textContent = "";
    container.querySelector("[data-supplier-profile]").innerHTML = `
      <header class="supplier-header supplier-profile-header"><div><button class="dashboard-back-btn" type="button" data-back>Voltar</button><span>Fornecedor #${escapeHtml(supplier.id)}</span><h1>${escapeHtml(supplier.tradeName || supplier.officialName)}</h1><p>${escapeHtml(supplier.officialName)}</p></div><div class="supplier-profile-actions"><button class="dashboard-back-btn" type="button" data-edit><i class="ti ti-pencil"></i>Editar</button><button class="dashboard-back-btn" type="button" data-status>${supplier.status === "ACTIVE" ? "Desativar" : "Ativar"}</button></div></header>
      <section class="supplier-panel"><h2>Identidade e contatos</h2><div class="supplier-detail-grid">${detail("Nome oficial",supplier.officialName)}${detail("Nome fantasia",supplier.tradeName)}${detail("Registro",supplier.registrationIdentifier)}${detail("País",supplier.countryOfEstablishment)}${detail("Site",supplier.website)}${detail("Contato comercial",supplier.businessContact)}${detail("Privacidade",supplier.privacyContact)}${detail("Incidentes",supplier.incidentContact)}${detail("Responsável interno",supplier.internalOwnerUserId)}${detail("Situação",label(supplier.status))}</div></section>
      ${(supplier.relationshipList || []).map((relationship,index)=>relationshipPanel(relationship,index)).join("")}`;
    const profile = container.querySelector("[data-supplier-profile]");
    profile.querySelector("[data-back]").onclick = () => options.onBack?.();
    profile.querySelector("[data-edit]").onclick = () => options.onEdit?.(supplier.id);
    profile.querySelector("[data-status]").onclick = async (event) => {
        const targetStatus = supplier.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
        if (targetStatus === "INACTIVE" && !window.confirm("Todas as relações já foram encerradas e tiveram o destino dos dados registrado?")) return;
        event.currentTarget.disabled = true;
        try { await changeSupplierStatus(supplier.id,targetStatus); await loadProfile(container,options); }
        catch(error) { feedback.textContent=error.message || "Não foi possível alterar a situação."; event.currentTarget.disabled=false; }
    };
    profile.querySelectorAll("[data-review-form]").forEach((form) => form.onsubmit = (event) => submitReview(event,container,supplier,options));
}

function relationshipPanel(relationship,index) {
    return `<section class="supplier-panel supplier-profile-relationship"><div class="supplier-relationship-head"><div><span>Relação ${index+1}</span><h2>${escapeHtml(relationship.serviceName)}</h2></div><div>${badge(label(relationship.role),"neutral")}${badge(label(relationship.riskLevel),riskClass(relationship.riskLevel))}${badge(label(relationship.governanceStatus),"governance")}</div></div>
      <div class="supplier-detail-groups"><div><h3>Finalidade e dados</h3>${detail("Descrição",relationship.description)}${detail("Finalidade",relationship.purpose)}${detail("Dados pessoais",relationship.personalDataCategories)}${detail("Titulares",relationship.dataSubjectCategories)}${detail("Ações",relationship.processingActions)}${detail("Avaliação do papel",relationship.roleAssessment)}</div><div><h3>Localização e transferência</h3>${detail("Localização",relationship.storageLocations)}${detail("Transferência internacional",relationship.internationalTransfer?"Sim":"Não")}${detail("Mecanismo",relationship.transferMechanism)}</div><div><h3>Retenção e segurança</h3>${detail("Retenção",relationship.retentionCriteria)}${detail("Eliminação/devolução",relationship.deletionOrReturnProcedure)}${detail("Segurança",relationship.securityMeasures)}${detail("Canal de incidentes",relationship.incidentNotificationChannel)}${detail("Expectativa de notificação",relationship.incidentNotificationExpectation)}</div><div><h3>Contrato e responsabilidades</h3>${detail("Contrato",label(relationship.contractStatus))}${detail("Referência",relationship.contractReference)}${detail("Vigência",`${formatDate(relationship.contractStartDate)} — ${formatDate(relationship.contractEndDate)}`)}${detail("Responsabilidades",relationship.responsibilitySummary)}${detail("Suboperadores",relationship.subOperatorInformation)}</div><div><h3>Revisão e encerramento</h3>${detail("Notas",relationship.assessmentNotes)}${detail("Revisado em",formatDateTime(relationship.reviewedAt))}${detail("Próxima revisão",formatDate(relationship.nextReviewDate))}${detail("Término",formatDate(relationship.endedAt))}${detail("Destino",label(relationship.dataDispositionStatus))}${detail("Justificativa",relationship.dataDispositionNotes)}</div></div>
      <form class="supplier-review-form" data-review-form data-relationship-id="${escapeHtml(relationship.id)}"><h3>Registrar revisão</h3><label>Decisão<select name="governanceStatus"><option value="PENDING">Pendente</option><option value="APPROVED">Aprovado</option><option value="BLOCKED">Bloqueado</option><option value="INACTIVE">Inativo</option></select></label><label>Risco<select name="riskLevel"><option value="LOW">Baixo</option><option value="MEDIUM">Médio</option><option value="HIGH">Alto</option></select></label><label>Próxima revisão<input name="nextReviewDate" type="date"></label><label>Notas<textarea name="assessmentNotes"></textarea></label><button class="btn btn-primary" type="submit">Salvar revisão</button><p>Uma aprovação registra a decisão do revisor; não constitui certificação jurídica automática.</p></form></section>`;
}

async function submitReview(event,container,supplier,options) {
    event.preventDefault(); const form=event.currentTarget; const button=form.querySelector("button"); const feedback=container.querySelector(".supplier-feedback");
    const review={governanceStatus:value(form,"governanceStatus"),riskLevel:value(form,"riskLevel"),nextReviewDate:value(form,"nextReviewDate"),assessmentNotes:value(form,"assessmentNotes")};
    if(review.governanceStatus === "APPROVED" && !review.nextReviewDate){feedback.textContent="Defina a próxima revisão antes de aprovar.";return;}
    button.disabled=true;
    try{await reviewSupplierRelationship(supplier.id,Number(form.dataset.relationshipId),review);await loadProfile(container,options);}
    catch(error){feedback.textContent=error.message||"Não foi possível registrar a revisão.";button.disabled=false;}
}
function detail(title,value){return `<div class="supplier-detail"><span>${escapeHtml(title)}</span><p>${escapeHtml(value ?? "Não informado")}</p></div>`;}
function badge(text,type){return `<span class="supplier-badge ${type}">${escapeHtml(text)}</span>`;}
function riskClass(value){return value === "HIGH"?"danger":value === "MEDIUM"?"warning":"active";}
function label(value){return ({OPERATOR:"Operador",SUB_OPERATOR:"Suboperador",INDEPENDENT_CONTROLLER:"Controlador independente",JOINT_CONTROLLER:"Controlador conjunto",RECIPIENT:"Destinatário",NO_PERSONAL_DATA:"Sem dados pessoais",LOW:"Baixo",MEDIUM:"Médio",HIGH:"Alto",DRAFT:"Rascunho",PENDING:"Pendente",APPROVED:"Aprovado",BLOCKED:"Bloqueado",ACTIVE:"Ativo",INACTIVE:"Inativo",NOT_REVIEWED:"Não avaliado",ABSENT:"Ausente",UNDER_REVIEW:"Em avaliação",EXPIRED:"Expirado",NOT_APPLICABLE:"Não aplicável",RETURNED:"Devolvidos",DELETED:"Eliminados",RETAINED_WITH_JUSTIFICATION:"Retidos com justificativa"})[value]||value||"Não informado";}
function formatDate(value){return value?new Intl.DateTimeFormat("pt-BR").format(new Date(`${value}T12:00:00`)):"Não definida";}
function formatDateTime(value){return value?new Intl.DateTimeFormat("pt-BR",{dateStyle:"short",timeStyle:"short"}).format(new Date(value)):"Não realizada";}
function value(container,name){return container.querySelector(`[name="${name}"]`)?.value.trim()||null;}
function escapeHtml(value){return String(value??"").replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;").replaceAll("'","&#039;");}
