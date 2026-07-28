package com.househost.privacy.policy.adapter.in.rest;

import com.househost.privacy.policy.application.port.in.PrivacyPolicyUseCase;
import com.househost.privacy.policy.application.port.in.PublicPrivacyPolicyUseCase;
import com.househost.security.adapter.in.config.SecurityConfig;
import com.househost.security.adapter.in.web.JwtAuthenticationFilter;
import com.househost.security.application.service.AuthenticationService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({PrivacyPolicyController.class, PublicPrivacyPolicyController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PrivacyPolicyAuthorizationTest {
    @Autowired MockMvc mockMvc;
    @MockBean PrivacyPolicyUseCase privacyPolicyUseCase;
    @MockBean PublicPrivacyPolicyUseCase publicPrivacyPolicyUseCase;
    @MockBean AuthenticationService authenticationService;

    @BeforeEach
    void configureUseCase() {
        when(privacyPolicyUseCase.findAll()).thenReturn(List.of());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdministratorToUseAdministrativeEndpoint() throws Exception {
        mockMvc.perform(get("/privacy-policies"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPTION")
    void deniesOperationalRoleFromAdministrativeEndpoint() throws Exception {
        mockMvc.perform(get("/privacy-policies"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requiresAuthenticationForAdministrativeEndpoint() throws Exception {
        mockMvc.perform(get("/privacy-policies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exposesCurrentPolicyEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/public/privacy-policy"))
                .andExpect(status().isOk());
    }
}
