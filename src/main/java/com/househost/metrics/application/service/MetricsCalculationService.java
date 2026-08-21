package com.househost.metrics.application.service;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.booking.booking.domain.model.BookingStatus;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.booking.checking.domain.model.CheckInStatus;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.booking.checkout.domain.model.CheckOutStatus;
import com.househost.finance.cashier.application.dto.CashierEntryResponseDTO;
import com.househost.finance.cashier.application.dto.CashierExpenseResponseDTO;
import com.househost.finance.financialtransaction.domain.model.FinancialTransactionStatus;
import com.househost.guest.domain.model.Guest;
import com.househost.guest.domain.model.GuestType;
import com.househost.metrics.application.dto.MetricsSummaryDTO;
import com.househost.room.domain.model.Room;
import com.househost.room.domain.model.RoomStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
class MetricsCalculationService {

    MetricsSummaryDTO calculate(MetricsDataSnapshot data) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        LocalDate weekEnd = weekStart.plusDays(6);
        YearMonth currentMonth = YearMonth.from(today);
        List<Booking> bookings = data.bookings();
        List<CashierEntryResponseDTO> cashierEntries = data.cashierEntries();
        List<CashierExpenseResponseDTO> cashierExpenses = data.cashierExpenses();
        List<Guest> guests = data.guests();
        List<Room> rooms = data.rooms();
        List<CheckIn> checkIns = data.checkIns();
        List<CheckOut> checkOuts = data.checkOuts();

        long pendingBookings = countBookingsByStatus(bookings, BookingStatus.UNCONFIRMED);
        long confirmedBookings = countBookingsByStatus(bookings, BookingStatus.CONFIRMED);
        long gotCheckinBookings = countBookingsByStatus(bookings, BookingStatus.IN_STAY);
        long cancelledBookings = countBookingsByStatus(bookings, BookingStatus.CANCELED);
        long guestsInStay = guests.stream()
                .filter(guest -> hasBookingWithStatus(guest.getId(), bookings, BookingStatus.IN_STAY))
                .count();
        long guestsWithBooking = guests.stream()
                .filter(guest -> !hasBookingWithStatus(guest.getId(), bookings, BookingStatus.IN_STAY))
                .filter(guest -> hasBlockingBooking(guest.getId(), bookings))
                .count();
        Set<Long> activeStayRoomIds = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.IN_STAY)
                .filter(booking -> !booking.getCheckInDate().isAfter(today))
                .filter(booking -> booking.getCheckOutDate().isAfter(today))
                .map(booking -> booking.getRoom().getId())
                .collect(Collectors.toSet());
        long occupiedRooms = rooms.stream()
                .filter(room -> room.getStatus() == RoomStatus.OCCUPIED
                        || activeStayRoomIds.contains(room.getId()))
                .count();
        long totalRooms = rooms.size();
        long maintenanceRooms = rooms.stream().filter(room -> room.getStatus() == RoomStatus.MAINTENANCE).count();
        long inactiveRooms = rooms.stream().filter(room -> room.getStatus() == RoomStatus.INACTIVE).count();
        long blockedRooms = maintenanceRooms + inactiveRooms;
        long availableRooms = Math.max(0, totalRooms - occupiedRooms - blockedRooms);
        int occupancy = totalRooms == 0 ? 0 : Math.round((occupiedRooms * 100f) / totalRooms);
        long checkInsToday = checkIns.stream()
                .filter(checkIn -> checkIn.getCreatedAt() != null)
                .filter(checkIn -> checkIn.getCreatedAt().toLocalDate().equals(today))
                .count();
        long checkInsThisWeek = checkIns.stream()
                .filter(checkIn -> checkIn.getCreatedAt() != null)
                .map(checkIn -> checkIn.getCreatedAt().toLocalDate())
                .filter(date -> !date.isBefore(weekStart) && !date.isAfter(weekEnd))
                .count();
        long checkInsThisMonth = checkIns.stream()
                .filter(checkIn -> checkIn.getCreatedAt() != null)
                .filter(checkIn -> YearMonth.from(checkIn.getCreatedAt()).equals(currentMonth))
                .count();
        long checkOutsToday = checkOuts.stream()
                .filter(checkOut -> checkOut.getActualCheckOutAt() != null)
                .filter(checkOut -> checkOut.getActualCheckOutAt().toLocalDate().equals(today))
                .count();
        long checkOutsThisWeek = checkOuts.stream()
                .filter(checkOut -> checkOut.getActualCheckOutAt() != null)
                .map(checkOut -> checkOut.getActualCheckOutAt().toLocalDate())
                .filter(date -> !date.isBefore(weekStart) && !date.isAfter(weekEnd))
                .count();
        long checkOutsThisMonth = checkOuts.stream()
                .filter(checkOut -> checkOut.getActualCheckOutAt() != null)
                .filter(checkOut -> YearMonth.from(checkOut.getActualCheckOutAt()).equals(currentMonth))
                .count();
        long bookingsToday = bookings.stream()
                .filter(this::isBlockingBooking)
                .filter(booking -> booking.getCheckInDate().equals(today))
                .count();
        long bookingsThisWeek = bookings.stream()
                .filter(this::isBlockingBooking)
                .filter(booking -> isWithinPeriod(booking.getCheckInDate(), weekStart, weekEnd))
                .count();
        long bookingsThisMonth = bookings.stream()
                .filter(this::isBlockingBooking)
                .filter(booking -> YearMonth.from(booking.getCheckInDate()).equals(currentMonth))
                .count();
        long staysLeavingToday = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.IN_STAY)
                .filter(booking -> booking.getCheckOutDate().equals(today))
                .count();
        long staysLeavingThisWeek = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.IN_STAY)
                .filter(booking -> isWithinPeriod(booking.getCheckOutDate(), weekStart, weekEnd))
                .count();
        long staysLeavingThisMonth = bookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.IN_STAY)
                .filter(booking -> YearMonth.from(booking.getCheckOutDate()).equals(currentMonth))
                .count();
        BigDecimal bookingsTotalRevenue = bookings.stream()
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, this::sum);
        BigDecimal checkoutExtrasThisMonth = checkOuts.stream()
                .filter(checkOut -> checkOut.getActualCheckOutAt() != null)
                .filter(checkOut -> YearMonth.from(checkOut.getActualCheckOutAt()).equals(currentMonth))
                .map(CheckOut::getExtraCharges)
                .reduce(BigDecimal.ZERO, this::sum);
        BigDecimal monthlyCashierEntries = cashierEntries.stream()
                .filter(entry -> YearMonth.from(entry.getDueDate()).equals(currentMonth))
                .filter(this::isDashboardEntry)
                .map(CashierEntryResponseDTO::getAmount)
                .reduce(BigDecimal.ZERO, this::sum);
        BigDecimal monthlyCashierExpenses = cashierExpenses.stream()
                .filter(expense -> YearMonth.from(expense.getDueDate()).equals(currentMonth))
                .filter(this::isDashboardExpense)
                .map(expense -> expense.getAmount().abs())
                .reduce(BigDecimal.ZERO, this::sum);
        BigDecimal monthlyCashierBalance = monthlyCashierEntries.subtract(monthlyCashierExpenses);

        return new MetricsSummaryDTO(
                bookings.size(),
                pendingBookings,
                confirmedBookings,
                cancelledBookings,
                gotCheckinBookings,
                guests.size(),
                guestsInStay,
                guestsWithBooking,
                guests.stream().filter(guest -> guest.getGuestTypeEnum() == GuestType.VIP).count(),
                guests.stream().map(Guest::getTotalSpent).reduce(BigDecimal.ZERO, this::sum),
                totalRooms,
                availableRooms,
                occupiedRooms,
                0,
                maintenanceRooms,
                inactiveRooms,
                blockedRooms,
                bookings.stream().filter(booking -> booking.getStatus() == BookingStatus.IN_STAY).count(),
                bookings.stream().filter(booking -> booking.getCheckOutDate().equals(today)).count(),
                bookingsTotalRevenue,
                occupancy,
                checkInsToday,
                Math.max(0, bookingsToday - checkInsToday),
                checkOutsToday,
                Math.max(0, staysLeavingToday - checkOutsToday),
                monthlyCashierBalance.add(checkoutExtrasThisMonth),
                bookings.stream().filter(booking -> YearMonth.from(booking.getCheckInDate()).equals(currentMonth)).count(),
                checkIns.size(),
                checkInsToday,
                checkInsThisWeek,
                checkInsThisMonth,
                bookingsToday,
                bookingsThisWeek,
                bookingsThisMonth,
                checkIns.stream().filter(checkIn -> checkIn.getStatus() == CheckInStatus.COMPLETED).count(),
                checkOuts.size(),
                checkOutsToday,
                checkOutsThisWeek,
                checkOutsThisMonth,
                staysLeavingToday,
                staysLeavingThisWeek,
                staysLeavingThisMonth,
                checkOuts.stream().filter(checkOut -> checkOut.getStatus() == CheckOutStatus.COMPLETED).count()
        );
    }

    private long countBookingsByStatus(List<Booking> bookings, BookingStatus status) {
        return bookings.stream().filter(booking -> booking.getStatus() == status).count();
    }

    private boolean isBlockingBooking(Booking booking) {
        return booking.getStatus() == BookingStatus.UNCONFIRMED
                || booking.getStatus() == BookingStatus.CONFIRMED
                || booking.getStatus() == BookingStatus.IN_STAY;
    }

    private boolean hasBookingWithStatus(Long guestId, List<Booking> bookings, BookingStatus status) {
        return bookings.stream()
                .anyMatch(booking -> booking.getGuest() != null
                        && booking.getGuest().getId().equals(guestId)
                        && booking.getStatus() == status);
    }

    private boolean hasBlockingBooking(Long guestId, List<Booking> bookings) {
        return bookings.stream()
                .anyMatch(booking -> booking.getGuest() != null
                        && booking.getGuest().getId().equals(guestId)
                        && isBlockingBooking(booking));
    }

    private boolean isWithinPeriod(LocalDate date, LocalDate start, LocalDate end) {
        return date != null && !date.isBefore(start) && !date.isAfter(end);
    }

    private boolean isDashboardEntry(CashierEntryResponseDTO entry) {
        return isDashboardStatus(entry.getStatus());
    }

    private boolean isDashboardExpense(CashierExpenseResponseDTO expense) {
        return isDashboardStatus(expense.getStatus());
    }

    private boolean isDashboardStatus(String status) {
        return FinancialTransactionStatus.SETTLED.name().equals(status)
                || FinancialTransactionStatus.WAITING.name().equals(status);
    }

    private BigDecimal sum(BigDecimal first, BigDecimal second) {
        return first.add(second == null ? BigDecimal.ZERO : second);
    }
}
