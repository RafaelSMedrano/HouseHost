package com.househost.publicapi.adapter.in.rest;

import com.househost.privacy.policy.domain.exception.PrivacyPolicyConflictException;
import com.househost.publicapi.application.port.in.PublicBookingUseCase;
import com.househost.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicBookingPolicyConflictHttpTest {
    @Test
    void returnsHttp409WhenPolicyChangedDuringFormCompletion() throws Exception {
        PublicBookingUseCase publicBookingUseCase = mock(PublicBookingUseCase.class);
        when(publicBookingUseCase.createBooking(any(), any())).thenThrow(
                new PrivacyPolicyConflictException("Politica atualizada.")
        );
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                        new PublicBookingController(publicBookingUseCase)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/public/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Politica atualizada."));
    }
}
