package com.househost.finance.controller;

import com.househost.finance.dto.CashierRequestDTO;
import com.househost.finance.service.CashierService;
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
@RequestMapping("/cashiers")
public class CashierController {

    private final CashierService cashierService;

    public CashierController(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody CashierRequestDTO request) {
        return cashierService.create(request);
    }

    @GetMapping
    public ResponseDTO findAll() {
        return cashierService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return cashierService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CashierRequestDTO request) {
        return cashierService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        return cashierService.delete(id);
    }
}
