package com.househost.metrics.service;

import com.househost.booking.model.Booking;
import com.househost.booking.model.BookingStatus;
import com.househost.booking.repository.BookingRepository;
import com.househost.finance.model.FinancialTransactionStatus;
import com.househost.finance.model.CashierEntry;
import com.househost.finance.model.CashierExpense;
import com.househost.finance.repository.CashierEntryRepository;
import com.househost.finance.repository.CashierExpenseRepository;
import com.househost.guest.model.Guest;
import com.househost.guest.model.GuestType;
import com.househost.guest.repository.GuestRepository;
import com.househost.metrics.dto.MetricsSummaryDTO;
import com.househost.room.model.Room;
import com.househost.room.model.RoomStatus;
import com.househost.room.repository.RoomRepository;
import com.househost.stay.model.CheckIn;
import com.househost.stay.model.CheckInStatus;
import com.househost.stay.model.CheckOut;
import com.househost.stay.model.CheckOutStatus;
import com.househost.stay.model.Stay;
import com.househost.stay.model.StayStatus;
import com.househost.stay.repository.CheckInRepository;
import com.househost.stay.repository.CheckOutRepository;
import com.househost.stay.repository.StayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private static final Long MAIN_CASHIER_ID = 1L;

    private final BookingRepository bookingRepository;
    private final CashierEntryRepository cashierEntryRepository;
    private final CashierExpenseRepository cashierExpenseRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final StayRepository stayRepository;
    private final CheckInRepository checkInRepository;
    private final CheckOutRepository checkOutRepository;

    public MetricsService(BookingRepository bookingRepository, CashierEntryRepository cashierEntryRepository, CashierExpenseRepository cashierExpenseRepository, GuestRepository guestRepository, RoomRepository roomRepository, StayRepository stayRepository, CheckInRepository checkInRepository, CheckOutRepository checkOutRepository) {
        this.bookingRepository = bookingRepository;
        this.cashierEntryRepository = cashierEntryRepository;
        this.cashierExpenseRepository = cashierExpenseRepository;
        this.guestRepository = guestRepository;
        this.roomRepository = roomRepository;
        this.stayRepository = stayRepository;
        this.checkInRepository = checkInRepository;
        this.checkOutRepository = checkOutRepository;
    }

    @Transactional(readOnly = true)
    public MetricsSummaryDTO summary() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        LocalDate weekEnd = weekStart.plusDays(6);
        YearMonth currentMonth = YearMonth.from(today);
        List<Booking> bookings = bookingRepository.findAll();
        List<CashierEntry> cashierEntries = cashierEntryRepository.findByCashierId(MAIN_CASHIER_ID);
        List<CashierExpense> cashierExpenses = cashierExpenseRepository.findByCashierId(MAIN_CASHIER_ID);
        List<Guest> guests = guestRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        List<Stay> stays = stayRepository.findAll();
        List<CheckIn> checkIns = checkInRepository.findAll();
        List<CheckOut> checkOuts = checkOutRepository.findAll();

        long pendingBookings = countBookingsByStatus(bookings, BookingStatus.PENDING);
        long confirmedBookings = countBookingsByStatus(bookings, BookingStatus.CONFIRMED);
        long gotCheckinBookings = countBookingsByStatus(bookings, BookingStatus.GOT_CHECKIN);
        long cancelledBookings = countBookingsByStatus(bookings, BookingStatus.CANCELLED);
        long guestsInStay = guests.stream()
                .filter(guest -> hasActiveStay(guest.getId(), stays))
                .count();
        long guestsWithBooking = guests.stream()
                .filter(guest -> !hasActiveStay(guest.getId(), stays))
                .filter(guest -> hasBlockingBooking(guest.getId(), bookings))
                .count();
        Set<Long> activeStayRoomIds = stays.stream()
                .filter(stay -> stay.getStatus() == StayStatus.ACTIVE)
                .filter(stay -> !stay.getCheckInDate().isAfter(today))
                .filter(stay -> stay.getExpectedCheckOutDate().isAfter(today))
                .map(stay -> stay.getRoom().getId())
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
                .filter(checkIn -> checkIn.getActualCheckInAt() != null)
                .filter(checkIn -> checkIn.getActualCheckInAt().toLocalDate().equals(today))
                .count();
        long checkInsThisWeek = checkIns.stream()
                .filter(checkIn -> checkIn.getActualCheckInAt() != null)
                .map(checkIn -> checkIn.getActualCheckInAt().toLocalDate())
                .filter(date -> !date.isBefore(weekStart) && !date.isAfter(weekEnd))
                .count();
        long checkInsThisMonth = checkIns.stream()
                .filter(checkIn -> checkIn.getActualCheckInAt() != null)
                .filter(checkIn -> YearMonth.from(checkIn.getActualCheckInAt()).equals(currentMonth))
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
        long staysLeavingToday = stays.stream()
                .filter(stay -> stay.getStatus() == StayStatus.ACTIVE)
                .filter(stay -> stay.getExpectedCheckOutDate().equals(today))
                .count();
        long staysLeavingThisWeek = stays.stream()
                .filter(stay -> stay.getStatus() == StayStatus.ACTIVE)
                .filter(stay -> isWithinPeriod(stay.getExpectedCheckOutDate(), weekStart, weekEnd))
                .count();
        long staysLeavingThisMonth = stays.stream()
                .filter(stay -> stay.getStatus() == StayStatus.ACTIVE)
                .filter(stay -> YearMonth.from(stay.getExpectedCheckOutDate()).equals(currentMonth))
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
                .filter(entry -> YearMonth.from(entry.getEntryDate()).equals(currentMonth))
                .filter(this::isDashboardEntry)
                .map(CashierEntry::getAmount)
                .reduce(BigDecimal.ZERO, this::sum);
        BigDecimal monthlyCashierExpenses = cashierExpenses.stream()
                .filter(expense -> YearMonth.from(expense.getExpenseDate()).equals(currentMonth))
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
                guests.stream().filter(Guest::isTravelsWithPets).count(),
                guests.stream().map(Guest::getTotalSpent).reduce(BigDecimal.ZERO, this::sum),
                totalRooms,
                availableRooms,
                occupiedRooms,
                0,
                maintenanceRooms,
                inactiveRooms,
                blockedRooms,
                stays.stream().filter(stay -> stay.getStatus() == StayStatus.ACTIVE).count(),
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
        return booking.getStatus() == BookingStatus.PENDING
                || booking.getStatus() == BookingStatus.CONFIRMED
                || booking.getStatus() == BookingStatus.GOT_CHECKIN;
    }

    private boolean hasActiveStay(Long guestId, List<Stay> stays) {
        return stays.stream()
                .anyMatch(stay -> stay.getGuest() != null
                        && stay.getGuest().getId().equals(guestId)
                        && stay.getStatus() == StayStatus.ACTIVE);
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

    private boolean isDashboardEntry(CashierEntry entry) {
        return isDashboardStatus(entry.getStatus());
    }

    private boolean isDashboardExpense(CashierExpense expense) {
        return isDashboardStatus(expense.getStatus());
    }

    private boolean isDashboardStatus(FinancialTransactionStatus status) {
        return status == FinancialTransactionStatus.SETTLED
                || status == FinancialTransactionStatus.PAID
                || status == FinancialTransactionStatus.WAITING;
    }

    private BigDecimal sum(BigDecimal first, BigDecimal second) {
        return first.add(second == null ? BigDecimal.ZERO : second);
    }
}
