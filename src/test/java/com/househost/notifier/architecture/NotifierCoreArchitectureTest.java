package com.househost.notifier.architecture;

import com.househost.notifier.application.records.EmailDeliveryResultRecord;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.application.records.NotificationClaimRecord;
import com.househost.notifier.application.records.NotificationFeedbackRecord;
import com.househost.notifier.application.records.NotificationRequestRecord;
import com.househost.notifier.application.records.NotificationRetryDecisionRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotifierCoreArchitectureTest {

    private static final Path NOTIFIER_SOURCE_PATH = Path.of(
            "src/main/java/com/househost/notifier"
    );
    private static final List<Path> NOTIFIER_CORE_SOURCE_PATH_LIST = List.of(
            NOTIFIER_SOURCE_PATH.resolve("domain"),
            NOTIFIER_SOURCE_PATH.resolve("application")
    );
    private static final Set<String> FORBIDDEN_CONSUMER_FIELD_NAME_SET = Set.of(
            "bookingId",
            "guestId",
            "paymentId",
            "reservationId"
    );

    @Test
    void coreHasNoConsumerFrameworkProviderOrTransportDependencies() throws IOException {
        List<String> forbiddenDependencyList = List.of(
                "com.househost.booking",
                "com.househost.guest",
                "com.househost.finance",
                "com.househost.publicapi",
                "software.amazon.awssdk",
                "com.amazonaws",
                "org.springframework",
                "jakarta.persistence",
                "com.fasterxml.jackson",
                "JpaEntity",
                "JpaRepository",
                "@Entity"
        );
        for (Path notifierCoreSourcePath : NOTIFIER_CORE_SOURCE_PATH_LIST) {
            assertSourcesDoNotContain(
                    notifierCoreSourcePath,
                    forbiddenDependencyList
            );
        }
    }

    @Test
    void coreContainsNoConsumerForeignIdentifier() throws IOException {
        try (Stream<Path> sourcePathStream = Files.walk(NOTIFIER_SOURCE_PATH)) {
            for (Path sourcePath : sourcePathStream.filter(this::isJavaSource).toList()) {
                String source = Files.readString(sourcePath);
                for (String forbiddenFieldName : FORBIDDEN_CONSUMER_FIELD_NAME_SET) {
                    assertFalse(
                            source.contains(forbiddenFieldName),
                            () -> sourcePath + " contains consumer identifier "
                                    + forbiddenFieldName
                    );
                }
            }
        }
    }

    @Test
    void applicationCarriersAreRecordsWithRequiredNamesAndComponentSuffixes() {
        List<Class<?>> applicationRecordClassList = List.of(
                NotificationRequestRecord.class,
                EmailMessageRecord.class,
                EmailDeliveryResultRecord.class,
                NotificationFeedbackRecord.class,
                NotificationClaimRecord.class,
                NotificationRetryDecisionRecord.class
        );

        for (Class<?> applicationRecordClass : applicationRecordClassList) {
            assertTrue(applicationRecordClass.isRecord());
            assertTrue(applicationRecordClass.getSimpleName().endsWith("Record"));
            for (RecordComponent recordComponent : applicationRecordClass.getRecordComponents()) {
                if (recordComponent.getType().isRecord()) {
                    assertTrue(
                            recordComponent.getName().endsWith("Record"),
                            () -> recordComponent.getName() + " must end with Record"
                    );
                }
            }
        }
    }

    @Test
    void normalizedContractsDoNotExposeRawProviderPayloads() {
        List<Class<?>> providerRecordClassList = List.of(
                EmailDeliveryResultRecord.class,
                NotificationFeedbackRecord.class
        );

        for (Class<?> providerRecordClass : providerRecordClassList) {
            for (RecordComponent recordComponent : providerRecordClass.getRecordComponents()) {
                String componentName = recordComponent.getName().toLowerCase();
                assertFalse(componentName.contains("payload"));
                assertFalse(componentName.contains("json"));
                assertFalse(componentName.contains("envelope"));
                assertFalse(componentName.contains("signature"));
                assertFalse(componentName.contains("header"));
            }
        }
    }

    @Test
    void operationalLoggingAdapterContainsNoMessageOrRecipientData() throws IOException {
        Path operationalLoggingAdapterPath = NOTIFIER_SOURCE_PATH.resolve(
                "adapter/out/integration/Slf4jNotificationOperationalEventAdapter.java"
        );
        String source = Files.readString(operationalLoggingAdapterPath);

        assertFalse(source.contains("recipient"));
        assertFalse(source.contains("subject"));
        assertFalse(source.contains("textBody"));
        assertFalse(source.contains("htmlBody"));
        assertFalse(source.contains("correlationKey"));
        assertFalse(source.contains("emailMessageRecord"));
    }

    @Test
    void awsSdkDependenciesRemainInsideNotifierAdapters() throws IOException {
        try (Stream<Path> sourcePathStream = Files.walk(NOTIFIER_SOURCE_PATH)) {
            for (Path sourcePath : sourcePathStream.filter(this::isJavaSource).toList()) {
                String source = Files.readString(sourcePath);
                if (source.contains("software.amazon.awssdk")) {
                    assertTrue(
                            sourcePath.toString().contains("adapter"),
                            () -> sourcePath + " contains misplaced AWS SDK dependency"
                    );
                }
            }
        }
    }

    @Test
    void awsClientUsesDefaultCredentialsAndNoStaticCredentials() throws IOException {
        Path awsSesClientProviderPath = NOTIFIER_SOURCE_PATH.resolve(
                "adapter/out/integration/DefaultAwsSesClientProvider.java"
        );
        String source = Files.readString(awsSesClientProviderPath);

        assertTrue(source.contains("DefaultCredentialsProvider.create()"));
        assertFalse(source.contains("StaticCredentialsProvider"));
        assertFalse(source.contains("AwsBasicCredentials"));
        assertFalse(source.contains("accessKey"));
        assertFalse(source.contains("secretKey"));
    }

    @Test
    void sesAdapterDoesNotWriteMessageDataToLogs() throws IOException {
        Path awsSesAdapterPath = NOTIFIER_SOURCE_PATH.resolve(
                "adapter/out/integration/AwsSesEmailDeliveryAdapter.java"
        );
        String source = Files.readString(awsSesAdapterPath);

        assertFalse(source.contains("LoggerFactory"));
        assertFalse(source.contains("LOGGER"));
        assertFalse(source.contains("System.out"));
        assertFalse(source.contains("System.err"));
    }

    @Test
    void snsInboundAdapterDoesNotLogOrPersistRawEnvelope() throws IOException {
        Path snsControllerPath = NOTIFIER_SOURCE_PATH.resolve(
                "adapter/in/http/SnsSesFeedbackController.java"
        );
        Path sesParserPath = NOTIFIER_SOURCE_PATH.resolve(
                "adapter/in/http/SesFeedbackMessageParser.java"
        );
        String snsControllerSource = Files.readString(snsControllerPath);
        String sesParserSource = Files.readString(sesParserPath);

        assertFalse(snsControllerSource.contains("LoggerFactory"));
        assertFalse(snsControllerSource.contains("LOGGER"));
        assertFalse(sesParserSource.contains("emailAddress"));
        assertFalse(sesParserSource.contains("diagnosticCode"));
        assertFalse(sesParserSource.contains("rawEventStorageKey"));
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
