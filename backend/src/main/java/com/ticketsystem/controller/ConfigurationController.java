package com.ticketsystem.controller;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.HolidayDTO;
import com.ticketsystem.dto.ShiftHoursDTO;
import com.ticketsystem.dto.SlaConfigDTO;
import com.ticketsystem.service.ConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService configService;

    // ── Shift Hours ──────────────────────────────────────────────────────────

    /** GET — readable by ADMIN and PROJECT_MANAGER (needed for employee shift assignment) */
    @GetMapping("/shifts")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftHoursDTO>>> getShifts() {
        return ResponseEntity.ok(ApiResponse.success("OK", configService.getAllShifts()));
    }

    @PostMapping("/shifts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShiftHoursDTO>> createShift(@Valid @RequestBody ShiftHoursDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shift created", configService.createShift(dto)));
    }

    @PutMapping("/shifts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShiftHoursDTO>> updateShift(
            @PathVariable Long id, @Valid @RequestBody ShiftHoursDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Shift updated", configService.updateShift(id, dto)));
    }

    @DeleteMapping("/shifts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteShift(@PathVariable Long id) {
        configService.deleteShift(id);
        return ResponseEntity.ok(ApiResponse.success("Shift deleted", null));
    }

    // ── Holidays ─────────────────────────────────────────────────────────────

    /** GET — readable by ADMIN and PROJECT_MANAGER */
    @GetMapping("/holidays")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<HolidayDTO>>> getHolidays() {
        return ResponseEntity.ok(ApiResponse.success("OK", configService.getAllHolidays()));
    }

    @PostMapping("/holidays")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HolidayDTO>> createHoliday(@Valid @RequestBody HolidayDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Holiday created", configService.createHoliday(dto)));
    }

    @PutMapping("/holidays/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HolidayDTO>> updateHoliday(
            @PathVariable Long id, @Valid @RequestBody HolidayDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Holiday updated", configService.updateHoliday(id, dto)));
    }

    @DeleteMapping("/holidays/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(@PathVariable Long id) {
        configService.deleteHoliday(id);
        return ResponseEntity.ok(ApiResponse.success("Holiday deleted", null));
    }

    // ── SLA Config ───────────────────────────────────────────────────────────

    /** GET — readable by ADMIN and PROJECT_MANAGER */
    @GetMapping("/sla")
    @PreAuthorize("hasAnyRole('ADMIN','PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<SlaConfigDTO>>> getSlaConfigs() {
        return ResponseEntity.ok(ApiResponse.success("OK", configService.getAllSlaConfigs()));
    }

    @PostMapping("/sla")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SlaConfigDTO>> createSlaConfig(@Valid @RequestBody SlaConfigDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("SLA config created", configService.createSlaConfig(dto)));
    }

    @PutMapping("/sla/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SlaConfigDTO>> updateSlaConfig(
            @PathVariable Long id, @Valid @RequestBody SlaConfigDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("SLA config updated", configService.updateSlaConfig(id, dto)));
    }

    @DeleteMapping("/sla/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSlaConfig(@PathVariable Long id) {
        configService.deleteSlaConfig(id);
        return ResponseEntity.ok(ApiResponse.success("SLA config deleted", null));
    }
}
