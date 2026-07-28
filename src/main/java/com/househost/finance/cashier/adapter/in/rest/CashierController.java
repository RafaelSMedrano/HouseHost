package com.househost.finance.cashier.adapter.in.rest;

import com.househost.finance.cashier.application.dto.CashierRequestDTO;
import com.househost.finance.cashier.application.dto.CashierResponseDTO;
import com.househost.finance.cashier.application.dto.CashierUpdateRequestDTO;
import com.househost.finance.cashier.application.port.in.CashierUseCase;
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
@RequestMapping("/cashiers")
public class CashierController {

    private final CashierUseCase cashierService;

    public CashierController(CashierUseCase cashierService) {
        this.cashierService = cashierService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody CashierRequestDTO request) {
        CashierResponseDTO data = cashierService.create(request);
        return new ResponseDTO("success", "Caixa cadastrado com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<CashierResponseDTO> data = cashierService.findAll();
        return new ResponseDTO("success", "Caixas encontrados com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        CashierResponseDTO data = cashierService.findById(id);
        return new ResponseDTO("success", "Caixa encontrado com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CashierUpdateRequestDTO request) {
        CashierResponseDTO data = cashierService.update(id, request);
        return new ResponseDTO("success", "Caixa atualizado com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        cashierService.delete(id);
        return new ResponseDTO("success", "Caixa removido com sucesso", null);
    }
}
