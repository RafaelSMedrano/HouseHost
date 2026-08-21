package com.househost.finance.financialtransaction.adapter.in.rest;

import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanUseCase;
import com.househost.finance.financialtransaction.application.port.in.FinancialTransactionPlanReplacementUseCase;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FinancialTransactionPlanController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class FinancialTransactionPlanAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinancialTransactionPlanUseCase financialTransactionPlanUseCase;

    @MockBean
    private FinancialTransactionPlanReplacementUseCase financialTransactionPlanReplacementUseCase;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser(roles = "RECEPTION")
    void operationalRoleReadsOnlyMinimizedContracts() throws Exception {
        mockMvc.perform(get("/financial-transaction-plans/booking/40"))
                .andExpect(status().isOk());
        mockMvc.perform(get(
                        "/financial-transaction-plans/50/scheduled/PLAN_CHECK_IN_PAYMENT"
                ))
                .andExpect(status().isOk());
        mockMvc.perform(get(
                        "/financial-transaction-plans/commands/reservation/reservation-command"
                ))
                .andExpect(status().isOk());
        mockMvc.perform(post(
                        "/financial-transaction-plans/50/scheduled/PLAN_CHECK_IN_PAYMENT/replace"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledFinancialTransactionId": 101,
                                  "structure": "SIMPLE",
                                  "method": "PIX",
                                  "idempotencyKey": "replacement-command"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(get(
                        "/financial-transaction-plans/50/commands/replacement/replacement-command"
                ))
                .andExpect(status().isOk());
        mockMvc.perform(get("/financial-transaction-plans/50"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/financial-transaction-plans/50/deadline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planDueDate\":\"2027-01-10\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/financial-transaction-plans/50"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/financial-transaction-plans/50/cancel"))
                .andExpect(status().isForbidden());

        verify(financialTransactionPlanUseCase).findByBookingId(40L);
        verify(financialTransactionPlanUseCase).findScheduledComponent(
                50L,
                com.househost.finance.financialtransaction.domain.model
                        .FinancialTransactionType.PLAN_CHECK_IN_PAYMENT
        );
        verify(financialTransactionPlanUseCase)
                .reconcileReservationCreation("reservation-command");
        verify(financialTransactionPlanReplacementUseCase).replace(
                org.mockito.ArgumentMatchers.any()
        );
        verify(financialTransactionPlanReplacementUseCase).reconcile(
                50L,
                "replacement-command"
        );
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managementRoleAccessesProfileAndCommands() throws Exception {
        mockMvc.perform(get("/financial-transaction-plans/50"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/financial-transaction-plans/50/deadline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"planDueDate\":\"2027-01-10\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/financial-transaction-plans/50"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/financial-transaction-plans/50/cancel"))
                .andExpect(status().isOk());

        verify(financialTransactionPlanUseCase).findProfile(50L);
        verify(financialTransactionPlanUseCase).extendDeadline(
                50L,
                java.time.LocalDate.of(2027, 1, 10)
        );
        verify(financialTransactionPlanUseCase).delete(50L);
        verify(financialTransactionPlanUseCase).cancel(50L);
    }

    @Test
    @WithMockUser(roles = "HOUSEKEEPING")
    void nonOperationalRoleCannotReadPlanContracts() throws Exception {
        mockMvc.perform(get("/financial-transaction-plans/booking/40"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/financial-transaction-plans/50"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(
                        "/financial-transaction-plans/50/scheduled/PLAN_CHECK_IN_PAYMENT/replace"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(financialTransactionPlanUseCase);
        verifyNoInteractions(financialTransactionPlanReplacementUseCase);
    }

    @Test
    void anonymousAccessIsRejected() throws Exception {
        mockMvc.perform(get("/financial-transaction-plans/booking/40"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/financial-transaction-plans/50"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(
                        "/financial-transaction-plans/50/scheduled/PLAN_CHECK_IN_PAYMENT/replace"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(financialTransactionPlanUseCase);
        verifyNoInteractions(financialTransactionPlanReplacementUseCase);
    }
}
