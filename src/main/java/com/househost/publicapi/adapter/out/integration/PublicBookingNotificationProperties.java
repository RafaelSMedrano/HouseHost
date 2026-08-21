package com.househost.publicapi.adapter.out.integration;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "househost.public-booking.notification")
public class PublicBookingNotificationProperties {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );
    private static final Pattern SYMBOLIC_KEY_PATTERN = Pattern.compile(
            "[A-Z][A-Z0-9_]*"
    );

    private boolean enabled;
    private String managementRecipient;
    private String deliveryProfileKey = "HOUSEHOST_TRANSACTIONAL";

    @PostConstruct
    void validate() {
        deliveryProfileKey = normalizeSymbolicKey(deliveryProfileKey);
        if (!enabled) {
            return;
        }
        if (managementRecipient == null
                || managementRecipient.isBlank()
                || managementRecipient.length() > 320
                || managementRecipient.contains("\r")
                || managementRecipient.contains("\n")
                || !EMAIL_PATTERN.matcher(managementRecipient.trim()).matches()) {
            throw new IllegalStateException(
                    "househost.public-booking.notification.management-recipient deve ser um email valido."
            );
        }
        managementRecipient = managementRecipient.trim().toLowerCase(Locale.ROOT);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getManagementRecipient() {
        return managementRecipient;
    }

    public void setManagementRecipient(String managementRecipient) {
        this.managementRecipient = managementRecipient;
    }

    public String getDeliveryProfileKey() {
        return deliveryProfileKey;
    }

    public void setDeliveryProfileKey(String deliveryProfileKey) {
        this.deliveryProfileKey = deliveryProfileKey;
    }

    private String normalizeSymbolicKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "househost.public-booking.notification.delivery-profile-key e obrigatorio."
            );
        }
        String normalizedValue = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');
        if (!SYMBOLIC_KEY_PATTERN.matcher(normalizedValue).matches()) {
            throw new IllegalStateException(
                    "househost.public-booking.notification.delivery-profile-key deve usar identificador simbolico."
            );
        }
        return normalizedValue;
    }
}
