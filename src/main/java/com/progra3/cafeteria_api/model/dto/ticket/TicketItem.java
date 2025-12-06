package com.progra3.cafeteria_api.model.dto.ticket;

import lombok.Builder;

import java.util.List;

/**
 * TicketItem represents a single line of the order in the ticket body.
 */
@Builder
public record TicketItem(

        /**
         * Quantity of the product.
         */
        Integer quantity,

        /**
         * Product name to display.
         */
        String productName,

        /**
         * Unit price of the product.
         * Used in BILL and PAYMENT tickets. Can be null for KITCHEN.
         */
        Double unitPrice,

        /**
         * Line total (quantity * unitPrice).
         * Used in BILL and PAYMENT tickets. Can be null for KITCHEN.
         */
        Double lineTotal,

        /**
         * Optional option groups for this item (e.g. "Cafe", "Cookies").
         * Mainly used for KITCHEN tickets.
         */
        List<TicketOptionGroup> optionGroups,

        /**
         * Optional comment associated with this item.
         * If not null/blank, it will be rendered below the item line.
         */
        String comment
) {
}
