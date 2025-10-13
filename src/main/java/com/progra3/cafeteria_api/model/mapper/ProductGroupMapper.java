package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductGroupRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductGroupResponseDTO;
import com.progra3.cafeteria_api.model.entity.ProductGroup;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {ProductOptionMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductGroupMapper {

    ProductGroupResponseDTO toDTO(ProductGroup productGroup);

    List<ProductGroupResponseDTO> toDTOList(List<ProductGroup> productGroups);

    ProductGroup toEntity(ProductGroupRequestDTO productGroupRequestDTO);

    ProductGroup updateProductGroupFromDTO(@MappingTarget ProductGroup productGroup, ProductGroupRequestDTO productGroupRequestDTO);
}
