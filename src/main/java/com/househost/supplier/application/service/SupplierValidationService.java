package com.househost.supplier.application.service;

import com.househost.supplier.application.dto.*;
import com.househost.supplier.domain.exception.SupplierException;
import java.util.HashSet;
import org.springframework.stereotype.Service;

@Service
public class SupplierValidationService {
    public void validate(SupplierRequestDTO request) {
        if (request == null || isBlank(request.officialName)) {
            throw new SupplierException("O nome oficial do fornecedor e obrigatorio.");
        }
        if (request.officialName.length() > 180) throw new SupplierException("O nome oficial excede 180 caracteres.");
        if (isBlank(request.countryOfEstablishment)) throw new SupplierException("O pais do fornecedor e obrigatorio.");
        if (request.relationshipList == null || request.relationshipList.isEmpty()) {
            throw new SupplierException("O fornecedor deve possuir ao menos uma relacao de servico.");
        }
        HashSet<Long> relationshipIdSet = new HashSet<>();
        for (SupplierRelationshipRequestDTO relationshipRequest : request.relationshipList) {
            if (relationshipRequest == null || isBlank(relationshipRequest.serviceName)) {
                throw new SupplierException("O nome do servico do fornecedor e obrigatorio.");
            }
            if (relationshipRequest.serviceName.length() > 180) {
                throw new SupplierException("O nome do servico excede 180 caracteres.");
            }
            if (relationshipRequest.id != null && !relationshipIdSet.add(relationshipRequest.id)) {
                throw new SupplierException("Uma relacao nao pode ser enviada mais de uma vez.");
            }
        }
    }

    public void validateReview(SupplierReviewRequestDTO request) {
        if (request == null || request.governanceStatus == null || request.riskLevel == null) {
            throw new SupplierException("Status de governanca e risco sao obrigatorios na revisao.");
        }
    }

    public boolean isBlank(String value) { return value == null || value.isBlank(); }
}
