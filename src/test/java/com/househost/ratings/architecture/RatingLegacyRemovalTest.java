package com.househost.ratings.architecture;

import com.househost.booking.checkout.adapter.out.persistence.entity.CheckOutJpaEntity;
import com.househost.booking.checkout.application.dto.CheckOutRatingRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutRequestDTO;
import com.househost.booking.checkout.application.dto.CheckOutResponseDTO;
import com.househost.booking.checkout.domain.model.CheckOut;
import com.househost.guest.adapter.out.persistence.entity.GuestJpaEntity;
import com.househost.guest.application.dto.GuestRegisterResponseDTO;
import com.househost.guest.domain.model.Guest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatingLegacyRemovalTest {

    @Test
    void obsoleteGenericRatingIsAbsentFromGuestAndCheckoutState() {
        List<Class<?>> legacyOwnerClassList = List.of(
                Guest.class,
                GuestRegisterResponseDTO.class,
                GuestJpaEntity.class,
                CheckOut.class,
                CheckOutResponseDTO.class,
                CheckOutJpaEntity.class
        );

        legacyOwnerClassList.forEach(legacyOwnerClass -> assertThrows(
                NoSuchFieldException.class,
                () -> legacyOwnerClass.getDeclaredField("rating")
        ));
        assertEquals(
                CheckOutRatingRequestDTO.class,
                checkoutRatingType()
        );
    }

    @Test
    void compatibilitySchemaDropsLegacyColumnsWithoutFabricatingRatings()
            throws IOException {
        String compatibilitySource = Files.readString(Path.of(
                "src/main/java/com/househost/config/DatabaseSchemaCompatibilityRunner.java"
        ));

        assertTrue(compatibilitySource.contains(
                "dropColumnIfExists(\"guests\", \"rating\")"
        ));
        assertTrue(compatibilitySource.contains(
                "dropColumnIfExists(\"check_outs\", \"rating\")"
        ));
        assertFalse(compatibilitySource.contains("insert into ratings"));
        assertFalse(compatibilitySource.contains("update ratings"));
    }

    private Class<?> checkoutRatingType() {
        try {
            return CheckOutRequestDTO.class.getDeclaredField("rating").getType();
        } catch (NoSuchFieldException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
