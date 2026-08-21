package com.househost.observability.application.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.househost.observability.domain.model.ClientLogLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class ClientLogRequestDTO {

    @NotNull
    private ClientLogLevel level;

    @NotBlank
    @Size(max = 80)
    @Pattern(regexp = "[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*")
    private String event;

    @NotBlank
    @Size(max = 1000)
    private String message;

    @Size(max = 64)
    private String correlationId;

    @Size(max = 512)
    private String route;

    @Pattern(regexp = "[A-Z]{3,10}")
    private String method;

    @Min(100)
    @Max(599)
    private Integer status;

    @PositiveOrZero
    @Max(86_400_000)
    private Long durationMs;

    @Size(max = 8000)
    private String stack;

    private Instant clientTimestamp;

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object ignoredValue) {
        throw new IllegalArgumentException("Unknown client log field: " + fieldName);
    }

    public ClientLogLevel getLevel() { return level; }
    public void setLevel(ClientLogLevel level) { this.level = level; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getStack() { return stack; }
    public void setStack(String stack) { this.stack = stack; }
    public Instant getClientTimestamp() { return clientTimestamp; }
    public void setClientTimestamp(Instant clientTimestamp) { this.clientTimestamp = clientTimestamp; }
}
