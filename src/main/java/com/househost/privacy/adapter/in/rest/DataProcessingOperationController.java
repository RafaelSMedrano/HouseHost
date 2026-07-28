package com.househost.privacy.adapter.in.rest;

import com.househost.privacy.application.dto.DataProcessingOperationResponseDTO;
import com.househost.privacy.application.port.in.DataProcessingOperationGovernanceUseCase;
import com.househost.privacy.processing.application.dto.DataProcessingOperationRequestDTO;
import com.househost.privacy.processing.application.dto.DataProcessingOperationStatusRequestDTO;
import com.househost.privacy.processing.domain.model.DataProcessingOperationStatus;
import com.househost.shared.dto.ResponseDTO;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/data-processing-operations")
public class DataProcessingOperationController {
    private final DataProcessingOperationGovernanceUseCase governanceUseCase;

    public DataProcessingOperationController(DataProcessingOperationGovernanceUseCase governanceUseCase) {
        this.governanceUseCase = governanceUseCase;
    }

    @PostMapping
    public ResponseDTO create(@RequestBody DataProcessingOperationRequestDTO request) {
        DataProcessingOperationResponseDTO data = governanceUseCase.create(request);
        return new ResponseDTO("success", "Operacao de tratamento cadastrada com sucesso", data);
    }

    @GetMapping
    public ResponseDTO findAll(@RequestParam(required = false) DataProcessingOperationStatus status) {
        List<DataProcessingOperationResponseDTO> dataList = governanceUseCase.findAll(status);
        return new ResponseDTO("success", "Operacoes de tratamento encontradas com sucesso", dataList);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        DataProcessingOperationResponseDTO data = governanceUseCase.findById(id);
        return new ResponseDTO("success", "Operacao de tratamento encontrada com sucesso", data);
    }

    @PutMapping("/{id}")
    public ResponseDTO update(
            @PathVariable Long id,
            @RequestBody DataProcessingOperationRequestDTO request
    ) {
        DataProcessingOperationResponseDTO data = governanceUseCase.update(id, request);
        return new ResponseDTO("success", "Operacao de tratamento atualizada com sucesso", data);
    }

    @PostMapping("/{id}/review")
    public ResponseDTO review(@PathVariable Long id, Authentication authentication) {
        String authenticatedEmail = authentication == null ? null : authentication.getName();
        DataProcessingOperationResponseDTO data = governanceUseCase.review(id, authenticatedEmail);
        return new ResponseDTO("success", "Operacao de tratamento revisada com sucesso", data);
    }

    @PutMapping("/{id}/status")
    public ResponseDTO changeStatus(
            @PathVariable Long id,
            @RequestBody DataProcessingOperationStatusRequestDTO request
    ) {
        DataProcessingOperationStatus status = request == null ? null : request.status;
        DataProcessingOperationResponseDTO data = governanceUseCase.changeStatus(id, status);
        return new ResponseDTO("success", "Status da operacao de tratamento atualizado com sucesso", data);
    }
}
