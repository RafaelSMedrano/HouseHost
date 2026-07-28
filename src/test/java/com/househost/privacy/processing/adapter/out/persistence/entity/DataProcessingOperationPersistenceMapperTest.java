package com.househost.privacy.processing.adapter.out.persistence.entity;

import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataProcessingOperationPersistenceMapperTest {

    @Test
    void preservesOperationStateInPersistenceRoundTrip() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 26, 10, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 26, 11, 30);
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 7, 26, 12, 30);
        DataProcessingOperation original = operation();
        original.restorePersistenceState(
                42L,
                DataProcessingOperationStatus.INACTIVE,
                createdAt,
                updatedAt,
                reviewedAt,
                9L
        );

        DataProcessingOperation restored = DataProcessingOperationPersistenceMapper.toDomain(
                DataProcessingOperationPersistenceMapper.toEntity(original)
        );

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getOperationCode(), restored.getOperationCode());
        assertEquals(original.getOperationName(), restored.getOperationName());
        assertEquals(original.getDescription(), restored.getDescription());
        assertEquals(original.getPurpose(), restored.getPurpose());
        assertEquals(original.getLegalBasis(), restored.getLegalBasis());
        assertEquals(original.getDataSubjectCategories(), restored.getDataSubjectCategories());
        assertEquals(original.getPersonalDataCategories(), restored.getPersonalDataCategories());
        assertEquals(original.getDataSource(), restored.getDataSource());
        assertEquals(original.getProcessingActions(), restored.getProcessingActions());
        assertEquals(original.getInternalAccessRoles(), restored.getInternalAccessRoles());
        assertEquals(original.getExternalRecipients(), restored.getExternalRecipients());
        assertEquals(original.getInternationalTransfer(), restored.getInternationalTransfer());
        assertEquals(original.getRetentionPeriod(), restored.getRetentionPeriod());
        assertEquals(original.getDeletionMethod(), restored.getDeletionMethod());
        assertEquals(original.getSecurityMeasures(), restored.getSecurityMeasures());
        assertEquals(original.getResponsibleArea(), restored.getResponsibleArea());
        assertEquals(original.getSystemName(), restored.getSystemName());
        assertEquals(original.getStatus(), restored.getStatus());
        assertEquals(original.getCreatedAt(), restored.getCreatedAt());
        assertEquals(original.getUpdatedAt(), restored.getUpdatedAt());
        assertEquals(original.getReviewedAt(), restored.getReviewedAt());
        assertEquals(original.getReviewedByUserId(), restored.getReviewedByUserId());
    }

    private DataProcessingOperation operation() {
        return new DataProcessingOperation(
                "SECURITY_AND_AUDIT",
                "Seguranca e auditoria",
                "Registro de eventos relevantes.",
                "Proteger o sistema.",
                "LEGITIMO_INTERESSE",
                "Usuarios e hospedes",
                "Identificadores e registros tecnicos",
                "Aplicacao",
                "Registro e consulta",
                "Administracao",
                "Infraestrutura",
                true,
                "Prazo de seguranca",
                "Exclusao segura",
                "Autenticacao e trilha de auditoria",
                "Tecnologia",
                "HouseHost"
        );
    }
}
