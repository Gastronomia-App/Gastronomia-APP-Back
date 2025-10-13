package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.exception.order.OrderNotFoundException;
import com.progra3.cafeteria_api.model.dto.*;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {
    OrderResponseDTO create(OrderRequestDTO dto);
    OrderResponseDTO getById(Long orderId) throws OrderNotFoundException;
    Page<OrderResponseDTO> getOrders(LocalDate startDate, LocalDate endDate, Long customerId, Long employeeId, OrderStatus status, Pageable pageable);
    OrderResponseDTO update(Long orderId, OrderRequestDTO dto);
    OrderResponseDTO updateDiscount(Long orderId, Integer discount);
    OrderResponseDTO updateStatus(Long orderId, OrderStatus status);

    Order getEntityById(Long orderId);
    List<Order> getByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<OrderResponseDTO> transferItemsBetweenOrders(Long originalOrderId, OrderSplitRequestDTO dto) ;

    //Item management methods
    OrderResponseDTO addItems(Long orderId, List<ItemRequestDTO> itemRequestDTOList);
    List<ItemResponseDTO> getItems(Long orderId);
    ItemResponseDTO removeItem(Long orderId, Long itemId);
    ItemResponseDTO updateItem(Long orderId, Long itemId, ItemRequestDTO itemDTO);
}