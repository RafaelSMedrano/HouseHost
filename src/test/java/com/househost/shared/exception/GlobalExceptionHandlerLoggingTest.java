package com.househost.shared.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

class GlobalExceptionHandlerLoggingTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private Logger handlerLogger;
    private Level originalLevel;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void captureHandlerLogs() {
        handlerLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        originalLevel = handlerLogger.getLevel();
        handlerLogger.setLevel(Level.INFO);
        logAppender = new ListAppender<>();
        logAppender.start();
        handlerLogger.addAppender(logAppender);
    }

    @AfterEach
    void releaseHandlerLogs() {
        handlerLogger.detachAppender(logAppender);
        handlerLogger.setLevel(originalLevel);
    }

    @Test
    void knownExceptionLogsWarningWithoutMessageOrStackTrace() {
        var response = handler.handleBooking(new BookingException("password=marker-password"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ILoggingEvent loggingEvent = lastEvent();
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertTrue(loggingEvent.getFormattedMessage().contains("event=exception.handled"));
        assertTrue(loggingEvent.getFormattedMessage().contains("type=BookingException"));
        assertFalse(loggingEvent.getFormattedMessage().contains("marker-password"));
        assertNull(loggingEvent.getThrowableProxy());
    }

    @Test
    void unexpectedExceptionLogsSanitizedStackAndReturnsGenericResponse() {
        var response = handler.handleUnexpected(
                new IllegalStateException("Authorization: Bearer marker-token")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Nao foi possivel concluir a operacao.", response.getBody().getMessage());

        ILoggingEvent loggingEvent = lastEvent();
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertTrue(loggingEvent.getFormattedMessage().contains("event=exception.unhandled"));
        assertTrue(loggingEvent.getFormattedMessage().contains("type=IllegalStateException"));
        assertFalse(loggingEvent.getFormattedMessage().contains("marker-token"));
        assertNotNull(loggingEvent.getThrowableProxy());
        assertEquals("Unexpected application failure", loggingEvent.getThrowableProxy().getMessage());
        assertFalse(loggingEvent.getThrowableProxy().getMessage().contains("marker-token"));
    }

    @Test
    void frameworkClientExceptionPreservesItsHttpStatus() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ParameterController())
                .setControllerAdvice(handler)
                .build();

        mockMvc.perform(get("/parameter-required"))
                .andExpect(status().isBadRequest());

        ILoggingEvent loggingEvent = lastEvent();
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertTrue(loggingEvent.getFormattedMessage().contains("status=400"));
        assertNull(loggingEvent.getThrowableProxy());
    }

    private ILoggingEvent lastEvent() {
        assertFalse(logAppender.list.isEmpty());
        return logAppender.list.get(logAppender.list.size() - 1);
    }

    @Controller
    private static class ParameterController {

        @GetMapping("/parameter-required")
        void parameterRequired(@RequestParam("value") String value) {
        }
    }
}
