package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.user.*;
import com.progra3.cafeteria_api.model.dto.EmployeeRequestDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeResponseDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Business;
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
        validateOwnerRole(dto.role());

        Employee existingEmployee = checkExistingEmployeeByDni(dto.dni());

        if (existingEmployee != null) {
            return handleExistingEmployee(existingEmployee, dto);
        }

        return createNewEmployee(dto);
    }

    private void validateOwnerRole(Role role) {
        if (role.equals(Role.OWNER)) {
            throw new OwnerAlreadyExistsException();
        }
    }

    private Employee checkExistingEmployeeByDni(String dni) {
        if (dni == null) {
            return null;
        }
        return employeeRepository.findByDniAndBusiness_Id(dni, employeeContext.getCurrentBusinessId());
    }

    private EmployeeResponseDTO handleExistingEmployee(Employee existingEmployee, EmployeeRequestDTO dto) {
        if (Boolean.TRUE.equals(existingEmployee.getDeleted())) {
            return reactivateEmployee(existingEmployee, dto);
        }
        throw new DniAlreadyExistsException(dto.dni());
    }

    private EmployeeResponseDTO reactivateEmployee(Employee employee, EmployeeRequestDTO dto) {
        updateEmployeeBasicInfo(employee, dto);
        employee.setDeleted(false);
        updateEmployeeCredentials(employee, dto.username(), dto.password());

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    private void updateEmployeeBasicInfo(Employee employee, EmployeeRequestDTO dto) {
        employee.setName(dto.name());
        employee.setLastName(dto.lastName());
        employee.setEmail(dto.email());
        employee.setPhoneNumber(dto.phoneNumber());
        employee.setRole(dto.role());
    }

    private void updateEmployeeCredentials(Employee employee, String baseUsername, String password) {
        String fullUsername = generateFullUsername(baseUsername, employee.getBusiness());
        employee.setUsername(fullUsername);
        employee.setPassword(passwordEncoder.encode(password));
    }

    private String generateFullUsername(String baseUsername, Business business) {
        String businessSuffix = business.getName().toLowerCase().replace(" ", "_");
        return baseUsername + "@" + businessSuffix;
    }

    private EmployeeResponseDTO createNewEmployee(EmployeeRequestDTO dto) {
        Employee employee = employeeMapper.toEntity(dto);
        employee.setBusiness(employeeContext.getCurrentBusiness());
        employee.setDeleted(false);

        updateEmployeeCredentials(employee, dto.username(), dto.password());

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    @Override
    public Employee getEntityById (Long employeeId) {
        return Optional.ofNullable(employeeId)
                .map(id -> employeeRepository.findByIdAndBusiness_Id(employeeId, employeeContext.getCurrentBusinessId())
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

    @Override
    @Transactional
    public EmployeeResponseDTO updateCurrentEmployee(EmployeeUpdateDTO dto) {
        Long currentEmployeeId = employeeContext.getCurrentEmployeeId();

        Employee employee = getEntityById(currentEmployeeId);

        if (dto.name() != null && !dto.name().isBlank()) {
            employee.setName(dto.name());
        }

        if (dto.lastName() != null && !dto.lastName().isBlank()) {
            employee.setLastName(dto.lastName());
        }

        if (dto.email() != null) {
            employee.setEmail(dto.email());
        }

        if (dto.phoneNumber() != null) {
            employee.setPhoneNumber(dto.phoneNumber());
        }

        if (dto.username() != null && !dto.username().isBlank()) {
            String currentUsername = employee.getUsername();
            String domain = "";
            int atIndex = currentUsername.indexOf('@');
            if (atIndex != -1) {
                domain = currentUsername.substring(atIndex);
            }
            employee.setUsername(dto.username() + domain);
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            employee.setPassword(passwordEncoder.encode(dto.password()));
        }

        Employee saved = employeeRepository.save(employee);

        return employeeMapper.toDTO(saved);
    }

}