package com.progra3.cafeteria_api.model.dto.ticket;

import lombok.Builder;

import java.util.List;

/**
 * TicketItem represents a single line of the order in the ticket body.
 */
@Builder
public record TicketItem(

        Integer quantity,
        String productName,
        Double unitPrice,
        Double lineTotal,
        Double taxPercent,
        List<TicketOptionGroup> optionGroups,
        String comment
) {
}
