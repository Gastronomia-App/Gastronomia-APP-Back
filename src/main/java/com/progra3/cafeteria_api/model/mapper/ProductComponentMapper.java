package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductComponentRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductComponentResponseDTO;
import com.progra3.cafeteria_api.model.entity.ProductComponent;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder (disableBuilder = true))
public interface ProductComponentMapper {
    @Mapping(source = "product.name", target = "name")
    @Mapping(source = "product.id", target = "productId")
    ProductComponentResponseDTO toDTO(ProductComponent productComponent);

    ProductComponent toEntity(ProductComponentRequestDTO dto);
}
