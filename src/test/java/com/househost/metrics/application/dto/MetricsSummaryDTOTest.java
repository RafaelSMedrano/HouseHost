package com.househost.metrics.application.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MetricsSummaryDTOTest {

    @Test
    void exposesNoGuestPetAssociation() {
        Set<String> metricFieldNameSet = Arrays.stream(MetricsSummaryDTO.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .collect(Collectors.toSet());

        assertFalse(metricFieldNameSet.contains("guestsWithPets"));
    }
}
