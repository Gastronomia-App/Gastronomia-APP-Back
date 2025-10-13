package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.event.OrderCreatedEvent;
import com.progra3.cafeteria_api.event.OrderFinalizedEvent;
import com.progra3.cafeteria_api.exception.order.ItemNotFoundException;
import com.progra3.cafeteria_api.exception.utilities.InvalidDateException;
import com.progra3.cafeteria_api.exception.order.OrderModificationNotAllowedException;
import com.progra3.cafeteria_api.exception.order.OrderNotFoundException;
import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemResponseDTO;
import com.progra3.cafeteria_api.model.dto.ItemTransferRequestDTO;
import com.progra3.cafeteria_api.model.dto.OrderRequestDTO;
import com.progra3.cafeteria_api.model.dto.OrderResponseDTO;
import com.progra3.cafeteria_api.model.dto.OrderSplitRequestDTO;
import com.progra3.cafeteria_api.model.mapper.ItemMapper;
import com.progra3.cafeteria_api.model.mapper.OrderMapper;
import com.progra3.cafeteria_api.model.entity.*;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import com.progra3.cafeteria_api.model.enums.SeatingStatus;
import com.progra3.cafeteria_api.repository.OrderRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.IOrderService;
import com.progra3.cafeteria_api.service.helper.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;

    private final EmployeeContext employeeContext;
    private final EmployeeService employeeService;
    private final CustomerService customerService;
    private final SeatingService seatingService;
    private final ItemService itemService;
    private final ApplicationEventPublisher eventPublisher;

    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;

    private final Clock clock;

    @Transactional
    @Override
    public OrderResponseDTO create(OrderRequestDTO dto) {
        Order order = createNewOrder(dto);

        return orderMapper.toDTO(orderRepository.save(order));
    }

    @Override
    public OrderResponseDTO getById(Long orderId) {
        return orderMapper.toDTO(getEntityById(orderId));
    }

    @Override
    public Page<OrderResponseDTO> getOrders(LocalDate startDate, LocalDate endDate, Long customerId, Long employeeId, OrderStatus status, Pageable pageable) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null) {
            startDateTime = startDate.atStartOfDay();
        }
        if (endDate != null) {
            endDateTime = endDate.atTime(LocalTime.MAX);
        }

        Page<Order> orders = orderRepository.findByBusiness_Id(
                startDateTime,
                endDateTime,
                customerId,
                employeeId,
                status,
                employeeContext.getCurrentBusinessId(),
                pageable);
        return orders.map(orderMapper::toDTO);
    }

    @Override
    public List<Order> getByDateTimeBetween(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new InvalidDateException("Start should be earlier than end");
        }
        return orderRepository.findByDateTimeBetweenAndBusiness_Id(start, end, employeeContext.getCurrentBusinessId());
    }

    @Transactional
    @Override
    public OrderResponseDTO update(Long orderId, OrderRequestDTO dto) {
        Order order = getEntityById(orderId);

        validateOrderStatus(order.getStatus());

        order.setEmployee(employeeService.getEntityById(dto.employeeId()));
        order.setCustomer(customerService.getEntityById(dto.customerId()));
        order.setSeating(seatingService.getEntityById(dto.seatingId()));
        order.setPeopleCount(dto.peopleCount());
        order.setType(dto.orderType());
        order.setDiscount(Optional.ofNullable(order.getCustomer())
                .map(Customer::getDiscount)
                .orElse(order.getDiscount()));

        recalculate(order);

        return orderMapper.toDTO(orderRepository.save(order));
    }

    @Transactional
    @Override
    public OrderResponseDTO updateDiscount(Long orderId, Integer discount) {
        Order order = getEntityById(orderId);

        validateOrderStatus(order.getStatus());

        applyDiscount(order, discount);

        return orderMapper.toDTO(orderRepository.save(order));
    }

    @Transactional
    @Override
    public OrderResponseDTO updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = getEntityById(orderId);

        validateOrderStatus(order.getStatus());

        seatingService.updateStatus(order.getSeating(), newStatus);

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.FINALIZED) {
            eventPublisher.publishEvent(new OrderFinalizedEvent(order));
        }

        return orderMapper.toDTO(orderRepository.save(order));
    }

    @Transactional
    @Override
    public List<OrderResponseDTO> transferItemsBetweenOrders(Long originalOrderId, OrderSplitRequestDTO dto) {
        Order originalOrder = getEntityById(originalOrderId);
        validateOrderStatus(originalOrder.getStatus());

        Order destinationOrder = getOrCreateDestinationOrder(dto.destinationOrder());

        validateDifferentOrders(originalOrder, destinationOrder);

        updatePeopleCount(originalOrder, destinationOrder, dto.destinationOrder().peopleCount());

        transferItems(originalOrder, destinationOrder, dto.itemsToMove());

        saveOrders(originalOrder, destinationOrder);

        return List.of(orderMapper.toDTO(originalOrder), orderMapper.toDTO(destinationOrder));
    }

    @Transactional
    @Override
    public OrderResponseDTO addItems(Long orderId, List<ItemRequestDTO> itemRequestDTOList) {
        Order order = getEntityById(orderId);
        validateOrderStatus(order.getStatus());

        List<ItemResponseDTO> itemsToAdd = itemRequestDTOList.stream()
                .map(item -> addItem(order, item))
                .toList();

        recalculate(order);

        return orderMapper.toDTO(orderRepository.save(order));
    }

    private ItemResponseDTO addItem(Order order, ItemRequestDTO itemDTO) {
        Item newItem = itemService.createItem(order, itemDTO);

        order.getItems().add(newItem);

        return itemMapper.toDTO(newItem);
    }

    @Transactional
    @Override
    public ItemResponseDTO removeItem(Long orderId, Long itemId) {
        Order order = getEntityById(orderId);

        validateOrderStatus(order.getStatus());

        Item itemToRemove = order.getItems().stream()
                .filter(item -> item.getId().equals(itemId) && !item.getDeleted())
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        itemToRemove.setDeleted(true);

        recalculate(order);
        orderRepository.save(order);

        return itemMapper.toDTO(itemToRemove);
    }

    @Transactional
    @Override
    public ItemResponseDTO updateItem(Long orderId, Long itemId, ItemRequestDTO itemDTO) {
        Order order = getEntityById(orderId);

        validateOrderStatus(order.getStatus());

        Item itemToUpdate = order.getItems().stream()
                .filter(item -> item.getId().equals(itemId) && !item.getDeleted())
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        itemToUpdate = itemService.updateItem(itemToUpdate, itemDTO);

        recalculate(order);

        orderRepository.save(order);

        return itemMapper.toDTO(itemToUpdate);
    }

    @Override
    public List<ItemResponseDTO> getItems(Long orderId) {
        Order order = getEntityById(orderId);

        return order.getItems().stream().map(itemMapper::toDTO).toList();
    }

    private Order createNewOrder(OrderRequestDTO dto) {
        Employee employee = employeeService.getEntityById(Optional.ofNullable(dto.employeeId())
                .orElse(employeeContext.getCurrentEmployeeId()));

        Customer customer = customerService.getEntityById(dto.customerId());

        Seating seating = Optional.ofNullable(dto.seatingId())
                .map(seatingService::getEntityById)
                .map(
                        availableSeating -> {
                            validateSeatingStatus(availableSeating);
                            availableSeating.setStatus(SeatingStatus.OCCUPIED);
                            return availableSeating;
                        }
                )
                .orElse(null);

        Order order = orderMapper.toEntity(dto);

        order.setBusiness(employeeContext.getCurrentBusiness());
        order.setEmployee(employee);
        order.setCustomer(customer);
        order.setSeating(seating);

        order.setDateTime(LocalDateTime.now(clock));
        order.setDiscount(Optional.ofNullable(customer).map(Customer::getDiscount).orElse(Constant.NO_DISCOUNT));
        order.setStatus(OrderStatus.ACTIVE);
        order.setSubtotal(Constant.ZERO_AMOUNT);
        order.setTotal(Constant.ZERO_AMOUNT);
        eventPublisher.publishEvent(new OrderCreatedEvent(order));

        return order;
    }

    private Order getOrCreateDestinationOrder(OrderRequestDTO destinationDto) {
        return orderRepository.findBySeating_IdAndStatusAndBusiness_Id(
                        Optional.ofNullable(destinationDto.seatingId())
                                .orElseThrow(() -> new IllegalArgumentException("Seating ID is required")),
                        OrderStatus.ACTIVE,
                        employeeContext.getCurrentBusinessId())
                .orElseGet(() -> {
                    Order newOrder = createNewOrder(destinationDto);
                    newOrder.setPeopleCount(0);
                    return newOrder;
                });
    }

    private void applyDiscount(Order order, Integer discount) {
        order.setDiscount(discount);
        recalculate(order);
    }

    @Override
    public Order getEntityById(Long orderId) {
        return orderRepository.findByIdAndBusiness_Id(orderId, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void validateDifferentOrders(Order originalOrder, Order destinationOrder) {
        if (originalOrder.equals(destinationOrder)) {
            throw new IllegalArgumentException("Original and destination orders cannot be the same.");
        }
    }

    private void validateSeatingStatus(Seating seating) {
        if (seating.getStatus() != SeatingStatus.FREE) {
            throw new IllegalArgumentException("Cannot create order for a occupied seating.");
        }
    }

    private void validateOrderStatus(OrderStatus orderStatus) {
        if (orderStatus != OrderStatus.ACTIVE) {
            throw new OrderModificationNotAllowedException(orderStatus.getName());
        }
    }

    private void saveOrders(Order originalOrder, Order destinationOrder) {
        recalculate(originalOrder);
        recalculate(destinationOrder);
        orderRepository.save(originalOrder);
        orderRepository.save(destinationOrder);
    }

    private void updatePeopleCount(Order originalOrder, Order destinationOrder, Integer peopleCount) {
        if (peopleCount - destinationOrder.getPeopleCount() > 0) {
            int peopleToTransfer = peopleCount - destinationOrder.getPeopleCount();
            validateEnoughPeopleToTransfer(originalOrder, peopleToTransfer);
            originalOrder.setPeopleCount(originalOrder.getPeopleCount() - peopleToTransfer);
            destinationOrder.setPeopleCount(destinationOrder.getPeopleCount() + peopleToTransfer);
        } else {
            int peopleToTransfer = destinationOrder.getPeopleCount() - peopleCount;
            validateEnoughPeopleToTransfer(destinationOrder, peopleToTransfer);
            originalOrder.setPeopleCount(originalOrder.getPeopleCount() + peopleToTransfer);
            destinationOrder.setPeopleCount(destinationOrder.getPeopleCount() - peopleToTransfer);
        }
    }

    private void validateEnoughPeopleToTransfer(Order originalOrder, int amount) {
        if (amount >= originalOrder.getPeopleCount()) {
            throw new IllegalArgumentException("Cannot transfer more people than available in original order.");
        }
    }

    private void transferItems(Order originalOrder, Order destinationOrder, List<ItemTransferRequestDTO> itemsToMove) {
        List<Item> transferred = itemService.transferItems(originalOrder, destinationOrder, itemsToMove);
        destinationOrder.getItems().addAll(transferred);

        recalculate(originalOrder);
        recalculate(destinationOrder);
    }

    private void calculateSubtotal(Order order) {
        order.setSubtotal(
                order.getItems()
                        .stream()
                        .filter(item -> !item.getDeleted())
                        .mapToDouble(Item::getTotalPrice)
                        .sum()
        );
    }

    private void calculateTotal(Order order) {
        double total = order.getSubtotal() * (1 - order.getDiscount() / 100.0);
        order.setTotal(Math.round(total * 100.0) / 100.0);
    }

    private void recalculate(Order order) {
        calculateSubtotal(order);
        calculateTotal(order);
    }
}
