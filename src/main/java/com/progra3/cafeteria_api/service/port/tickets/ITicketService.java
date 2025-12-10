package com.progra3.cafeteria_api.service.port.tickets;

import com.progra3.cafeteria_api.model.dto.ticket.FiscalTicketRequest;
import com.progra3.cafeteria_api.model.entity.Order;

import java.util.List;

/**
 * Service interface for ticket operations including electronic invoicing and pre-ticket printing.
 */
public interface ITicketService {

    /**
     * Generates a pre-ticket (cuenta) for an order before payment.
     * This ticket shows items and totals without payment details or CAE.
     *
     * @param order The order to generate pre-ticket for
     * @return PDF byte array of the pre-ticket
     */
    byte[] generatePreTicket(Long orderId);

    /**
     * Generates a fiscal ticket for an order after electronic invoice generation.
     * This ticket includes payment details and CAE from AFIP.
     *
     * @param order The order to generate fiscal ticket for
     * @return PDF byte array of the fiscal ticket
     */
    byte[] generateFiscalTicket(Long orderId, FiscalTicketRequest fiscalTicketRequest);

    /**
     * Generates a kitchen ticket (comanda) for the specified items in an order.
     * This ticket is used by kitchen staff to prepare the ordered items.
     *
     * @param orderId The order containing the items
     * @param itemIds The specific item Ids to include in the kitchen ticket
     * @return PDF byte array of the kitchen ticket
     */
    byte[] generateKitchenTicket(Long orderId, List<Long> itemIds);
}

