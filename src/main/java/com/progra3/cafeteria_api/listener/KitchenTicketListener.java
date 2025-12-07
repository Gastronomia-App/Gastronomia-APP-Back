package com.progra3.cafeteria_api.listener;

import com.progra3.cafeteria_api.event.OrderItemsAddedEvent;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.service.port.ITicketBuilderService;
import com.progra3.cafeteria_api.service.port.ITicketPdfService;
import com.progra3.cafeteria_api.service.port.ITicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Listener that automatically generates kitchen tickets when items are added to an order.
 * This separates the ticket generation logic from the order service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenTicketListener {

    private final ITicketService ticketService;

    /**
     * Handles OrderItemsAddedEvent by generating a kitchen ticket for the new items.
     * This method is executed asynchronously to avoid blocking order creation.
     */
    @EventListener
    @Async
    public void onOrderItemsAdded(OrderItemsAddedEvent event) {
        Order order = event.order();
        List<Item> newItems = event.newItems();

        ticketService.generateKitchenTicket(order, newItems);
    }
}
