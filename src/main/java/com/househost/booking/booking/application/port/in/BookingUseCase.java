package com.househost.booking.booking.application.port.in;

import com.househost.booking.booking.application.dto.BookingRequestDTO;
import com.househost.booking.booking.application.dto.BookingResponseDTO;

import java.util.List;

public interface BookingUseCase {

    BookingResponseDTO create(BookingRequestDTO request);

    List<BookingResponseDTO> findAll();

    BookingResponseDTO findById(Long id);

    List<BookingResponseDTO> findByGuestId(Long guestId);

    List<BookingResponseDTO> findByRoomId(Long roomId);

    BookingResponseDTO update(Long id, BookingRequestDTO request);

    void delete(Long id);
}
