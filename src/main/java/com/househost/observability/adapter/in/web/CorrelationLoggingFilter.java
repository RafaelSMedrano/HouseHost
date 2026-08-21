package com.househost.observability.adapter.in.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import com.househost.observability.application.service.CorrelationIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationLoggingFilter extends OncePerRequestFilter {

    public static final String CORRELATION_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_MDC_KEY = "correlationId";

    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationLoggingFilter.class);
    private static final int MAX_PATH_LENGTH = 512;
    private static final Pattern CONTROL_CHARACTER_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\\t]]+");

    private final CorrelationIdService correlationIdService;

    public CorrelationLoggingFilter() {
        this(new CorrelationIdService());
    }

    public CorrelationLoggingFilter(CorrelationIdService correlationIdService) {
        this.correlationIdService = correlationIdService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = correlationIdService.resolve(request.getHeader(CORRELATION_HEADER));
        long startedAtNanos = System.nanoTime();
        boolean requestFailed = false;

        MDC.put(CORRELATION_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException exception) {
            requestFailed = true;
            throw exception;
        } finally {
            long durationMilliseconds = (System.nanoTime() - startedAtNanos) / 1_000_000;
            int responseStatus = requestFailed && response.getStatus() < 500
                    ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                    : response.getStatus();
            logCompletion(
                    request.getMethod(),
                    normalizePath(request.getRequestURI(), request.getContextPath()),
                    responseStatus,
                    durationMilliseconds
            );
            MDC.remove(CORRELATION_MDC_KEY);
        }
    }

    private String normalizePath(String requestUri, String contextPath) {
        String path = requestUri == null || requestUri.isBlank() ? "/" : requestUri;
        if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        path = CONTROL_CHARACTER_PATTERN.matcher(path).replaceAll("");
        return path.length() <= MAX_PATH_LENGTH ? path : path.substring(0, MAX_PATH_LENGTH);
    }

    private void logCompletion(String method, String path, int status, long durationMilliseconds) {
        String normalizedMethod = normalizeMethod(method);
        if (status >= 500) {
            LOGGER.error(
                    "event=request.completed method={} path={} status={} durationMs={}",
                    normalizedMethod,
                    path,
                    status,
                    durationMilliseconds
            );
            return;
        }
        if (status >= 400) {
            LOGGER.warn(
                    "event=request.completed method={} path={} status={} durationMs={}",
                    normalizedMethod,
                    path,
                    status,
                    durationMilliseconds
            );
            return;
        }
        LOGGER.info(
                "event=request.completed method={} path={} status={} durationMs={}",
                normalizedMethod,
                path,
                status,
                durationMilliseconds
        );
    }

    private String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return "UNKNOWN";
        }
        String normalizedMethod = CONTROL_CHARACTER_PATTERN.matcher(method).replaceAll("");
        return normalizedMethod.length() <= 16 ? normalizedMethod : normalizedMethod.substring(0, 16);
    }
}
