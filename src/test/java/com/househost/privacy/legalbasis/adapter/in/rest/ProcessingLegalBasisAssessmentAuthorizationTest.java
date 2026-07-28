package com.househost.privacy.legalbasis.adapter.in.rest;

import com.househost.privacy.legalbasis.application.port.in.ProcessingLegalBasisAssessmentUseCase;
import com.househost.security.adapter.in.config.SecurityConfig;
import com.househost.security.adapter.in.web.JwtAuthenticationFilter;
import com.househost.security.application.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessingLegalBasisAssessmentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ProcessingLegalBasisAssessmentAuthorizationTest {
    @Autowired MockMvc mockMvc;
    @MockBean ProcessingLegalBasisAssessmentUseCase useCase;
    @MockBean AuthenticationService authenticationService;

    @BeforeEach
    void configureUseCase() {
        when(useCase.findByOperation(1L)).thenReturn(List.of());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAdministrator() throws Exception {
        mockMvc.perform(get("/data-processing-operations/1/legal-basis-assessments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECEPTION")
    void deniesOperationalRole() throws Exception {
        mockMvc.perform(get("/data-processing-operations/1/legal-basis-assessments"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/data-processing-operations/1/legal-basis-assessments"))
                .andExpect(status().isUnauthorized());
    }
}
