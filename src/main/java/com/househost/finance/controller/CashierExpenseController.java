package com.househost.finance.controller;

import com.househost.finance.dto.CashierExpenseRequestDTO;
import com.househost.finance.service.CashierExpenseService;
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
@RequestMapping("/cashier-expenses")
public class CashierExpenseController {

    private final CashierExpenseService cashierExpenseService;

    public CashierExpenseController(CashierExpenseService cashierExpenseService) {
        this.cashierExpenseService = cashierExpenseService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody CashierExpenseRequestDTO request) {
        return cashierExpenseService.create(request);
    }

    @GetMapping
    public ResponseDTO findAll() {
        return cashierExpenseService.findAll();
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseDTO findByCashierId(@PathVariable Long cashierId) {
        return cashierExpenseService.findByCashierId(cashierId);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return cashierExpenseService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CashierExpenseRequestDTO request) {
        return cashierExpenseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        return cashierExpenseService.delete(id);
    }
}
