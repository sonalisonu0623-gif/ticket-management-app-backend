package com.ticketsystem.controller;

import com.ticketsystem.dto.ApiResponse;
import com.ticketsystem.dto.EmployeeDTO;
import com.ticketsystem.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getAll(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String supportLevel) {

        List<EmployeeDTO> employees;
        if (projectId != null && supportLevel != null) {
            employees = employeeService.getEmployeesByProjectAndLevel(projectId, supportLevel);
        } else if (projectId != null) {
            employees = employeeService.getEmployeesByProject(projectId);
        } else {
            employees = employeeService.getAllEmployees();
        }
        return ResponseEntity.ok(ApiResponse.success("Employees fetched", employees));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Employee fetched",
                employeeService.getEmployeeById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> create(@Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created", employeeService.createEmployee(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> update(
            @PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Employee updated",
                employeeService.updateEmployee(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted", null));
    }

    /** Assign employee to a single project */
    @PostMapping("/{employeeId}/projects/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> assignToProject(
            @PathVariable Long employeeId,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success("Employee assigned to project",
                employeeService.assignToProject(employeeId, projectId)));
    }

    /** Remove employee from a single project */
    @DeleteMapping("/{employeeId}/projects/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> removeFromProject(
            @PathVariable Long employeeId,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success("Employee removed from project",
                employeeService.removeFromProject(employeeId, projectId)));
    }
}
