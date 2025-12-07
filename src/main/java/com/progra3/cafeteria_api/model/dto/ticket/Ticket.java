package com.progra3.cafeteria_api.model.dto.ticket;

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

        List<TicketItem> items,

        TicketTotals totals
) {
}