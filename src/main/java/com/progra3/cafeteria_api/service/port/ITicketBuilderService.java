package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.model.dto.ticket.Ticket;

import java.util.List;

/**
 * ITicketBuilderService builds a printable Ticket model from an Order.
 */
public interface ITicketBuilderService {

    /**
     * Builds a pre-ticket using all items from the given order.
     */
    Ticket buildPreTicket(Order order);

    /**
     * Builds a fiscal ticket using all items from the given order.
     */
    Ticket buildFiscalTicket(Order order, String cae);

    /**
     * Builds a kitchen ticket for the given explicit list of items.
     * Intended to be used when printing a comanda for already filtered items.
     */
    Ticket buildKitchenTicket(Order order, List<Item> items);

}
