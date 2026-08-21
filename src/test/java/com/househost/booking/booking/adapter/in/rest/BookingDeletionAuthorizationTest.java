package com.househost.booking.booking.adapter.in.rest;

import com.househost.booking.booking.application.port.in.BookingFormUseCase;
import com.househost.booking.booking.application.port.in.BookingUseCase;
import com.househost.security.adapter.in.config.SecurityConfig;
import com.househost.security.adapter.in.web.JwtAuthenticationFilter;
import com.househost.security.application.service.AuthenticationService;
import com.househost.ratings.domain.exception.RatingConflictException;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(BookingController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class BookingDeletionAuthorizationTest {
    @Autowired MockMvc mockMvc;
    @MockBean BookingUseCase bookingUseCase;
    @MockBean BookingFormUseCase bookingFormUseCase;
    @MockBean AuthenticationService authenticationService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsManagementRoleToDeleteBooking() throws Exception {
        mockMvc.perform(delete("/bookings/7"))
                .andExpect(status().isOk());

        verify(bookingUseCase).delete(7L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void ratedBookingDeletionReturnsBusinessConflict() throws Exception {
        doThrow(new RatingConflictException(
                "A reserva possui avaliacao e nao pode ser removida antes do tratamento "
                        + "autorizado dessa avaliacao."
        )).when(bookingUseCase).delete(7L);

        mockMvc.perform(delete("/bookings/7"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "A reserva possui avaliacao e nao pode ser removida antes do tratamento "
                                + "autorizado dessa avaliacao."
                ));
    }

    @Test
    @WithMockUser(roles = "RECEPTION")
    void keepsOperationalRoleForbiddenFromDeletingBooking() throws Exception {
        mockMvc.perform(delete("/bookings/7"))
                .andExpect(status().isForbidden());
    }

    @Test
    void keepsNormalUnauthenticatedRequestUnauthorized() throws Exception {
        mockMvc.perform(delete("/bookings/7"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permitsInternalErrorDispatchWithoutReclassifyingItAsJwtFailure() throws Exception {
        mockMvc.perform(get("/error").with(request -> {
                    request.setDispatcherType(DispatcherType.ERROR);
                    return request;
                }))
                .andExpect(status().is5xxServerError());
    }
}
