package com.househost.booking.checkout.adapter.in.rest;

import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutResponseDTO;
import com.househost.booking.checkout.application.port.in.CheckOutUseCase;
import com.househost.shared.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/check-outs")
public class CheckOutController {
    private final CheckOutUseCase checkOutService;

    public CheckOutController(CheckOutUseCase checkOutService) {
        this.checkOutService = checkOutService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody CheckOutRequestDTO request) {
        CheckOutResponseDTO data = checkOutService.create(request);
        return new ResponseDTO("success", "Check-out cadastrado com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<CheckOutResponseDTO> data = checkOutService.findAll();
        return new ResponseDTO("success", "Check-outs encontrados com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        CheckOutResponseDTO data = checkOutService.findById(id);
        return new ResponseDTO("success", "Check-out encontrado com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CheckOutRequestDTO request) {
        CheckOutResponseDTO data = checkOutService.update(id, request);
        return new ResponseDTO("success", "Check-out atualizado com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        checkOutService.delete(id);
        return new ResponseDTO("success", "Check-out removido com sucesso", null);
    }
}
