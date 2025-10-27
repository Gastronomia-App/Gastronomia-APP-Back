package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.model.entity.Product;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {ProductComponentMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    ProductResponseDTO toDTO(Product product);

    List<ProductResponseDTO> toDTOList(List<Product> products);

    @Mapping(target = "components", ignore = true)
    @Mapping(target = "productGroups", ignore = true)
    Product toEntity(ProductRequestDTO dto);

    @Mapping(target = "components", ignore = true)
    @Mapping(target = "productGroups", ignore = true)
    Product updateProductFromDTO(@MappingTarget Product product, ProductRequestDTO dto);
}