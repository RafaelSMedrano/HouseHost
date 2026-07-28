package com.househost.supplier.adapter.in.rest;

import com.househost.shared.dto.ResponseDTO;
import com.househost.supplier.application.dto.*;
import com.househost.supplier.application.port.in.SupplierUseCase;
import com.househost.supplier.domain.model.*;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    private final SupplierUseCase supplierUseCase;
    public SupplierController(SupplierUseCase supplierUseCase) { this.supplierUseCase = supplierUseCase; }

    @PostMapping
    public ResponseDTO create(@RequestBody SupplierRequestDTO request) {
        return new ResponseDTO("success", "Fornecedor cadastrado com sucesso", supplierUseCase.create(request));
    }

    @GetMapping
    public ResponseDTO findAll(@RequestParam(required = false) String name,
            @RequestParam(required = false) SupplierDataRole role,
            @RequestParam(required = false) SupplierRiskLevel risk,
            @RequestParam(required = false) SupplierGovernanceStatus governanceStatus,
            @RequestParam(required = false) SupplierStatus status) {
        List<SupplierListResponseDTO> supplierList = supplierUseCase.findAll(name, role, risk, governanceStatus, status);
        return new ResponseDTO("success", "Fornecedores encontrados com sucesso", supplierList);
    }

    @GetMapping("/{id}")
    public ResponseDTO findById(@PathVariable Long id) {
        return new ResponseDTO("success", "Fornecedor encontrado com sucesso", supplierUseCase.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseDTO update(@PathVariable Long id, @RequestBody SupplierRequestDTO request) {
        return new ResponseDTO("success", "Fornecedor atualizado com sucesso", supplierUseCase.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseDTO changeStatus(@PathVariable Long id, @RequestBody SupplierStatusRequestDTO request) {
        return new ResponseDTO("success", "Status do fornecedor atualizado com sucesso", supplierUseCase.changeStatus(id, request));
    }

    @PostMapping("/{id}/relationships/{relationshipId}/review")
    public ResponseDTO reviewRelationship(@PathVariable Long id, @PathVariable Long relationshipId,
            @RequestBody SupplierReviewRequestDTO request, Authentication authentication) {
        String reviewerEmail = authentication == null ? null : authentication.getName();
        return new ResponseDTO("success", "Relacao revisada com sucesso",
                supplierUseCase.reviewRelationship(id, relationshipId, request, reviewerEmail));
    }
}
