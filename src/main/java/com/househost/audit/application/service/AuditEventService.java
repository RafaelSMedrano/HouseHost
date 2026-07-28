package com.househost.audit.application.service;

import com.househost.audit.application.port.out.*;
import com.househost.audit.domain.model.*;
import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditEventService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventService.class);
    private final AuditEventPersistencePort persistencePort;
    private final AuditProcessingOperationPort processingOperationPort;
    private final AuditActorContextPort actorContextPort;
    private final AuditValidationService validationService;
    private final AuditMetadataService metadataService;

    public AuditEventService(AuditEventPersistencePort persistencePort, AuditProcessingOperationPort processingOperationPort,
                             AuditActorContextPort actorContextPort, AuditValidationService validationService,
                             AuditMetadataService metadataService) {
        this.persistencePort = persistencePort;
        this.processingOperationPort = processingOperationPort;
        this.actorContextPort = actorContextPort;
        this.validationService = validationService;
        this.metadataService = metadataService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordForJwtActor(String operationCode,
                                     String eventType,
                                     String entityType,
                                     Long entityId,
                                     Map<String, Object> metadata) {
        AuditActor actor = actorContextPort.currentActor();
        record(operationCode, eventType, entityType, entityId, actor.actorType(), actor.actorId(), actor.actorLabel(), actor.context(), metadata);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordForExplicitActor(String operationCode,
                               String eventType,
                               String entityType,
                               Long entityId,
                               String actorType,
                               Long actorId,
                               String actorLabel,
                               Map<String, Object> metadata) {
        record(operationCode, eventType, entityType, entityId, actorType, actorId, actorLabel,
                actorContextPort.currentRequestContext(), metadata);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordForExplicitContext(String operationCode, String eventType, String entityType,
                                         Long entityId, String actorType, Long actorId,
                                         String actorLabel, AuditEventContext context,
                                         Map<String, Object> metadata) {
        record(operationCode, eventType, entityType, entityId, actorType, actorId, actorLabel,
                context, metadata);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String operationCode, String eventType, String entityType, Long entityId, String actorType,
                       Long actorId, String actorLabel, AuditEventContext context, Map<String, Object> metadata) {
        try {
            String requiredOperationCode = validationService.required(operationCode, "processingOperationCode");
            Long operationId = processingOperationPort.findIdByOperationCode(requiredOperationCode)
                    .orElseThrow(() -> new IllegalStateException("Operacao de tratamento nao cadastrada: " + requiredOperationCode));
            AuditEvent event = new AuditEvent(
                    validationService.required(eventType, "eventType"),
                    validationService.required(entityType, "entityType"), entityId, operationId,
                    validationService.required(actorType, "actorType"), actorId,
                    validationService.truncate(actorLabel, 180), LocalDateTime.now(),
                    validationService.truncate(context == null ? null : context.ipAddress(), 80),
                    validationService.truncate(context == null ? null : context.userAgent(), 500),
                    metadataService.serialize(metadata));
            persistencePort.save(event);
        } catch (RuntimeException exception) {
            LOGGER.warn("Nao foi possivel registrar evento de auditoria {} para {}#{}.", eventType, entityType, entityId, exception);
        }
    }
}
