package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.SelectedProductOptionRequestDTO;
import com.progra3.cafeteria_api.model.dto.SelectedProductOptionResponseDTO;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import com.progra3.cafeteria_api.model.entity.SelectedProductOption;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-16T20:41:23-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class SelectedProductOptionMapperImpl implements SelectedProductOptionMapper {

    @Override
    public SelectedProductOptionResponseDTO toDTO(SelectedProductOption selectedProductOption) {
        if ( selectedProductOption == null ) {
            return null;
        }

        Long productOptionId = null;
        Long id = null;
        Long quantity = null;

        productOptionId = selectedProductOptionProductOptionId( selectedProductOption );
        id = selectedProductOption.getId();
        if ( selectedProductOption.getQuantity() != null ) {
            quantity = selectedProductOption.getQuantity().longValue();
        }

        SelectedProductOptionResponseDTO selectedProductOptionResponseDTO = new SelectedProductOptionResponseDTO( id, productOptionId, quantity );

        return selectedProductOptionResponseDTO;
    }

    @Override
    public List<SelectedProductOption> toEntityList(List<SelectedProductOptionRequestDTO> dtos) {
        if ( dtos == null ) {
            return null;
        }

        List<SelectedProductOption> list = new ArrayList<SelectedProductOption>( dtos.size() );
        for ( SelectedProductOptionRequestDTO selectedProductOptionRequestDTO : dtos ) {
            list.add( toEntity( selectedProductOptionRequestDTO ) );
        }

        return list;
    }

    @Override
    public SelectedProductOption toEntity(SelectedProductOptionRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        SelectedProductOption selectedProductOption = new SelectedProductOption();

        selectedProductOption.setProductOption( selectedProductOptionRequestDTOToProductOption( dto ) );
        selectedProductOption.setQuantity( dto.quantity() );

        return selectedProductOption;
    }

    private Long selectedProductOptionProductOptionId(SelectedProductOption selectedProductOption) {
        if ( selectedProductOption == null ) {
            return null;
        }
        ProductOption productOption = selectedProductOption.getProductOption();
        if ( productOption == null ) {
            return null;
        }
        Long id = productOption.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected ProductOption selectedProductOptionRequestDTOToProductOption(SelectedProductOptionRequestDTO selectedProductOptionRequestDTO) {
        if ( selectedProductOptionRequestDTO == null ) {
            return null;
        }

        ProductOption productOption = new ProductOption();

        productOption.setId( selectedProductOptionRequestDTO.productOptionId() );

        return productOption;
    }
}
