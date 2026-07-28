package com.househost.metrics.application.service;

import com.househost.metrics.application.dto.MetricsSummaryDTO;
import com.househost.metrics.application.port.in.MetricsUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricsService implements MetricsUseCase {
    private final MetricsDataService dataService;
    private final MetricsCalculationService calculationService;

    public MetricsService(MetricsDataService dataService, MetricsCalculationService calculationService) {
        this.dataService = dataService;
        this.calculationService = calculationService;
    }

    @Override
    @Transactional(readOnly = true)
    public MetricsSummaryDTO summary() {
        return calculationService.calculate(dataService.load());
    }
}
