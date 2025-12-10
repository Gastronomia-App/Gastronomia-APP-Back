package com.progra3.cafeteria_api.model.dto.ticket;

import lombok.Builder;

/**
 * TicketOptionLine represents a single option inside a group.
 * Example: "1 x Café con Leche".
 */
@Builder
public record TicketOptionLine(

        /**
         * Option quantity.
         */
        Integer quantity,

        /**
         * Option display name (e.g. product name used as option).
         */
        String name,

        /**
         * Nesting level for rendering indentation in kitchen tickets.
         * Level 1 = option directly selected on the item, 2+ = nested options.
         */
        Integer level
) {
}
