package com.househost.booking.checkout.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.checkout.application.port.out.CheckOutAuditPort;
import com.househost.booking.checkout.application.port.out.CheckOutPersistencePort;
import com.househost.booking.checkout.domain.model.CheckOut;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckOutBookingQueryTest {

    @Test
    void findsCompletedCheckoutContextByBookingWithoutMutation() {
        CheckOutPersistencePort checkOutPersistencePort = mock(CheckOutPersistencePort.class);
        CheckOut checkOut = mock(CheckOut.class);
        CheckOutService checkOutService = new CheckOutService(
                checkOutPersistencePort,
                mock(BookingService.class),
                mock(CheckOutParticipantNotifier.class),
                mock(CheckOutAuditPort.class),
                mock(CheckOutValidationService.class)
        );
        when(checkOutPersistencePort.findByBookingId(42L)).thenReturn(Optional.of(checkOut));

        assertEquals(checkOut, checkOutService.findCheckOutByBookingId(42L));
        assertNull(checkOutService.findCheckOutByBookingId(null));
        verify(checkOutPersistencePort).findByBookingId(42L);
    }
}
