package com.househost.notifier.adapter.out.integration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

public interface AwsSesClientProvider {

    SesV2Client getClient(Region region);
}
