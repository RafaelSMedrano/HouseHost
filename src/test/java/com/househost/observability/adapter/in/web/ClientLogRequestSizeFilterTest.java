package com.househost.observability.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ClientLogRequestSizeFilterTest {

    private final ClientLogRequestSizeFilter filter = new ClientLogRequestSizeFilter();

    @Test
    void rejectsOversizedClientLogBody() throws Exception {
        MockHttpServletRequest request = post(new byte[ClientLogRequestSizeFilter.MAX_REQUEST_BYTES + 1]);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(413, response.getStatus());
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void replaysAcceptedBody() throws Exception {
        byte[] body = "{\"level\":\"WARN\"}".getBytes(StandardCharsets.UTF_8);
        AtomicReference<byte[]> receivedBodyReference = new AtomicReference<>();

        filter.doFilter(post(body), new MockHttpServletResponse(),
                (request, response) -> receivedBodyReference.set(request.getInputStream().readAllBytes()));

        assertArrayEquals(body, receivedBodyReference.get());
    }

    private MockHttpServletRequest post(byte[] body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/client-logs");
        request.setContentType("application/json");
        request.setContent(body);
        return request;
    }
}
