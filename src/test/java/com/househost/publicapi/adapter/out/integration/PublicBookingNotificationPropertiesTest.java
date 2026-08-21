package com.househost.publicapi.adapter.out.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PublicBookingNotificationPropertiesTest {

    @Test
    void normalizesTrustedConfigurationWhenEnabled() {
        PublicBookingNotificationProperties publicBookingNotificationProperties =
                new PublicBookingNotificationProperties();
        publicBookingNotificationProperties.setEnabled(true);
        publicBookingNotificationProperties.setManagementRecipient(
                " GERENCIA@EXAMPLE.COM "
        );
        publicBookingNotificationProperties.setDeliveryProfileKey(
                "househost-transactional"
        );

        publicBookingNotificationProperties.validate();

        assertEquals(
                "gerencia@example.com",
                publicBookingNotificationProperties.getManagementRecipient()
        );
        assertEquals(
                "HOUSEHOST_TRANSACTIONAL",
                publicBookingNotificationProperties.getDeliveryProfileKey()
        );
    }

    @Test
    void enabledIntegrationFailsClosedWithoutTrustedManagementRecipient() {
        PublicBookingNotificationProperties publicBookingNotificationProperties =
                new PublicBookingNotificationProperties();
        publicBookingNotificationProperties.setEnabled(true);

        assertThrows(
                IllegalStateException.class,
                publicBookingNotificationProperties::validate
        );
    }
}
