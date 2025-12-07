package com.progra3.cafeteria_api.service.port.tickets;

import com.progra3.cafeteria_api.model.dto.ticket.TicketContext;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.dto.ticket.Ticket;
import com.progra3.cafeteria_api.model.enums.TicketType;

import java.util.List;

/**
 * ITicketBuilderService builds a printable Ticket model from an Order.
 */
public interface ITicketBuilderService {

    /**
     * Builds a ticket using strategy-factory pattern.
     */
    Ticket build(TicketType type, Order order, TicketContext ticketContext);

}
