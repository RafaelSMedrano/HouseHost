package com.househost.notifier.adapter.in.http;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

@ConfigurationProperties(prefix = "househost.notifier.sns")
public class NotifierSnsProperties {

    private boolean enabled;
    private String topicArn;
    private String region;
    private boolean subscriptionConfirmationEnabled;
    private boolean requireHttps = true;
    private DataSize maxRequestSize = DataSize.ofKilobytes(64);
    private Duration subscriptionConfirmationTimeout = Duration.ofSeconds(5);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTopicArn() {
        return topicArn;
    }

    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isSubscriptionConfirmationEnabled() {
        return subscriptionConfirmationEnabled;
    }

    public void setSubscriptionConfirmationEnabled(
            boolean subscriptionConfirmationEnabled
    ) {
        this.subscriptionConfirmationEnabled = subscriptionConfirmationEnabled;
    }

    public boolean isRequireHttps() {
        return requireHttps;
    }

    public void setRequireHttps(boolean requireHttps) {
        this.requireHttps = requireHttps;
    }

    public DataSize getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(DataSize maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public Duration getSubscriptionConfirmationTimeout() {
        return subscriptionConfirmationTimeout;
    }

    public void setSubscriptionConfirmationTimeout(
            Duration subscriptionConfirmationTimeout
    ) {
        this.subscriptionConfirmationTimeout = subscriptionConfirmationTimeout;
    }

    public void validate() {
        if (!enabled) {
            return;
        }
        if (topicArn == null || topicArn.isBlank()) {
            throw new IllegalStateException("Topico SNS do notifier e obrigatorio.");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("Regiao SNS do notifier e obrigatoria.");
        }
        String[] topicArnPartArray = topicArn.split(":", 6);
        if (topicArnPartArray.length != 6
                || !"sns".equals(topicArnPartArray[2])
                || !region.equals(topicArnPartArray[3])) {
            throw new IllegalStateException(
                    "Topico SNS deve pertencer a regiao configurada."
            );
        }
        if (maxRequestSize == null
                || maxRequestSize.toBytes() <= 0
                || maxRequestSize.toBytes() > Integer.MAX_VALUE) {
            throw new IllegalStateException("Limite da requisicao SNS e invalido.");
        }
        if (subscriptionConfirmationTimeout == null
                || subscriptionConfirmationTimeout.isNegative()
                || subscriptionConfirmationTimeout.isZero()) {
            throw new IllegalStateException(
                    "Timeout da confirmacao SNS deve ser positivo."
            );
        }
    }
}
