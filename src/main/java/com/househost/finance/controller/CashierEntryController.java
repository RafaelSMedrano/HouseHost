package com.househost.finance.controller;

import com.househost.finance.dto.CashierEntryRequestDTO;
import com.househost.finance.dto.CashierEntryResponseDTO;
import com.househost.finance.service.CashierEntryService;
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
@RequestMapping("/cashier-entries")
public class CashierEntryController {

    private final CashierEntryService cashierEntryService;

    public CashierEntryController(CashierEntryService cashierEntryService) {
        this.cashierEntryService = cashierEntryService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody CashierEntryRequestDTO request) {
        CashierEntryResponseDTO data = cashierEntryService.create(request);
        return new ResponseDTO("success", "Entrada cadastrada com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<CashierEntryResponseDTO> data = cashierEntryService.findAll();
        return new ResponseDTO("success", "Entradas encontradas com sucesso", data);
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseDTO findByCashierId(@PathVariable Long cashierId) {
        List<CashierEntryResponseDTO> data = cashierEntryService.findByCashierId(cashierId);
        return new ResponseDTO("success", "Entradas do caixa encontradas com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        CashierEntryResponseDTO data = cashierEntryService.findById(id);
        return new ResponseDTO("success", "Entrada encontrada com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CashierEntryRequestDTO request) {
        CashierEntryResponseDTO data = cashierEntryService.update(id, request);
        return new ResponseDTO("success", "Entrada atualizada com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        cashierEntryService.delete(id);
        return new ResponseDTO("success", "Entrada removida com sucesso", null);
    }
}
