package com.househost.notifier.adapter.out.integration;

import com.househost.notifier.application.port.out.EmailDeliveryPort;
import com.househost.notifier.application.records.EmailDeliveryResultRecord;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.domain.model.NotificationFailureCategory;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class AwsSesEmailDeliveryAdapter implements EmailDeliveryPort {

    private static final String UTF_8 = "UTF-8";
    private static final Set<String> THROTTLED_ERROR_CODE_SET = Set.of(
            "TOOMANYREQUESTSEXCEPTION",
            "LIMITEXCEEDEDEXCEPTION"
    );
    private static final Set<String> CONFIGURATION_ERROR_CODE_SET = Set.of(
            "ACCOUNTSUSPENDEDEXCEPTION",
            "MAILFROMDOMAINNOTVERIFIEDEXCEPTION",
            "NOTFOUNDEXCEPTION",
            "SENDINGPAUSEDEXCEPTION"
    );

    private final NotifierDeliveryProfileProperties notifierDeliveryProfileProperties;
    private final AwsSesClientProvider awsSesClientProvider;

    public AwsSesEmailDeliveryAdapter(
            NotifierDeliveryProfileProperties notifierDeliveryProfileProperties,
            AwsSesClientProvider awsSesClientProvider
    ) {
        this.notifierDeliveryProfileProperties = notifierDeliveryProfileProperties;
        this.awsSesClientProvider = awsSesClientProvider;
    }

    @Override
    public EmailDeliveryResultRecord deliver(
            String sourceSystem,
            String deliveryProfileKey,
            EmailMessageRecord emailMessageRecord
    ) {
        Optional<NotifierDeliveryProfileProperties.DeliveryProfileProperties>
                deliveryProfilePropertiesOptional =
                notifierDeliveryProfileProperties.resolveEnabledProfileOptional(
                        deliveryProfileKey,
                        sourceSystem
                );
        if (deliveryProfilePropertiesOptional.isEmpty()) {
            return EmailDeliveryResultRecord.permanentFailure(
                    NotificationFailureCategory.CONFIGURATION
            );
        }

        NotifierDeliveryProfileProperties.DeliveryProfileProperties
                deliveryProfileProperties = deliveryProfilePropertiesOptional.orElseThrow();
        SendEmailRequest sendEmailRequest = buildRequest(
                deliveryProfileProperties,
                emailMessageRecord
        );
        try {
            SesV2Client sesV2Client = awsSesClientProvider.getClient(
                    Region.of(deliveryProfileProperties.getRegion())
            );
            SendEmailResponse sendEmailResponse = sesV2Client.sendEmail(sendEmailRequest);
            if (sendEmailResponse.messageId() == null
                    || sendEmailResponse.messageId().isBlank()) {
                return EmailDeliveryResultRecord.retryableFailure(
                        NotificationFailureCategory.UNKNOWN
                );
            }
            return EmailDeliveryResultRecord.accepted(sendEmailResponse.messageId());
        } catch (SesV2Exception sesV2Exception) {
            return classifyServiceFailure(sesV2Exception);
        } catch (SdkClientException sdkClientException) {
            return EmailDeliveryResultRecord.retryableFailure(
                    NotificationFailureCategory.NETWORK
            );
        }
    }

    private SendEmailRequest buildRequest(
            NotifierDeliveryProfileProperties.DeliveryProfileProperties
                    deliveryProfileProperties,
            EmailMessageRecord emailMessageRecord
    ) {
        Content subjectContent = Content.builder()
                .charset(UTF_8)
                .data(emailMessageRecord.subject())
                .build();
        Body body = Body.builder()
                .text(Content.builder()
                        .charset(UTF_8)
                        .data(emailMessageRecord.textBody())
                        .build())
                .html(Content.builder()
                        .charset(UTF_8)
                        .data(emailMessageRecord.htmlBody())
                        .build())
                .build();
        Message message = Message.builder()
                .subject(subjectContent)
                .body(body)
                .build();
        SendEmailRequest.Builder sendEmailRequestBuilder = SendEmailRequest.builder()
                .fromEmailAddress(deliveryProfileProperties.getSender())
                .destination(Destination.builder()
                        .toAddresses(emailMessageRecord.recipient())
                        .build())
                .content(EmailContent.builder().simple(message).build())
                .configurationSetName(deliveryProfileProperties.getConfigurationSet());
        if (deliveryProfileProperties.getReplyTo() != null
                && !deliveryProfileProperties.getReplyTo().isBlank()) {
            sendEmailRequestBuilder.replyToAddresses(
                    deliveryProfileProperties.getReplyTo()
            );
        }
        return sendEmailRequestBuilder.build();
    }

    private EmailDeliveryResultRecord classifyServiceFailure(
            SesV2Exception sesV2Exception
    ) {
        int statusCode = sesV2Exception.statusCode();
        String errorCode = normalizeErrorCode(sesV2Exception);
        if (statusCode == 429 || THROTTLED_ERROR_CODE_SET.contains(errorCode)) {
            return EmailDeliveryResultRecord.retryableFailure(
                    NotificationFailureCategory.THROTTLED
            );
        }
        if (statusCode == 408) {
            return EmailDeliveryResultRecord.retryableFailure(
                    NotificationFailureCategory.NETWORK
            );
        }
        if (statusCode >= 500) {
            return EmailDeliveryResultRecord.retryableFailure(
                    NotificationFailureCategory.PROVIDER_UNAVAILABLE
            );
        }
        if (statusCode == 401 || statusCode == 403
                || "ACCESSDENIEDEXCEPTION".equals(errorCode)) {
            return EmailDeliveryResultRecord.permanentFailure(
                    NotificationFailureCategory.AUTHENTICATION
            );
        }
        if (CONFIGURATION_ERROR_CODE_SET.contains(errorCode)) {
            return EmailDeliveryResultRecord.permanentFailure(
                    NotificationFailureCategory.CONFIGURATION
            );
        }
        if ("MESSAGEREJECTEDEXCEPTION".equals(errorCode)) {
            return EmailDeliveryResultRecord.permanentFailure(
                    NotificationFailureCategory.CONTENT_REJECTED
            );
        }
        if (statusCode >= 400 && statusCode < 500) {
            return EmailDeliveryResultRecord.permanentFailure(
                    NotificationFailureCategory.INVALID_REQUEST
            );
        }
        return EmailDeliveryResultRecord.retryableFailure(
                NotificationFailureCategory.UNKNOWN
        );
    }

    private String normalizeErrorCode(SesV2Exception sesV2Exception) {
        if (sesV2Exception.awsErrorDetails() == null
                || sesV2Exception.awsErrorDetails().errorCode() == null) {
            return "";
        }
        return sesV2Exception.awsErrorDetails()
                .errorCode()
                .replace("_", "")
                .replace("-", "")
                .toUpperCase(Locale.ROOT);
    }
}
