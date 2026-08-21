package com.househost.publicapi.application.port.out;

import com.househost.publicapi.application.records.PublicBookingNotificationRecord;

public interface PublicBookingNotificationPort {

    void requestNotifications(
            PublicBookingNotificationRecord publicBookingNotificationRecord
    );
}
