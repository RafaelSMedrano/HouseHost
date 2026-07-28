package com.househost.booking.booking.adapter.out.persistence.entity;

import com.househost.booking.booking.domain.model.Booking;
import com.househost.guest.adapter.out.persistence.entity.GuestPersistenceMapper;
import com.househost.room.adapter.out.persistence.entity.RoomPersistenceMapper;

public final class BookingPersistenceMapper {

    private BookingPersistenceMapper() {
    }

    public static Booking toDomain(BookingJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Booking booking = new Booking(
                GuestPersistenceMapper.toDomain(entity.guest),
                RoomPersistenceMapper.toDomain(entity.room),
                entity.checkInDate,
                entity.checkOutDate,
                entity.status,
                entity.totalAmount,
                entity.origin,
                entity.adults,
                entity.children,
                entity.pets,
                entity.paymentMethod,
                entity.installments,
                entity.dailyRate,
                entity.discount,
                entity.paidAmount,
                entity.paymentDate,
                entity.specialRequests,
                entity.internalNotes
        );
        booking.restorePersistenceState(
                entity.id,
                entity.paymentStatus,
                entity.privacyPolicyVersion,
                entity.privacyPolicyContentHash,
                entity.termsVersion,
                entity.privacyAcceptedAt,
                entity.marketingOptIn,
                entity.marketingOptInAt,
                entity.createdAt,
                entity.updatedAt
        );
        return booking;
    }

    public static BookingJpaEntity toEntity(Booking booking) {
        if (booking == null) {
            return null;
        }
        if (booking instanceof BookingJpaEntity entity) {
            return entity;
        }

        BookingJpaEntity entity = new BookingJpaEntity();
        entity.id = booking.getId();
        entity.guest = GuestPersistenceMapper.toEntity(booking.getGuest());
        entity.room = RoomPersistenceMapper.toEntity(booking.getRoom());
        entity.checkInDate = booking.getCheckInDate();
        entity.checkOutDate = booking.getCheckOutDate();
        entity.status = booking.getStatus();
        entity.totalAmount = booking.getTotalAmount();
        entity.origin = booking.getOrigin();
        entity.adults = booking.getAdults();
        entity.children = booking.getChildren();
        entity.pets = booking.getPets();
        entity.paymentMethod = booking.getPaymentMethod();
        entity.installments = booking.getInstallments();
        entity.dailyRate = booking.getDailyRate();
        entity.discount = booking.getDiscount();
        entity.paidAmount = booking.getPaidAmount();
        entity.paymentDate = booking.getPaymentDate();
        entity.paymentStatus = booking.getPaymentStatus();
        entity.specialRequests = booking.getSpecialRequests();
        entity.internalNotes = booking.getInternalNotes();
        entity.privacyPolicyVersion = booking.getPrivacyPolicyVersion();
        entity.privacyPolicyContentHash = booking.getPrivacyPolicyContentHash();
        entity.termsVersion = booking.getTermsVersion();
        entity.privacyAcceptedAt = booking.getPrivacyAcceptedAt();
        entity.marketingOptIn = booking.getMarketingOptIn();
        entity.marketingOptInAt = booking.getMarketingOptInAt();
        entity.createdAt = booking.getCreatedAt();
        entity.updatedAt = booking.getUpdatedAt();
        return entity;
    }
}
