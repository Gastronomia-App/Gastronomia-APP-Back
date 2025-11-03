package com.progra3.cafeteria_api.security;

import com.progra3.cafeteria_api.model.entity.Employee;
import com.progra3.cafeteria_api.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Employee employee = employeeRepository.findByUsernameWithBusiness(username)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found"));

        // Validar que el empleado no esté eliminado
        if (employee.getDeleted()) {
            throw new UsernameNotFoundException("Employee account is disabled");
        }

        // Validar que el negocio del empleado no esté eliminado
        if (employee.getBusiness().getDeleted()) {
            throw new UsernameNotFoundException("Business account is disabled");
        }

        return new EmployeeDetails(
                username,
                employee.getPassword(),
                employee.getId(),
                List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())),
                employee.getBusiness().getId()
        );
    }
}
