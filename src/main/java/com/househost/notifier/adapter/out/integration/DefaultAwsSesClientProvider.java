package com.househost.notifier.adapter.out.integration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultAwsSesClientProvider implements AwsSesClientProvider, AutoCloseable {

    private final Duration apiCallTimeout;
    private final Duration apiCallAttemptTimeout;
    private final Map<Region, SesV2Client> sesV2ClientMap = new ConcurrentHashMap<>();

    public DefaultAwsSesClientProvider(
            Duration apiCallTimeout,
            Duration apiCallAttemptTimeout
    ) {
        this.apiCallTimeout = apiCallTimeout;
        this.apiCallAttemptTimeout = apiCallAttemptTimeout;
    }

    @Override
    public SesV2Client getClient(Region region) {
        return sesV2ClientMap.computeIfAbsent(region, this::createClient);
    }

    @Override
    public void close() {
        sesV2ClientMap.values().forEach(SesV2Client::close);
        sesV2ClientMap.clear();
    }

    private SesV2Client createClient(Region region) {
        ClientOverrideConfiguration clientOverrideConfiguration =
                ClientOverrideConfiguration.builder()
                        .apiCallTimeout(apiCallTimeout)
                        .apiCallAttemptTimeout(apiCallAttemptTimeout)
                        .build();
        return SesV2Client.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .overrideConfiguration(clientOverrideConfiguration)
                .build();
    }
}
