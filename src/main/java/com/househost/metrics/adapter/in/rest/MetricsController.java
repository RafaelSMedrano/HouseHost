package com.househost.metrics.adapter.in.rest;

import com.househost.metrics.application.dto.MetricsSummaryDTO;
import com.househost.metrics.application.port.in.MetricsUseCase;
import com.househost.shared.dto.ResponseDTO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private final MetricsUseCase metricsUseCase;

    public MetricsController(MetricsUseCase metricsUseCase) {
        this.metricsUseCase = metricsUseCase;
    }

    @GetMapping("/summary")
    public ResponseDTO summary() {
        MetricsSummaryDTO data = metricsUseCase.summary();
        return new ResponseDTO("success", "Metricas encontradas com sucesso", data);
    }
}
