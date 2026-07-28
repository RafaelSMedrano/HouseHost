package com.househost.booking.checking.adapter.in.rest;

import com.househost.shared.dto.ResponseDTO;
import com.househost.booking.checking.application.dto.CheckInRequestDTO;
import com.househost.booking.checking.application.dto.CheckInResponseDTO;
import com.househost.booking.checking.application.port.in.CheckInUseCase;
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
@RequestMapping("/check-ins")
public class CheckInController {

    private final CheckInUseCase checkInService;

    public CheckInController(CheckInUseCase checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody CheckInRequestDTO request) {
        CheckInResponseDTO data = checkInService.create(request);
        return new ResponseDTO("success", "Check-in cadastrado com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<CheckInResponseDTO> data = checkInService.findAll();
        return new ResponseDTO("success", "Check-ins encontrados com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        CheckInResponseDTO data = checkInService.findById(id);
        return new ResponseDTO("success", "Check-in encontrado com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CheckInRequestDTO request) {
        CheckInResponseDTO data = checkInService.update(id, request);
        return new ResponseDTO("success", "Check-in atualizado com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        checkInService.delete(id);
        return new ResponseDTO("success", "Check-in removido com sucesso", null);
    }
}
