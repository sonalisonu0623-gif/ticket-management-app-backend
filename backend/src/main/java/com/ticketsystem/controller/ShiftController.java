package com.ticketsystem.controller;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.ShiftDTO;
import com.ticketsystem.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShiftDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Shifts fetched", shiftService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShiftDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Shift fetched", shiftService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShiftDTO>> create(@Valid @RequestBody ShiftDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shift created", shiftService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShiftDTO>> update(
            @PathVariable Long id, @Valid @RequestBody ShiftDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Shift updated", shiftService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        shiftService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Shift deleted", null));
    }
}
