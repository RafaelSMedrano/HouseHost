package com.househost.privacy.processing.architecture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class PrivacyProcessingArchitectureTest {
    private static final Path PROCESSING_SOURCE = Path.of(
            "src/main/java/com/househost/privacy/processing"
    );

    @Test
    void domainDoesNotDependOnFrameworkOrLegalBasis() throws IOException {
        assertSourcesDoNotContain(
                PROCESSING_SOURCE.resolve("domain"),
                List.of(
                        "jakarta.persistence",
                        "org.springframework",
                        "com.househost.privacy.legalbasis",
                        "ProcessingLegalBasisAssessment"
                )
        );
    }

    @Test
    void applicationDoesNotDependOnAssessmentPersistenceOrReadiness() throws IOException {
        assertSourcesDoNotContain(
                PROCESSING_SOURCE.resolve("application"),
                List.of(
                        "ProcessingLegalBasisAssessmentPersistencePort",
                        "LegalBasisAssessmentReadinessService",
                        "com.househost.privacy.legalbasis",
                        "com.househost.privacy.legalbasis.adapter",
                        "com.househost.privacy.legalbasis.persistence"
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
