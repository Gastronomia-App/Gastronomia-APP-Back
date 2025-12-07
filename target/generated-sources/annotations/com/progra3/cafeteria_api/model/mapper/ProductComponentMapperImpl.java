package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductComponentRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductComponentResponseDTO;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.ProductComponent;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T13:54:46-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class ProductComponentMapperImpl implements ProductComponentMapper {

    @Override
    public ProductComponentResponseDTO toDTO(ProductComponent productComponent) {
        if ( productComponent == null ) {
            return null;
        }

        String name = null;
        Long productId = null;
        Long id = null;
        Integer quantity = null;

        name = productComponentProductName( productComponent );
        productId = productComponentProductId( productComponent );
        id = productComponent.getId();
        quantity = productComponent.getQuantity();

        ProductComponentResponseDTO productComponentResponseDTO = new ProductComponentResponseDTO( id, productId, name, quantity );

        return productComponentResponseDTO;
    }

    @Override
    public ProductComponent toEntity(ProductComponentRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ProductComponent productComponent = new ProductComponent();

        productComponent.setId( dto.id() );
        productComponent.setQuantity( dto.quantity() );

        return productComponent;
    }

    private String productComponentProductName(ProductComponent productComponent) {
        if ( productComponent == null ) {
            return null;
        }
        Product product = productComponent.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long productComponentProductId(ProductComponent productComponent) {
        if ( productComponent == null ) {
            return null;
        }
        Product product = productComponent.getProduct();
        if ( product == null ) {
            return null;
        }
        Long id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
