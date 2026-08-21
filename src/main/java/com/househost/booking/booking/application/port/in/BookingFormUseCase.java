package com.househost.booking.booking.application.port.in;

import com.househost.booking.booking.application.dto.BookingFormCreateRequestDTO;
import com.househost.booking.booking.application.dto.BookingFormCreateResponseDTO;

public interface BookingFormUseCase {

    BookingFormCreateResponseDTO create(BookingFormCreateRequestDTO request);
}
