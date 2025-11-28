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
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeContext employeeContext;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;

    // =====================  Helpers de username  =====================

    private String extractBaseUsername(String username) {
        if (username == null) {
            return null;
        }
        int atIndex = username.indexOf('@');
        return atIndex == -1 ? username : username.substring(0, atIndex);
    }

    private String generateFullUsername(String baseUsername, Business business) {
        if (baseUsername == null || baseUsername.isBlank() || business == null) {
            return null;
        }
        String businessSuffix = business.getName()
                .toLowerCase()
                .replace(" ", "_");
        return baseUsername + "@" + businessSuffix;
    }

    private void setUsernameWithBusiness(Employee employee, String rawUsername) {
        String baseUsername = extractBaseUsername(rawUsername);
        if (baseUsername == null || baseUsername.isBlank()) {
            return;
        }
        String fullUsername = generateFullUsername(baseUsername, employee.getBusiness());
        if (fullUsername != null) {
            employee.setUsername(fullUsername);
        }
    }

    private void updateEmployeeCredentials(Employee employee, String rawUsername, String password) {
        setUsernameWithBusiness(employee, rawUsername);
        employee.setPassword(passwordEncoder.encode(password));
    }

    // =====================  CREATE  =====================

    @Override
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {
        validateOwnerRole(dto.role());

        Employee existingEmployee = checkExistingEmployeeByDni(dto.dni());

        if (existingEmployee != null) {
            // Reactivación o DNI duplicado
            return handleExistingEmployee(existingEmployee, dto);
        }

        // Nuevo empleado: validar unicidad
        validateUniqueFieldsOnCreate(dto);

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
            // Reactivación: misma entidad, pero validar colisiones
            validateUniqueFieldsForExistingEmployee(existingEmployee, dto);
            return reactivateEmployee(existingEmployee, dto);
        }
        // Empleado activo con mismo DNI
        throw new DniAlreadyExistsException(dto.dni());
    }

    // --- Validaciones para creación de nuevo empleado ---

    private void validateUniqueFieldsOnCreate(EmployeeRequestDTO dto) {
        Long businessId = employeeContext.getCurrentBusinessId();

        if (dto.email() != null &&
                employeeRepository.existsByEmailAndBusiness_Id(dto.email(), businessId)) {
            throw new EmailAlreadyExistsException(dto.email());
        }

        if (dto.phoneNumber() != null &&
                employeeRepository.existsByPhoneNumberAndBusiness_Id(dto.phoneNumber(), businessId)) {
            throw new PhoneNumberAlreadyExistsException(dto.phoneNumber());
        }

        String baseUsername = extractBaseUsername(dto.username());
        if (baseUsername != null && !baseUsername.isBlank() &&
                employeeRepository.existsByBaseUsernameAndBusiness(baseUsername, businessId)) {
            throw new UsernameAlreadyExistsException(baseUsername);
        }
    }

    // --- Validaciones para reactivar empleado eliminado ---

    private void validateUniqueFieldsForExistingEmployee(Employee existingEmployee, EmployeeRequestDTO dto) {
        Long businessId = employeeContext.getCurrentBusinessId();
        Long employeeId = existingEmployee.getId();

        if (dto.email() != null &&
                employeeRepository.existsByEmailAndBusiness_IdAndIdNot(dto.email(), businessId, employeeId)) {
            throw new EmailAlreadyExistsException(dto.email());
        }

        if (dto.phoneNumber() != null &&
                employeeRepository.existsByPhoneNumberAndBusiness_IdAndIdNot(dto.phoneNumber(), businessId, employeeId)) {
            throw new PhoneNumberAlreadyExistsException(dto.phoneNumber());
        }

        String baseUsername = extractBaseUsername(dto.username());
        if (baseUsername != null && !baseUsername.isBlank() &&
                employeeRepository.existsByBaseUsernameAndBusinessAndIdNot(baseUsername, businessId, employeeId)) {
            throw new UsernameAlreadyExistsException(baseUsername);
        }
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

    private EmployeeResponseDTO createNewEmployee(EmployeeRequestDTO dto) {
        Employee employee = employeeMapper.toEntity(dto);
        employee.setBusiness(employeeContext.getCurrentBusiness());
        employee.setDeleted(false);

        updateEmployeeCredentials(employee, dto.username(), dto.password());

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    // =====================  LECTURA / DELETE  =====================

    @Override
    public Employee getEntityById(Long employeeId) {
        return Optional.ofNullable(employeeId)
                .map(id -> employeeRepository.findByIdAndBusiness_Id(employeeId, employeeContext.getCurrentBusinessId())
                        .orElseThrow(() -> new EmployeeNotFoundException(employeeId)))
                .orElse(null);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO deleteEmployee(Long id) {
        Employee employee = getEntityById(id);
        employee.setDeleted(true);

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    // =====================  UPDATE (cualquier empleado)  =====================

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeUpdateDTO dto) {
        Employee employee = getEntityById(id);

        // Validar unicidad antes de aplicar cambios
        validateUniqueFieldsOnUpdate(employee, dto);

        employee = employeeMapper.updateEmployeeFromDTO(dto, employee);

        // Username: misma lógica que para el perfil actual
        if (dto.username() != null && !dto.username().isBlank()) {
            setUsernameWithBusiness(employee, dto.username());
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            employee.setPassword(passwordEncoder.encode(dto.password()));
        }

        return employeeMapper.toDTO(employeeRepository.save(employee));
    }

    // --- Validaciones para UPDATE (admin actualiza a cualquier empleado) ---

    private void validateUniqueFieldsOnUpdate(Employee employee, EmployeeUpdateDTO dto) {
        Long businessId = employeeContext.getCurrentBusinessId();
        Long employeeId = employee.getId();

        if (dto.dni() != null && !dto.dni().isBlank() &&
                employeeRepository.existsByDniAndBusiness_IdAndIdNot(dto.dni(), businessId, employeeId)) {
            throw new DniAlreadyExistsException(dto.dni());
        }

        if (dto.email() != null &&
                employeeRepository.existsByEmailAndBusiness_IdAndIdNot(dto.email(), businessId, employeeId)) {
            throw new EmailAlreadyExistsException(dto.email());
        }

        if (dto.phoneNumber() != null &&
                employeeRepository.existsByPhoneNumberAndBusiness_IdAndIdNot(dto.phoneNumber(), businessId, employeeId)) {
            throw new PhoneNumberAlreadyExistsException(dto.phoneNumber());
        }

        String baseUsername = extractBaseUsername(dto.username());
        if (baseUsername != null && !baseUsername.isBlank() &&
                employeeRepository.existsByBaseUsernameAndBusinessAndIdNot(baseUsername, businessId, employeeId)) {
            throw new UsernameAlreadyExistsException(baseUsername);
        }
    }

    // =====================  Otros métodos  =====================

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        return employeeMapper.toDTO(getEntityById(id));
    }

    @Override
    @Transactional
    public Page<EmployeeResponseDTO> getEmployees(String name, String lastName, String dni, String email, Role role, Pageable pageable) {
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

    // =====================  UPDATE perfil actual  =====================

    @Override
    @Transactional
    public EmployeeResponseDTO updateCurrentEmployee(EmployeeUpdateDTO dto) {
        Long currentEmployeeId = employeeContext.getCurrentEmployeeId();

        Employee employee = getEntityById(currentEmployeeId);

        // Mismas validaciones que en updateEmployee
        validateUniqueFieldsOnUpdate(employee, dto);

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
            setUsernameWithBusiness(employee, dto.username());
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            employee.setPassword(passwordEncoder.encode(dto.password()));
        }

        Employee saved = employeeRepository.save(employee);

        return employeeMapper.toDTO(saved);
    }
}