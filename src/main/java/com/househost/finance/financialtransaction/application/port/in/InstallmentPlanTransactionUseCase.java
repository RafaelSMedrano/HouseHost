package com.househost.finance.financialtransaction.application.port.in;

import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionResponseDTO;

public interface InstallmentPlanTransactionUseCase {
    InstallmentPlanTransactionResponseDTO create(InstallmentPlanTransactionRequestDTO request);
    InstallmentPlanTransactionResponseDTO settleInstallment(Long planId, Integer installmentNumber);
}
