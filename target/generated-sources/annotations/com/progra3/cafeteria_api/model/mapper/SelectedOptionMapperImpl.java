package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ProductOptionResponseDTO;
import com.progra3.cafeteria_api.model.dto.SelectedOptionRequestDTO;
import com.progra3.cafeteria_api.model.dto.SelectedOptionResponseDTO;
import com.progra3.cafeteria_api.model.entity.SelectedOption;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-30T13:58:55-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class SelectedOptionMapperImpl extends SelectedOptionMapper {

    @Autowired
    private ProductOptionMapper productOptionMapper;

    @Override
    public SelectedOptionResponseDTO toDTO(SelectedOption entity) {
        if ( entity == null ) {
            return null;
        }

        ProductOptionResponseDTO productOption = null;
        Long id = null;
        Integer quantity = null;
        List<SelectedOptionResponseDTO> selectedOptions = null;

        productOption = productOptionMapper.toDTO( entity.getProductOption() );
        id = entity.getId();
        quantity = entity.getQuantity();
        selectedOptions = selectedOptionListToSelectedOptionResponseDTOList( entity.getSelectedOptions() );

        SelectedOptionResponseDTO selectedOptionResponseDTO = new SelectedOptionResponseDTO( id, productOption, quantity, selectedOptions );

        return selectedOptionResponseDTO;
    }

    @Override
    public SelectedOption toEntity(SelectedOptionRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        SelectedOption selectedOption = new SelectedOption();

        selectedOption.setProductOption( mapProductOption( dto.productOptionId() ) );
        selectedOption.setSelectedOptions( selectedOptionRequestDTOListToSelectedOptionList( dto.selectedOptions() ) );
        selectedOption.setQuantity( dto.quantity() );

        linkOptionRelationships( selectedOption );

        return selectedOption;
    }

    protected List<SelectedOptionResponseDTO> selectedOptionListToSelectedOptionResponseDTOList(List<SelectedOption> list) {
        if ( list == null ) {
            return null;
        }

        List<SelectedOptionResponseDTO> list1 = new ArrayList<SelectedOptionResponseDTO>( list.size() );
        for ( SelectedOption selectedOption : list ) {
            list1.add( toDTO( selectedOption ) );
        }

        return list1;
    }

    protected List<SelectedOption> selectedOptionRequestDTOListToSelectedOptionList(List<SelectedOptionRequestDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<SelectedOption> list1 = new ArrayList<SelectedOption>( list.size() );
        for ( SelectedOptionRequestDTO selectedOptionRequestDTO : list ) {
            list1.add( toEntity( selectedOptionRequestDTO ) );
        }

        return list1;
    }
}
