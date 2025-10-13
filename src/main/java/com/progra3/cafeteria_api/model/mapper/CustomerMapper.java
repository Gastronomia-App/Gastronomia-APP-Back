package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.CustomerRequestDTO;
import com.progra3.cafeteria_api.model.dto.CustomerResponseDTO;
import com.progra3.cafeteria_api.model.dto.CustomerUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Business;
import com.progra3.cafeteria_api.model.entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper {
    CustomerResponseDTO toDTO(Customer customer);

    Customer toEntity(CustomerRequestDTO customerRequestDTO);

    void updateCustomerFromDTO(CustomerUpdateDTO customerUpdateDTO, @MappingTarget Customer customer);
}
