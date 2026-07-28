package com.househost.metrics.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.finance.cashier.application.dto.CashierEntryResponseDTO;
import com.househost.finance.cashier.application.dto.CashierExpenseResponseDTO;
import com.househost.guest.domain.model.Guest;
import com.househost.room.domain.model.Room;
import java.util.List;

record MetricsDataSnapshot(
        List<Booking> bookings,
        List<CashierEntryResponseDTO> cashierEntries,
        List<CashierExpenseResponseDTO> cashierExpenses,
        List<Guest> guests,
        List<Room> rooms,
        List<CheckIn> checkIns,
        List<CheckOut> checkOuts
) {
}
