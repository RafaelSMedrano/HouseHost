package com.househost.metrics.application.service;

import com.househost.booking.booking.application.service.BookingService;
import com.househost.booking.checking.application.service.CheckInService;
import com.househost.booking.checkout.application.service.CheckOutService;
import com.househost.finance.cashier.application.port.in.CashierEntryUseCase;
import com.househost.finance.cashier.application.port.in.CashierExpenseUseCase;
import com.househost.guest.application.service.GuestService;
import com.househost.room.application.service.RoomService;
import org.springframework.stereotype.Service;

@Service
class MetricsDataService {
    private static final Long MAIN_CASHIER_ID = 1L;
    private final BookingService bookingService;
    private final CashierEntryUseCase cashierEntryUseCase;
    private final CashierExpenseUseCase cashierExpenseUseCase;
    private final GuestService guestService;
    private final RoomService roomService;
    private final CheckInService checkInService;
    private final CheckOutService checkOutService;

    MetricsDataService(BookingService bookingService, CashierEntryUseCase cashierEntryUseCase,
                       CashierExpenseUseCase cashierExpenseUseCase, GuestService guestService,
                       RoomService roomService, CheckInService checkInService, CheckOutService checkOutService) {
        this.bookingService = bookingService;
        this.cashierEntryUseCase = cashierEntryUseCase;
        this.cashierExpenseUseCase = cashierExpenseUseCase;
        this.guestService = guestService;
        this.roomService = roomService;
        this.checkInService = checkInService;
        this.checkOutService = checkOutService;
    }

    MetricsDataSnapshot load() {
        return new MetricsDataSnapshot(
                bookingService.findAllBookings(),
                cashierEntryUseCase.findByCashierId(MAIN_CASHIER_ID),
                cashierExpenseUseCase.findByCashierId(MAIN_CASHIER_ID),
                guestService.findAllGuests(),
                roomService.findAllRooms(),
                checkInService.findAllCheckIns(),
                checkOutService.findAllCheckOuts()
        );
    }
}
