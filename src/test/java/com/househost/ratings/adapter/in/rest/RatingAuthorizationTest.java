package com.househost.ratings.adapter.in.rest;

import com.househost.ratings.application.dto.RatingPageResponseDTO;
import com.househost.ratings.application.port.in.RatingUseCase;
import com.househost.ratings.application.records.RatingPageRecord;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RatingAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingUseCase ratingUseCase;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    @WithMockUser(roles = "RECEPTION")
    void operationalRoleCanCreateAndReadEveryRatingsContract() throws Exception {
        RatingPageResponseDTO emptyRatingPageResponseDTO = emptyPage();
        when(ratingUseCase.findAll(0, 20)).thenReturn(emptyRatingPageResponseDTO);
        when(ratingUseCase.findByGuestId(7L, 0, 20))
                .thenReturn(emptyRatingPageResponseDTO);

        mockMvc.perform(post("/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/ratings"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/ratings/guest/7"))
                .andExpect(status().isOk());

        verify(ratingUseCase).findAll(0, 20);
        verify(ratingUseCase).findByGuestId(7L, 0, 20);
        verify(ratingUseCase).createForCompletedBooking(any());
    }

    @Test
    @WithMockUser(roles = "HOUSEKEEPING")
    void nonOperationalRoleCannotCreateOrReadRatings() throws Exception {
        mockMvc.perform(post("/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/ratings"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/ratings/guest/7"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(ratingUseCase);
    }

    @Test
    void unauthenticatedRequestsCannotCreateOrReadRatings() throws Exception {
        mockMvc.perform(post("/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/ratings"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/ratings/guest/7"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(ratingUseCase);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void ordinaryApiExposesNoUpdatePatchOrDeleteEndpoint() throws Exception {
        mockMvc.perform(put("/ratings/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/ratings/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/ratings/9"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(ratingUseCase);
    }

    private RatingPageResponseDTO emptyPage() {
        return new RatingPageResponseDTO(
                new RatingPageRecord(List.of(), 0, 20, 0, 0)
        );
    }

    private String validRequestJson() {
        return """
                {
                  "bookingId": 42,
                  "checkInProcedureScore": 5,
                  "checkOutProcedureScore": 4,
                  "accommodationCleanlinessScore": 5,
                  "teamCommunicationScore": 4,
                  "locationScore": 5,
                  "comfortScore": 4,
                  "observations": "Conteudo pessoal que nao deve ser auditado"
                }
                """;
    }
}
