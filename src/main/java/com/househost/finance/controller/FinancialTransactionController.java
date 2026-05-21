package com.househost.finance.controller;

import com.househost.finance.dto.FinancialTransactionRequestDTO;
import com.househost.finance.service.FinancialTransactionService;
import com.househost.shared.dto.ResponseDTO;
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
        return financialTransactionService.create(request);
    }

    @GetMapping
    public ResponseDTO findAll() {
        return financialTransactionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return financialTransactionService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody FinancialTransactionRequestDTO request) {
        return financialTransactionService.update(id, request);
    }

    @PutMapping("/{id}/settle")
    public ResponseDTO toSettle(@PathVariable Long id) {
        return financialTransactionService.toSettle(id);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        return financialTransactionService.delete(id);
    }
}
