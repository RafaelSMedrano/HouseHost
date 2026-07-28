package com.househost.finance.cashier.adapter.in.rest;

import com.househost.finance.cashier.application.dto.CashierEntryResponseDTO;
import com.househost.finance.cashier.application.port.in.CashierEntryUseCase;
import com.househost.shared.dto.ResponseDTO;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cashier-entries")
public class CashierEntryController {

    private final CashierEntryUseCase cashierEntryService;

    public CashierEntryController(CashierEntryUseCase cashierEntryService) {
        this.cashierEntryService = cashierEntryService;
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

}
