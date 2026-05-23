package com.househost.finance.controller;

import com.househost.finance.dto.CashierExpenseRequestDTO;
import com.househost.finance.dto.CashierExpenseResponseDTO;
import com.househost.finance.service.CashierExpenseService;
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
@RequestMapping("/cashier-expenses")
public class CashierExpenseController {

    private final CashierExpenseService cashierExpenseService;

    public CashierExpenseController(CashierExpenseService cashierExpenseService) {
        this.cashierExpenseService = cashierExpenseService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody CashierExpenseRequestDTO request) {
        CashierExpenseResponseDTO data = cashierExpenseService.create(request);
        return new ResponseDTO("success", "Saida cadastrada com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<CashierExpenseResponseDTO> data = cashierExpenseService.findAll();
        return new ResponseDTO("success", "Saidas encontradas com sucesso", data);
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseDTO findByCashierId(@PathVariable Long cashierId) {
        List<CashierExpenseResponseDTO> data = cashierExpenseService.findByCashierId(cashierId);
        return new ResponseDTO("success", "Saidas do caixa encontradas com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        CashierExpenseResponseDTO data = cashierExpenseService.findById(id);
        return new ResponseDTO("success", "Saida encontrada com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CashierExpenseRequestDTO request) {
        CashierExpenseResponseDTO data = cashierExpenseService.update(id, request);
        return new ResponseDTO("success", "Saida atualizada com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        cashierExpenseService.delete(id);
        return new ResponseDTO("success", "Saida removida com sucesso", null);
    }
}
