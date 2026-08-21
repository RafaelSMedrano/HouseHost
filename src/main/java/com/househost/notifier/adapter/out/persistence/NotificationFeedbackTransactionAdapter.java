package com.househost.notifier.adapter.out.persistence;

import com.househost.notifier.application.port.out.NotificationFeedbackTransactionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class NotificationFeedbackTransactionAdapter
        implements NotificationFeedbackTransactionPort {

    private final TransactionTemplate transactionTemplate;

    public NotificationFeedbackTransactionAdapter(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void execute(Runnable notificationFeedbackOperation) {
        transactionTemplate.executeWithoutResult(
                transactionStatus -> notificationFeedbackOperation.run()
        );
    }
}
