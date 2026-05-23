package com.househost.guest.controller;

import com.househost.guest.dto.GuestRegisterRequestDTO;
import com.househost.guest.dto.GuestRegisterResponseDTO;
import com.househost.guest.dto.GuestRequestDTO;
import com.househost.guest.dto.GuestResponseDTO;
import com.househost.guest.service.GuestService;
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
@RequestMapping("/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody GuestRequestDTO request) {
        GuestResponseDTO data = guestService.create(request);
        return new ResponseDTO("success", "Hospede cadastrado com sucesso", data);
    }

    @PostMapping("/register")
    public ResponseDTO guestRegister(@RequestBody GuestRegisterRequestDTO request) {
        GuestRegisterResponseDTO data = guestService.guestRegister(request);
        return new ResponseDTO("success", "Hospede registrado com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<GuestResponseDTO> data = guestService.findAll();
        return new ResponseDTO("success", "Hospedes encontrados com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        GuestResponseDTO data = guestService.findById(id);
        return new ResponseDTO("success", "Hospede encontrado com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody GuestRequestDTO request) {
        GuestResponseDTO data = guestService.update(id, request);
        return new ResponseDTO("success", "Hospede atualizado com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        guestService.delete(id);
        return new ResponseDTO("success", "Hospede removido com sucesso", null);
    }
}
