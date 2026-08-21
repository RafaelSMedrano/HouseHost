package com.househost.booking.checking.adapter.out.persistence.entity;

import com.househost.booking.booking.adapter.out.persistence.entity.BookingPersistenceMapper;
import com.househost.booking.checking.domain.model.CheckIn;
import com.househost.guest.adapter.out.persistence.entity.GuestPersistenceMapper;
import com.househost.room.adapter.out.persistence.entity.RoomPersistenceMapper;

public final class CheckInPersistenceMapper {
    private CheckInPersistenceMapper() {
    }

    public static CheckIn toDomain(CheckInJpaEntity entity) {
        CheckIn checkIn = new CheckIn(
                entity.booking == null ? null : BookingPersistenceMapper.toDomain(entity.booking),
                GuestPersistenceMapper.toDomain(entity.guest),
                RoomPersistenceMapper.toDomain(entity.room),
                entity.adults, entity.children, entity.pets, entity.documentVerified,
                entity.paymentVerified, entity.registrationFormSigned, entity.rulesAccepted,
                entity.keysDelivered, entity.vehiclePlate, entity.vehicleModel, entity.performedBy,
                entity.notes, entity.status
        );
        checkIn.restorePersistenceState(entity.id, entity.createdAt);
        return checkIn;
    }

    public static CheckInJpaEntity toEntity(CheckIn checkIn) {
        CheckInJpaEntity entity = new CheckInJpaEntity();
        entity.id = checkIn.getId();
        entity.booking = checkIn.getBooking() == null
                ? null
                : BookingPersistenceMapper.toEntity(checkIn.getBooking());
        entity.guest = GuestPersistenceMapper.toEntity(checkIn.getGuest());
        entity.room = RoomPersistenceMapper.toEntity(checkIn.getRoom());
        entity.adults = checkIn.getAdults();
        entity.children = checkIn.getChildren();
        entity.pets = checkIn.getPets();
        entity.documentVerified = checkIn.isDocumentVerified();
        entity.paymentVerified = checkIn.isPaymentVerified();
        entity.registrationFormSigned = checkIn.isRegistrationFormSigned();
        entity.rulesAccepted = checkIn.isRulesAccepted();
        entity.keysDelivered = checkIn.isKeysDelivered();
        entity.vehiclePlate = checkIn.getVehiclePlate();
        entity.vehicleModel = checkIn.getVehicleModel();
        entity.performedBy = checkIn.getPerformedBy();
        entity.notes = checkIn.getNotes();
        entity.status = checkIn.getStatus();
        entity.createdAt = checkIn.getCreatedAt();
        return entity;
    }
}
