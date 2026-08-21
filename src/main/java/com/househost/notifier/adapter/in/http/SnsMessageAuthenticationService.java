package com.househost.notifier.adapter.in.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.messagemanager.sns.SnsMessageManager;
import software.amazon.awssdk.messagemanager.sns.model.SnsMessage;

public class SnsMessageAuthenticationService {

    private final SnsMessageManager snsMessageManager;
    private final ObjectMapper objectMapper;

    public SnsMessageAuthenticationService(
            SnsMessageManager snsMessageManager,
            ObjectMapper objectMapper
    ) {
        this.snsMessageManager = snsMessageManager;
        this.objectMapper = objectMapper;
    }

    public SnsMessage authenticate(String rawMessage) {
        validateJsonSyntax(rawMessage);
        try {
            return snsMessageManager.parseMessage(rawMessage);
        } catch (RuntimeException exception) {
            throw SnsFeedbackException.forbidden(
                    "Envelope SNS nao autenticado.",
                    exception
            );
        }
    }

    private void validateJsonSyntax(String rawMessage) {
        try {
            objectMapper.readTree(rawMessage);
        } catch (JsonProcessingException exception) {
            throw SnsFeedbackException.malformed(
                    "Envelope SNS malformado.",
                    exception
            );
        }
    }
}
