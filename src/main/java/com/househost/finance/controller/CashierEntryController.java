package com.househost.finance.controller;

import com.househost.finance.dto.CashierEntryRequestDTO;
import com.househost.finance.service.CashierEntryService;
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
@RequestMapping("/cashier-entries")
public class CashierEntryController {

    private final CashierEntryService cashierEntryService;

    public CashierEntryController(CashierEntryService cashierEntryService) {
        this.cashierEntryService = cashierEntryService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody CashierEntryRequestDTO request) {
        return cashierEntryService.create(request);
    }

    @GetMapping
    public ResponseDTO findAll() {
        return cashierEntryService.findAll();
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseDTO findByCashierId(@PathVariable Long cashierId) {
        return cashierEntryService.findByCashierId(cashierId);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return cashierEntryService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CashierEntryRequestDTO request) {
        return cashierEntryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        return cashierEntryService.delete(id);
    }
}
