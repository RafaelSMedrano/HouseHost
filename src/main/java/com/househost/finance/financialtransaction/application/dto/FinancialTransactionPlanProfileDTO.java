package com.househost.finance.financialtransaction.application.dto;

import com.househost.finance.financialtransaction.domain.model.FinancialTransactionPlan;

import java.util.List;

public class FinancialTransactionPlanProfileDTO {

    private final FinancialTransactionPlanSummaryDTO summary;
    private final String senderType;
    private final Long senderId;
    private final String receiverType;
    private final Long receiverId;
    private final String sourceType;
    private final Long sourceId;
    private final String description;
    private final Long version;
    private final List<FinancialTransactionResponseDTO> financialTransactionResponseDTOList;

    public FinancialTransactionPlanProfileDTO(FinancialTransactionPlan financialTransactionPlan) {
        summary = new FinancialTransactionPlanSummaryDTO(financialTransactionPlan);
        senderType = financialTransactionPlan.getSenderType().name();
        senderId = financialTransactionPlan.getSenderId();
        receiverType = financialTransactionPlan.getReceiverType().name();
        receiverId = financialTransactionPlan.getReceiverId();
        sourceType = financialTransactionPlan.getSourceType().name();
        sourceId = financialTransactionPlan.getSourceId();
        description = financialTransactionPlan.getDescription();
        version = financialTransactionPlan.getVersion();
        financialTransactionResponseDTOList =
                financialTransactionPlan.getFinancialTransactionList().stream()
                        .map(FinancialTransactionResponseDTO::new)
                        .toList();
    }

    public FinancialTransactionPlanSummaryDTO getSummary() {
        return summary;
    }

    public String getSenderType() {
        return senderType;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public String getDescription() {
        return description;
    }

    public Long getVersion() {
        return version;
    }

    public List<FinancialTransactionResponseDTO> getFinancialTransactionResponseDTOList() {
        return financialTransactionResponseDTOList;
    }
}
