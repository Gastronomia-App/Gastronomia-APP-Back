package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.user.*;
import com.progra3.cafeteria_api.model.dto.EmployeeRequestDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeResponseDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Employee;
import com.progra3.cafeteria_api.model.enums.Role;
import com.progra3.cafeteria_api.model.mapper.EmployeeMapper;
import com.progra3.cafeteria_api.repository.EmployeeRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.IEmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService{

    private final EmployeeRepository employeeRepository;

    private final EmployeeContext employeeContext;

    private final EmployeeMapper employeeMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto){

        if (dto.role().equals(Role.OWNER)){
            throw new OwnerAlreadyExistsException();
        }

        Employee employee = employeeMapper.toEntity(dto);
        employee.setBusiness(employeeContext.getCurrentBusiness());
        employee.setDeleted(false);
        employee.setUsername(employee.getUsername() + "@" + employee.getBusiness().getName().toLowerCase().replace(" ", "_"));
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    @Override
    public Employee getEntityById (Long employeeId) {
        return Optional.ofNullable(employeeId)
                .map(customer -> employeeRepository.findByIdAndBusiness_Id(employeeId, employeeContext.getCurrentBusinessId())
                        .orElseThrow(() -> new EmployeeNotFoundException(employeeId)))
                .orElse(null);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO deleteEmployee(Long id){
        Employee employee = getEntityById(id);
        employee.setDeleted(true);

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeUpdateDTO dto){
        Employee employee = getEntityById(id);
        employee = employeeMapper.updateEmployeeFromDTO(dto, employee);

        if (dto.password() != null && !dto.password().isBlank()) {
            employee.setPassword(passwordEncoder.encode(dto.password()));
        }

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id){
        return employeeMapper.toDTO(getEntityById(id));
    }

    @Override
    @Transactional
    public Page<EmployeeResponseDTO> getEmployees(String name, String lastName, String dni, String email, Role role, Pageable pageable){
        Page<Employee> employees = employeeRepository.findByBusiness_Id(
                name,
                lastName,
                dni,
                email,
                role,
                employeeContext.getCurrentBusinessId(),
                pageable);

        return employees.map(employeeMapper::toDTO);
    }
}