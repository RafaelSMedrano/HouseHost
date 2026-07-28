package com.househost.booking.booking.application.service;

import com.househost.booking.booking.application.port.out.BookingPersistencePort;
import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingPaymentStatus;
import com.househost.finance.financialtransaction.domain.model.FinancialTransaction;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionSourceType;
import com.househost.finance.financialtransaction.application.port.out.FinancialTransactionSource;
import com.househost.shared.exception.BookingException;
import org.springframework.stereotype.Service;

@Service
public class BookingPaymentService implements FinancialTransactionSource {
    private final BookingPersistencePort bookingRepository;

    public BookingPaymentService(BookingPersistencePort bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void onSettle(Long sourceId, FinancialTransaction transaction) {
        if (sourceId == null) {
            throw new BookingException("Reserva de origem da transacao financeira nao encontrada.");
        }

        Booking booking = bookingRepository.findById(sourceId)
                .orElseThrow(() -> new BookingException("Reserva de origem da transacao financeira nao encontrada."));
        booking.changePaymentStatus(BookingPaymentStatus.PAID);
        bookingRepository.save(booking);
    }

    @Override
    public FinancialTransactionSourceType getType() {
        return FinancialTransactionSourceType.BOOKING;
    }
}
