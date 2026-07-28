package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.application.port.in.ProcessingLegalBasisAssessmentCatalogUseCase;
import com.househost.privacy.legalbasis.application.port.out.ProcessingLegalBasisAssessmentPersistencePort;
import com.househost.privacy.legalbasis.application.records.LegalBasisCatalogCandidateRecord;
import com.househost.privacy.legalbasis.domain.model.LegalBasisType;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import com.househost.privacy.processing.application.records.ProcessingOperationRecord;
import com.househost.privacy.processing.application.service.DataProcessingOperationService;
import com.househost.privacy.processing.domain.model.DataProcessingOperationCodes;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessingLegalBasisAssessmentCatalogService
        implements ProcessingLegalBasisAssessmentCatalogUseCase {
    private final DataProcessingOperationService processingOperationService;
    private final ProcessingLegalBasisAssessmentPersistencePort assessmentPersistencePort;

    public ProcessingLegalBasisAssessmentCatalogService(
            DataProcessingOperationService processingOperationService,
            ProcessingLegalBasisAssessmentPersistencePort assessmentPersistencePort
    ) {
        this.processingOperationService = processingOperationService;
        this.assessmentPersistencePort = assessmentPersistencePort;
    }

    @Override
    @Transactional
    public void initializeCatalog() {
        processingOperationService.findAllOperationRecords().stream()
                .filter(processingOperationRecord -> !DataProcessingOperationCodes.WHATSAPP_MARKETING.equals(
                        processingOperationRecord.operationCode()
                ))
                .forEach(processingOperationRecord -> candidates(processingOperationRecord).forEach(
                        candidateRecord -> createIfMissing(
                                processingOperationRecord,
                                candidateRecord
                        )
                ));
    }

    private void createIfMissing(
            ProcessingOperationRecord processingOperationRecord,
            LegalBasisCatalogCandidateRecord candidateRecord
    ) {
        if (assessmentPersistencePort.existsByOperationIdAndPurpose(
                processingOperationRecord.operationId(),
                candidateRecord.purpose()
        )) {
            return;
        }
        ProcessingLegalBasisAssessment assessment = new ProcessingLegalBasisAssessment(
                processingOperationRecord.operationId(),
                candidateRecord.purpose(),
                candidateRecord.legalBasis()
        );
        assessment.updateDetails(
                candidateRecord.purpose(),
                candidateRecord.legalBasis(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null
        );
        assessmentPersistencePort.save(assessment);
    }

    private List<LegalBasisCatalogCandidateRecord> candidates(
            ProcessingOperationRecord processingOperationRecord
    ) {
        return switch (processingOperationRecord.operationCode()) {
            case DataProcessingOperationCodes.BOOKING_MANAGEMENT -> List.of(
                    candidate("Administrar reserva e pre-reserva", LegalBasisType.CONTRACT_OR_PRE_CONTRACT));
            case DataProcessingOperationCodes.GUEST_MANAGEMENT -> List.of(
                    candidate("Manter cadastro necessario a reserva e hospedagem", LegalBasisType.CONTRACT_OR_PRE_CONTRACT));
            case DataProcessingOperationCodes.STAY_MANAGEMENT -> List.of(
                    candidate("Executar a hospedagem", LegalBasisType.CONTRACT_OR_PRE_CONTRACT),
                    candidate("Cumprir registros legalmente exigidos da hospedagem", LegalBasisType.LEGAL_OR_REGULATORY_OBLIGATION));
            case DataProcessingOperationCodes.FINANCIAL_MANAGEMENT -> List.of(
                    candidate("Executar cobrancas e pagamentos da hospedagem", LegalBasisType.CONTRACT_OR_PRE_CONTRACT),
                    candidate("Cumprir obrigacoes fiscais e contabeis", LegalBasisType.LEGAL_OR_REGULATORY_OBLIGATION));
            case DataProcessingOperationCodes.USER_ACCESS_MANAGEMENT -> List.of(
                    candidate("Administrar o vinculo e acesso do usuario interno", LegalBasisType.CONTRACT_OR_PRE_CONTRACT),
                    candidate("Proteger o sistema e responsabilizar acessos", LegalBasisType.LEGITIMATE_INTEREST));
            case DataProcessingOperationCodes.SUPPLIER_GOVERNANCE -> List.of(
                    candidate("Cumprir deveres de governanca sobre operadores", LegalBasisType.LEGAL_OR_REGULATORY_OBLIGATION),
                    candidate("Avaliar seguranca e confiabilidade de fornecedores", LegalBasisType.LEGITIMATE_INTEREST));
            case DataProcessingOperationCodes.SECURITY_AUDIT_MANAGEMENT -> List.of(
                    candidate("Prevenir abuso, investigar incidentes e manter rastreabilidade", LegalBasisType.LEGITIMATE_INTEREST));
            case DataProcessingOperationCodes.PRIVACY_GOVERNANCE -> List.of(
                    candidate("Demonstrar conformidade e responsabilizacao em protecao de dados", LegalBasisType.LEGAL_OR_REGULATORY_OBLIGATION));
            default -> List.of();
        };
    }

    private LegalBasisCatalogCandidateRecord candidate(
            String purpose,
            LegalBasisType legalBasis
    ) {
        return new LegalBasisCatalogCandidateRecord(purpose, legalBasis);
    }
}
