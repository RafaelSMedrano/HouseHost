package com.househost.guest.application.service;

import com.househost.guest.domain.model.Guest;
import org.springframework.stereotype.Service;

@Service
public class GuestDataSecurityService {

    public String maskData(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof String text && text.isBlank()) {
            return text;
        }
        return "***";
    }

    public Guest maskFullData(Guest guest) {
        if (guest == null) {
            return null;
        }

        Guest maskedGuest = new Guest();
        maskedGuest.updateProfile(
                guest.getFullName(),
                maskData(guest.getEmail()),
                maskData(guest.getPhone()),
                maskData(guest.getDocumentNumber()),
                guest.getCity(),
                guest.getState(),
                maskData(guest.getAddress()),
                null,
                guest.getGender(),
                guest.getGuestTypeEnum(),
                guest.getStatus(),
                guest.isTravelsWithPets(),
                guest.getPetType(),
                guest.isNeedsAccessibility(),
                guest.getFavoriteRoom(),
                guest.getStayCount(),
                guest.getTotalSpent(),
                guest.getLastStayDate(),
                guest.getRating(),
                guest.getOriginChannel(),
                guest.getReferredBy(),
                maskData(guest.getNotes()),
                guest.getPreferences()
        );
        maskedGuest.restorePersistenceState(
                guest.getId(),
                guest.getFinancialStatus(),
                guest.getFinancialTransactionIds(),
                guest.getCreatedAt(),
                guest.getUpdatedAt()
        );
        return maskedGuest;
    }

}
