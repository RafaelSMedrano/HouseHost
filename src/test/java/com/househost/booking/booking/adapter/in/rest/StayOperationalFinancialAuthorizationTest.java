package com.househost.booking.booking.adapter.in.rest;

import com.househost.booking.checking.adapter.in.rest.CheckInController;
import com.househost.booking.checking.application.port.in.CheckInUseCase;
import com.househost.booking.checkout.adapter.in.rest.CheckOutController;
import com.househost.booking.checkout.application.port.in.CheckOutUseCase;
import com.househost.security.adapter.in.config.SecurityConfig;
import com.househost.security.adapter.in.web.JwtAuthenticationFilter;
import com.househost.security.application.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CheckInController.class, CheckOutController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class StayOperationalFinancialAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckInUseCase checkInUseCase;

    @MockBean
    private CheckOutUseCase checkOutUseCase;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser(roles = "RECEPTION")
    void operationalRoleCanSubmitMaterializationOnlyThroughStayCommands() throws Exception {
        mockMvc.perform(post("/check-ins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookingId": 23,
                                  "status": "COMPLETED",
                                  "paymentMaterialization": {
                                    "structure": "SIMPLE",
                                    "method": "PIX",
                                    "idempotencyKey": "check-in-payment"
                                  }
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/check-outs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookingId": 23,
                                  "status": "COMPLETED",
                                  "paymentMaterialization": {
                                    "structure": "INSTALLMENT",
                                    "method": "CREDIT_CARD",
                                    "installmentsQuantity": 3,
                                    "idempotencyKey": "check-out-payment"
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        verify(checkInUseCase).create(any());
        verify(checkOutUseCase).create(any());
    }

    @Test
    @WithMockUser(roles = "HOUSEKEEPING")
    void nonOperationalRoleCannotSubmitStayMaterialization() throws Exception {
        mockMvc.perform(post("/check-ins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":23}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/check-outs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":23}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(checkInUseCase);
        verifyNoInteractions(checkOutUseCase);
    }

    @Test
    void anonymousStayMaterializationIsRejected() throws Exception {
        mockMvc.perform(post("/check-ins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":23}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/check-outs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingId\":23}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(checkInUseCase);
        verifyNoInteractions(checkOutUseCase);
    }
}
