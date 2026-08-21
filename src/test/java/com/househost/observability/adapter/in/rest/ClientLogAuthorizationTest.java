package com.househost.observability.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.househost.observability.application.port.in.ClientLogUseCase;
import com.househost.observability.application.records.ClientLogRequestContextRecord;
import com.househost.observability.application.service.ClientLogContextService;
import com.househost.security.adapter.in.config.SecurityConfig;
import com.househost.security.adapter.in.web.JwtAuthenticationFilter;
import com.househost.security.application.service.AuthenticationService;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClientLogController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ClientLogAuthorizationTest {

    private static final String VALID_BODY = """
            {"level":"WARN","event":"client.api_failed","message":"safe"}
            """;

    @Autowired MockMvc mockMvc;
    @MockBean ClientLogUseCase clientLogUseCase;
    @MockBean ClientLogContextService clientLogContextService;
    @MockBean AuthenticationService authenticationService;

    @Test
    void rejectsAnonymousRequestWithoutAcceptingAnEvent() throws Exception {
        mockMvc.perform(post("/client-logs").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verify(clientLogUseCase, never()).record(any(), any());
    }

    @ParameterizedTest
    @MethodSource("currentRoles")
    void acceptsEveryCurrentAuthenticatedRole(String role) throws Exception {
        org.mockito.Mockito.when(clientLogContextService.create(any(), any(), any()))
                .thenReturn(new ClientLogRequestContextRecord("actor-ref", "origin-ref", "correlation", Instant.EPOCH));

        mockMvc.perform(post("/client-logs")
                        .with(user("operator@example.invalid").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void rejectsUnknownFields() throws Exception {
        String body = """
                {"level":"ERROR","event":"client.failed","message":"safe","userId":"browser-value"}
                """;

        mockMvc.perform(post("/client-logs")
                        .with(user("operator@example.invalid").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private static Stream<String> currentRoles() {
        return Stream.of("CEO", "CTO", "ADMIN", "MANAGER", "RECEPTION", "HOUSEKEEPING");
    }
}
