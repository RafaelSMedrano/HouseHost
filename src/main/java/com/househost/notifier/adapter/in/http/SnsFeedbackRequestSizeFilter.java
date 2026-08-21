package com.househost.notifier.adapter.in.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(
        name = "househost.notifier.sns.enabled",
        havingValue = "true"
)
public class SnsFeedbackRequestSizeFilter extends OncePerRequestFilter {

    private final NotifierSnsProperties notifierSnsProperties;

    public SnsFeedbackRequestSizeFilter(NotifierSnsProperties notifierSnsProperties) {
        this.notifierSnsProperties = notifierSnsProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest httpServletRequest) {
        return !HttpMethod.POST.matches(httpServletRequest.getMethod())
                || !SnsSesFeedbackController.ENDPOINT_PATH.equals(
                        httpServletRequest.getRequestURI()
                );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            FilterChain filterChain
    ) throws ServletException, IOException {
        int maximumRequestBytes = Math.toIntExact(
                notifierSnsProperties.getMaxRequestSize().toBytes()
        );
        if (httpServletRequest.getContentLengthLong() > maximumRequestBytes) {
            httpServletResponse.sendError(HttpStatus.PAYLOAD_TOO_LARGE.value());
            return;
        }
        byte[] requestBody = httpServletRequest.getInputStream()
                .readNBytes(maximumRequestBytes + 1);
        if (requestBody.length > maximumRequestBytes) {
            httpServletResponse.sendError(HttpStatus.PAYLOAD_TOO_LARGE.value());
            return;
        }
        filterChain.doFilter(
                new CachedBodyRequest(httpServletRequest, requestBody),
                httpServletResponse
        );
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] requestBody;

        private CachedBodyRequest(
                HttpServletRequest httpServletRequest,
                byte[] requestBody
        ) {
            super(httpServletRequest);
            this.requestBody = requestBody.clone();
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestBody);
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException(
                            "Leitura assincrona nao e suportada."
                    );
                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(
                    new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)
            );
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
}
