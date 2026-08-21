package com.househost.notifier.adapter.out.integration;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.regions.Region;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "househost.notifier.ses")
public class NotifierDeliveryProfileProperties {

    private static final Pattern SYMBOLIC_KEY_PATTERN = Pattern.compile(
            "[A-Z][A-Z0-9_]*"
    );
    private static final Pattern CONFIGURATION_SET_PATTERN = Pattern.compile(
            "[A-Za-z0-9_-]{1,64}"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );

    private boolean enabled;
    private Duration apiCallTimeout = Duration.ofSeconds(10);
    private Duration apiCallAttemptTimeout = Duration.ofSeconds(5);
    private Map<String, DeliveryProfileProperties> deliveryProfileMap =
            new LinkedHashMap<>();
    private Map<String, DeliveryProfileProperties> normalizedDeliveryProfileMap =
            Map.of();

    @PostConstruct
    void validate() {
        if (!enabled) {
            normalizedDeliveryProfileMap = Map.of();
            return;
        }
        requirePositiveDuration(apiCallTimeout, "api-call-timeout");
        requirePositiveDuration(apiCallAttemptTimeout, "api-call-attempt-timeout");
        if (apiCallAttemptTimeout.compareTo(apiCallTimeout) > 0) {
            throw invalidProperty(
                    "api-call-attempt-timeout",
                    "nao pode exceder api-call-timeout"
            );
        }

        Map<String, DeliveryProfileProperties> validatedDeliveryProfileMap =
                new LinkedHashMap<>();
        for (Map.Entry<String, DeliveryProfileProperties> deliveryProfileEntry
                : deliveryProfileMap.entrySet()) {
            String normalizedDeliveryProfileKey = normalizeSymbolicKey(
                    deliveryProfileEntry.getKey(),
                    "profiles"
            );
            DeliveryProfileProperties deliveryProfileProperties =
                    deliveryProfileEntry.getValue();
            if (deliveryProfileProperties == null) {
                throw invalidProperty(
                        "profiles." + deliveryProfileEntry.getKey(),
                        "deve ser configurado"
                );
            }
            if (validatedDeliveryProfileMap.putIfAbsent(
                    normalizedDeliveryProfileKey,
                    deliveryProfileProperties
            ) != null) {
                throw invalidProperty(
                        "profiles",
                        "contem chaves equivalentes"
                );
            }
            if (deliveryProfileProperties.isEnabled()) {
                validateEnabledProfile(
                        normalizedDeliveryProfileKey,
                        deliveryProfileProperties
                );
            }
        }
        boolean hasEnabledProfile = validatedDeliveryProfileMap.values()
                .stream()
                .anyMatch(DeliveryProfileProperties::isEnabled);
        if (!hasEnabledProfile) {
            throw invalidProperty("profiles", "deve conter ao menos um perfil habilitado");
        }
        normalizedDeliveryProfileMap = Map.copyOf(validatedDeliveryProfileMap);
    }

    public Optional<DeliveryProfileProperties> resolveEnabledProfileOptional(
            String deliveryProfileKey,
            String sourceSystem
    ) {
        if (!enabled) {
            return Optional.empty();
        }
        String normalizedDeliveryProfileKey = normalizeSymbolicKey(
                deliveryProfileKey,
                "deliveryProfileKey"
        );
        String normalizedSourceSystem = normalizeSymbolicKey(
                sourceSystem,
                "sourceSystem"
        );
        DeliveryProfileProperties deliveryProfileProperties =
                normalizedDeliveryProfileMap.get(normalizedDeliveryProfileKey);
        if (deliveryProfileProperties == null
                || !deliveryProfileProperties.isEnabled()
                || !deliveryProfileProperties
                        .getPermittedSourceSystems()
                        .contains(normalizedSourceSystem)) {
            return Optional.empty();
        }
        return Optional.of(deliveryProfileProperties);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getApiCallTimeout() {
        return apiCallTimeout;
    }

    public void setApiCallTimeout(Duration apiCallTimeout) {
        this.apiCallTimeout = apiCallTimeout;
    }

    public Duration getApiCallAttemptTimeout() {
        return apiCallAttemptTimeout;
    }

    public void setApiCallAttemptTimeout(Duration apiCallAttemptTimeout) {
        this.apiCallAttemptTimeout = apiCallAttemptTimeout;
    }

    public Map<String, DeliveryProfileProperties> getProfiles() {
        return deliveryProfileMap;
    }

    public void setProfiles(
            Map<String, DeliveryProfileProperties> deliveryProfileMap
    ) {
        this.deliveryProfileMap = deliveryProfileMap == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(deliveryProfileMap);
    }

    private void validateEnabledProfile(
            String deliveryProfileKey,
            DeliveryProfileProperties deliveryProfileProperties
    ) {
        String propertyPrefix = "profiles." + deliveryProfileKey.toLowerCase(Locale.ROOT);
        requireText(deliveryProfileProperties.getRegion(), propertyPrefix + ".region");
        String normalizedRegion = deliveryProfileProperties
                .getRegion()
                .trim()
                .toLowerCase(Locale.ROOT);
        Region region = Region.of(normalizedRegion);
        if (!Region.regions().contains(region)) {
            throw invalidProperty(propertyPrefix + ".region", "e invalida");
        }
        deliveryProfileProperties.setRegion(normalizedRegion);
        requireEmail(deliveryProfileProperties.getSender(), propertyPrefix + ".sender");
        deliveryProfileProperties.setSender(deliveryProfileProperties.getSender().trim());
        if (deliveryProfileProperties.getReplyTo() != null
                && !deliveryProfileProperties.getReplyTo().isBlank()) {
            requireEmail(
                    deliveryProfileProperties.getReplyTo(),
                    propertyPrefix + ".reply-to"
            );
            deliveryProfileProperties.setReplyTo(
                    deliveryProfileProperties.getReplyTo().trim()
            );
        } else {
            deliveryProfileProperties.setReplyTo(null);
        }
        requireText(
                deliveryProfileProperties.getConfigurationSet(),
                propertyPrefix + ".configuration-set"
        );
        if (!CONFIGURATION_SET_PATTERN.matcher(
                deliveryProfileProperties.getConfigurationSet().trim()
        ).matches()) {
            throw invalidProperty(
                    propertyPrefix + ".configuration-set",
                    "possui formato invalido"
            );
        }
        deliveryProfileProperties.setConfigurationSet(
                deliveryProfileProperties.getConfigurationSet().trim()
        );
        if (deliveryProfileProperties.getPermittedSourceSystems().isEmpty()) {
            throw invalidProperty(
                    propertyPrefix + ".permitted-source-systems",
                    "deve conter ao menos um sistema"
            );
        }
        Set<String> normalizedPermittedSourceSystems = new LinkedHashSet<>();
        for (String permittedSourceSystem
                : deliveryProfileProperties.getPermittedSourceSystems()) {
            normalizedPermittedSourceSystems.add(normalizeSymbolicKey(
                    permittedSourceSystem,
                    propertyPrefix + ".permitted-source-systems"
            ));
        }
        deliveryProfileProperties.setPermittedSourceSystems(
                Set.copyOf(normalizedPermittedSourceSystems)
        );
    }

    private String normalizeSymbolicKey(String value, String propertyName) {
        requireText(value, propertyName);
        String normalizedValue = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');
        if (!SYMBOLIC_KEY_PATTERN.matcher(normalizedValue).matches()) {
            throw invalidProperty(propertyName, "deve usar identificador simbolico");
        }
        return normalizedValue;
    }

    private void requireEmail(String value, String propertyName) {
        requireText(value, propertyName);
        if (value.contains("\r")
                || value.contains("\n")
                || value.length() > 320
                || !EMAIL_PATTERN.matcher(value.trim()).matches()) {
            throw invalidProperty(propertyName, "deve ser um email valido");
        }
    }

    private void requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw invalidProperty(propertyName, "e obrigatorio");
        }
    }

    private void requirePositiveDuration(Duration duration, String propertyName) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw invalidProperty(propertyName, "deve ser positiva");
        }
    }

    private IllegalStateException invalidProperty(String propertyName, String message) {
        return new IllegalStateException(
                "househost.notifier.ses." + propertyName + " " + message + "."
        );
    }

    public static class DeliveryProfileProperties {

        private boolean enabled;
        private String region;
        private String sender;
        private String replyTo;
        private String configurationSet;
        private Set<String> permittedSourceSystems = new LinkedHashSet<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getReplyTo() {
            return replyTo;
        }

        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }

        public String getConfigurationSet() {
            return configurationSet;
        }

        public void setConfigurationSet(String configurationSet) {
            this.configurationSet = configurationSet;
        }

        public Set<String> getPermittedSourceSystems() {
            return permittedSourceSystems;
        }

        public void setPermittedSourceSystems(Set<String> permittedSourceSystems) {
            this.permittedSourceSystems = permittedSourceSystems == null
                    ? new LinkedHashSet<>()
                    : new LinkedHashSet<>(permittedSourceSystems);
        }
    }
}
