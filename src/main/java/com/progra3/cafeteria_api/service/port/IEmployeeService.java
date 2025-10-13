package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.EmployeeRequestDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeResponseDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Employee;
import com.progra3.cafeteria_api.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEmployeeService {
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto);
    EmployeeResponseDTO getEmployeeById(Long id);
    Page<EmployeeResponseDTO> getEmployees(String name, String lastName, String dni, String email, Role role, Pageable pageable);
    EmployeeResponseDTO updateEmployee(Long id, EmployeeUpdateDTO updateDTO);
    EmployeeResponseDTO deleteEmployee(Long id);

    Employee getEntityById (Long employeeId);
}