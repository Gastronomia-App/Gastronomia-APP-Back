package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.model.ticket.Ticket;

/**
 * ITicketBuilderService builds a printable Ticket model from an Order.
 */
public interface ITicketBuilderService {

    /**
     * Builds a Ticket representation for the given order and ticket type.
     *
     * @param order the source order, must not be null
     * @param type  the ticket type to build
     * @return a Ticket ready to be rendered to PDF
     */
    Ticket buildTicket(Order order, TicketType type);
}