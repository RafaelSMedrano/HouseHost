package com.househost.publicapi.application.dto;

import java.math.BigDecimal;

public record PublicQuoteResponseDTO(
        boolean available,
        Long roomId,
        BigDecimal nightlyRate,
        long nights,
        BigDecimal subtotal,
        BigDecimal fees,
        BigDecimal total,
        String currency
) {
}
