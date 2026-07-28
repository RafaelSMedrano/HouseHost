package com.househost.publicapi.adapter.in.rest;

import com.househost.audit.domain.model.AuditEventContext;
import com.househost.publicapi.application.dto.PublicAvailabilityResponseDTO;
import com.househost.publicapi.application.dto.PublicBookingRequestDTO;
import com.househost.publicapi.application.dto.PublicBookingResponseDTO;
import com.househost.publicapi.application.dto.PublicQuoteRequestDTO;
import com.househost.publicapi.application.dto.PublicQuoteResponseDTO;
import com.househost.publicapi.application.dto.PublicRoomResponseDTO;
import com.househost.publicapi.application.port.in.PublicBookingUseCase;
import com.househost.shared.dto.ResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/public")
public class PublicBookingController {

    private final PublicBookingUseCase publicBookingUseCase;

    public PublicBookingController(PublicBookingUseCase publicBookingUseCase) {
        this.publicBookingUseCase = publicBookingUseCase;
    }

    @GetMapping("/rooms")
    public ResponseDTO findRooms() {
        List<PublicRoomResponseDTO> data = publicBookingUseCase.findPublicRooms();
        return new ResponseDTO("success", "Acomodacoes publicas encontradas com sucesso", data);
    }

    @GetMapping("/availability")
    public ResponseDTO checkAvailability(
            @RequestParam(required = false) Long roomId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut,
            @RequestParam(required = false) Integer guests
    ) {
        PublicAvailabilityResponseDTO data = publicBookingUseCase.checkAvailability(roomId, checkIn, checkOut, guests);
        return new ResponseDTO("success", "Disponibilidade consultada com sucesso", data);
    }

    @PostMapping("/quote")
    public ResponseDTO quote(@RequestBody PublicQuoteRequestDTO request) {
        PublicQuoteResponseDTO data = publicBookingUseCase.quote(request);
        return new ResponseDTO("success", "Cotacao calculada com sucesso", data);
    }

    @PostMapping("/bookings")
    public ResponseDTO createBooking(@RequestBody PublicBookingRequestDTO request, HttpServletRequest httpRequest) {
        PublicBookingResponseDTO data = publicBookingUseCase.createBooking(request, auditContext(httpRequest));
        return new ResponseDTO("success", "Reserva publica criada com sucesso", data);
    }

    private AuditEventContext auditContext(HttpServletRequest request) {
        return new AuditEventContext(resolveIpAddress(request), request.getHeader("User-Agent"));
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
