package com.househost.guest.adapter.in.rest;

import com.househost.guest.application.dto.GuestContactResponseDTO;
import com.househost.guest.application.dto.GuestLookupResponseDTO;
import com.househost.guest.application.dto.GuestRegisterRequestDTO;
import com.househost.guest.application.dto.GuestRegisterResponseDTO;
import com.househost.guest.application.port.in.GuestUseCase;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/guests")
public class GuestController {

    private final GuestUseCase guestUseCase;

    public GuestController(GuestUseCase guestUseCase) {
        this.guestUseCase = guestUseCase;
    }

    @PostMapping("/register")
    public ResponseDTO guestRegister(@RequestBody GuestRegisterRequestDTO request) {
        GuestRegisterResponseDTO data = guestUseCase.guestRegister(request);
        return new ResponseDTO("success", "Hospede registrado com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll(@RequestParam(defaultValue = "true") boolean masked) {
        List<GuestRegisterResponseDTO> data = guestUseCase.findAll(masked);
        return new ResponseDTO("success", "Hospedes encontrados com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean masked
    ) {
        GuestRegisterResponseDTO data = guestUseCase.findById(id, masked);
        return new ResponseDTO("success", "Hospede encontrado com sucesso", data);
    }

    @GetMapping("/search/name")
    public ResponseDTO findGuestsByName(@RequestParam String value) {
        List<GuestLookupResponseDTO> data = guestUseCase.findGuestsByName(value);
        return new ResponseDTO("success", "Hospedes encontrados com sucesso", data);
    }

    @GetMapping("/search/document")
    public ResponseDTO findGuestsByDocumentNumber(@RequestParam String value) {
        List<GuestLookupResponseDTO> data = guestUseCase.findGuestsByDocumentNumber(value);
        return new ResponseDTO("success", "Hospedes encontrados com sucesso", data);
    }

    @GetMapping("/search/email")
    public ResponseDTO findGuestsByEmail(@RequestParam String value) {
        return new ResponseDTO("success", "Hospedes encontrados com sucesso", guestUseCase.findGuestsByEmail(value));
    }

    @GetMapping("/search/phone")
    public ResponseDTO findGuestsByPhone(@RequestParam String value) {
        return new ResponseDTO("success", "Hospedes encontrados com sucesso", guestUseCase.findGuestsByPhone(value));
    }

    @GetMapping("/search/city")
    public ResponseDTO findGuestsByCity(@RequestParam String value) {
        return new ResponseDTO("success", "Hospedes encontrados com sucesso", guestUseCase.findGuestsByCity(value));
    }

    @GetMapping("/{id}/contact")
    public ResponseDTO revealContact(@PathVariable Long id) {
        GuestContactResponseDTO data = guestUseCase.revealContact(id);
        return new ResponseDTO("success", "Contato revelado com sucesso", data);
    }

    @GetMapping("/{id}/edit")
    public ResponseDTO findByIdForEdit(@PathVariable Long id) {
        GuestRegisterResponseDTO data = guestUseCase.findByIdForEdit(id);
        return new ResponseDTO("success", "Dados do hospede encontrados para edicao", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody GuestRegisterRequestDTO request) {
        GuestRegisterResponseDTO data = guestUseCase.update(id, request);
        return new ResponseDTO("success", "Hospede atualizado com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        GuestRegisterResponseDTO data = guestUseCase.delete(id);
        return new ResponseDTO("success", "Hospede removido com sucesso", data);
    }
}
