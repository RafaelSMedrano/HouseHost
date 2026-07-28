package com.househost.booking.booking.architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BookingPrivacyPersistenceIsolationTest {
    private static final Path BOOKING_SOURCE = Path.of(
            "src/main/java/com/househost/booking/booking"
    );

    @Test
    void bookingDomainAndPersistenceDoNotDependOnPrivacyTypesOrPolicyIds() throws IOException {
        assertSourcesDoNotContain(
                BOOKING_SOURCE.resolve("domain"),
                List.of("com.househost.privacy", "privacyPolicyId")
        );
        assertSourcesDoNotContain(
                BOOKING_SOURCE.resolve("adapter/out/persistence"),
                List.of(
                        "com.househost.privacy",
                        "privacyPolicyId",
                        "privacy_policy_id",
                        "PrivacyPolicyJpaEntity"
                )
        );
    }

    private void assertSourcesDoNotContain(Path sourceRoot, List<String> forbiddenTermList)
            throws IOException {
        try (Stream<Path> sourcePathStream = Files.walk(sourceRoot)) {
            for (Path sourcePath : sourcePathStream.filter(this::isJavaSource).toList()) {
                String source = Files.readString(sourcePath);
                for (String forbiddenTerm : forbiddenTermList) {
                    assertFalse(
                            source.contains(forbiddenTerm),
                            () -> sourcePath + " contains forbidden dependency " + forbiddenTerm
                    );
                }
            }
        }
    }

    private boolean isJavaSource(Path path) {
        return Files.isRegularFile(path) && path.toString().endsWith(".java");
    }
}
