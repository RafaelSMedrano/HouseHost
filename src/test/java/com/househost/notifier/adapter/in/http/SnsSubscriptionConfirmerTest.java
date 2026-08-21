package com.househost.notifier.adapter.in.http;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SnsSubscriptionConfirmerTest {

    private final HttpClient httpClient = mock(HttpClient.class);
    private final NotifierSnsProperties notifierSnsProperties = properties();
    private final SnsSubscriptionConfirmer snsSubscriptionConfirmer =
            new SnsSubscriptionConfirmer(httpClient, notifierSnsProperties);

    @Test
    void confirmsOnlyAuthenticatedRegionalHttpsUrl() throws Exception {
        HttpResponse<Void> httpResponse = mock(HttpResponse.class);
        doReturn(200).when(httpResponse).statusCode();
        doReturn(httpResponse).when(httpClient).send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        );
        URI subscribeUri = URI.create(
                "https://sns.us-east-1.amazonaws.com/"
                        + "?Action=ConfirmSubscription&Token=safe"
        );

        snsSubscriptionConfirmer.confirm(subscribeUri);

        verify(httpClient).send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        );
    }

    @Test
    void rejectsUntrustedConfirmationUrlWithoutNetworkCall() throws Exception {
        URI subscribeUri = URI.create(
                "https://attacker.invalid/?Action=ConfirmSubscription"
        );

        assertThrows(
                SnsFeedbackException.class,
                () -> snsSubscriptionConfirmer.confirm(subscribeUri)
        );

        verify(httpClient, never()).send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        );
    }

    private NotifierSnsProperties properties() {
        NotifierSnsProperties properties = new NotifierSnsProperties();
        properties.setRegion("us-east-1");
        return properties;
    }
}
