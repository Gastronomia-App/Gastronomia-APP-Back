package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.EmployeeRequestDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeResponseDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Employee;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-17T00:03:11-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public EmployeeResponseDTO toDTO(Employee employee) {
        if ( employee == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String lastName = null;
        String dni = null;
        String phoneNumber = null;
        String email = null;
        Boolean deleted = null;

        id = employee.getId();
        name = employee.getName();
        lastName = employee.getLastName();
        dni = employee.getDni();
        phoneNumber = employee.getPhoneNumber();
        email = employee.getEmail();
        deleted = employee.getDeleted();

        String role = employee.getRole().name();

        EmployeeResponseDTO employeeResponseDTO = new EmployeeResponseDTO( id, name, lastName, dni, phoneNumber, email, role, deleted );

        return employeeResponseDTO;
    }

    @Override
    public Employee toEntity(EmployeeRequestDTO employeeRequestDTO) {
        if ( employeeRequestDTO == null ) {
            return null;
        }

        Employee employee = new Employee();

        employee.setName( employeeRequestDTO.name() );
        employee.setLastName( employeeRequestDTO.lastName() );
        employee.setDni( employeeRequestDTO.dni() );
        employee.setPhoneNumber( employeeRequestDTO.phoneNumber() );
        employee.setEmail( employeeRequestDTO.email() );
        employee.setUsername( employeeRequestDTO.username() );
        employee.setPassword( employeeRequestDTO.password() );
        employee.setRole( employeeRequestDTO.role() );

        return employee;
    }

    @Override
    public Employee updateEmployeeFromDTO(EmployeeUpdateDTO employeeUpdateDTO, Employee employee) {
        if ( employeeUpdateDTO == null ) {
            return employee;
        }

        if ( employeeUpdateDTO.name() != null ) {
            employee.setName( employeeUpdateDTO.name() );
        }
        if ( employeeUpdateDTO.lastName() != null ) {
            employee.setLastName( employeeUpdateDTO.lastName() );
        }
        if ( employeeUpdateDTO.dni() != null ) {
            employee.setDni( employeeUpdateDTO.dni() );
        }
        if ( employeeUpdateDTO.phoneNumber() != null ) {
            employee.setPhoneNumber( employeeUpdateDTO.phoneNumber() );
        }
        if ( employeeUpdateDTO.email() != null ) {
            employee.setEmail( employeeUpdateDTO.email() );
        }
        if ( employeeUpdateDTO.username() != null ) {
            employee.setUsername( employeeUpdateDTO.username() );
        }
        if ( employeeUpdateDTO.password() != null ) {
            employee.setPassword( employeeUpdateDTO.password() );
        }
        if ( employeeUpdateDTO.role() != null ) {
            employee.setRole( employeeUpdateDTO.role() );
        }

        return employee;
    }
}
