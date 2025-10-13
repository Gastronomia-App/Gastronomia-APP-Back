package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductComponentRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductComponentResponseDTO;
import com.progra3.cafeteria_api.model.entity.ProductComponent;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder (disableBuilder = true))
public interface ProductComponentMapper {
    @Mapping(target = "productName", source = "product.name")
    ProductComponentResponseDTO toDTO(ProductComponent productComponent);

    ProductComponent toEntity(ProductComponentRequestDTO dto);
}
