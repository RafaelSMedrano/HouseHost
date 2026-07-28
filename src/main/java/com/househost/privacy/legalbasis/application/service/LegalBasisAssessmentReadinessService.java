package com.househost.privacy.legalbasis.application.service;

import com.househost.privacy.legalbasis.application.dto.DataProcessingOperationLegalBasisSummaryDTO;
import com.househost.privacy.legalbasis.domain.model.LegalBasisAssessmentStatus;
import com.househost.privacy.legalbasis.domain.model.LegalBasisReadiness;
import com.househost.privacy.legalbasis.domain.model.ProcessingLegalBasisAssessment;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LegalBasisAssessmentReadinessService {
    public LegalBasisReadiness readiness(List<ProcessingLegalBasisAssessment> assessmentList) {
        List<ProcessingLegalBasisAssessment> currentList = current(assessmentList);
        if (currentList.isEmpty()) {
            return LegalBasisReadiness.NOT_ASSESSED;
        }
        if (hasStatus(currentList, LegalBasisAssessmentStatus.REJECTED)) {
            return LegalBasisReadiness.REJECTED;
        }
        if (hasStatus(currentList, LegalBasisAssessmentStatus.UNDER_REVIEW)) {
            return LegalBasisReadiness.UNDER_REVIEW;
        }
        if (hasStatus(currentList, LegalBasisAssessmentStatus.DRAFT)) {
            return LegalBasisReadiness.DRAFT;
        }
        return LegalBasisReadiness.APPROVED;
    }

    public List<DataProcessingOperationLegalBasisSummaryDTO> summaries(
            List<ProcessingLegalBasisAssessment> assessmentList
    ) {
        Map<String, ProcessingLegalBasisAssessment> currentByPurposeMap = current(assessmentList).stream()
                .collect(Collectors.toMap(this::purposeKey, Function.identity()));
        return assessmentList.stream()
                .map(assessment -> new DataProcessingOperationLegalBasisSummaryDTO(
                        assessment,
                        currentByPurposeMap.get(purposeKey(assessment)) == assessment
                ))
                .toList();
    }

    private List<ProcessingLegalBasisAssessment> current(List<ProcessingLegalBasisAssessment> assessmentList) {
        return assessmentList.stream()
                .filter(assessment -> assessment.getStatus() != LegalBasisAssessmentStatus.SUPERSEDED)
                .collect(Collectors.toMap(this::purposeKey, Function.identity(), this::latest))
                .values().stream()
                .toList();
    }

    private ProcessingLegalBasisAssessment latest(
            ProcessingLegalBasisAssessment first,
            ProcessingLegalBasisAssessment second
    ) {
        return Comparator.comparingInt(ProcessingLegalBasisAssessment::getAssessmentVersion)
                .compare(first, second) >= 0 ? first : second;
    }

    private boolean hasStatus(
            List<ProcessingLegalBasisAssessment> assessmentList,
            LegalBasisAssessmentStatus status
    ) {
        return assessmentList.stream().anyMatch(assessment -> assessment.getStatus() == status);
    }

    private String purposeKey(ProcessingLegalBasisAssessment assessment) {
        return assessment.getPurpose().trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
