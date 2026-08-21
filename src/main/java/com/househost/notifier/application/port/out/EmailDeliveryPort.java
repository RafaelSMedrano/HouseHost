package com.househost.notifier.application.port.out;

import com.househost.notifier.application.records.EmailDeliveryResultRecord;
import com.househost.notifier.application.records.EmailMessageRecord;

public interface EmailDeliveryPort {

    EmailDeliveryResultRecord deliver(
            String sourceSystem,
            String deliveryProfileKey,
            EmailMessageRecord emailMessageRecord
    );
}
