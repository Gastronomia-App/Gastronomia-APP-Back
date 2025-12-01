package com.progra3.cafeteria_api.model.ticket;

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
        String name
) {
}
