package com.househost.notifier.adapter.in.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.messagemanager.sns.SnsMessageManager;

import static org.assertj.core.api.Assertions.assertThat;

class NotifierSnsConfigurationTest {

    private final ApplicationContextRunner applicationContextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(NotifierSnsConfiguration.class)
                    .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void createsOfficialValidatorOnlyForCompleteRegionalConfiguration() {
        applicationContextRunner
                .withPropertyValues(
                        "househost.notifier.sns.enabled=true",
                        "househost.notifier.sns.region=sa-east-1",
                        "househost.notifier.sns.topic-arn="
                                + "arn:aws:sns:sa-east-1:123456789012:ses-feedback"
                )
                .run(applicationContext -> {
                    assertThat(applicationContext).hasSingleBean(SnsMessageManager.class);
                    assertThat(applicationContext).hasSingleBean(
                            SnsMessageAuthenticationService.class
                    );
                });
    }

    @Test
    void failsStartupWhenTopicAndConfiguredRegionDiverge() {
        applicationContextRunner
                .withPropertyValues(
                        "househost.notifier.sns.enabled=true",
                        "househost.notifier.sns.region=sa-east-1",
                        "househost.notifier.sns.topic-arn="
                                + "arn:aws:sns:us-east-1:123456789012:ses-feedback"
                )
                .run(applicationContext -> assertThat(applicationContext).hasFailed());
    }

    @Test
    void createsNoProviderValidatorWhenSnsIsDisabled() {
        applicationContextRunner
                .withPropertyValues("househost.notifier.sns.enabled=false")
                .run(applicationContext -> assertThat(applicationContext)
                        .doesNotHaveBean(SnsMessageManager.class));
    }
}
