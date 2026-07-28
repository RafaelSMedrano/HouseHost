package com.househost.guest.adapter.out.persistence.entity;

import com.househost.guest.domain.model.Guest;

public final class GuestPersistenceMapper {

    private GuestPersistenceMapper() {
    }

    public static Guest toDomain(GuestJpaEntity entity) {
        Guest guest = new Guest();
        guest.updateProfile(
                entity.fullName, entity.email, entity.phone, entity.documentNumber,
                entity.city, entity.state, entity.address, entity.birthDate, entity.gender,
                entity.guestType, entity.status, entity.travelsWithPets, entity.petType,
                entity.needsAccessibility, entity.favoriteRoom, entity.stayCount,
                entity.totalSpent, entity.lastStayDate, entity.rating, entity.originChannel,
                entity.referredBy, entity.notes, entity.preferences
        );
        guest.restorePersistenceState(
                entity.id, entity.financialStatus, entity.financialTransactionIds,
                entity.createdAt, entity.updatedAt
        );
        return guest;
    }

    public static GuestJpaEntity toEntity(Guest guest) {
        if (guest == null) {
            return null;
        }
        if (guest instanceof GuestJpaEntity entity) {
            return entity;
        }
        GuestJpaEntity entity = new GuestJpaEntity();
        entity.id = guest.getId();
        entity.fullName = guest.getFullName();
        entity.email = guest.getEmail();
        entity.phone = guest.getPhone();
        entity.documentNumber = guest.getDocumentNumber();
        entity.city = guest.getCity();
        entity.state = guest.getState();
        entity.address = guest.getAddress();
        entity.birthDate = guest.getBirthDate();
        entity.gender = guest.getGender();
        entity.guestType = guest.getGuestTypeEnum();
        entity.status = guest.getStatus();
        entity.financialStatus = guest.getFinancialStatus();
        entity.travelsWithPets = guest.isTravelsWithPets();
        entity.petType = guest.getPetType();
        entity.needsAccessibility = guest.isNeedsAccessibility();
        entity.favoriteRoom = guest.getFavoriteRoom();
        entity.stayCount = guest.getStayCount();
        entity.totalSpent = guest.getTotalSpent();
        entity.lastStayDate = guest.getLastStayDate();
        entity.rating = guest.getRating();
        entity.originChannel = guest.getOriginChannel();
        entity.referredBy = guest.getReferredBy();
        entity.notes = guest.getNotes();
        entity.preferences = new java.util.ArrayList<>(guest.getPreferences());
        entity.financialTransactionIds = new java.util.ArrayList<>(guest.getFinancialTransactionIds());
        entity.createdAt = guest.getCreatedAt();
        entity.updatedAt = guest.getUpdatedAt();
        return entity;
    }
}
