package com.househost.notifier.adapter.in.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.messagemanager.sns.SnsMessageManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SnsMessageAuthenticationServiceTest {

    private final SnsMessageManager snsMessageManager = mock(SnsMessageManager.class);
    private final SnsMessageAuthenticationService snsMessageAuthenticationService =
            new SnsMessageAuthenticationService(
                    snsMessageManager,
                    new ObjectMapper()
            );

    @Test
    void rejectsMalformedEnvelopeBeforeSignatureValidation() {
        SnsFeedbackException snsFeedbackException = assertThrows(
                SnsFeedbackException.class,
                () -> snsMessageAuthenticationService.authenticate("not-json")
        );

        assertEquals(400, snsFeedbackException.getHttpStatus().value());
        verify(snsMessageManager, never()).parseMessage("not-json");
    }

    @Test
    void rejectsEnvelopeWhenOfficialAwsValidatorRejectsSignatureOrCertificate() {
        String rawMessage = "{\"Type\":\"Notification\"}";
        when(snsMessageManager.parseMessage(rawMessage)).thenThrow(
                SdkClientException.builder().message("invalid signature").build()
        );

        SnsFeedbackException snsFeedbackException = assertThrows(
                SnsFeedbackException.class,
                () -> snsMessageAuthenticationService.authenticate(rawMessage)
        );

        assertEquals(403, snsFeedbackException.getHttpStatus().value());
    }
}
