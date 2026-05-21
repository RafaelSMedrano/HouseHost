package com.househost.metrics.dto;

import java.math.BigDecimal;

public class MetricsSummaryDTO {

    private long totalBookings;
    private long pendingBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private long gotCheckinBookings;
    private long totalGuests;
    private long guestsInStay;
    private long guestsWithBooking;
    private long vipGuests;
    private long guestsWithPets;
    private BigDecimal guestsTotalRevenue;
    private long totalRooms;
    private long availableRooms;
    private long occupiedRooms;
    private long cleaningRooms;
    private long maintenanceRooms;
    private long inactiveRooms;
    private long blockedRooms;
    private long activeStays;
    private long checkOutsTodayByReservation;
    private BigDecimal bookingsTotalRevenue;
    private int dashboardOccupancyPercent;
    private long dashboardDoneCheckInsToday;
    private long dashboardPendingCheckInsToday;
    private long dashboardDoneCheckOutsToday;
    private long dashboardPendingCheckOutsToday;
    private BigDecimal dashboardMonthlyRevenue;
    private long dashboardMonthlyBookings;
    private long totalCheckIns;
    private long checkInsToday;
    private long checkInsThisWeek;
    private long checkInsThisMonth;
    private long expectedCheckInsToday;
    private long expectedCheckInsThisWeek;
    private long expectedCheckInsThisMonth;
    private long completedCheckIns;
    private long totalCheckOuts;
    private long checkOutsToday;
    private long checkOutsThisWeek;
    private long checkOutsThisMonth;
    private long expectedCheckOutsToday;
    private long expectedCheckOutsThisWeek;
    private long expectedCheckOutsThisMonth;
    private long completedCheckOuts;

    public MetricsSummaryDTO(
            long totalBookings,
            long pendingBookings,
            long confirmedBookings,
            long cancelledBookings,
            long gotCheckinBookings,
            long totalGuests,
            long guestsInStay,
            long guestsWithBooking,
            long vipGuests,
            long guestsWithPets,
            BigDecimal guestsTotalRevenue,
            long totalRooms,
            long availableRooms,
            long occupiedRooms,
            long cleaningRooms,
            long maintenanceRooms,
            long inactiveRooms,
            long blockedRooms,
            long activeStays,
            long checkOutsTodayByReservation,
            BigDecimal bookingsTotalRevenue,
            int dashboardOccupancyPercent,
            long dashboardDoneCheckInsToday,
            long dashboardPendingCheckInsToday,
            long dashboardDoneCheckOutsToday,
            long dashboardPendingCheckOutsToday,
            BigDecimal dashboardMonthlyRevenue,
            long dashboardMonthlyBookings,
            long totalCheckIns,
            long checkInsToday,
            long checkInsThisWeek,
            long checkInsThisMonth,
            long expectedCheckInsToday,
            long expectedCheckInsThisWeek,
            long expectedCheckInsThisMonth,
            long completedCheckIns,
            long totalCheckOuts,
            long checkOutsToday,
            long checkOutsThisWeek,
            long checkOutsThisMonth,
            long expectedCheckOutsToday,
            long expectedCheckOutsThisWeek,
            long expectedCheckOutsThisMonth,
            long completedCheckOuts
    ) {
        this.totalBookings = totalBookings;
        this.pendingBookings = pendingBookings;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.gotCheckinBookings = gotCheckinBookings;
        this.totalGuests = totalGuests;
        this.guestsInStay = guestsInStay;
        this.guestsWithBooking = guestsWithBooking;
        this.vipGuests = vipGuests;
        this.guestsWithPets = guestsWithPets;
        this.guestsTotalRevenue = guestsTotalRevenue;
        this.totalRooms = totalRooms;
        this.availableRooms = availableRooms;
        this.occupiedRooms = occupiedRooms;
        this.cleaningRooms = cleaningRooms;
        this.maintenanceRooms = maintenanceRooms;
        this.inactiveRooms = inactiveRooms;
        this.blockedRooms = blockedRooms;
        this.activeStays = activeStays;
        this.checkOutsTodayByReservation = checkOutsTodayByReservation;
        this.bookingsTotalRevenue = bookingsTotalRevenue;
        this.dashboardOccupancyPercent = dashboardOccupancyPercent;
        this.dashboardDoneCheckInsToday = dashboardDoneCheckInsToday;
        this.dashboardPendingCheckInsToday = dashboardPendingCheckInsToday;
        this.dashboardDoneCheckOutsToday = dashboardDoneCheckOutsToday;
        this.dashboardPendingCheckOutsToday = dashboardPendingCheckOutsToday;
        this.dashboardMonthlyRevenue = dashboardMonthlyRevenue;
        this.dashboardMonthlyBookings = dashboardMonthlyBookings;
        this.totalCheckIns = totalCheckIns;
        this.checkInsToday = checkInsToday;
        this.checkInsThisWeek = checkInsThisWeek;
        this.checkInsThisMonth = checkInsThisMonth;
        this.expectedCheckInsToday = expectedCheckInsToday;
        this.expectedCheckInsThisWeek = expectedCheckInsThisWeek;
        this.expectedCheckInsThisMonth = expectedCheckInsThisMonth;
        this.completedCheckIns = completedCheckIns;
        this.totalCheckOuts = totalCheckOuts;
        this.checkOutsToday = checkOutsToday;
        this.checkOutsThisWeek = checkOutsThisWeek;
        this.checkOutsThisMonth = checkOutsThisMonth;
        this.expectedCheckOutsToday = expectedCheckOutsToday;
        this.expectedCheckOutsThisWeek = expectedCheckOutsThisWeek;
        this.expectedCheckOutsThisMonth = expectedCheckOutsThisMonth;
        this.completedCheckOuts = completedCheckOuts;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public long getPendingBookings() {
        return pendingBookings;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public long getGotCheckinBookings() {
        return gotCheckinBookings;
    }

    public long getTotalGuests() {
        return totalGuests;
    }

    public long getGuestsInStay() {
        return guestsInStay;
    }

    public long getGuestsWithBooking() {
        return guestsWithBooking;
    }

    public long getVipGuests() {
        return vipGuests;
    }

    public long getGuestsWithPets() {
        return guestsWithPets;
    }

    public BigDecimal getGuestsTotalRevenue() {
        return guestsTotalRevenue;
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public long getAvailableRooms() {
        return availableRooms;
    }

    public long getOccupiedRooms() {
        return occupiedRooms;
    }

    public long getCleaningRooms() {
        return cleaningRooms;
    }

    public long getMaintenanceRooms() {
        return maintenanceRooms;
    }

    public long getInactiveRooms() {
        return inactiveRooms;
    }

    public long getBlockedRooms() {
        return blockedRooms;
    }

    public long getActiveStays() {
        return activeStays;
    }

    public long getCheckOutsTodayByReservation() {
        return checkOutsTodayByReservation;
    }

    public BigDecimal getBookingsTotalRevenue() {
        return bookingsTotalRevenue;
    }

    public int getDashboardOccupancyPercent() {
        return dashboardOccupancyPercent;
    }

    public long getDashboardDoneCheckInsToday() {
        return dashboardDoneCheckInsToday;
    }

    public long getDashboardPendingCheckInsToday() {
        return dashboardPendingCheckInsToday;
    }

    public long getDashboardDoneCheckOutsToday() {
        return dashboardDoneCheckOutsToday;
    }

    public long getDashboardPendingCheckOutsToday() {
        return dashboardPendingCheckOutsToday;
    }

    public BigDecimal getDashboardMonthlyRevenue() {
        return dashboardMonthlyRevenue;
    }

    public long getDashboardMonthlyBookings() {
        return dashboardMonthlyBookings;
    }

    public long getTotalCheckIns() {
        return totalCheckIns;
    }

    public long getCheckInsToday() {
        return checkInsToday;
    }

    public long getCheckInsThisWeek() {
        return checkInsThisWeek;
    }

    public long getCheckInsThisMonth() {
        return checkInsThisMonth;
    }

    public long getExpectedCheckInsToday() {
        return expectedCheckInsToday;
    }

    public long getExpectedCheckInsThisWeek() {
        return expectedCheckInsThisWeek;
    }

    public long getExpectedCheckInsThisMonth() {
        return expectedCheckInsThisMonth;
    }

    public long getCompletedCheckIns() {
        return completedCheckIns;
    }

    public long getTotalCheckOuts() {
        return totalCheckOuts;
    }

    public long getCheckOutsToday() {
        return checkOutsToday;
    }

    public long getCheckOutsThisWeek() {
        return checkOutsThisWeek;
    }

    public long getCheckOutsThisMonth() {
        return checkOutsThisMonth;
    }

    public long getExpectedCheckOutsToday() {
        return expectedCheckOutsToday;
    }

    public long getExpectedCheckOutsThisWeek() {
        return expectedCheckOutsThisWeek;
    }

    public long getExpectedCheckOutsThisMonth() {
        return expectedCheckOutsThisMonth;
    }

    public long getCompletedCheckOuts() {
        return completedCheckOuts;
    }
}
