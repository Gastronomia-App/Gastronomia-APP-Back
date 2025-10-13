package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductGroupRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductGroupResponseDTO;
import com.progra3.cafeteria_api.model.dto.ProductOptionResponseDTO;
import com.progra3.cafeteria_api.model.entity.ProductGroup;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-16T20:41:22-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class ProductGroupMapperImpl implements ProductGroupMapper {

    @Autowired
    private ProductOptionMapper productOptionMapper;

    @Override
    public ProductGroupResponseDTO toDTO(ProductGroup productGroup) {
        if ( productGroup == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        Integer minQuantity = null;
        Integer maxQuantity = null;
        List<ProductOptionResponseDTO> options = null;

        id = productGroup.getId();
        name = productGroup.getName();
        minQuantity = productGroup.getMinQuantity();
        maxQuantity = productGroup.getMaxQuantity();
        options = productOptionListToProductOptionResponseDTOList( productGroup.getOptions() );

        ProductGroupResponseDTO productGroupResponseDTO = new ProductGroupResponseDTO( id, name, minQuantity, maxQuantity, options );

        return productGroupResponseDTO;
    }

    @Override
    public List<ProductGroupResponseDTO> toDTOList(List<ProductGroup> productGroups) {
        if ( productGroups == null ) {
            return null;
        }

        List<ProductGroupResponseDTO> list = new ArrayList<ProductGroupResponseDTO>( productGroups.size() );
        for ( ProductGroup productGroup : productGroups ) {
            list.add( toDTO( productGroup ) );
        }

        return list;
    }

    @Override
    public ProductGroup toEntity(ProductGroupRequestDTO productGroupRequestDTO) {
        if ( productGroupRequestDTO == null ) {
            return null;
        }

        ProductGroup productGroup = new ProductGroup();

        productGroup.setName( productGroupRequestDTO.name() );
        productGroup.setMinQuantity( productGroupRequestDTO.minQuantity() );
        productGroup.setMaxQuantity( productGroupRequestDTO.maxQuantity() );

        return productGroup;
    }

    @Override
    public ProductGroup updateProductGroupFromDTO(ProductGroup productGroup, ProductGroupRequestDTO productGroupRequestDTO) {
        if ( productGroupRequestDTO == null ) {
            return productGroup;
        }

        if ( productGroupRequestDTO.name() != null ) {
            productGroup.setName( productGroupRequestDTO.name() );
        }
        if ( productGroupRequestDTO.minQuantity() != null ) {
            productGroup.setMinQuantity( productGroupRequestDTO.minQuantity() );
        }
        if ( productGroupRequestDTO.maxQuantity() != null ) {
            productGroup.setMaxQuantity( productGroupRequestDTO.maxQuantity() );
        }

        return productGroup;
    }

    protected List<ProductOptionResponseDTO> productOptionListToProductOptionResponseDTOList(List<ProductOption> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductOptionResponseDTO> list1 = new ArrayList<ProductOptionResponseDTO>( list.size() );
        for ( ProductOption productOption : list ) {
            list1.add( productOptionMapper.toDTO( productOption ) );
        }

        return list1;
    }
}
