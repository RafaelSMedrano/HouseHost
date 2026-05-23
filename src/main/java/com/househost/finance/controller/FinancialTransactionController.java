package com.househost.finance.controller;

import com.househost.finance.dto.FinancialTransactionResponseDTO;
import com.househost.finance.dto.FinancialTransactionRequestDTO;
import com.househost.finance.service.FinancialTransactionService;
import com.househost.shared.dto.ResponseDTO;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/financial-transactions")
public class FinancialTransactionController {

    private final FinancialTransactionService financialTransactionService;

    public FinancialTransactionController(FinancialTransactionService financialTransactionService) {
        this.financialTransactionService = financialTransactionService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody FinancialTransactionRequestDTO request) {
        FinancialTransactionResponseDTO data = financialTransactionService.create(request);
        return new ResponseDTO("success", "Transacao financeira cadastrada com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<FinancialTransactionResponseDTO> data = financialTransactionService.findAll();
        return new ResponseDTO("success", "Transacoes financeiras encontradas com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        FinancialTransactionResponseDTO data = financialTransactionService.findById(id);
        return new ResponseDTO("success", "Transacao financeira encontrada com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody FinancialTransactionRequestDTO request) {
        FinancialTransactionResponseDTO data = financialTransactionService.update(id, request);
        return new ResponseDTO("success", "Transacao financeira atualizada com sucesso", data);
    }

    @PutMapping("/{id}/settle")
    public ResponseDTO toSettle(@PathVariable Long id) {
        FinancialTransactionResponseDTO data = financialTransactionService.toSettle(id);
        return new ResponseDTO("success", "Transacao financeira liquidada com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        financialTransactionService.delete(id);
        return new ResponseDTO("success", "Transacao financeira removida com sucesso", null);
    }
}
