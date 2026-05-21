package com.househost.stay.controller;

import com.househost.shared.dto.ResponseDTO;
import com.househost.stay.dto.StayRequestDTO;
import com.househost.stay.service.StayService;
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
        return stayService.create(request);
    }

    @GetMapping
    public ResponseDTO findAll() {
        return stayService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return stayService.findById(id);
    }

    @GetMapping("/guest/{guestId}")
    public ResponseDTO findByGuestId(@PathVariable Long guestId) {
        return stayService.findByGuestId(guestId);
    }

    @GetMapping("/room/{roomId}")
    public ResponseDTO findByRoomId(@PathVariable Long roomId) {
        return stayService.findByRoomId(roomId);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseDTO findByBookingId(@PathVariable Long bookingId) {
        return stayService.findByBookingId(bookingId);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody StayRequestDTO request) {
        return stayService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        return stayService.delete(id);
    }
}
