package com.househost.booking.controller;

import com.househost.booking.dto.BookingFormCreateRequestDTO;
import com.househost.booking.dto.BookingRequestDTO;
import com.househost.booking.service.BookingService;
import com.househost.shared.dto.ResponseDTO;
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
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody BookingRequestDTO request) {
        return bookingService.create(request);
    }

    @PostMapping("/form")
    public ResponseDTO createFromForm(@RequestBody BookingFormCreateRequestDTO request) {
        return bookingService.createFromForm(request);
    }

    @GetMapping
    public ResponseDTO findAll() {
        return bookingService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @GetMapping("/guest/{guestId}")
    public ResponseDTO findByGuestId(@PathVariable Long guestId) {
        return bookingService.findByGuestId(guestId);
    }

    @GetMapping("/room/{roomId}")
    public ResponseDTO findByRoomId(@PathVariable Long roomId) {
        return bookingService.findByRoomId(roomId);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody BookingRequestDTO request) {
        return bookingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        return bookingService.delete(id);
    }
}
