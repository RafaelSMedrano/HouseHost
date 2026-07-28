package com.househost.booking.checkout.adapter.out.persistence.entity;

import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.guest.adapter.out.persistence.entity.GuestPersistenceMapper;
import com.househost.room.adapter.out.persistence.entity.RoomPersistenceMapper;
import com.househost.booking.booking.adapter.out.persistence.entity.BookingPersistenceMapper;

public final class CheckOutPersistenceMapper {
    private CheckOutPersistenceMapper() {
    }

    public static CheckOut toDomain(CheckOutJpaEntity entity) {
        CheckOut checkOut = new CheckOut(
                BookingPersistenceMapper.toDomain(entity.booking),
                GuestPersistenceMapper.toDomain(entity.guest),
                RoomPersistenceMapper.toDomain(entity.room),
                entity.actualCheckOutAt,
                entity.roomInspected,
                entity.keysReturned,
                entity.consumablesChecked,
                entity.pendingAmountPaid,
                entity.extraCharges,
                entity.pendingAmount,
                entity.performedBy,
                entity.notes,
                entity.status
        );
        checkOut.restorePersistenceState(entity.id, entity.createdAt, entity.updatedAt);
        return checkOut;
    }

    public static CheckOutJpaEntity toEntity(CheckOut checkOut) {
        CheckOutJpaEntity entity = new CheckOutJpaEntity();
        entity.id = checkOut.getId();
        entity.booking = BookingPersistenceMapper.toEntity(checkOut.getBooking());
        entity.guest = GuestPersistenceMapper.toEntity(checkOut.getGuest());
        entity.room = RoomPersistenceMapper.toEntity(checkOut.getRoom());
        entity.actualCheckOutAt = checkOut.getActualCheckOutAt();
        entity.roomInspected = checkOut.isRoomInspected();
        entity.keysReturned = checkOut.isKeysReturned();
        entity.consumablesChecked = checkOut.isConsumablesChecked();
        entity.pendingAmountPaid = checkOut.isPendingAmountPaid();
        entity.extraCharges = checkOut.getExtraCharges();
        entity.pendingAmount = checkOut.getPendingAmount();
        entity.performedBy = checkOut.getPerformedBy();
        entity.notes = checkOut.getNotes();
        entity.status = checkOut.getStatus();
        entity.createdAt = checkOut.getCreatedAt();
        entity.updatedAt = checkOut.getUpdatedAt();
        return entity;
    }
}
