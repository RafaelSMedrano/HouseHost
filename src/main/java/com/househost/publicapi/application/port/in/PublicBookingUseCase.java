package com.househost.publicapi.application.port.in;

import com.househost.audit.domain.model.AuditEventContext;
import com.househost.publicapi.application.dto.PublicAvailabilityResponseDTO;
import com.househost.publicapi.application.dto.PublicBookingRequestDTO;
import com.househost.publicapi.application.dto.PublicBookingResponseDTO;
import com.househost.publicapi.application.dto.PublicQuoteRequestDTO;
import com.househost.publicapi.application.dto.PublicQuoteResponseDTO;
import com.househost.publicapi.application.dto.PublicRoomResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface PublicBookingUseCase {

    List<PublicRoomResponseDTO> findPublicRooms();

    PublicAvailabilityResponseDTO checkAvailability(
            Long roomId,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guests
    );

    PublicQuoteResponseDTO quote(PublicQuoteRequestDTO request);

    PublicBookingResponseDTO createBooking(PublicBookingRequestDTO request);

    PublicBookingResponseDTO createBooking(PublicBookingRequestDTO request, AuditEventContext auditContext);
}
