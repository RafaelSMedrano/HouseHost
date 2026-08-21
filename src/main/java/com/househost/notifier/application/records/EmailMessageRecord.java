package com.househost.notifier.application.records;

import com.househost.notifier.domain.model.NotificationIntent;

public record EmailMessageRecord(
        String recipient,
        String subject,
        String textBody,
        String htmlBody
) {

    public EmailMessageRecord {
        recipient = NotificationRecordValidation.requireEmail(
                recipient,
                NotificationIntent.MAX_RECIPIENT_LENGTH
        );
        subject = NotificationRecordValidation.requireHeader(
                subject,
                NotificationIntent.MAX_SUBJECT_LENGTH,
                "Assunto"
        );
        textBody = NotificationRecordValidation.requireContent(
                textBody,
                NotificationIntent.MAX_TEXT_BODY_LENGTH,
                "Corpo textual"
        );
        htmlBody = NotificationRecordValidation.requireContent(
                htmlBody,
                NotificationIntent.MAX_HTML_BODY_LENGTH,
                "Corpo HTML"
        );
    }
}
