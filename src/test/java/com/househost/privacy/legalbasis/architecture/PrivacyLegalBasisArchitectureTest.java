package com.househost.privacy.legalbasis.architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PrivacyLegalBasisArchitectureTest {
    private static final Path LEGAL_BASIS_SOURCE = Path.of(
            "src/main/java/com/househost/privacy/legalbasis"
    );

    @Test
    void domainDoesNotDependOnFrameworkAuthenticationOrAuditInfrastructure() throws IOException {
        assertSourcesDoNotContain(
                LEGAL_BASIS_SOURCE.resolve("domain"),
                List.of(
                        "jakarta.persistence",
                        "org.springframework",
                        "com.househost.auth",
                        "com.househost.audit",
                        "com.househost.security"
                )
        );
    }

    @Test
    void applicationDoesNotDependOnProcessingPersistenceOrAdapters() throws IOException {
        assertSourcesDoNotContain(
                LEGAL_BASIS_SOURCE.resolve("application"),
                List.of(
                        "com.househost.privacy.processing.adapter",
                        "com.househost.privacy.processing.application.port.out",
                        "DataProcessingOperationPersistencePort",
                        "DataProcessingOperationJpaEntity",
                        "DataProcessingOperationJpaRepository"
                )
        );
    }

    @Test
    void parentPrivacyContainsNoDomainOrPersistenceImplementation() {
        assertFalse(Files.exists(Path.of("src/main/java/com/househost/privacy/domain")));
        assertFalse(Files.exists(Path.of("src/main/java/com/househost/privacy/adapter/out/persistence")));
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
