package com.househost.room.controller;

import com.househost.room.dto.RoomRequestDTO;
import com.househost.room.dto.RoomResponseDTO;
import com.househost.room.service.RoomService;
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
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody RoomRequestDTO request) {
        RoomResponseDTO data = roomService.create(request);
        return new ResponseDTO("success", "Quarto cadastrado com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<RoomResponseDTO> data = roomService.findAll();
        return new ResponseDTO("success", "Quartos encontrados com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        RoomResponseDTO data = roomService.findById(id);
        return new ResponseDTO("success", "Quarto encontrado com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody RoomRequestDTO request) {
        RoomResponseDTO data = roomService.update(id, request);
        return new ResponseDTO("success", "Quarto atualizado com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        roomService.delete(id);
        return new ResponseDTO("success", "Quarto removido com sucesso", null);
    }
}
