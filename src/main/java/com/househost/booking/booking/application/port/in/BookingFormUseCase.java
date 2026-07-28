package com.househost.booking.booking.application.port.in;

import com.househost.booking.booking.application.dto.BookingFormCreateRequestDTO;
import com.househost.booking.booking.application.dto.BookingResponseDTO;

public interface BookingFormUseCase {

    BookingResponseDTO create(BookingFormCreateRequestDTO request);
}
