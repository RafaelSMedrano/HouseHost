package com.househost.notifier.adapter.in.http;

import com.househost.notifier.application.port.in.NotificationFeedbackUseCase;
import com.househost.notifier.application.records.NotificationFeedbackRecord;
import com.househost.notifier.domain.model.NotificationEventType;
import com.househost.security.adapter.in.config.SecurityConfig;
import com.househost.security.adapter.in.web.JwtAuthenticationFilter;
import com.househost.security.application.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.messagemanager.sns.model.SnsNotification;
import software.amazon.awssdk.messagemanager.sns.model.SnsSubscriptionConfirmation;

import java.net.URI;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SnsSesFeedbackController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        SnsFeedbackExceptionHandler.class,
        SnsFeedbackRequestSizeFilter.class
})
@EnableConfigurationProperties(NotifierSnsProperties.class)
@TestPropertySource(properties = {
        "househost.notifier.sns.enabled=true",
        "househost.notifier.sns.topic-arn=arn:aws:sns:us-east-1:123456789012:ses-feedback",
        "househost.notifier.sns.region=us-east-1",
        "househost.notifier.sns.max-request-size=1KB"
})
class SnsSesFeedbackControllerTest {

    private static final String TOPIC_ARN =
            "arn:aws:sns:us-east-1:123456789012:ses-feedback";
    private static final String RAW_MESSAGE = "{\"Type\":\"Notification\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SnsMessageAuthenticationService snsMessageAuthenticationService;

    @MockBean
    private SesFeedbackMessageParser sesFeedbackMessageParser;

    @MockBean
    private SnsSubscriptionConfirmer snsSubscriptionConfirmer;

    @MockBean
    private NotificationFeedbackUseCase notificationFeedbackUseCase;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void anonymousSnsNotificationReachesNotifierThroughDedicatedRoute() throws Exception {
        SnsNotification snsNotification = notification(TOPIC_ARN);
        NotificationFeedbackRecord notificationFeedbackRecord = feedbackRecord();
        when(snsMessageAuthenticationService.authenticate(RAW_MESSAGE))
                .thenReturn(snsNotification);
        when(sesFeedbackMessageParser.parse("sns-message-1", "ses-message"))
                .thenReturn(notificationFeedbackRecord);

        mockMvc.perform(post(SnsSesFeedbackController.ENDPOINT_PATH)
                        .secure(true)
                        .header("x-amz-sns-message-type", "Notification")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(RAW_MESSAGE))
                .andExpect(status().isNoContent());

        verify(notificationFeedbackUseCase).processFeedback(notificationFeedbackRecord);
    }

    @Test
    void rejectsInvalidSignatureBeforeNestedSesParsing() throws Exception {
        when(snsMessageAuthenticationService.authenticate(RAW_MESSAGE)).thenThrow(
                SnsFeedbackException.forbidden("Envelope SNS nao autenticado.", null)
        );

        mockMvc.perform(post(SnsSesFeedbackController.ENDPOINT_PATH)
                        .secure(true)
                        .header("x-amz-sns-message-type", "Notification")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(RAW_MESSAGE))
                .andExpect(status().isForbidden());

        verify(sesFeedbackMessageParser, never()).parse(any(), any());
        verify(notificationFeedbackUseCase, never()).processFeedback(any());
    }

    @Test
    void rejectsAuthenticatedMessageFromUnexpectedTopic() throws Exception {
        when(snsMessageAuthenticationService.authenticate(RAW_MESSAGE))
                .thenReturn(notification(
                        "arn:aws:sns:us-east-1:123456789012:other-topic"
                ));

        mockMvc.perform(post(SnsSesFeedbackController.ENDPOINT_PATH)
                        .secure(true)
                        .header("x-amz-sns-message-type", "Notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(RAW_MESSAGE))
                .andExpect(status().isForbidden());

        verify(notificationFeedbackUseCase, never()).processFeedback(any());
    }

    @Test
    void rejectsInsecureTransportAndUnsupportedContentType() throws Exception {
        mockMvc.perform(post(SnsSesFeedbackController.ENDPOINT_PATH)
                        .header("x-amz-sns-message-type", "Notification")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(RAW_MESSAGE))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(SnsSesFeedbackController.ENDPOINT_PATH)
                        .secure(true)
                        .header("x-amz-sns-message-type", "Notification")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(RAW_MESSAGE))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void rejectsRequestLargerThanConfiguredLimit() throws Exception {
        mockMvc.perform(post(SnsSesFeedbackController.ENDPOINT_PATH)
                        .secure(true)
                        .header("x-amz-sns-message-type", "Notification")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("x".repeat(1025)))
                .andExpect(status().isPayloadTooLarge());

        verify(snsMessageAuthenticationService, never()).authenticate(any());
    }

    @Test
    void acceptsAuthenticatedSubscriptionWithoutConfirmingWhenControlIsDisabled()
            throws Exception {
        SnsSubscriptionConfirmation snsSubscriptionConfirmation =
                SnsSubscriptionConfirmation.builder()
                        .messageId("sns-subscription-1")
                        .message("confirm")
                        .topicArn(TOPIC_ARN)
                        .subscribeUrl(URI.create(
                                "https://sns.us-east-1.amazonaws.com/?Action=ConfirmSubscription"
                        ))
                        .build();
        when(snsMessageAuthenticationService.authenticate(RAW_MESSAGE))
                .thenReturn(snsSubscriptionConfirmation);

        mockMvc.perform(post(SnsSesFeedbackController.ENDPOINT_PATH)
                        .secure(true)
                        .header("x-amz-sns-message-type", "SubscriptionConfirmation")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(RAW_MESSAGE))
                .andExpect(status().isAccepted());

        verify(snsSubscriptionConfirmer, never()).confirm(any());
    }

    private SnsNotification notification(String topicArn) {
        return SnsNotification.builder()
                .messageId("sns-message-1")
                .message("ses-message")
                .topicArn(topicArn)
                .build();
    }

    private NotificationFeedbackRecord feedbackRecord() {
        Instant occurredAt = Instant.parse("2026-08-21T12:00:00Z");
        return new NotificationFeedbackRecord(
                "sns-message-1",
                null,
                "ses-message-1",
                NotificationEventType.DELIVERY,
                null,
                null,
                null,
                null,
                occurredAt,
                occurredAt.plusSeconds(1),
                null
        );
    }
}
