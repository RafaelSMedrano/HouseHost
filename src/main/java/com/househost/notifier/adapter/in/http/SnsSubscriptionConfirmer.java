package com.househost.notifier.adapter.in.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SnsSubscriptionConfirmer {

    private final HttpClient httpClient;
    private final NotifierSnsProperties notifierSnsProperties;

    public SnsSubscriptionConfirmer(
            HttpClient httpClient,
            NotifierSnsProperties notifierSnsProperties
    ) {
        this.httpClient = httpClient;
        this.notifierSnsProperties = notifierSnsProperties;
    }

    public void confirm(URI subscribeUri) {
        validateSubscribeUri(subscribeUri);
        HttpRequest httpRequest = HttpRequest.newBuilder(subscribeUri)
                .timeout(notifierSnsProperties.getSubscriptionConfirmationTimeout())
                .GET()
                .build();
        try {
            HttpResponse<Void> httpResponse = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.discarding()
            );
            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
                throw SnsFeedbackException.unavailable(
                        "Confirmacao da assinatura SNS falhou.",
                        null
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw SnsFeedbackException.unavailable(
                    "Confirmacao da assinatura SNS foi interrompida.",
                    exception
            );
        } catch (IOException exception) {
            throw SnsFeedbackException.unavailable(
                    "Confirmacao da assinatura SNS ficou indisponivel.",
                    exception
            );
        }
    }

    private void validateSubscribeUri(URI subscribeUri) {
        String expectedAwsHost = "sns."
                + notifierSnsProperties.getRegion()
                + ".amazonaws.com";
        String expectedChinaHost = expectedAwsHost + ".cn";
        boolean trustedHost = expectedAwsHost.equalsIgnoreCase(subscribeUri.getHost())
                || expectedChinaHost.equalsIgnoreCase(subscribeUri.getHost());
        if (!"https".equalsIgnoreCase(subscribeUri.getScheme())
                || !trustedHost
                || subscribeUri.getPort() != -1
                || subscribeUri.getUserInfo() != null
                || subscribeUri.getFragment() != null) {
            throw SnsFeedbackException.forbidden(
                    "URL de confirmacao SNS nao confiavel.",
                    null
            );
        }
    }
}
