package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.OrderRequestDTO;
import com.progra3.cafeteria_api.model.dto.OrderResponseDTO;
import com.progra3.cafeteria_api.model.entity.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {ItemMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "customerName", expression = "java(order.getCustomer() != null ? order.getCustomer().getName() : null)")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "employeeName", expression = "java(order.getEmployee() != null ? order.getEmployee().getName() : null)")
    @Mapping(target = "seatingNumber", expression = "java(order.getSeating() != null ? order.getSeating().getNumber() : null)")
    @Mapping(target = "orderType", source = "type")
    @Mapping(target = "paymentMethods", expression = "java(mapPaymentMethods(order.getOrderPaymentMethods()))")
    OrderResponseDTO toDTO(Order order);

    default java.util.List<com.progra3.cafeteria_api.model.dto.OrderPaymentMethodResponseDTO> mapPaymentMethods(java.util.List<com.progra3.cafeteria_api.model.entity.OrderPaymentMethod> orderPaymentMethods) {
        if (orderPaymentMethods == null || orderPaymentMethods.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return orderPaymentMethods.stream()
                .map(opm -> new com.progra3.cafeteria_api.model.dto.OrderPaymentMethodResponseDTO(
                        opm.getPaymentMethod().getId(),
                        opm.getPaymentMethod().getName(),
                        opm.getAmount()
                ))
                .toList();
    }
    @Mapping(target = "type", source = "orderType")
    Order toEntity(OrderRequestDTO orderRequestDTO);
}
