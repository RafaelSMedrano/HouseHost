package com.househost.notifier.application.port.in;

import com.househost.notifier.application.records.NotificationFeedbackRecord;

public interface NotificationFeedbackUseCase {

    void processFeedback(NotificationFeedbackRecord notificationFeedbackRecord);
}
