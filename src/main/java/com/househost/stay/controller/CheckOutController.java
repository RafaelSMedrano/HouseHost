package com.househost.stay.controller;

import com.househost.shared.dto.ResponseDTO;
import com.househost.stay.dto.CheckOutRequestDTO;
import com.househost.stay.service.CheckOutService;
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
        return checkOutService.create(request);
    }

    @GetMapping
    public ResponseDTO findAll() {
        return checkOutService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return checkOutService.findById(id);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody CheckOutRequestDTO request) {
        return checkOutService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        return checkOutService.delete(id);
    }
}
