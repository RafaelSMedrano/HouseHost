package com.househost.notifier.domain.model;

import com.househost.notifier.domain.exception.NotificationDomainException;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

public class NotificationIntent {

    public static final int MAX_SOURCE_SYSTEM_LENGTH = 100;
    public static final int MAX_EXTERNAL_EVENT_ID_LENGTH = 160;
    public static final int MAX_IDEMPOTENCY_KEY_LENGTH = 200;
    public static final int MAX_CORRELATION_KEY_LENGTH = 200;
    public static final int MAX_NOTIFICATION_TYPE_LENGTH = 100;
    public static final int MAX_DELIVERY_PROFILE_KEY_LENGTH = 100;
    public static final int MAX_RECIPIENT_LENGTH = 320;
    public static final int MAX_SUBJECT_LENGTH = 255;
    public static final int MAX_TEXT_BODY_LENGTH = 100_000;
    public static final int MAX_HTML_BODY_LENGTH = 200_000;
    public static final int MAX_PROVIDER_MESSAGE_ID_LENGTH = 255;

    private static final Pattern SYMBOLIC_KEY_PATTERN = Pattern.compile(
            "[A-Za-z][A-Za-z0-9_]*(?:[.-][A-Za-z0-9_]+)*"
    );
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );

    private final UUID id;
    private final String sourceSystem;
    private final String externalEventId;
    private final String idempotencyKey;
    private final String correlationKey;
    private final String notificationType;
    private final NotificationChannel channel;
    private final String deliveryProfileKey;
    private final String recipient;
    private final String subject;
    private final String textBody;
    private final String htmlBody;
    private NotificationStatus status;
    private int attemptCount;
    private Instant nextAttemptAt;
    private Instant leaseUntil;
    private String providerMessageId;
    private NotificationFailureCategory lastErrorCategory;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant acceptedAt;
    private Instant deliveredAt;
    private Instant failedAt;
    private final Instant retentionUntil;
    private final Long version;

    private NotificationIntent(
            UUID id,
            String sourceSystem,
            String externalEventId,
            String idempotencyKey,
            String correlationKey,
            String notificationType,
            NotificationChannel channel,
            String deliveryProfileKey,
            String recipient,
            String subject,
            String textBody,
            String htmlBody,
            NotificationStatus status,
            int attemptCount,
            Instant nextAttemptAt,
            Instant leaseUntil,
            String providerMessageId,
            NotificationFailureCategory lastErrorCategory,
            Instant createdAt,
            Instant updatedAt,
            Instant acceptedAt,
            Instant deliveredAt,
            Instant failedAt,
            Instant retentionUntil,
            Long version,
            boolean anonymizedContentAllowed
    ) {
        this.id = requireValue(id, "Identificador da notificacao e obrigatorio.");
        this.sourceSystem = requireSymbolicKey(
                sourceSystem,
                MAX_SOURCE_SYSTEM_LENGTH,
                "Sistema de origem"
        );
        this.externalEventId = requireText(
                externalEventId,
                MAX_EXTERNAL_EVENT_ID_LENGTH,
                "Identificador externo do evento"
        );
        this.idempotencyKey = requireText(
                idempotencyKey,
                MAX_IDEMPOTENCY_KEY_LENGTH,
                "Chave de idempotencia"
        );
        this.correlationKey = optionalText(
                correlationKey,
                MAX_CORRELATION_KEY_LENGTH,
                "Chave de correlacao"
        );
        this.notificationType = requireSymbolicKey(
                notificationType,
                MAX_NOTIFICATION_TYPE_LENGTH,
                "Tipo da notificacao"
        );
        this.channel = requireValue(channel, "Canal da notificacao e obrigatorio.");
        this.deliveryProfileKey = requireSymbolicKey(
                deliveryProfileKey,
                MAX_DELIVERY_PROFILE_KEY_LENGTH,
                "Perfil de entrega"
        );
        boolean contentAnonymized = anonymizedContentAllowed
                && recipient == null
                && subject == null
                && textBody == null
                && htmlBody == null;
        this.recipient = contentAnonymized ? null : requireRecipient(recipient);
        this.subject = contentAnonymized
                ? null
                : requireHeader(subject, MAX_SUBJECT_LENGTH, "Assunto");
        this.textBody = contentAnonymized
                ? null
                : requireContent(textBody, MAX_TEXT_BODY_LENGTH, "Corpo textual");
        this.htmlBody = contentAnonymized
                ? null
                : requireContent(htmlBody, MAX_HTML_BODY_LENGTH, "Corpo HTML");
        this.status = requireValue(status, "Status da notificacao e obrigatorio.");
        this.attemptCount = requireNonNegative(attemptCount, "Quantidade de tentativas");
        this.nextAttemptAt = nextAttemptAt;
        this.leaseUntil = leaseUntil;
        this.providerMessageId = optionalText(
                providerMessageId,
                MAX_PROVIDER_MESSAGE_ID_LENGTH,
                "Identificador da mensagem no provedor"
        );
        this.lastErrorCategory = lastErrorCategory;
        this.createdAt = requireValue(createdAt, "Data de criacao e obrigatoria.");
        this.updatedAt = requireValue(updatedAt, "Data de atualizacao e obrigatoria.");
        this.acceptedAt = acceptedAt;
        this.deliveredAt = deliveredAt;
        this.failedAt = failedAt;
        this.retentionUntil = requireValue(
                retentionUntil,
                "Limite de retencao e obrigatorio."
        );
        this.version = version;
        validateRestoredState();
    }

    public static NotificationIntent create(
            UUID id,
            String sourceSystem,
            String externalEventId,
            String idempotencyKey,
            String correlationKey,
            String notificationType,
            NotificationChannel channel,
            String deliveryProfileKey,
            String recipient,
            String subject,
            String textBody,
            String htmlBody,
            Instant createdAt,
            Instant retentionUntil
    ) {
        return new NotificationIntent(
                id,
                sourceSystem,
                externalEventId,
                idempotencyKey,
                correlationKey,
                notificationType,
                channel,
                deliveryProfileKey,
                recipient,
                subject,
                textBody,
                htmlBody,
                NotificationStatus.PENDING,
                0,
                createdAt,
                null,
                null,
                null,
                createdAt,
                createdAt,
                null,
                null,
                null,
                retentionUntil,
                null,
                false
        );
    }

    public static NotificationIntent restore(
            UUID id,
            String sourceSystem,
            String externalEventId,
            String idempotencyKey,
            String correlationKey,
            String notificationType,
            NotificationChannel channel,
            String deliveryProfileKey,
            String recipient,
            String subject,
            String textBody,
            String htmlBody,
            NotificationStatus status,
            int attemptCount,
            Instant nextAttemptAt,
            Instant leaseUntil,
            String providerMessageId,
            NotificationFailureCategory lastErrorCategory,
            Instant createdAt,
            Instant updatedAt,
            Instant acceptedAt,
            Instant deliveredAt,
            Instant failedAt,
            Instant retentionUntil,
            Long version
    ) {
        return new NotificationIntent(
                id,
                sourceSystem,
                externalEventId,
                idempotencyKey,
                correlationKey,
                notificationType,
                channel,
                deliveryProfileKey,
                recipient,
                subject,
                textBody,
                htmlBody,
                status,
                attemptCount,
                nextAttemptAt,
                leaseUntil,
                providerMessageId,
                lastErrorCategory,
                createdAt,
                updatedAt,
                acceptedAt,
                deliveredAt,
                failedAt,
                retentionUntil,
                version,
                true
        );
    }

    public void claim(Instant claimedAt, Instant claimedLeaseUntil) {
        requireChronologicalTransition(claimedAt);
        requireValue(claimedLeaseUntil, "Expiracao da concessao e obrigatoria.");
        if (!claimedLeaseUntil.isAfter(claimedAt)) {
            throw new NotificationDomainException(
                    "Expiracao da concessao deve ser posterior a reivindicacao."
            );
        }
        if (!isEligibleForClaimAt(claimedAt)) {
            throw invalidTransition("reivindicar para processamento");
        }

        status = NotificationStatus.PROCESSING;
        attemptCount++;
        nextAttemptAt = null;
        leaseUntil = claimedLeaseUntil;
        updatedAt = claimedAt;
    }

    public void markAccepted(String acceptedProviderMessageId, Instant providerAcceptedAt) {
        if (status == NotificationStatus.ACCEPTED
                && providerMessageId != null
                && providerMessageId.equals(acceptedProviderMessageId)) {
            return;
        }
        requireStatus(NotificationStatus.PROCESSING, "registrar aceite do provedor");
        requireChronologicalTransition(providerAcceptedAt);

        providerMessageId = requireText(
                acceptedProviderMessageId,
                MAX_PROVIDER_MESSAGE_ID_LENGTH,
                "Identificador da mensagem no provedor"
        );
        status = NotificationStatus.ACCEPTED;
        acceptedAt = providerAcceptedAt;
        lastErrorCategory = null;
        leaseUntil = null;
        nextAttemptAt = null;
        updatedAt = providerAcceptedAt;
    }

    public void markRetryableFailure(
            NotificationFailureCategory failureCategory,
            Instant retryAt,
            Instant failureRecordedAt
    ) {
        requireStatus(NotificationStatus.PROCESSING, "registrar falha retentavel");
        requireChronologicalTransition(failureRecordedAt);
        requireValue(failureCategory, "Categoria da falha e obrigatoria.");
        requireValue(retryAt, "Data da proxima tentativa e obrigatoria.");
        if (!retryAt.isAfter(failureRecordedAt)) {
            throw new NotificationDomainException(
                    "Proxima tentativa deve ser posterior ao registro da falha."
            );
        }

        status = NotificationStatus.RETRYABLE_FAILURE;
        lastErrorCategory = failureCategory;
        nextAttemptAt = retryAt;
        leaseUntil = null;
        updatedAt = failureRecordedAt;
    }

    public void markExhausted(
            NotificationFailureCategory failureCategory,
            Instant failureRecordedAt
    ) {
        requireStatus(NotificationStatus.PROCESSING, "esgotar tentativas");
        requireChronologicalTransition(failureRecordedAt);

        status = NotificationStatus.EXHAUSTED;
        lastErrorCategory = requireValue(
                failureCategory,
                "Categoria da falha e obrigatoria."
        );
        failedAt = failureRecordedAt;
        nextAttemptAt = null;
        leaseUntil = null;
        updatedAt = failureRecordedAt;
    }

    public void requeueExhausted(Instant requeuedAt) {
        requireStatus(NotificationStatus.EXHAUSTED, "recolocar na fila");
        requireChronologicalTransition(requeuedAt);

        status = NotificationStatus.PENDING;
        attemptCount = 0;
        nextAttemptAt = requeuedAt;
        leaseUntil = null;
        providerMessageId = null;
        lastErrorCategory = null;
        acceptedAt = null;
        deliveredAt = null;
        failedAt = null;
        updatedAt = requeuedAt;
    }

    public void markDelivered(Instant deliveryRecordedAt) {
        if (status == NotificationStatus.DELIVERED) {
            return;
        }
        requireStatus(NotificationStatus.ACCEPTED, "registrar entrega");
        requireChronologicalTransition(deliveryRecordedAt);

        status = NotificationStatus.DELIVERED;
        deliveredAt = deliveryRecordedAt;
        updatedAt = deliveryRecordedAt;
    }

    public void markBounced(
            NotificationFailureCategory failureCategory,
            Instant failureRecordedAt
    ) {
        if (status == NotificationStatus.BOUNCED) {
            return;
        }
        requireStatus(NotificationStatus.ACCEPTED, "registrar bounce");
        requireChronologicalTransition(failureRecordedAt);

        status = NotificationStatus.BOUNCED;
        lastErrorCategory = requireValue(
                failureCategory,
                "Categoria do bounce e obrigatoria."
        );
        failedAt = failureRecordedAt;
        updatedAt = failureRecordedAt;
    }

    public void markComplaint(
            NotificationFailureCategory failureCategory,
            Instant failureRecordedAt
    ) {
        if (status == NotificationStatus.COMPLAINT) {
            return;
        }
        if (status != NotificationStatus.ACCEPTED
                && status != NotificationStatus.DELIVERED) {
            throw invalidTransition("registrar reclamacao");
        }
        requireChronologicalTransition(failureRecordedAt);

        status = NotificationStatus.COMPLAINT;
        lastErrorCategory = requireValue(
                failureCategory,
                "Categoria da reclamacao e obrigatoria."
        );
        failedAt = failureRecordedAt;
        updatedAt = failureRecordedAt;
    }

    public boolean isEligibleForClaimAt(Instant referenceAt) {
        requireValue(referenceAt, "Data de referencia e obrigatoria.");
        if (status == NotificationStatus.PENDING
                || status == NotificationStatus.RETRYABLE_FAILURE) {
            return nextAttemptAt != null && !nextAttemptAt.isAfter(referenceAt);
        }
        return status == NotificationStatus.PROCESSING
                && leaseUntil != null
                && !leaseUntil.isAfter(referenceAt);
    }

    public UUID getId() {
        return id;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public String getExternalEventId() {
        return externalEventId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getDeliveryProfileKey() {
        return deliveryProfileKey;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getTextBody() {
        return textBody;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public Instant getLeaseUntil() {
        return leaseUntil;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public NotificationFailureCategory getLastErrorCategory() {
        return lastErrorCategory;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public Instant getRetentionUntil() {
        return retentionUntil;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isContentAnonymized() {
        return recipient == null;
    }

    private void validateRestoredState() {
        if (updatedAt.isBefore(createdAt)) {
            throw new NotificationDomainException(
                    "Data de atualizacao nao pode anteceder a criacao."
            );
        }
        if (!retentionUntil.isAfter(createdAt)) {
            throw new NotificationDomainException(
                    "Limite de retencao deve ser posterior a criacao."
            );
        }
        if ((status == NotificationStatus.PENDING
                || status == NotificationStatus.RETRYABLE_FAILURE)
                && nextAttemptAt == null) {
            throw new NotificationDomainException(
                    "Notificacao elegivel exige data da proxima tentativa."
            );
        }
        if (status == NotificationStatus.PROCESSING && leaseUntil == null) {
            throw new NotificationDomainException(
                    "Notificacao em processamento exige concessao vigente."
            );
        }
        if (status == NotificationStatus.ACCEPTED
                || status == NotificationStatus.DELIVERED
                || status == NotificationStatus.BOUNCED
                || status == NotificationStatus.COMPLAINT) {
            if (providerMessageId == null || acceptedAt == null) {
                throw new NotificationDomainException(
                        "Estado aceito pelo provedor exige identificador e data de aceite."
                );
            }
        }
        if (status == NotificationStatus.DELIVERED && deliveredAt == null) {
            throw new NotificationDomainException(
                    "Notificacao entregue exige data de entrega."
            );
        }
        if (status.isTerminal() && failedAt == null) {
            throw new NotificationDomainException(
                    "Estado terminal exige data da falha."
            );
        }
    }

    private void requireStatus(NotificationStatus requiredStatus, String operation) {
        if (status != requiredStatus) {
            throw invalidTransition(operation);
        }
    }

    private void requireChronologicalTransition(Instant transitionAt) {
        requireValue(transitionAt, "Data da transicao e obrigatoria.");
        if (transitionAt.isBefore(updatedAt)) {
            throw new NotificationDomainException(
                    "Data da transicao nao pode anteceder a ultima atualizacao."
            );
        }
    }

    private NotificationDomainException invalidTransition(String operation) {
        return new NotificationDomainException(
                "Status " + status + " nao permite " + operation + "."
        );
    }

    private static String requireRecipient(String recipient) {
        String normalizedRecipient = requireText(
                recipient,
                MAX_RECIPIENT_LENGTH,
                "Destinatario"
        );
        if (!EMAIL_PATTERN.matcher(normalizedRecipient).matches()) {
            throw new NotificationDomainException("Destinatario deve ser um email valido.");
        }
        return normalizedRecipient;
    }

    private static String requireHeader(String value, int maximumLength, String fieldName) {
        String normalizedValue = requireText(value, maximumLength, fieldName);
        if (normalizedValue.contains("\r") || normalizedValue.contains("\n")) {
            throw new NotificationDomainException(
                    fieldName + " nao pode conter quebra de linha."
            );
        }
        return normalizedValue;
    }

    private static String requireSymbolicKey(
            String value,
            int maximumLength,
            String fieldName
    ) {
        String normalizedValue = requireText(value, maximumLength, fieldName);
        if (!SYMBOLIC_KEY_PATTERN.matcher(normalizedValue).matches()) {
            throw new NotificationDomainException(
                    fieldName + " deve usar identificador textual estavel."
            );
        }
        return normalizedValue;
    }

    private static String requireText(String value, int maximumLength, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new NotificationDomainException(fieldName + " e obrigatorio.");
        }
        String normalizedValue = value.trim();
        if (normalizedValue.length() > maximumLength) {
            throw new NotificationDomainException(
                    fieldName + " excede o limite de " + maximumLength + " caracteres."
            );
        }
        return normalizedValue;
    }

    private static String requireContent(String value, int maximumLength, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new NotificationDomainException(fieldName + " e obrigatorio.");
        }
        if (value.length() > maximumLength) {
            throw new NotificationDomainException(
                    fieldName + " excede o limite de " + maximumLength + " caracteres."
            );
        }
        return value;
    }

    private static String optionalText(String value, int maximumLength, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireText(value, maximumLength, fieldName);
    }

    private static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new NotificationDomainException(fieldName + " nao pode ser negativa.");
        }
        return value;
    }

    private static <T> T requireValue(T value, String message) {
        if (value == null) {
            throw new NotificationDomainException(message);
        }
        return value;
    }
}
