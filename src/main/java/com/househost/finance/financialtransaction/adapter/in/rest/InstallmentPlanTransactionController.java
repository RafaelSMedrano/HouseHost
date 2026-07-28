package com.househost.finance.financialtransaction.adapter.in.rest;

import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionRequestDTO;
import com.househost.finance.financialtransaction.application.dto.InstallmentPlanTransactionResponseDTO;
import com.househost.finance.financialtransaction.application.port.in.InstallmentPlanTransactionUseCase;
import com.househost.shared.dto.ResponseDTO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/installment-plans")
public class InstallmentPlanTransactionController {

    private final InstallmentPlanTransactionUseCase installmentPlanUseCase;

    public InstallmentPlanTransactionController(InstallmentPlanTransactionUseCase installmentPlanUseCase) {
        this.installmentPlanUseCase = installmentPlanUseCase;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody InstallmentPlanTransactionRequestDTO request) {
        InstallmentPlanTransactionResponseDTO data = installmentPlanUseCase.create(request);
        return new ResponseDTO("success", "Plano parcelado cadastrado com sucesso", data);
    }

    @PutMapping("/{planId}/installments/{installmentNumber}/settle")
    public ResponseDTO settleInstallment(
            @PathVariable Long planId,
            @PathVariable Integer installmentNumber
    ) {
        InstallmentPlanTransactionResponseDTO data = installmentPlanUseCase.settleInstallment(
                planId,
                installmentNumber
        );
        return new ResponseDTO("success", "Parcela liquidada com sucesso", data);
    }
}
