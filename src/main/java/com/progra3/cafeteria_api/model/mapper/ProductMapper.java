package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.ProductGroup;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {ProductComponentMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(source = "category.name", target = "categoryName")
    ProductResponseDTO toDTO(Product product);

    List<ProductResponseDTO> toDTOList(List<Product> products);

    Product toEntity(ProductRequestDTO dto);

    Product updateProductFromDTO(@MappingTarget Product product, ProductRequestDTO dto);

    default List<String> map(Set<ProductGroup> groups) {
        return groups.stream()
                .map(ProductGroup::getName)
                .toList();
    }
}