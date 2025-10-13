package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.CategoryRequestDTO;
import com.progra3.cafeteria_api.model.dto.CategoryResponseDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.model.entity.Category;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-17T00:03:11-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public CategoryResponseDTO toDTO(Category category) {
        if ( category == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        List<ProductResponseDTO> products = null;

        id = category.getId();
        name = category.getName();
        products = productMapper.toDTOList( category.getProducts() );

        CategoryResponseDTO categoryResponseDTO = new CategoryResponseDTO( id, name, products );

        return categoryResponseDTO;
    }

    @Override
    public Category toEntity(CategoryRequestDTO categoryRequestDTO) {
        if ( categoryRequestDTO == null ) {
            return null;
        }

        Category category = new Category();

        category.setName( categoryRequestDTO.name() );

        return category;
    }
}
