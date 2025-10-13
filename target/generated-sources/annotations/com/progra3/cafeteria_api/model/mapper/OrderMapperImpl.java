package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ItemResponseDTO;
import com.progra3.cafeteria_api.model.dto.OrderRequestDTO;
import com.progra3.cafeteria_api.model.dto.OrderResponseDTO;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.OrderType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-17T00:03:10-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public OrderResponseDTO toDTO(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderType orderType = null;
        Long id = null;
        List<ItemResponseDTO> items = null;
        LocalDateTime dateTime = null;
        Integer peopleCount = null;
        Integer discount = null;
        String status = null;
        Double subtotal = null;
        Double total = null;

        orderType = order.getType();
        id = order.getId();
        items = itemListToItemResponseDTOList( order.getItems() );
        dateTime = order.getDateTime();
        peopleCount = order.getPeopleCount();
        discount = order.getDiscount();
        if ( order.getStatus() != null ) {
            status = order.getStatus().name();
        }
        subtotal = order.getSubtotal();
        total = order.getTotal();

        String customerName = order.getCustomer() != null ? order.getCustomer().getName() : null;
        String employeeName = order.getEmployee() != null ? order.getEmployee().getName() : null;
        Integer seatingNumber = order.getSeating() != null ? order.getSeating().getNumber() : null;

        OrderResponseDTO orderResponseDTO = new OrderResponseDTO( id, employeeName, customerName, seatingNumber, orderType, items, dateTime, peopleCount, discount, status, subtotal, total );

        return orderResponseDTO;
    }

    @Override
    public Order toEntity(OrderRequestDTO orderRequestDTO) {
        if ( orderRequestDTO == null ) {
            return null;
        }

        Order order = new Order();

        order.setType( orderRequestDTO.orderType() );
        order.setPeopleCount( orderRequestDTO.peopleCount() );

        return order;
    }

    protected List<ItemResponseDTO> itemListToItemResponseDTOList(List<Item> list) {
        if ( list == null ) {
            return null;
        }

        List<ItemResponseDTO> list1 = new ArrayList<ItemResponseDTO>( list.size() );
        for ( Item item : list ) {
            list1.add( itemMapper.toDTO( item ) );
        }

        return list1;
    }
}
