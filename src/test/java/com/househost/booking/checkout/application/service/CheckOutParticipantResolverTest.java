package com.househost.booking.checkout.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.guest.application.service.GuestService;
import com.househost.guest.domain.model.Guest;
import com.househost.room.application.service.RoomService;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import com.househost.ratings.application.dto.RatingRequestDTO;
import com.househost.ratings.application.port.in.RatingUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckOutParticipantResolverTest {

    @Test
    void bookingResolverFinishesBookingThroughBookingService() {
        BookingService bookingService = mock(BookingService.class);
        CheckOutBookingResolver checkOutBookingResolver = new CheckOutBookingResolver(bookingService);
        Booking booking = mock(Booking.class);
        when(booking.getId()).thenReturn(23L);

        checkOutBookingResolver.resolveBookingStatus(booking);

        verify(bookingService).setStatus(23L, BookingStatus.FINISHED);
    }

    @Test
    void roomResolverReleasesInspectedRoomThroughRoomService() {
        RoomService roomService = mock(RoomService.class);
        CheckOutRoomResolver checkOutRoomResolver = new CheckOutRoomResolver(roomService);
        Room room = mock(Room.class);
        when(room.getId()).thenReturn(1L);
        when(room.getStatus()).thenReturn(RoomStatus.OCCUPIED);

        checkOutRoomResolver.resolveRoomStatus(room, true);

        verify(roomService).changeStatus(1L, RoomStatus.AVAILABLE);
    }

    @Test
    void guestResolverUsesBackendCalculatedBookingTotal() {
        GuestService guestService = mock(GuestService.class);
        CheckOutGuestResolver checkOutGuestResolver = new CheckOutGuestResolver(guestService);
        CheckOut checkOut = mock(CheckOut.class);
        Booking booking = mock(Booking.class);
        Guest guest = mock(Guest.class);
        LocalDateTime actualCheckOutAt = LocalDateTime.of(2026, 8, 12, 11, 30);
        when(checkOut.getBooking()).thenReturn(booking);
        when(checkOut.getGuest()).thenReturn(guest);
        when(checkOut.getActualCheckOutAt()).thenReturn(actualCheckOutAt);
        when(booking.getTotalAmount()).thenReturn(new BigDecimal("700.00"));
        when(guest.getId()).thenReturn(7L);

        checkOutGuestResolver.resolveGuestHistory(checkOut);

        verify(guestService).applyCompletedStay(
                7L,
                LocalDate.of(2026, 8, 12),
                new BigDecimal("700.00")
        );
    }

    @Test
    void ratingResolverTranslatesCheckoutRatingAndUsesBookingIdentity() {
        RatingUseCase ratingUseCase = mock(RatingUseCase.class);
        CheckOutRatingResolver checkOutRatingResolver = new CheckOutRatingResolver(ratingUseCase);
        CheckOut checkOut = mock(CheckOut.class);
        Booking booking = mock(Booking.class);
        CheckOutRatingRequestDTO checkOutRatingRequestDTO = ratingRequest();
        when(checkOut.getBooking()).thenReturn(booking);
        when(booking.getId()).thenReturn(23L);

        checkOutRatingResolver.resolveRating(checkOut, checkOutRatingRequestDTO);

        ArgumentCaptor<RatingRequestDTO> ratingRequestDTOCaptor =
                ArgumentCaptor.forClass(RatingRequestDTO.class);
        verify(ratingUseCase).createForCompletedBooking(ratingRequestDTOCaptor.capture());
        RatingRequestDTO ratingRequestDTO = ratingRequestDTOCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(23L, ratingRequestDTO.bookingId);
        org.junit.jupiter.api.Assertions.assertEquals(1, ratingRequestDTO.checkInProcedureScore);
        org.junit.jupiter.api.Assertions.assertEquals(2, ratingRequestDTO.checkOutProcedureScore);
        org.junit.jupiter.api.Assertions.assertEquals(
                "Atendimento excelente",
                ratingRequestDTO.observations
        );
    }

    private CheckOutRatingRequestDTO ratingRequest() {
        CheckOutRatingRequestDTO checkOutRatingRequestDTO = new CheckOutRatingRequestDTO();
        checkOutRatingRequestDTO.checkInProcedureScore = 1;
        checkOutRatingRequestDTO.checkOutProcedureScore = 2;
        checkOutRatingRequestDTO.accommodationCleanlinessScore = 3;
        checkOutRatingRequestDTO.teamCommunicationScore = 4;
        checkOutRatingRequestDTO.locationScore = 5;
        checkOutRatingRequestDTO.comfortScore = 4;
        checkOutRatingRequestDTO.observations = "Atendimento excelente";
        return checkOutRatingRequestDTO;
    }
}
