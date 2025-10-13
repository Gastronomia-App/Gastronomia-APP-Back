package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.AddressRequestDTO;
import com.progra3.cafeteria_api.model.dto.AddressResponseDTO;
import com.progra3.cafeteria_api.model.dto.AddressUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {
    AddressResponseDTO toDTO(Address address);

    Address toEntity(AddressRequestDTO addressRequestDTO);

    void updateAddressFromDTO(AddressUpdateDTO addressUpdateDTO, @MappingTarget Address address);
}
