package com.househost.notifier.adapter.out.integration;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultAwsSesClientProviderTest {

    @Test
    void createsAndCachesSynchronousClientByRegion() {
        DefaultAwsSesClientProvider defaultAwsSesClientProvider =
                new DefaultAwsSesClientProvider(
                        Duration.ofSeconds(10),
                        Duration.ofSeconds(5)
                );
        try {
            SesV2Client firstSesV2Client = defaultAwsSesClientProvider.getClient(
                    Region.SA_EAST_1
            );
            SesV2Client secondSesV2Client = defaultAwsSesClientProvider.getClient(
                    Region.SA_EAST_1
            );

            assertSame(firstSesV2Client, secondSesV2Client);
        } finally {
            defaultAwsSesClientProvider.close();
        }
    }
}
