package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.CustomerRequestDTO;
import com.progra3.cafeteria_api.model.dto.CustomerResponseDTO;
import com.progra3.cafeteria_api.model.dto.CustomerUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Customer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-17T00:03:10-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public CustomerResponseDTO toDTO(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String lastName = null;
        String dni = null;
        String phoneNumber = null;
        String email = null;
        Boolean deleted = null;
        Integer discount = null;

        id = customer.getId();
        name = customer.getName();
        lastName = customer.getLastName();
        dni = customer.getDni();
        phoneNumber = customer.getPhoneNumber();
        email = customer.getEmail();
        deleted = customer.getDeleted();
        discount = customer.getDiscount();

        CustomerResponseDTO customerResponseDTO = new CustomerResponseDTO( id, name, lastName, dni, phoneNumber, email, deleted, discount );

        return customerResponseDTO;
    }

    @Override
    public Customer toEntity(CustomerRequestDTO customerRequestDTO) {
        if ( customerRequestDTO == null ) {
            return null;
        }

        Customer customer = new Customer();

        customer.setName( customerRequestDTO.name() );
        customer.setLastName( customerRequestDTO.lastName() );
        customer.setDni( customerRequestDTO.dni() );
        customer.setPhoneNumber( customerRequestDTO.phoneNumber() );
        customer.setEmail( customerRequestDTO.email() );
        customer.setDiscount( customerRequestDTO.discount() );

        return customer;
    }

    @Override
    public void updateCustomerFromDTO(CustomerUpdateDTO customerUpdateDTO, Customer customer) {
        if ( customerUpdateDTO == null ) {
            return;
        }

        if ( customerUpdateDTO.name() != null ) {
            customer.setName( customerUpdateDTO.name() );
        }
        if ( customerUpdateDTO.lastName() != null ) {
            customer.setLastName( customerUpdateDTO.lastName() );
        }
        if ( customerUpdateDTO.dni() != null ) {
            customer.setDni( customerUpdateDTO.dni() );
        }
        if ( customerUpdateDTO.phoneNumber() != null ) {
            customer.setPhoneNumber( customerUpdateDTO.phoneNumber() );
        }
        if ( customerUpdateDTO.email() != null ) {
            customer.setEmail( customerUpdateDTO.email() );
        }
        if ( customerUpdateDTO.discount() != null ) {
            customer.setDiscount( customerUpdateDTO.discount() );
        }
    }
}
