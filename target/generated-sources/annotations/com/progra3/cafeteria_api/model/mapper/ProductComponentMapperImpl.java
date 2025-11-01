package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductComponentRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductComponentResponseDTO;
import com.progra3.cafeteria_api.model.entity.ProductComponent;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-31T15:29:26-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class ProductComponentMapperImpl implements ProductComponentMapper {

    @Override
    public ProductComponentResponseDTO toDTO(ProductComponent productComponent) {
        if ( productComponent == null ) {
            return null;
        }

        Long id = null;
        Integer quantity = null;

        id = productComponent.getId();
        quantity = productComponent.getQuantity();

        String name = null;

        ProductComponentResponseDTO productComponentResponseDTO = new ProductComponentResponseDTO( id, name, quantity );

        return productComponentResponseDTO;
    }

    @Override
    public ProductComponent toEntity(ProductComponentRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ProductComponent productComponent = new ProductComponent();

        productComponent.setQuantity( dto.quantity() );

        return productComponent;
    }
}
