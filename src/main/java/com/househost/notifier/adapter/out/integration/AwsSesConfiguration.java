package com.househost.notifier.adapter.out.integration;

import com.househost.notifier.application.port.out.EmailDeliveryPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        prefix = "househost.notifier.ses",
        name = "enabled",
        havingValue = "true"
)
@EnableConfigurationProperties(NotifierDeliveryProfileProperties.class)
public class AwsSesConfiguration {

    @Bean(destroyMethod = "close")
    public AwsSesClientProvider awsSesClientProvider(
            NotifierDeliveryProfileProperties notifierDeliveryProfileProperties
    ) {
        return new DefaultAwsSesClientProvider(
                notifierDeliveryProfileProperties.getApiCallTimeout(),
                notifierDeliveryProfileProperties.getApiCallAttemptTimeout()
        );
    }

    @Bean
    public EmailDeliveryPort emailDeliveryPort(
            NotifierDeliveryProfileProperties notifierDeliveryProfileProperties,
            AwsSesClientProvider awsSesClientProvider
    ) {
        return new AwsSesEmailDeliveryAdapter(
                notifierDeliveryProfileProperties,
                awsSesClientProvider
        );
    }
}
