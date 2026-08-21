package com.househost.notifier.adapter.out.integration;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotifierDeliveryProfilePropertiesTest {

    @Test
    void normalizesTrustedProfileAndSourceSystemKeys() {
        NotifierDeliveryProfileProperties notifierDeliveryProfileProperties =
                validProperties();

        assertTrue(notifierDeliveryProfileProperties.resolveEnabledProfileOptional(
                "HOUSEHOST_TRANSACTIONAL",
                "househost"
        ).isPresent());
    }

    @Test
    void failsClosedWhenSesHasNoEnabledProfile() {
        NotifierDeliveryProfileProperties notifierDeliveryProfileProperties =
                new NotifierDeliveryProfileProperties();
        notifierDeliveryProfileProperties.setEnabled(true);

        assertThrows(
                IllegalStateException.class,
                notifierDeliveryProfileProperties::validate
        );
    }

    @Test
    void rejectsIncompleteEnabledProfileAndInvalidTimeouts() {
        NotifierDeliveryProfileProperties notifierDeliveryProfileProperties =
                validPropertiesWithoutValidation();
        notifierDeliveryProfileProperties.getProfiles()
                .values()
                .iterator()
                .next()
                .setSender(" ");
        assertThrows(
                IllegalStateException.class,
                notifierDeliveryProfileProperties::validate
        );

        NotifierDeliveryProfileProperties timeoutProperties =
                validPropertiesWithoutValidation();
        timeoutProperties.setApiCallTimeout(Duration.ofSeconds(2));
        timeoutProperties.setApiCallAttemptTimeout(Duration.ofSeconds(3));
        assertThrows(IllegalStateException.class, timeoutProperties::validate);
    }

    @Test
    void disabledSesDoesNotRequireDeliveryConfiguration() {
        NotifierDeliveryProfileProperties notifierDeliveryProfileProperties =
                new NotifierDeliveryProfileProperties();

        assertDoesNotThrow(notifierDeliveryProfileProperties::validate);
    }

    private NotifierDeliveryProfileProperties validProperties() {
        NotifierDeliveryProfileProperties notifierDeliveryProfileProperties =
                validPropertiesWithoutValidation();
        notifierDeliveryProfileProperties.validate();
        return notifierDeliveryProfileProperties;
    }

    private NotifierDeliveryProfileProperties validPropertiesWithoutValidation() {
        NotifierDeliveryProfileProperties.DeliveryProfileProperties
                deliveryProfileProperties =
                new NotifierDeliveryProfileProperties.DeliveryProfileProperties();
        deliveryProfileProperties.setEnabled(true);
        deliveryProfileProperties.setRegion("sa-east-1");
        deliveryProfileProperties.setSender("no-reply@example.com");
        deliveryProfileProperties.setConfigurationSet("househost-transactional");
        deliveryProfileProperties.setPermittedSourceSystems(Set.of("househost"));

        NotifierDeliveryProfileProperties notifierDeliveryProfileProperties =
                new NotifierDeliveryProfileProperties();
        notifierDeliveryProfileProperties.setEnabled(true);
        Map<String, NotifierDeliveryProfileProperties.DeliveryProfileProperties>
                deliveryProfilePropertiesMap = Map.of(
                        "househost-transactional",
                        deliveryProfileProperties
                );
        notifierDeliveryProfileProperties.setProfiles(deliveryProfilePropertiesMap);
        return notifierDeliveryProfileProperties;
    }
}
