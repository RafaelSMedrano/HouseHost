package com.househost.metrics.application.port.in;

import com.househost.metrics.application.dto.MetricsSummaryDTO;

public interface MetricsUseCase {
    MetricsSummaryDTO summary();
}
