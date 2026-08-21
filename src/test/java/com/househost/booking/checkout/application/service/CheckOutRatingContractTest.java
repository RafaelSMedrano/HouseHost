package com.househost.booking.checkout.application.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.househost.booking.checkout.adapter.out.persistence.entity.CheckOutJpaEntity;
import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutResponseDTO;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.guest.adapter.out.persistence.entity.GuestJpaEntity;
import com.househost.guest.application.dto.GuestRegisterRequestDTO;
import com.househost.guest.application.dto.GuestRegisterResponseDTO;
import com.househost.guest.domain.model.Guest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckOutRatingContractTest {

    @Test
    void checkoutUsesNestedSixCriterionRatingInput() throws Exception {
        assertEquals(
                CheckOutRatingRequestDTO.class,
                CheckOutRequestDTO.class.getDeclaredField("rating").getType()
        );
        assertEquals(7, CheckOutRatingRequestDTO.class.getDeclaredFields().length);
    }

    @Test
    void genericRatingNoLongerBelongsToGuestOrCheckoutState() {
        assertHasNoRatingField(CheckOut.class);
        assertHasNoRatingField(CheckOutResponseDTO.class);
        assertHasNoRatingField(CheckOutJpaEntity.class);
        assertHasNoRatingField(Guest.class);
        assertHasNoRatingField(GuestRegisterResponseDTO.class);
        assertHasNoRatingField(GuestJpaEntity.class);

        JsonIgnoreProperties jsonIgnoreProperties = GuestRegisterRequestDTO.class
                .getAnnotation(JsonIgnoreProperties.class);
        assertFalse(Arrays.asList(jsonIgnoreProperties.value()).contains("rating"));
    }

    private void assertHasNoRatingField(Class<?> inspectedClass) {
        assertThrows(
                NoSuchFieldException.class,
                () -> inspectedClass.getDeclaredField("rating")
        );
    }
}
