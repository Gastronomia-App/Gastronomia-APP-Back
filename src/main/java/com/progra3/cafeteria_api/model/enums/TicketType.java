package com.progra3.cafeteria_api.model.enums;
/**
 * TicketType defines the different printable ticket variants.
 */
public enum TicketType {
    /**
     * Internal ticket for kitchen/bar.
     * Shows items and options, without prices.
     */
    KITCHEN_TICKET,
    /**
     * Customer-facing ticket for reviewing the bill.
     * Shows items and totals, without payment details.
     */
    PRE_TICKET,
    /**
     * Customer-facing ticket after the order fiscalized.
     * Shows items and totals. Payment details can be added later.
     */
    FISCAL_TICKET
}
