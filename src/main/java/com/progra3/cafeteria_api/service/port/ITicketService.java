package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ticket.FiscalTicketRequestDTO;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.InvoiceType;

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
    byte[] generateFiscalTicket(Long orderId, FiscalTicketRequestDTO fiscalTicketRequestDTO);

    /**
     * Generates a kitchen ticket (comanda) for the specified items in an order.
     * This ticket is used by kitchen staff to prepare the ordered items.
     *
     * @param order The order containing the items
     * @param items The specific items to include in the kitchen ticket
     * @return PDF byte array of the kitchen ticket
     */
    byte[] generateKitchenTicket(Order order, List<Item> items);
}

