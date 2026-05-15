package com.ticketsystem.service;

import com.ticketsystem.dto.EmployeeDTO;
import com.ticketsystem.entity.Employee;
import com.ticketsystem.exception.ResourceNotFoundException;
import com.ticketsystem.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        return toDTO(findById(id));
    }

    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        Employee emp = Employee.builder()
                .employeeName(dto.getEmployeeName())
                .supportLevel(dto.getSupportLevel())
                .build();
        return toDTO(employeeRepository.save(emp));
    }

    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        Employee emp = findById(id);
        emp.setEmployeeName(dto.getEmployeeName());
        emp.setSupportLevel(dto.getSupportLevel());
        return toDTO(employeeRepository.save(emp));
    }

    public void deleteEmployee(Long id) {
        employeeRepository.delete(findById(id));
    }

    private Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private EmployeeDTO toDTO(Employee e) {
        return EmployeeDTO.builder()
                .id(e.getId())
                .employeeName(e.getEmployeeName())
                .supportLevel(e.getSupportLevel())
                .build();
    }
}
