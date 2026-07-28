package com.househost.privacy.processing.application.service;

import com.househost.privacy.processing.application.dto.ProcessingOperationResponseDTO;
import com.househost.privacy.processing.application.port.out.DataProcessingOperationPersistencePort;
import com.househost.privacy.processing.application.port.out.ProcessingOperationReviewerPort;
import com.househost.privacy.processing.domain.model.DataProcessingOperation;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataProcessingOperationReviewServiceTest {

    @Test
    void reviewsOperationWithAuthenticatedUser() {
        DataProcessingOperation operation = operation();
        DataProcessingOperationPersistencePort persistencePort = persistencePort(operation);
        ProcessingOperationReviewerPort reviewerPort = email -> 7L;
        DataProcessingOperationReviewService service =
                new DataProcessingOperationReviewService(persistencePort, reviewerPort);

        ProcessingOperationResponseDTO response = service.review(1L, "admin@househost.com");

        assertNotNull(response.getReviewedAt());
        assertEquals(7L, response.getReviewedByUserId());
    }

    private DataProcessingOperationPersistencePort persistencePort(DataProcessingOperation operation) {
        return new DataProcessingOperationPersistencePort() {
            @Override
            public DataProcessingOperation save(DataProcessingOperation candidate) {
                return candidate;
            }

            @Override
            public Optional<DataProcessingOperation> findById(Long id) {
                return id.equals(1L) ? Optional.of(operation) : Optional.empty();
            }

            @Override
            public Optional<DataProcessingOperation> findByOperationCode(String operationCode) {
                return Optional.empty();
            }

            @Override
            public List<DataProcessingOperation> findAllOrderedByName() {
                return List.of();
            }

            @Override
            public List<DataProcessingOperation> findAllByStatusOrderedByName(DataProcessingOperationStatus status) {
                return List.of();
            }

            @Override
            public boolean existsByOperationName(String operationName) {
                return false;
            }

            @Override
            public boolean existsByOperationNameExcludingId(String operationName, Long id) {
                return false;
            }

            @Override
            public boolean existsByOperationCode(String operationCode) {
                return false;
            }
        };
    }

    private DataProcessingOperation operation() {
        return new DataProcessingOperation(
                "Gestao de reservas",
                "Recebimento e administracao de reservas.",
                "Administrar hospedagens.",
                "EXECUCAO_DE_CONTRATO",
                "Hospedes",
                "Nome, telefone e e-mail",
                "Site publico e painel administrativo",
                "Coleta, armazenamento, consulta e atualizacao",
                "Administracao e recepcao",
                "Provedores de infraestrutura",
                false,
                "Conforme politica de retencao",
                "Exclusao ou anonimizacao",
                "Autenticacao e controle de acesso",
                "Administracao",
                "HouseHost"
        );
    }
}
