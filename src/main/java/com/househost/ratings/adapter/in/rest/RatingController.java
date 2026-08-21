package com.househost.ratings.adapter.in.rest;

import com.househost.ratings.application.dto.RatingPageResponseDTO;
import com.househost.ratings.application.dto.RatingRequestDTO;
import com.househost.ratings.application.dto.RatingResponseDTO;
import com.househost.ratings.application.port.in.RatingUseCase;
import com.househost.shared.dto.ResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ratings")
public class RatingController {

    private final RatingUseCase ratingUseCase;

    public RatingController(RatingUseCase ratingUseCase) {
        this.ratingUseCase = ratingUseCase;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody RatingRequestDTO request) {
        RatingResponseDTO ratingResponseDTO =
                ratingUseCase.createForCompletedBooking(request);
        return new ResponseDTO(
                "success",
                "Avaliacao cadastrada com sucesso.",
                ratingResponseDTO
        );
    }

    @GetMapping
    public ResponseDTO findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        RatingPageResponseDTO ratingPageResponseDTO = ratingUseCase.findAll(page, size);
        return new ResponseDTO(
                "success",
                "Avaliacoes encontradas com sucesso.",
                ratingPageResponseDTO
        );
    }

    @GetMapping("/guest/{guestId}")
    public ResponseDTO findByGuestId(
            @PathVariable Long guestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        RatingPageResponseDTO ratingPageResponseDTO =
                ratingUseCase.findByGuestId(guestId, page, size);
        return new ResponseDTO(
                "success",
                "Historico de avaliacoes encontrado com sucesso.",
                ratingPageResponseDTO
        );
    }
}
