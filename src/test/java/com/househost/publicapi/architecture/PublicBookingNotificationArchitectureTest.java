package com.househost.publicapi.architecture;

import com.househost.notifier.application.port.in.NotificationRequestUseCase;
import com.househost.publicapi.adapter.out.integration.NotifierPublicBookingAdapter;
import com.househost.publicapi.application.port.out.PublicBookingNotificationPort;
import com.househost.publicapi.application.service.PublicBookingNotificationResolver;
import com.househost.publicapi.application.service.PublicBookingParticipantNotifier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicBookingNotificationArchitectureTest {

    private static final Path SOURCE_ROOT = Path.of("src/main/java/com/househost");

    @Test
    void publicBookingUsesConsumerPortResolverAndNotifierInputPortAdapter() {
        assertTrue(PublicBookingNotificationPort.class.isAssignableFrom(
                NotifierPublicBookingAdapter.class
        ));
        assertTrue(hasFieldOfType(
                PublicBookingParticipantNotifier.class,
                PublicBookingNotificationResolver.class
        ));
        assertTrue(hasFieldOfType(
                PublicBookingNotificationResolver.class,
                PublicBookingNotificationPort.class
        ));
        assertTrue(hasFieldOfType(
                NotifierPublicBookingAdapter.class,
                NotificationRequestUseCase.class
        ));
    }

    @Test
    void publicBookingCoreDoesNotImportNotifierOrAwsInfrastructure() throws IOException {
        String publicApiApplicationSource = sourceUnder(
                SOURCE_ROOT.resolve("publicapi/application")
        );
        String publicApiSource = sourceUnder(SOURCE_ROOT.resolve("publicapi"));

        assertFalse(publicApiApplicationSource.contains("com.househost.notifier"));
        assertFalse(publicApiSource.contains("software.amazon.awssdk"));
        assertFalse(publicApiSource.contains("notifier.adapter.out.persistence"));
        assertFalse(publicApiSource.contains("notifier.adapter.in.http"));
    }

    @Test
    void notifierDoesNotCallBackPublicBookingOrConsumerDomains() throws IOException {
        String notifierSource = sourceUnder(SOURCE_ROOT.resolve("notifier"));

        assertFalse(notifierSource.contains("com.househost.publicapi"));
        assertFalse(notifierSource.contains("com.househost.booking"));
        assertFalse(notifierSource.contains("com.househost.guest"));
    }

    private boolean hasFieldOfType(Class<?> ownerClass, Class<?> fieldClass) {
        return List.of(ownerClass.getDeclaredFields())
                .stream()
                .map(Field::getType)
                .anyMatch(fieldType -> fieldType.equals(fieldClass));
    }

    private String sourceUnder(Path directory) throws IOException {
        try (var sourcePathStream = Files.walk(directory)) {
            StringBuilder sourceBuilder = new StringBuilder();
            for (Path sourcePath : sourcePathStream
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                sourceBuilder.append(Files.readString(sourcePath));
            }
            return sourceBuilder.toString();
        }
    }
}
