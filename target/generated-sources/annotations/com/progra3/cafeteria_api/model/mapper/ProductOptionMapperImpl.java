package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductOptionRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductOptionResponseDTO;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-16T20:41:22-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class ProductOptionMapperImpl implements ProductOptionMapper {

    @Override
    public ProductOptionResponseDTO toDTO(ProductOption productOption) {
        if ( productOption == null ) {
            return null;
        }

        Long productId = null;
        Long id = null;
        Integer maxQuantity = null;
        Double priceIncrease = null;

        productId = productOptionProductId( productOption );
        id = productOption.getId();
        maxQuantity = productOption.getMaxQuantity();
        priceIncrease = productOption.getPriceIncrease();

        ProductOptionResponseDTO productOptionResponseDTO = new ProductOptionResponseDTO( id, productId, maxQuantity, priceIncrease );

        return productOptionResponseDTO;
    }

    @Override
    public ProductOption toEntity(ProductOptionRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ProductOption productOption = new ProductOption();

        productOption.setMaxQuantity( dto.maxQuantity() );
        productOption.setPriceIncrease( dto.priceIncrease() );

        return productOption;
    }

    @Override
    public ProductOption updateProductOptionFromDTO(ProductOption productOption, ProductOptionRequestDTO dto) {
        if ( dto == null ) {
            return productOption;
        }

        if ( dto.maxQuantity() != null ) {
            productOption.setMaxQuantity( dto.maxQuantity() );
        }
        if ( dto.priceIncrease() != null ) {
            productOption.setPriceIncrease( dto.priceIncrease() );
        }

        return productOption;
    }

    private Long productOptionProductId(ProductOption productOption) {
        if ( productOption == null ) {
            return null;
        }
        Product product = productOption.getProduct();
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
