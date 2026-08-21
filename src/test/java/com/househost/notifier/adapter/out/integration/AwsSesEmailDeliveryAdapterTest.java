package com.househost.notifier.adapter.out.integration;

import com.househost.notifier.application.records.EmailDeliveryResultRecord;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.domain.model.EmailDeliveryOutcome;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AwsSesEmailDeliveryAdapterTest {

    private SesV2Client sesV2Client;
    private AwsSesClientProvider awsSesClientProvider;
    private AwsSesEmailDeliveryAdapter awsSesEmailDeliveryAdapter;

    @BeforeEach
    void setUp() {
        sesV2Client = mock(SesV2Client.class);
        awsSesClientProvider = mock(AwsSesClientProvider.class);
        when(awsSesClientProvider.getClient(Region.SA_EAST_1)).thenReturn(sesV2Client);
        awsSesEmailDeliveryAdapter = new AwsSesEmailDeliveryAdapter(
                validProperties(),
                awsSesClientProvider
        );
    }

    @Test
    void sendsTrustedTextAndHtmlMessageWithConfiguredSesControls() {
        when(sesV2Client.sendEmail(any(SendEmailRequest.class))).thenReturn(
                SendEmailResponse.builder().messageId("ses-message-42").build()
        );

        EmailDeliveryResultRecord emailDeliveryResultRecord =
                awsSesEmailDeliveryAdapter.deliver(
                        "HOUSEHOST",
                        "HOUSEHOST_TRANSACTIONAL",
                        messageRecord()
                );

        assertEquals(EmailDeliveryOutcome.ACCEPTED, emailDeliveryResultRecord.outcome());
        assertEquals("ses-message-42", emailDeliveryResultRecord.providerMessageId());
        ArgumentCaptor<SendEmailRequest> sendEmailRequestArgumentCaptor =
                ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesV2Client).sendEmail(sendEmailRequestArgumentCaptor.capture());
        SendEmailRequest sendEmailRequest = sendEmailRequestArgumentCaptor.getValue();
        assertEquals("no-reply@example.com", sendEmailRequest.fromEmailAddress());
        assertEquals("support@example.com", sendEmailRequest.replyToAddresses().getFirst());
        assertEquals("househost-transactional", sendEmailRequest.configurationSetName());
        assertEquals("guest@example.com", sendEmailRequest.destination().toAddresses().getFirst());
        assertEquals(
                "Request received",
                sendEmailRequest.content().simple().subject().data()
        );
        assertEquals("UTF-8", sendEmailRequest.content().simple().subject().charset());
        assertEquals(
                "We received your request.",
                sendEmailRequest.content().simple().body().text().data()
        );
        assertEquals(
                "<p>We received your request.</p>",
                sendEmailRequest.content().simple().body().html().data()
        );
    }

    @Test
    void rejectsUnauthorizedSourceBeforeCallingSes() {
        EmailDeliveryResultRecord emailDeliveryResultRecord =
                awsSesEmailDeliveryAdapter.deliver(
                        "ANOTHER_APPLICATION",
                        "HOUSEHOST_TRANSACTIONAL",
                        messageRecord()
                );

        assertEquals(
                EmailDeliveryOutcome.PERMANENT_FAILURE,
                emailDeliveryResultRecord.outcome()
        );
        assertEquals(
                NotificationFailureCategory.CONFIGURATION,
                emailDeliveryResultRecord.failureCategory()
        );
        verify(awsSesClientProvider, never()).getClient(any(Region.class));
        verify(sesV2Client, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void rejectsUnknownProfileBeforeCallingSes() {
        EmailDeliveryResultRecord emailDeliveryResultRecord =
                awsSesEmailDeliveryAdapter.deliver(
                        "HOUSEHOST",
                        "UNKNOWN_PROFILE",
                        messageRecord()
                );

        assertEquals(
                NotificationFailureCategory.CONFIGURATION,
                emailDeliveryResultRecord.failureCategory()
        );
        verify(awsSesClientProvider, never()).getClient(any(Region.class));
    }

    @Test
    void mapsThrottlingAndProviderUnavailabilityToRetryableFailures() {
        SesV2Exception throttledSesV2Exception = serviceException(
                429,
                "TooManyRequestsException"
        );
        SesV2Exception unavailableSesV2Exception = serviceException(
                503,
                "ServiceUnavailableException"
        );
        when(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(throttledSesV2Exception)
                .thenThrow(unavailableSesV2Exception);

        EmailDeliveryResultRecord throttledEmailDeliveryResultRecord =
                awsSesEmailDeliveryAdapter.deliver(
                        "HOUSEHOST",
                        "HOUSEHOST_TRANSACTIONAL",
                        messageRecord()
                );
        EmailDeliveryResultRecord unavailableEmailDeliveryResultRecord =
                awsSesEmailDeliveryAdapter.deliver(
                        "HOUSEHOST",
                        "HOUSEHOST_TRANSACTIONAL",
                        messageRecord()
                );

        assertEquals(
                NotificationFailureCategory.THROTTLED,
                throttledEmailDeliveryResultRecord.failureCategory()
        );
        assertEquals(
                NotificationFailureCategory.PROVIDER_UNAVAILABLE,
                unavailableEmailDeliveryResultRecord.failureCategory()
        );
        assertEquals(
                EmailDeliveryOutcome.RETRYABLE_FAILURE,
                unavailableEmailDeliveryResultRecord.outcome()
        );
    }

    @Test
    void mapsAuthenticationContentAndNetworkFailures() {
        SesV2Exception authenticationSesV2Exception = serviceException(
                403,
                "AccessDeniedException"
        );
        SesV2Exception messageRejectedSesV2Exception = serviceException(
                400,
                "MessageRejectedException"
        );
        when(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(authenticationSesV2Exception)
                .thenThrow(messageRejectedSesV2Exception)
                .thenThrow(SdkClientException.builder().message("network unavailable").build());

        EmailDeliveryResultRecord authenticationEmailDeliveryResultRecord = deliver();
        EmailDeliveryResultRecord contentEmailDeliveryResultRecord = deliver();
        EmailDeliveryResultRecord networkEmailDeliveryResultRecord = deliver();

        assertEquals(
                NotificationFailureCategory.AUTHENTICATION,
                authenticationEmailDeliveryResultRecord.failureCategory()
        );
        assertEquals(
                NotificationFailureCategory.CONTENT_REJECTED,
                contentEmailDeliveryResultRecord.failureCategory()
        );
        assertEquals(
                NotificationFailureCategory.NETWORK,
                networkEmailDeliveryResultRecord.failureCategory()
        );
        assertEquals(
                EmailDeliveryOutcome.RETRYABLE_FAILURE,
                networkEmailDeliveryResultRecord.outcome()
        );
    }

    @Test
    void treatsMissingProviderMessageIdentifierAsRetryableUnknownFailure() {
        when(sesV2Client.sendEmail(any(SendEmailRequest.class))).thenReturn(
                SendEmailResponse.builder().build()
        );

        EmailDeliveryResultRecord emailDeliveryResultRecord = deliver();

        assertEquals(
                NotificationFailureCategory.UNKNOWN,
                emailDeliveryResultRecord.failureCategory()
        );
        assertEquals(
                EmailDeliveryOutcome.RETRYABLE_FAILURE,
                emailDeliveryResultRecord.outcome()
        );
    }

    private EmailDeliveryResultRecord deliver() {
        return awsSesEmailDeliveryAdapter.deliver(
                "HOUSEHOST",
                "HOUSEHOST_TRANSACTIONAL",
                messageRecord()
        );
    }

    private SesV2Exception serviceException(int statusCode, String errorCode) {
        SesV2Exception sesV2Exception = mock(SesV2Exception.class);
        when(sesV2Exception.statusCode()).thenReturn(statusCode);
        when(sesV2Exception.awsErrorDetails()).thenReturn(
                AwsErrorDetails.builder().errorCode(errorCode).build()
        );
        return sesV2Exception;
    }

    private NotifierDeliveryProfileProperties validProperties() {
        NotifierDeliveryProfileProperties.DeliveryProfileProperties
                deliveryProfileProperties =
                new NotifierDeliveryProfileProperties.DeliveryProfileProperties();
        deliveryProfileProperties.setEnabled(true);
        deliveryProfileProperties.setRegion("sa-east-1");
        deliveryProfileProperties.setSender("no-reply@example.com");
        deliveryProfileProperties.setReplyTo("support@example.com");
        deliveryProfileProperties.setConfigurationSet("househost-transactional");
        deliveryProfileProperties.setPermittedSourceSystems(Set.of("HOUSEHOST"));

        NotifierDeliveryProfileProperties notifierDeliveryProfileProperties =
                new NotifierDeliveryProfileProperties();
        notifierDeliveryProfileProperties.setEnabled(true);
        Map<String, NotifierDeliveryProfileProperties.DeliveryProfileProperties>
                deliveryProfilePropertiesMap = Map.of(
                        "househost-transactional",
                        deliveryProfileProperties
                );
        notifierDeliveryProfileProperties.setProfiles(deliveryProfilePropertiesMap);
        notifierDeliveryProfileProperties.validate();
        return notifierDeliveryProfileProperties;
    }

    private EmailMessageRecord messageRecord() {
        return new EmailMessageRecord(
                "guest@example.com",
                "Request received",
                "We received your request.",
                "<p>We received your request.</p>"
        );
    }
}
