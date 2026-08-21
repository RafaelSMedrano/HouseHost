package com.househost.ratings.architecture;

import com.househost.ratings.adapter.in.rest.RatingController;
import com.househost.ratings.adapter.out.integration.RatingAuditAdapter;
import com.househost.ratings.application.port.in.RatingUseCase;
import com.househost.ratings.application.port.out.RatingAuditPort;
import com.househost.ratings.application.service.RatingService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatingModuleArchitectureTest {

    private static final Path RATING_SOURCE_PATH = Path.of(
            "src/main/java/com/househost/ratings"
    );

    @Test
    void domainRemainsFreeOfFrameworkAndPersistenceDependencies() throws IOException {
        assertSourcesDoNotContain(
                RATING_SOURCE_PATH.resolve("domain"),
                List.of(
                        "jakarta.persistence",
                        "org.springframework",
                        ".adapter.",
                        "JpaEntity",
                        "JpaRepository"
                )
        );
    }

    @Test
    void applicationUsesPortsInsteadOfAuditAndPersistenceAdapters() throws IOException {
        assertSourcesDoNotContain(
                RATING_SOURCE_PATH.resolve("application"),
                List.of(
                        "AuditEventService",
                        "RatingAuditAdapter",
                        "RatingJpaEntity",
                        "RatingJpaRepository",
                        "RatingPersistenceAdapter"
                )
        );
        assertTrue(hasFieldOfType(RatingService.class, RatingAuditPort.class));
        assertTrue(RatingAuditPort.class.isAssignableFrom(RatingAuditAdapter.class));
    }

    @Test
    void controllerDependsOnUseCaseAndOffersOnlyCreateAndPaginatedReads() {
        assertEquals(
                RatingUseCase.class,
                RatingController.class.getDeclaredFields()[0].getType()
        );
        Set<String> controllerMethodNameSet = Arrays.stream(
                        RatingController.class.getDeclaredMethods()
                )
                .map(java.lang.reflect.Method::getName)
                .collect(Collectors.toSet());

        assertEquals(
                Set.of("create", "findAll", "findByGuestId"),
                controllerMethodNameSet
        );
        assertFalse(controllerMethodNameSet.contains("update"));
        assertFalse(controllerMethodNameSet.contains("delete"));
    }

    @Test
    void ratingsSourceContainsNoApplicationLoggingSink() throws IOException {
        assertSourcesDoNotContain(
                RATING_SOURCE_PATH,
                List.of(
                        "LoggerFactory",
                        "System.out",
                        "System.err"
                )
        );
    }

    private boolean hasFieldOfType(Class<?> ownerClass, Class<?> fieldClass) {
        return Arrays.stream(ownerClass.getDeclaredFields())
                .map(java.lang.reflect.Field::getType)
                .anyMatch(fieldType -> fieldType.equals(fieldClass));
    }

    private void assertSourcesDoNotContain(
            Path sourceRootPath,
            List<String> forbiddenTermList
    ) throws IOException {
        try (Stream<Path> sourcePathStream = Files.walk(sourceRootPath)) {
            for (Path sourcePath : sourcePathStream.filter(this::isJavaSource).toList()) {
                String source = Files.readString(sourcePath);
                for (String forbiddenTerm : forbiddenTermList) {
                    assertFalse(
                            source.contains(forbiddenTerm),
                            () -> sourcePath + " contains forbidden dependency "
                                    + forbiddenTerm
                    );
                }
            }
        }
    }

    private boolean isJavaSource(Path sourcePath) {
        return Files.isRegularFile(sourcePath)
                && sourcePath.toString().endsWith(".java");
    }
}
