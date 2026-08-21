package com.househost.notifier.adapter.out.integration;

import com.househost.notifier.application.port.out.EmailDeliveryPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AwsSesConfigurationTest {

    private final ApplicationContextRunner applicationContextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(AwsSesConfiguration.class);

    @Test
    void createsAdapterOnlyForCompleteEnabledConfiguration() {
        applicationContextRunner
                .withPropertyValues(
                        "househost.notifier.ses.enabled=true",
                        "househost.notifier.ses.profiles.transactional.enabled=true",
                        "househost.notifier.ses.profiles.transactional.region=sa-east-1",
                        "househost.notifier.ses.profiles.transactional.sender=no-reply@example.com",
                        "househost.notifier.ses.profiles.transactional.configuration-set=transactional",
                        "househost.notifier.ses.profiles.transactional.permitted-source-systems=HOUSEHOST"
                )
                .run(applicationContext -> {
                    assertThat(applicationContext).hasSingleBean(EmailDeliveryPort.class);
                    assertThat(applicationContext).hasSingleBean(AwsSesClientProvider.class);
                });
    }

    @Test
    void failsStartupForEnabledSesWithoutEnabledProfile() {
        applicationContextRunner
                .withPropertyValues("househost.notifier.ses.enabled=true")
                .run(applicationContext -> assertThat(applicationContext).hasFailed());
    }

    @Test
    void createsNoProviderBeansWhenSesIsDisabled() {
        applicationContextRunner
                .withPropertyValues("househost.notifier.ses.enabled=false")
                .run(applicationContext -> {
                    assertThat(applicationContext).doesNotHaveBean(EmailDeliveryPort.class);
                    assertThat(applicationContext).doesNotHaveBean(AwsSesClientProvider.class);
                });
    }
}
