package com.househost.privacy.processing.application.service;

import com.househost.privacy.processing.application.dto.DataProcessingOperationRequestDTO;
import com.househost.shared.exception.PrivacyException;
import org.springframework.stereotype.Component;

@Component
public class DataProcessingOperationValidationService {
    public void validate(DataProcessingOperationRequestDTO request) {
        if (request == null) {
            throw new PrivacyException("Dados da operacao de tratamento sao obrigatorios.");
        }
        require(request.operationName, "Nome da operacao");
        require(request.description, "Descricao");
        require(request.purpose, "Finalidade");
        require(request.legalBasis, "Base legal");
        require(request.dataSubjectCategories, "Categorias de titulares");
        require(request.personalDataCategories, "Categorias de dados pessoais");
        require(request.dataSource, "Origem dos dados");
        require(request.processingActions, "Acoes de tratamento");
        require(request.internalAccessRoles, "Perfis internos com acesso");
        require(request.retentionPeriod, "Prazo de retencao");
        require(request.deletionMethod, "Metodo de exclusao ou anonimizacao");
        require(request.securityMeasures, "Medidas de seguranca");
        require(request.responsibleArea, "Area responsavel");
        require(request.systemName, "Nome do sistema");
    }

    private void require(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new PrivacyException(label + " e obrigatorio.");
        }
    }
}
