package com.househost.supplier.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.househost.supplier.domain.exception.SupplierException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SupplierDomainTest {

    @Test
    void supplierRequiresOfficialNameAndRelationship() {
        assertThrows(SupplierException.class, () -> new Supplier(null, "", "", null,
                null, null, "Brasil", null, null, null, null,
                SupplierStatus.ACTIVE, List.of()));
    }

    @Test
    void noPersonalDataRejectsTreatmentFields() {
        assertThrows(SupplierException.class, () -> relationship(SupplierDataRole.NO_PERSONAL_DATA,
                "Nome", SupplierGovernanceStatus.DRAFT));
    }

    @Test
    void approvalRequiresReviewerAndReviewDate() {
        SupplierDataProcessingRelationship relationship = relationship(SupplierDataRole.OPERATOR,
                "Nome", SupplierGovernanceStatus.DRAFT);
        assertThrows(SupplierException.class, () -> relationship.review(
                SupplierGovernanceStatus.APPROVED, SupplierRiskLevel.LOW,
                "Aprovado", LocalDate.now().plusYears(1), null, null));
        assertDoesNotThrow(() -> relationship.review(SupplierGovernanceStatus.APPROVED,
                SupplierRiskLevel.LOW, "Aprovado", LocalDate.now().plusYears(1),
                1L, LocalDateTime.now()));
    }

    @Test
    void approvalRejectsContractWithoutCurrentReadiness() {
        List<SupplierContractStatus> blockedContractStatusList = List.of(
                SupplierContractStatus.NOT_REVIEWED,
                SupplierContractStatus.ABSENT,
                SupplierContractStatus.UNDER_REVIEW,
                SupplierContractStatus.EXPIRED
        );

        blockedContractStatusList.forEach(contractStatus -> {
            SupplierDataProcessingRelationship relationship = relationship(
                    SupplierDataRole.OPERATOR,
                    "Nome",
                    SupplierGovernanceStatus.DRAFT,
                    contractStatus
            );

            assertThrows(SupplierException.class, () -> relationship.review(
                    SupplierGovernanceStatus.APPROVED,
                    SupplierRiskLevel.LOW,
                    "Contrato avaliado",
                    LocalDate.now().plusYears(1),
                    1L,
                    LocalDateTime.now()
            ));
        });
    }

    @Test
    void approvalAllowsNotApplicableContractOnlyWithJustification() {
        SupplierDataProcessingRelationship relationshipWithoutJustification = relationship(
                SupplierDataRole.OPERATOR,
                "Nome",
                SupplierGovernanceStatus.DRAFT,
                SupplierContractStatus.NOT_APPLICABLE
        );
        SupplierDataProcessingRelationship justifiedRelationship = relationship(
                SupplierDataRole.OPERATOR,
                "Nome",
                SupplierGovernanceStatus.DRAFT,
                SupplierContractStatus.NOT_APPLICABLE
        );

        assertThrows(SupplierException.class, () -> relationshipWithoutJustification.review(
                SupplierGovernanceStatus.APPROVED,
                SupplierRiskLevel.LOW,
                null,
                LocalDate.now().plusYears(1),
                1L,
                LocalDateTime.now()
        ));
        assertDoesNotThrow(() -> justifiedRelationship.review(
                SupplierGovernanceStatus.APPROVED,
                SupplierRiskLevel.LOW,
                "A relacao nao exige instrumento contratual especifico.",
                LocalDate.now().plusYears(1),
                1L,
                LocalDateTime.now()
        ));
    }

    @Test
    void inactiveRelationshipRequiresDispositionEvidence() {
        assertThrows(SupplierException.class, () -> new SupplierDataProcessingRelationship(
                null, "Hospedagem", null, "Operar sistema", "Nome", "Hospedes",
                "Armazenamento", SupplierDataRole.OPERATOR, "Segue instrucoes", "Brasil",
                false, null, "Durante contrato", "Excluir", "Criptografia", null,
                null, null, SupplierContractStatus.ACTIVE, "Contrato", null, null,
                "Responsabilidades", SupplierRiskLevel.MEDIUM, SupplierGovernanceStatus.INACTIVE,
                null, null, LocalDate.now(), SupplierDataDispositionStatus.NOT_APPLICABLE, null));
    }

    private SupplierDataProcessingRelationship relationship(SupplierDataRole role,
            String personalDataCategories, SupplierGovernanceStatus governanceStatus) {
        return relationship(
                role,
                personalDataCategories,
                governanceStatus,
                SupplierContractStatus.ACTIVE
        );
    }

    private SupplierDataProcessingRelationship relationship(
            SupplierDataRole role,
            String personalDataCategories,
            SupplierGovernanceStatus governanceStatus,
            SupplierContractStatus contractStatus
    ) {
        return new SupplierDataProcessingRelationship(null, "Hospedagem", null,
                role == SupplierDataRole.NO_PERSONAL_DATA ? null : "Operar sistema",
                personalDataCategories, role == SupplierDataRole.NO_PERSONAL_DATA ? null : "Hospedes",
                role == SupplierDataRole.NO_PERSONAL_DATA ? null : "Armazenamento", role,
                role == SupplierDataRole.NO_PERSONAL_DATA ? null : "Segue instrucoes",
                role == SupplierDataRole.NO_PERSONAL_DATA ? null : "Brasil", false, null,
                role == SupplierDataRole.NO_PERSONAL_DATA ? null : "Durante contrato",
                role == SupplierDataRole.NO_PERSONAL_DATA ? null : "Excluir",
                role == SupplierDataRole.NO_PERSONAL_DATA ? null : "Criptografia", null,
                null, null, contractStatus, "Contrato", null, null,
                role == SupplierDataRole.NO_PERSONAL_DATA ? null : "Responsabilidades",
                SupplierRiskLevel.MEDIUM, governanceStatus, null, null, null,
                SupplierDataDispositionStatus.NOT_APPLICABLE, null);
    }
}
