package com.ticketsystem.controller;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.SlaConfigDTO;
import com.ticketsystem.service.SlaConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sla-configs")
@RequiredArgsConstructor
public class SlaConfigController {

    private final SlaConfigService slaConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlaConfigDTO>>> getAll(
            @RequestParam(required = false) Long projectId) {
        List<SlaConfigDTO> configs = (projectId != null)
                ? slaConfigService.getByProject(projectId)
                : slaConfigService.getAll();
        return ResponseEntity.ok(ApiResponse.success("SLA configs fetched", configs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SlaConfigDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("SLA config fetched",
                slaConfigService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SlaConfigDTO>> create(@Valid @RequestBody SlaConfigDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SLA config created", slaConfigService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SlaConfigDTO>> update(
            @PathVariable Long id, @Valid @RequestBody SlaConfigDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("SLA config updated",
                slaConfigService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        slaConfigService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("SLA config deleted", null));
    }
}
