package com.househost.notifier.adapter.in.http;

import com.househost.notifier.application.port.in.NotificationFeedbackUseCase;
import com.househost.notifier.application.records.NotificationFeedbackRecord;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.messagemanager.sns.model.SnsMessage;
import software.amazon.awssdk.messagemanager.sns.model.SnsMessageType;
import software.amazon.awssdk.messagemanager.sns.model.SnsNotification;
import software.amazon.awssdk.messagemanager.sns.model.SnsSubscriptionConfirmation;

@RestController
@RequestMapping(SnsSesFeedbackController.ENDPOINT_PATH)
@ConditionalOnProperty(
        name = "househost.notifier.sns.enabled",
        havingValue = "true"
)
public class SnsSesFeedbackController {

    public static final String ENDPOINT_PATH = "/notifier/provider-feedback/sns";

    private final SnsMessageAuthenticationService snsMessageAuthenticationService;
    private final SesFeedbackMessageParser sesFeedbackMessageParser;
    private final SnsSubscriptionConfirmer snsSubscriptionConfirmer;
    private final NotificationFeedbackUseCase notificationFeedbackUseCase;
    private final NotifierSnsProperties notifierSnsProperties;

    public SnsSesFeedbackController(
            SnsMessageAuthenticationService snsMessageAuthenticationService,
            SesFeedbackMessageParser sesFeedbackMessageParser,
            SnsSubscriptionConfirmer snsSubscriptionConfirmer,
            NotificationFeedbackUseCase notificationFeedbackUseCase,
            NotifierSnsProperties notifierSnsProperties
    ) {
        this.snsMessageAuthenticationService = snsMessageAuthenticationService;
        this.sesFeedbackMessageParser = sesFeedbackMessageParser;
        this.snsSubscriptionConfirmer = snsSubscriptionConfirmer;
        this.notificationFeedbackUseCase = notificationFeedbackUseCase;
        this.notifierSnsProperties = notifierSnsProperties;
    }

    @PostMapping(
            consumes = {
                    MediaType.TEXT_PLAIN_VALUE,
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    public ResponseEntity<Void> receive(
            @RequestBody String rawMessage,
            @RequestHeader(name = "x-amz-sns-message-type", required = false)
                    String transportMessageType,
            HttpServletRequest httpServletRequest
    ) {
        requireSecureTransport(httpServletRequest);
        SnsMessage snsMessage = snsMessageAuthenticationService.authenticate(rawMessage);
        validateAuthenticatedEnvelope(snsMessage, transportMessageType);

        if (snsMessage.type() == SnsMessageType.NOTIFICATION) {
            processNotification((SnsNotification) snsMessage);
            return ResponseEntity.noContent().build();
        }
        if (snsMessage.type() == SnsMessageType.SUBSCRIPTION_CONFIRMATION) {
            return processSubscriptionConfirmation(
                    (SnsSubscriptionConfirmation) snsMessage
            );
        }
        if (snsMessage.type() == SnsMessageType.UNSUBSCRIBE_CONFIRMATION) {
            return ResponseEntity.noContent().build();
        }
        throw SnsFeedbackException.forbidden("Tipo de mensagem SNS invalido.", null);
    }

    private void processNotification(SnsNotification snsNotification) {
        NotificationFeedbackRecord notificationFeedbackRecord =
                sesFeedbackMessageParser.parse(
                        snsNotification.messageId(),
                        snsNotification.message()
                );
        notificationFeedbackUseCase.processFeedback(notificationFeedbackRecord);
    }

    private ResponseEntity<Void> processSubscriptionConfirmation(
            SnsSubscriptionConfirmation snsSubscriptionConfirmation
    ) {
        if (!notifierSnsProperties.isSubscriptionConfirmationEnabled()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        snsSubscriptionConfirmer.confirm(snsSubscriptionConfirmation.subscribeUrl());
        return ResponseEntity.noContent().build();
    }

    private void requireSecureTransport(HttpServletRequest httpServletRequest) {
        if (notifierSnsProperties.isRequireHttps() && !httpServletRequest.isSecure()) {
            throw SnsFeedbackException.forbidden("Transporte HTTPS obrigatorio.", null);
        }
    }

    private void validateAuthenticatedEnvelope(
            SnsMessage snsMessage,
            String transportMessageType
    ) {
        if (!notifierSnsProperties.getTopicArn().equals(snsMessage.topicArn())) {
            throw SnsFeedbackException.forbidden("Topico SNS inesperado.", null);
        }
        if (transportMessageType == null
                || !snsMessage.type().toString().equals(transportMessageType)) {
            throw SnsFeedbackException.forbidden(
                    "Tipo do transporte SNS diverge do envelope.",
                    null
            );
        }
    }
}
