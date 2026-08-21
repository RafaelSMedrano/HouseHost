package com.househost.notifier.adapter.in.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.messagemanager.sns.SnsMessageManager;
import software.amazon.awssdk.regions.Region;

import java.net.http.HttpClient;
import java.time.Clock;

@Configuration
@EnableConfigurationProperties(NotifierSnsProperties.class)
@ConditionalOnProperty(
        name = "househost.notifier.sns.enabled",
        havingValue = "true"
)
public class NotifierSnsConfiguration {

    @Bean(destroyMethod = "close")
    public SdkHttpClient snsCertificateHttpClient(
            NotifierSnsProperties notifierSnsProperties
    ) {
        notifierSnsProperties.validate();
        return UrlConnectionHttpClient.builder()
                .connectionTimeout(
                        notifierSnsProperties.getSubscriptionConfirmationTimeout()
                )
                .socketTimeout(
                        notifierSnsProperties.getSubscriptionConfirmationTimeout()
                )
                .build();
    }

    @Bean(destroyMethod = "close")
    public SnsMessageManager snsMessageManager(
            NotifierSnsProperties notifierSnsProperties,
            SdkHttpClient snsCertificateHttpClient
    ) {
        return SnsMessageManager.builder()
                .region(Region.of(notifierSnsProperties.getRegion()))
                .httpClient(snsCertificateHttpClient)
                .build();
    }

    @Bean
    public SnsMessageAuthenticationService snsMessageAuthenticationService(
            SnsMessageManager snsMessageManager,
            ObjectMapper objectMapper
    ) {
        return new SnsMessageAuthenticationService(snsMessageManager, objectMapper);
    }

    @Bean
    public SesFeedbackMessageParser sesFeedbackMessageParser(ObjectMapper objectMapper) {
        return new SesFeedbackMessageParser(objectMapper, Clock.systemUTC());
    }

    @Bean
    public SnsSubscriptionConfirmer snsSubscriptionConfirmer(
            NotifierSnsProperties notifierSnsProperties
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(
                        notifierSnsProperties.getSubscriptionConfirmationTimeout()
                )
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        return new SnsSubscriptionConfirmer(httpClient, notifierSnsProperties);
    }
}
