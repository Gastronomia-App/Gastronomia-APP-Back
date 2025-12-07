package com.progra3.cafeteria_api.service.port.tickets;

import com.progra3.cafeteria_api.model.dto.ticket.Ticket;
import com.progra3.cafeteria_api.model.dto.ticket.TicketContext;
import com.progra3.cafeteria_api.model.entity.Order;

public interface TicketBuilderStrategy {
    Ticket build(Order order, TicketContext ticketContext);
}
