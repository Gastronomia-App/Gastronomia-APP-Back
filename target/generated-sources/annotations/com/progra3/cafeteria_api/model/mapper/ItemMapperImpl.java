package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemResponseDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.model.dto.SelectedOptionRequestDTO;
import com.progra3.cafeteria_api.model.dto.SelectedOptionResponseDTO;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
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
public class ItemMapperImpl extends ItemMapper {

    @Autowired
    private SelectedOptionMapper selectedOptionMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ItemResponseDTO toDTO(Item item) {
        if ( item == null ) {
            return null;
        }

        Long orderId = null;
        Long id = null;
        ProductResponseDTO product = null;
        List<SelectedOptionResponseDTO> selectedOptions = null;
        Double unitPrice = null;
        Integer quantity = null;
        String comment = null;
        Double totalPrice = null;
        Boolean deleted = null;

        orderId = itemOrderId( item );
        id = item.getId();
        product = productMapper.toDTO( item.getProduct() );
        selectedOptions = selectedOptionListToSelectedOptionResponseDTOList( item.getSelectedOptions() );
        unitPrice = item.getUnitPrice();
        quantity = item.getQuantity();
        comment = item.getComment();
        totalPrice = item.getTotalPrice();
        deleted = item.getDeleted();

        ItemResponseDTO itemResponseDTO = new ItemResponseDTO( id, orderId, product, selectedOptions, unitPrice, quantity, comment, totalPrice, deleted );

        return itemResponseDTO;
    }

    @Override
    public Item toEntity(ItemRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Item item = new Item();

        item.setProduct( mapProduct( dto.productId() ) );
        item.setComment( dto.comment() );
        item.setQuantity( dto.quantity() );
        item.setSelectedOptions( selectedOptionRequestDTOListToSelectedOptionList( dto.selectedOptions() ) );

        item.setDeleted( false );

        linkItemToOptions( item );

        return item;
    }

    @Override
    public Item updateItemFromDTO(ItemRequestDTO dto, Item item) {
        if ( dto == null ) {
            return item;
        }

        item.setComment( dto.comment() );
        item.setQuantity( dto.quantity() );
        if ( item.getSelectedOptions() != null ) {
            List<SelectedOption> list = selectedOptionRequestDTOListToSelectedOptionList( dto.selectedOptions() );
            if ( list != null ) {
                item.getSelectedOptions().clear();
                item.getSelectedOptions().addAll( list );
            }
            else {
                item.setSelectedOptions( null );
            }
        }
        else {
            List<SelectedOption> list = selectedOptionRequestDTOListToSelectedOptionList( dto.selectedOptions() );
            if ( list != null ) {
                item.setSelectedOptions( list );
            }
        }

        linkItemToOptions( item );

        return item;
    }

    private Long itemOrderId(Item item) {
        if ( item == null ) {
            return null;
        }
        Order order = item.getOrder();
        if ( order == null ) {
            return null;
        }
        Long id = order.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<SelectedOptionResponseDTO> selectedOptionListToSelectedOptionResponseDTOList(List<SelectedOption> list) {
        if ( list == null ) {
            return null;
        }

        List<SelectedOptionResponseDTO> list1 = new ArrayList<SelectedOptionResponseDTO>( list.size() );
        for ( SelectedOption selectedOption : list ) {
            list1.add( selectedOptionMapper.toDTO( selectedOption ) );
        }

        return list1;
    }

    protected List<SelectedOption> selectedOptionRequestDTOListToSelectedOptionList(List<SelectedOptionRequestDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<SelectedOption> list1 = new ArrayList<SelectedOption>( list.size() );
        for ( SelectedOptionRequestDTO selectedOptionRequestDTO : list ) {
            list1.add( selectedOptionMapper.toEntity( selectedOptionRequestDTO ) );
        }

        return list1;
    }
}
