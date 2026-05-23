package com.househost.stay.controller;

import com.househost.shared.dto.ResponseDTO;
import com.househost.stay.dto.StayRequestDTO;
import com.househost.stay.dto.StayResponseDTO;
import com.househost.stay.service.StayService;
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
@RequestMapping("/stays")
public class StayController {

    private final StayService stayService;

    public StayController(StayService stayService) {
        this.stayService = stayService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody StayRequestDTO request) {
        StayResponseDTO data = stayService.create(request);
        return new ResponseDTO("success", "Estadia cadastrada com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<StayResponseDTO> data = stayService.findAll();
        return new ResponseDTO("success", "Estadias encontradas com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        StayResponseDTO data = stayService.findById(id);
        return new ResponseDTO("success", "Estadia encontrada com sucesso", data);
    }

    @GetMapping("/guest/{guestId}")
    public ResponseDTO findByGuestId(@PathVariable Long guestId) {
        List<StayResponseDTO> data = stayService.findByGuestId(guestId);
        return new ResponseDTO("success", "Estadias do hospede encontradas com sucesso", data);
    }

    @GetMapping("/room/{roomId}")
    public ResponseDTO findByRoomId(@PathVariable Long roomId) {
        List<StayResponseDTO> data = stayService.findByRoomId(roomId);
        return new ResponseDTO("success", "Estadias do quarto encontradas com sucesso", data);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseDTO findByBookingId(@PathVariable Long bookingId) {
        List<StayResponseDTO> data = stayService.findByBookingId(bookingId);
        return new ResponseDTO("success", "Estadias da reserva encontradas com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody StayRequestDTO request) {
        StayResponseDTO data = stayService.update(id, request);
        return new ResponseDTO("success", "Estadia atualizada com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        stayService.delete(id);
        return new ResponseDTO("success", "Estadia removida com sucesso", null);
    }
}
