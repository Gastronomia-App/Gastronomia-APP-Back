package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.OrderRequestDTO;
import com.progra3.cafeteria_api.model.dto.OrderResponseDTO;
import com.progra3.cafeteria_api.model.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {ItemMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "customerName", expression = "java(order.getCustomer() != null ? order.getCustomer().getName() : null)")
    @Mapping(target = "employeeName", expression = "java(order.getEmployee() != null ? order.getEmployee().getName() : null)")
    @Mapping(target = "seatingNumber", expression = "java(order.getSeating() != null ? order.getSeating().getNumber() : null)")
    @Mapping(target = "orderType", source = "type")
    OrderResponseDTO toDTO(Order order);
    @Mapping(target = "type", source = "orderType")
    Order toEntity(OrderRequestDTO orderRequestDTO);
}
