package com.progra3.cafeteria_api.model.ticket;

import com.progra3.cafeteria_api.model.enums.TicketType;
import lombok.Builder;

import java.util.List;

/**
 * Ticket is a printable representation of an Order.
 * It is an intermediate model between the domain and the PDF renderer.
 */
@Builder
public record Ticket(

        TicketType type,

        TicketHeader header,

        /**
         * List of items to be printed in the body section of the ticket.
         */
        List<TicketItem> items,

        /**
         * Totals information.
         * Can be null for tickets that do not show totals (e.g. KITCHEN).
         */
        TicketTotals totals
) {
}