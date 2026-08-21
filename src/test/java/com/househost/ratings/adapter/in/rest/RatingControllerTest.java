package com.househost.ratings.adapter.in.rest;

import com.househost.ratings.application.dto.RatingPageResponseDTO;
import com.househost.ratings.application.dto.RatingRequestDTO;
import com.househost.ratings.application.port.in.RatingUseCase;
import com.househost.ratings.application.records.RatingPageRecord;
import com.househost.ratings.domain.exception.RatingConflictException;
import com.househost.ratings.domain.exception.RatingEligibilityException;
import com.househost.ratings.domain.exception.RatingException;
import com.househost.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RatingControllerTest {

    private RatingUseCase ratingUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ratingUseCase = mock(RatingUseCase.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RatingController(ratingUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsThroughUseCaseWithExactSixScoreContract() throws Exception {
        mockMvc.perform(post("/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookingId": 42,
                                  "checkInProcedureScore": 5,
                                  "checkOutProcedureScore": 4,
                                  "accommodationCleanlinessScore": 5,
                                  "teamCommunicationScore": 4,
                                  "locationScore": 5,
                                  "comfortScore": 4,
                                  "observations": "Otima estadia"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        ArgumentCaptor<RatingRequestDTO> ratingRequestDTOCaptor =
                ArgumentCaptor.forClass(RatingRequestDTO.class);
        verify(ratingUseCase).createForCompletedBooking(ratingRequestDTOCaptor.capture());
        RatingRequestDTO ratingRequestDTO = ratingRequestDTOCaptor.getValue();
        assertEquals(42L, ratingRequestDTO.bookingId);
        assertEquals(5, ratingRequestDTO.checkInProcedureScore);
        assertEquals(4, ratingRequestDTO.comfortScore);
    }

    @Test
    void delegatesBoundedListAndGuestHistoryQueries() throws Exception {
        RatingPageResponseDTO emptyRatingPageResponseDTO = new RatingPageResponseDTO(
                new RatingPageRecord(List.of(), 0, 20, 0, 0)
        );
        when(ratingUseCase.findAll(0, 20)).thenReturn(emptyRatingPageResponseDTO);
        when(ratingUseCase.findByGuestId(7L, 1, 10)).thenReturn(
                new RatingPageResponseDTO(new RatingPageRecord(List.of(), 1, 10, 0, 0))
        );

        mockMvc.perform(get("/ratings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.ratingSummaryDTOList").isArray());
        mockMvc.perform(get("/ratings/guest/7?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1));

        verify(ratingUseCase).findAll(0, 20);
        verify(ratingUseCase).findByGuestId(7L, 1, 10);
    }

    @Test
    void exposesStableRatingErrorSemantics() throws Exception {
        when(ratingUseCase.findAll(0, 20)).thenThrow(new RatingException("invalid"));
        when(ratingUseCase.findByGuestId(7L, 0, 20)).thenThrow(
                new RatingEligibilityException("ineligible")
        );
        when(ratingUseCase.findByGuestId(8L, 0, 20)).thenThrow(
                new RatingConflictException("duplicate")
        );

        mockMvc.perform(get("/ratings"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid"));
        mockMvc.perform(get("/ratings/guest/7"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("ineligible"));
        mockMvc.perform(get("/ratings/guest/8"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("duplicate"));
    }

    @Test
    void keepsControllerOnUseCaseBoundaryWithoutIndividualDetailContract() {
        assertEquals(
                RatingUseCase.class,
                RatingController.class.getDeclaredFields()[0].getType()
        );
        assertFalse(Arrays.stream(RatingUseCase.class.getDeclaredMethods())
                .map(java.lang.reflect.Method::getName)
                .anyMatch(methodName -> methodName.equals("findById")
                        || methodName.equals("update")
                        || methodName.equals("delete")));
    }
}
