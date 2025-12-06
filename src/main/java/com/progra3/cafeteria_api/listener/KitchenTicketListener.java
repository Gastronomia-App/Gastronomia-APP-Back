package com.progra3.cafeteria_api.listener;

import com.progra3.cafeteria_api.event.OrderItemsAddedEvent;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.dto.ticket.Ticket;
import com.progra3.cafeteria_api.service.port.ITicketBuilderService;
import com.progra3.cafeteria_api.service.port.ITicketPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KitchenTicketListener {

    private final ITicketBuilderService ticketBuilderService;
    private final ITicketPdfService ticketPdfService;
    // Inyectá acá lo que uses para mandar el PDF a front/impresora

    @EventListener
    public void onOrderItemsAdded(OrderItemsAddedEvent event) {
        Order order = event.order();
        List<Item> newItems = event.newItems();

        // Nuevo método en el builder que acepte sólo esos items
        Ticket ticket = ticketBuilderService.buildKitchenTicket(order, newItems);

        byte[] pdf = ticketPdfService.generateTicketPdf(ticket);

        // Aquí usás tu mecanismo actual (WebSocket, cola, etc.).
        // kitchenSender.send(order, pdf);
    }
}