package com.househost.publicapi.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

class PublicRequestSizeFilterTest {

    private final PublicRequestSizeFilter filter = new PublicRequestSizeFilter();

    @Test
    void rejectsOversizedPublicBodyWithHttp413() throws Exception {
        MockHttpServletRequest request = publicPost(new byte[PublicRequestSizeFilter.MAX_PUBLIC_REQUEST_BYTES + 1]);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(413, response.getStatus());
        assertTrue(response.getContentAsString().contains("16 KiB"));
        verify(filterChain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void replaysAcceptedPublicBodyToTheApplication() throws Exception {
        byte[] body = "{\"adults\":2}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = publicPost(body);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<byte[]> receivedBodyReference = new AtomicReference<>();
        FilterChain filterChain = (servletRequest, servletResponse) ->
                receivedBodyReference.set(servletRequest.getInputStream().readAllBytes());

        filter.doFilter(request, response, filterChain);

        assertArrayEquals(body, receivedBodyReference.get());
    }

    private MockHttpServletRequest publicPost(byte[] body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/public/bookings");
        request.setContent(body);
        request.setContentType("application/json");
        return request;
    }
}
