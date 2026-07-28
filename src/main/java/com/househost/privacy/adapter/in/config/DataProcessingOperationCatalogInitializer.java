package com.househost.privacy.adapter.in.config;

import com.househost.privacy.legalbasis.application.port.in.ProcessingLegalBasisAssessmentCatalogUseCase;
import com.househost.privacy.processing.application.port.in.DataProcessingOperationCatalogUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class DataProcessingOperationCatalogInitializer implements ApplicationRunner {
    private final DataProcessingOperationCatalogUseCase catalogUseCase;
    private final ProcessingLegalBasisAssessmentCatalogUseCase assessmentCatalogUseCase;

    public DataProcessingOperationCatalogInitializer(
            DataProcessingOperationCatalogUseCase catalogUseCase,
            ProcessingLegalBasisAssessmentCatalogUseCase assessmentCatalogUseCase
    ) {
        this.catalogUseCase = catalogUseCase;
        this.assessmentCatalogUseCase = assessmentCatalogUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        catalogUseCase.initializeCatalog();
        assessmentCatalogUseCase.initializeCatalog();
    }
}
