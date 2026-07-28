package com.househost.finance.cashier.adapter.in.rest;

import com.househost.finance.cashier.application.dto.CashierExpenseResponseDTO;
import com.househost.finance.cashier.application.port.in.CashierExpenseUseCase;
import com.househost.shared.dto.ResponseDTO;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cashier-expenses")
public class CashierExpenseController {

    private final CashierExpenseUseCase cashierExpenseService;

    public CashierExpenseController(CashierExpenseUseCase cashierExpenseService) {
        this.cashierExpenseService = cashierExpenseService;
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

}
