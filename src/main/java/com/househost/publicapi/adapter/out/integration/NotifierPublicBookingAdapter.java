package com.househost.publicapi.adapter.out.integration;

import com.househost.notifier.application.port.in.NotificationRequestUseCase;
import com.househost.notifier.application.records.EmailMessageRecord;
import com.househost.notifier.application.records.NotificationRequestRecord;
import com.househost.notifier.domain.model.NotificationChannel;
import com.househost.publicapi.application.port.out.PublicBookingNotificationPort;
import com.househost.publicapi.application.records.PublicBookingNotificationRecord;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class NotifierPublicBookingAdapter implements PublicBookingNotificationPort {

    private static final String SOURCE_SYSTEM = "HOUSEHOST";
    private static final String GUEST_NOTIFICATION_TYPE = "GUEST_REQUEST_RECEIVED";
    private static final String MANAGEMENT_NOTIFICATION_TYPE = "MANAGEMENT_NEW_REQUEST";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificationRequestUseCase notificationRequestUseCase;
    private final PublicBookingNotificationProperties publicBookingNotificationProperties;

    public NotifierPublicBookingAdapter(
            NotificationRequestUseCase notificationRequestUseCase,
            PublicBookingNotificationProperties publicBookingNotificationProperties
    ) {
        this.notificationRequestUseCase = notificationRequestUseCase;
        this.publicBookingNotificationProperties = publicBookingNotificationProperties;
    }

    @Override
    public void requestNotifications(
            PublicBookingNotificationRecord publicBookingNotificationRecord
    ) {
        if (!publicBookingNotificationProperties.isEnabled()) {
            return;
        }
        notificationRequestUseCase.requestNotification(
                guestRequestRecord(publicBookingNotificationRecord)
        );
        notificationRequestUseCase.requestNotification(
                managementRequestRecord(publicBookingNotificationRecord)
        );
    }

    private NotificationRequestRecord guestRequestRecord(
            PublicBookingNotificationRecord publicBookingNotificationRecord
    ) {
        String textBody = "Ola, " + publicBookingNotificationRecord.guestFirstName() + "!\n\n"
                + "Recebemos seu pedido de reserva "
                + publicBookingNotificationRecord.bookingCode() + ".\n"
                + "Referencia do evento: "
                + publicBookingNotificationRecord.externalEventId() + ".\n"
                + "Periodo solicitado: "
                + DATE_FORMATTER.format(publicBookingNotificationRecord.checkIn())
                + " a "
                + DATE_FORMATTER.format(publicBookingNotificationRecord.checkOut())
                + ".\n\n"
                + "A reserva ainda nao esta confirmada. Entraremos em contato pelo WhatsApp "
                + "para confirmar a disponibilidade e combinar a forma de pagamento.";
        String htmlBody = "<p>Ola, "
                + escapeHtml(publicBookingNotificationRecord.guestFirstName())
                + "!</p><p>Recebemos seu pedido de reserva <strong>"
                + escapeHtml(publicBookingNotificationRecord.bookingCode())
                + "</strong>.</p><p>Referencia do evento: "
                + escapeHtml(publicBookingNotificationRecord.externalEventId())
                + ".</p><p>Periodo solicitado: "
                + DATE_FORMATTER.format(publicBookingNotificationRecord.checkIn())
                + " a "
                + DATE_FORMATTER.format(publicBookingNotificationRecord.checkOut())
                + ".</p><p>A reserva ainda nao esta confirmada. Entraremos em contato pelo "
                + "WhatsApp para confirmar a disponibilidade e combinar a forma de pagamento.</p>";
        return requestRecord(
                publicBookingNotificationRecord,
                GUEST_NOTIFICATION_TYPE,
                publicBookingNotificationRecord.guestEmail(),
                "Recebemos seu pedido de reserva "
                        + publicBookingNotificationRecord.bookingCode(),
                textBody,
                htmlBody
        );
    }

    private NotificationRequestRecord managementRequestRecord(
            PublicBookingNotificationRecord publicBookingNotificationRecord
    ) {
        String textBody = "Novo pedido de reserva nao confirmado.\n\n"
                + managementDetails(publicBookingNotificationRecord, "\n", false)
                + "\nEntre em contato com o hospede pelo WhatsApp para confirmar a reserva "
                + "e combinar a forma de pagamento.";
        String htmlBody = "<p><strong>Novo pedido de reserva nao confirmado.</strong></p>"
                + "<p>"
                + managementDetails(publicBookingNotificationRecord, "<br>", true)
                + "</p><p>Entre em contato com o hospede pelo WhatsApp para confirmar a reserva "
                + "e combinar a forma de pagamento.</p>";
        return requestRecord(
                publicBookingNotificationRecord,
                MANAGEMENT_NOTIFICATION_TYPE,
                publicBookingNotificationProperties.getManagementRecipient(),
                "Novo pedido de reserva " + publicBookingNotificationRecord.bookingCode(),
                textBody,
                htmlBody
        );
    }

    private NotificationRequestRecord requestRecord(
            PublicBookingNotificationRecord publicBookingNotificationRecord,
            String notificationType,
            String recipient,
            String subject,
            String textBody,
            String htmlBody
    ) {
        return new NotificationRequestRecord(
                SOURCE_SYSTEM,
                publicBookingNotificationRecord.externalEventId(),
                publicBookingNotificationRecord.externalEventId() + ":" + notificationType,
                publicBookingNotificationRecord.bookingCode(),
                notificationType,
                NotificationChannel.EMAIL,
                publicBookingNotificationProperties.getDeliveryProfileKey(),
                new EmailMessageRecord(recipient, subject, textBody, htmlBody)
        );
    }

    private String managementDetails(
            PublicBookingNotificationRecord publicBookingNotificationRecord,
            String separator,
            boolean html
    ) {
        return "Pedido: " + display(publicBookingNotificationRecord.bookingCode(), html) + separator
                + "Evento: " + display(publicBookingNotificationRecord.externalEventId(), html) + separator
                + "Recebido em: "
                + DATE_TIME_FORMATTER.format(publicBookingNotificationRecord.requestedAt()) + separator
                + "Acomodacao: "
                + display(publicBookingNotificationRecord.roomIdentification(), html) + separator
                + "Periodo: " + DATE_FORMATTER.format(publicBookingNotificationRecord.checkIn())
                + " a " + DATE_FORMATTER.format(publicBookingNotificationRecord.checkOut()) + separator
                + "Hospedes: " + publicBookingNotificationRecord.adults() + " adultos, "
                + publicBookingNotificationRecord.children() + " criancas" + separator
                + "Pets: " + publicBookingNotificationRecord.pets() + separator
                + "Valor cotado: " + publicBookingNotificationRecord.currency() + " "
                + publicBookingNotificationRecord.quotedTotal() + separator
                + "Hospede: " + display(publicBookingNotificationRecord.guestFirstName(), html) + " "
                + display(publicBookingNotificationRecord.guestLastName(), html) + separator
                + "WhatsApp: " + display(publicBookingNotificationRecord.guestWhatsApp(), html) + separator
                + "Status: " + publicBookingNotificationRecord.status().name();
    }

    private String display(String value, boolean html) {
        return html ? escapeHtml(value) : value;
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
