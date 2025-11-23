package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.BusinessRequestDTO;
import com.progra3.cafeteria_api.model.dto.BusinessResponseDTO;
import com.progra3.cafeteria_api.model.dto.BusinessUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Business;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        uses = {AddressMapper.class, EmployeeMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BusinessMapper {
    @Mapping(target = "employeesCount", ignore = true)
    @Mapping(target = "activeEmployeesCount", ignore = true)
    @Mapping(target = "inactiveEmployeesCount", ignore = true)
    BusinessResponseDTO toDTO(Business business);

    Business toEntity(BusinessRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employees", ignore = true)
    @Mapping(target = "customers", ignore = true)
    @Mapping(target = "suppliers", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "tables", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "audits", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "productGroups", ignore = true)
    void updateBusinessFromDTO(BusinessUpdateDTO dto, @MappingTarget Business business);
}
