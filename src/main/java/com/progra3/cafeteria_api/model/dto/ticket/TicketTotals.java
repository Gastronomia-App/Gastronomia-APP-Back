package com.progra3.cafeteria_api.model.dto.ticket;

import lombok.Builder;

/**
 * TicketTotals contains subtotal, discount and total information.
 */
@Builder
public record TicketTotals(

        /**
         * Subtotal before discounts.
         */
        Double subtotal,

        /**
         * Discount percentage applied to the order (0-100).
         */
        Integer discountPercent,

        /**
         * Discount amount in monetary value.
         */
        Double discountAmount,

        /**
         * Final total after applying discount.
         */
        Double total,

        /**
         * Indicates whether the warning "Este documento no es factura."
         * should be printed at the end of the ticket.
         */
        boolean printInvoiceWarning
) {
}
