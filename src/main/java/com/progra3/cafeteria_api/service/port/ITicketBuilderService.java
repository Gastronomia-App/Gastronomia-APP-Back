package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.model.ticket.Ticket;

import java.util.List;

/**
 * ITicketBuilderService builds a printable Ticket model from an Order.
 */
public interface ITicketBuilderService {

    /**
     * Builds a ticket using all items from the given order.
     */
    Ticket buildTicket(Order order, TicketType type);

    /**
     * Builds a kitchen ticket for the given explicit list of items.
     * Intended to be used when printing a comanda for already filtered items.
     */
    Ticket buildKitchenTicket(Order order, List<Item> items);

    /**
     * Builds a kitchen ticket only for the provided item IDs of the order.
     * The service is responsible for resolving, filtering and excluding deleted items.
     */
    Ticket buildKitchenTicketForItems(Order order, List<Long> itemIds);
}
