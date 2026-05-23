package com.househost.stay.controller;

import com.househost.shared.dto.ResponseDTO;
import com.househost.stay.dto.CheckOutRequestDTO;
import com.househost.stay.dto.CheckOutResponseDTO;
import com.househost.stay.service.CheckOutService;
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
@RequestMapping("/check-outs")
public class CheckOutController {

    private final CheckOutService checkOutService;

    public CheckOutController(CheckOutService checkOutService) {
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
