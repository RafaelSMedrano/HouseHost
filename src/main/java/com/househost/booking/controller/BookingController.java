package com.househost.booking.controller;

import com.househost.booking.dto.BookingFormCreateRequestDTO;
import com.househost.booking.dto.BookingRequestDTO;
import com.househost.booking.dto.BookingResponseDTO;
import com.househost.booking.service.BookingService;
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
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody BookingRequestDTO request) {
        BookingResponseDTO data = bookingService.create(request);
        return new ResponseDTO("success", "Reserva cadastrada com sucesso", data);
    }

    @PostMapping("/form")
    public ResponseDTO createFromForm(@RequestBody BookingFormCreateRequestDTO request) {
        BookingResponseDTO data = bookingService.createFromForm(request);
        return new ResponseDTO("success", "Reserva cadastrada com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll() {
        List<BookingResponseDTO> data = bookingService.findAll();
        return new ResponseDTO("success", "Reservas encontradas com sucesso", data);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        BookingResponseDTO data = bookingService.findById(id);
        return new ResponseDTO("success", "Reserva encontrada com sucesso", data);
    }

    @GetMapping("/guest/{guestId}")
    public ResponseDTO findByGuestId(@PathVariable Long guestId) {
        List<BookingResponseDTO> data = bookingService.findByGuestId(guestId);
        return new ResponseDTO("success", "Reservas do hospede encontradas com sucesso", data);
    }

    @GetMapping("/room/{roomId}")
    public ResponseDTO findByRoomId(@PathVariable Long roomId) {
        List<BookingResponseDTO> data = bookingService.findByRoomId(roomId);
        return new ResponseDTO("success", "Reservas do quarto encontradas com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody BookingRequestDTO request) {
        BookingResponseDTO data = bookingService.update(id, request);
        return new ResponseDTO("success", "Reserva atualizada com sucesso", data);
    }

    @DeleteMapping("/{id}")
    public ResponseDTO delete(@PathVariable Long id) {
        bookingService.delete(id);
        return new ResponseDTO("success", "Reserva removida com sucesso", null);
    }
}
