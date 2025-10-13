package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemResponseDTO;
import com.progra3.cafeteria_api.model.dto.SelectedProductOptionResponseDTO;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.SelectedProductOption;
import java.util.ArrayList;
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
public class ItemMapperImpl implements ItemMapper {

    @Autowired
    private SelectedProductOptionMapper selectedProductOptionMapper;

    @Override
    public ItemResponseDTO toDTO(Item item) {
        if ( item == null ) {
            return null;
        }

        Long productId = null;
        Long orderId = null;
        Long id = null;
        List<SelectedProductOptionResponseDTO> selectedOptions = null;
        Double unitPrice = null;
        Integer quantity = null;
        String comment = null;
        Double totalPrice = null;
        Boolean deleted = null;

        productId = itemProductId( item );
        orderId = itemOrderId( item );
        id = item.getId();
        selectedOptions = selectedProductOptionListToSelectedProductOptionResponseDTOList( item.getSelectedOptions() );
        unitPrice = item.getUnitPrice();
        quantity = item.getQuantity();
        comment = item.getComment();
        totalPrice = item.getTotalPrice();
        deleted = item.getDeleted();

        ItemResponseDTO itemResponseDTO = new ItemResponseDTO( id, orderId, productId, selectedOptions, unitPrice, quantity, comment, totalPrice, deleted );

        return itemResponseDTO;
    }

    @Override
    public Item toEntity(ItemRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Item item = new Item();

        item.setSelectedOptions( selectedProductOptionMapper.toEntityList( dto.selectedOptions() ) );
        item.setComment( dto.comment() );
        item.setQuantity( dto.quantity() );

        return item;
    }

    @Override
    public Item updateItemFromDTO(ItemRequestDTO itemDTO, Item item) {
        if ( itemDTO == null ) {
            return item;
        }

        if ( item.getSelectedOptions() != null ) {
            List<SelectedProductOption> list = selectedProductOptionMapper.toEntityList( itemDTO.selectedOptions() );
            if ( list != null ) {
                item.getSelectedOptions().clear();
                item.getSelectedOptions().addAll( list );
            }
            else {
                item.setSelectedOptions( null );
            }
        }
        else {
            List<SelectedProductOption> list = selectedProductOptionMapper.toEntityList( itemDTO.selectedOptions() );
            if ( list != null ) {
                item.setSelectedOptions( list );
            }
        }
        item.setComment( itemDTO.comment() );
        item.setQuantity( itemDTO.quantity() );

        return item;
    }

    private Long itemProductId(Item item) {
        if ( item == null ) {
            return null;
        }
        Product product = item.getProduct();
        if ( product == null ) {
            return null;
        }
        Long id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
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

    protected List<SelectedProductOptionResponseDTO> selectedProductOptionListToSelectedProductOptionResponseDTOList(List<SelectedProductOption> list) {
        if ( list == null ) {
            return null;
        }

        List<SelectedProductOptionResponseDTO> list1 = new ArrayList<SelectedProductOptionResponseDTO>( list.size() );
        for ( SelectedProductOption selectedProductOption : list ) {
            list1.add( selectedProductOptionMapper.toDTO( selectedProductOption ) );
        }

        return list1;
    }
}
