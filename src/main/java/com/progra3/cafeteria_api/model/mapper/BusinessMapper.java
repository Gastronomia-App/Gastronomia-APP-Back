package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.BusinessRequestDTO;
import com.progra3.cafeteria_api.model.dto.BusinessResponseDTO;
import com.progra3.cafeteria_api.model.entity.Business;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        uses = {AddressMapper.class, EmployeeMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessMapper {
    BusinessResponseDTO toDTO(Business business);

    Business toEntity(BusinessRequestDTO dto);
}
