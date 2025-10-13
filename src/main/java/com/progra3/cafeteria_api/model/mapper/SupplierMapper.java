package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.SupplierRequestDTO;
import com.progra3.cafeteria_api.model.dto.SupplierResponseDTO;
import com.progra3.cafeteria_api.model.dto.SupplierUpdateDTO;
import com.progra3.cafeteria_api.model.entity.Supplier;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {AddressMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplierMapper {
    SupplierResponseDTO toDTO(Supplier supplier);

    Supplier toEntity(SupplierRequestDTO supplierRequestDTO);

    Supplier updateSupplierFromDTO(SupplierUpdateDTO supplierUpdateDTO, @MappingTarget Supplier supplier);
}
