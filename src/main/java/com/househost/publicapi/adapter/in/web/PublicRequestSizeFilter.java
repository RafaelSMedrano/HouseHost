package com.househost.publicapi.adapter.in.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class PublicRequestSizeFilter extends OncePerRequestFilter {

    static final int MAX_PUBLIC_REQUEST_BYTES = 16 * 1024;
    private static final Set<String> BODY_METHOD_SET = Set.of("POST", "PUT", "PATCH");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !BODY_METHOD_SET.contains(request.getMethod())
                || !request.getRequestURI().startsWith(request.getContextPath() + "/public/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getContentLengthLong() > MAX_PUBLIC_REQUEST_BYTES) {
            rejectOversizedRequest(response);
            return;
        }

        byte[] requestBody = request.getInputStream().readNBytes(MAX_PUBLIC_REQUEST_BYTES + 1);
        if (requestBody.length > MAX_PUBLIC_REQUEST_BYTES) {
            rejectOversizedRequest(response);
            return;
        }

        filterChain.doFilter(new CachedBodyRequest(request, requestBody), response);
    }

    private void rejectOversizedRequest(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"status\":\"error\",\"message\":\"Requisicao publica excede o limite de 16 KiB.\",\"data\":null}"
        );
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] requestBody;

        private CachedBodyRequest(HttpServletRequest request, byte[] requestBody) {
            super(request);
            this.requestBody = requestBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(requestBody);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return requestBody.length;
        }

        @Override
        public long getContentLengthLong() {
            return requestBody.length;
        }
    }

    private static final class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        private CachedBodyServletInputStream(byte[] requestBody) {
            inputStream = new ByteArrayInputStream(requestBody);
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            try {
                if (!isFinished()) {
                    readListener.onDataAvailable();
                }
                if (isFinished()) {
                    readListener.onAllDataRead();
                }
            } catch (IOException exception) {
                readListener.onError(exception);
            }
        }
    }
}
