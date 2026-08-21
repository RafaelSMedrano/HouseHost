package com.househost.notifier.application.port.out;

public interface NotificationFeedbackTransactionPort {

    void execute(Runnable notificationFeedbackOperation);
}
