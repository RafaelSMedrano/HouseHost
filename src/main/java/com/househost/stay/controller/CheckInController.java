package com.househost.stay.controller;

import com.househost.shared.dto.ResponseDTO;
import com.househost.stay.dto.CheckInRequestDTO;
import com.househost.stay.service.CheckInService;
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

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody CheckInRequestDTO request) {
        return checkInService.create(request);
    }

    @GetMapping
    public ResponseDTO findAll() {
        return checkInService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return checkInService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CheckInRequestDTO request) {
        return checkInService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        return checkInService.delete(id);
    }
}
