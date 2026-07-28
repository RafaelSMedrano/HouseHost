package com.househost.privacy.policy.architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PrivacyPolicyArchitectureTest {
    private static final Path POLICY_SOURCE = Path.of(
            "src/main/java/com/househost/privacy/policy"
    );

    @Test
    void domainDoesNotDependOnFrameworkPersistenceOrOtherBusinessModules() throws IOException {
        assertSourcesDoNotContain(
                POLICY_SOURCE.resolve("domain"),
                List.of(
                        "jakarta.persistence",
                        "org.springframework",
                        "com.househost.booking",
                        "com.househost.audit",
                        "com.househost.auth"
                )
        );
    }

    private void assertSourcesDoNotContain(Path sourceRoot, List<String> forbiddenTerms)
            throws IOException {
        try (Stream<Path> sourcePathStream = Files.walk(sourceRoot)) {
            for (Path sourcePath : sourcePathStream.filter(this::isJavaSource).toList()) {
                String source = Files.readString(sourcePath);
                for (String forbiddenTerm : forbiddenTerms) {
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
