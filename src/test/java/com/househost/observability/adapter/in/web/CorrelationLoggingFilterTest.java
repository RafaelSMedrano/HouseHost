package com.househost.observability.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.househost.observability.application.service.CorrelationIdService;

class CorrelationLoggingFilterTest {

    private final CorrelationLoggingFilter filter = new CorrelationLoggingFilter();
    private Logger filterLogger;
    private Level originalLevel;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void captureFilterLogs() {
        filterLogger = (Logger) LoggerFactory.getLogger(CorrelationLoggingFilter.class);
        originalLevel = filterLogger.getLevel();
        filterLogger.setLevel(Level.INFO);
        logAppender = new ListAppender<>();
        logAppender.start();
        filterLogger.addAppender(logAppender);
    }

    @AfterEach
    void releaseFilterLogs() {
        filterLogger.detachAppender(logAppender);
        filterLogger.setLevel(originalLevel);
        MDC.clear();
    }

    @Test
    void propagatesValidCorrelationIdAndClearsMdc() throws Exception {
        MockHttpServletRequest request = request("GET", "/rooms");
        request.addHeader(CorrelationLoggingFilter.CORRELATION_HEADER, "client-request_123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (filteredRequest, filteredResponse) -> {
            assertEquals("client-request_123", MDC.get(CorrelationLoggingFilter.CORRELATION_MDC_KEY));
            ((MockHttpServletResponse) filteredResponse).setStatus(204);
        });

        assertEquals("client-request_123", response.getHeader(CorrelationLoggingFilter.CORRELATION_HEADER));
        assertNull(MDC.get(CorrelationLoggingFilter.CORRELATION_MDC_KEY));
        assertLog(Level.INFO, "method=GET", "path=/rooms", "status=204", "durationMs=");
    }

    @Test
    void replacesMissingAndMalformedCorrelationIds() throws Exception {
        MockHttpServletRequest missingRequest = request("GET", "/rooms");
        MockHttpServletResponse missingResponse = new MockHttpServletResponse();
        filter.doFilter(missingRequest, missingResponse, (request, response) -> { });

        MockHttpServletRequest malformedRequest = request("GET", "/rooms");
        malformedRequest.addHeader(CorrelationLoggingFilter.CORRELATION_HEADER, "invalid id\r\nforged=true");
        MockHttpServletResponse malformedResponse = new MockHttpServletResponse();
        filter.doFilter(malformedRequest, malformedResponse, (request, response) -> { });

        String missingCorrelationId = missingResponse.getHeader(CorrelationLoggingFilter.CORRELATION_HEADER);
        String malformedCorrelationId = malformedResponse.getHeader(CorrelationLoggingFilter.CORRELATION_HEADER);
        CorrelationIdService correlationIdService = new CorrelationIdService();
        assertTrue(correlationIdService.isValid(missingCorrelationId));
        assertTrue(correlationIdService.isValid(malformedCorrelationId));
        assertNotEquals("invalid id\r\nforged=true", malformedCorrelationId);
        assertNotEquals(missingCorrelationId, malformedCorrelationId);
    }

    @Test
    void logsOnlyNormalizedRequestMetadata() throws Exception {
        MockHttpServletRequest request = request("POST", "/bookings");
        request.setQueryString("password=marker-password");
        request.addHeader("Authorization", "Bearer marker-token");
        request.setContent("documentNumber=marker-document".getBytes(StandardCharsets.UTF_8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(422);

        filter.doFilter(request, response, (filteredRequest, filteredResponse) -> { });

        ILoggingEvent loggingEvent = lastEvent();
        String message = loggingEvent.getFormattedMessage();
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertTrue(message.contains("event=request.completed"));
        assertTrue(message.contains("path=/bookings"));
        assertTrue(message.contains("status=422"));
        assertFalse(message.contains("marker-password"));
        assertFalse(message.contains("marker-token"));
        assertFalse(message.contains("marker-document"));
    }

    @Test
    void logsServerFailureAndClearsMdcWhenChainThrows() {
        MockHttpServletRequest request = request("POST", "/bookings");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(ServletException.class, () -> filter.doFilter(
                request,
                response,
                (filteredRequest, filteredResponse) -> {
                    throw new ServletException("marker-sensitive-cause");
                }
        ));

        assertNull(MDC.get(CorrelationLoggingFilter.CORRELATION_MDC_KEY));
        ILoggingEvent loggingEvent = lastEvent();
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertTrue(loggingEvent.getFormattedMessage().contains("status=500"));
        assertFalse(loggingEvent.getFormattedMessage().contains("marker-sensitive-cause"));
    }

    private MockHttpServletRequest request(String method, String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, requestUri);
        request.setRequestURI(requestUri);
        return request;
    }

    private void assertLog(Level level, String... expectedFragments) {
        ILoggingEvent loggingEvent = lastEvent();
        assertEquals(level, loggingEvent.getLevel());
        for (String expectedFragment : expectedFragments) {
            assertTrue(loggingEvent.getFormattedMessage().contains(expectedFragment));
        }
    }

    private ILoggingEvent lastEvent() {
        assertFalse(logAppender.list.isEmpty());
        return logAppender.list.get(logAppender.list.size() - 1);
    }
}
