package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.user.EmployeeDeletedException;
import com.progra3.cafeteria_api.exception.user.EmployeeNotFoundException;
import com.progra3.cafeteria_api.exception.user.InvalidPasswordException;
import com.progra3.cafeteria_api.model.dto.EmployeeResponseDTO;
import com.progra3.cafeteria_api.model.dto.LoginRequestDTO;
import com.progra3.cafeteria_api.model.dto.LoginResponseDTO;
import com.progra3.cafeteria_api.model.entity.Employee;
import com.progra3.cafeteria_api.model.mapper.EmployeeMapper;
import com.progra3.cafeteria_api.repository.EmployeeRepository;
import com.progra3.cafeteria_api.security.JwtService;
import com.progra3.cafeteria_api.security.EmployeeDetails;
import com.progra3.cafeteria_api.service.port.IAuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {

        Employee employee = employeeRepository.findByUsername(dto.username())
                .orElseThrow(() -> new EmployeeNotFoundException(dto.username()));

        if (!passwordEncoder.matches(dto.password(), employee.getPassword())) {
            throw new InvalidPasswordException();
        }

        if (employee.getDeleted()) {
            throw new EmployeeDeletedException();
        }

        EmployeeDetails userDetails = new EmployeeDetails(
                employee.getUsername(),
                employee.getPassword(),
                employee.getId(),
                List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())),
                employee.getBusiness().getId()
        );

        String token = jwtService.generateToken(userDetails);

        EmployeeResponseDTO employeeDTO = employeeMapper.toDTO(employee);

        return LoginResponseDTO.builder()
                .token(token)
                .employee(employeeDTO)
                .build();
    }
}