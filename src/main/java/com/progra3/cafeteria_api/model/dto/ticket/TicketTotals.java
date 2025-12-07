package com.progra3.cafeteria_api.model.dto.ticket;

import lombok.Builder;

import java.time.LocalDate;

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
    boolean printInvoiceWarning,

    /**
     * CAE (Código de Autorización Electrónico) from AFIP.
     * Only present for fiscal/electronic invoices.
     */
    String cae,

    /**
     * CAE expiration date from AFIP.
     * Only present for fiscal/electronic invoices.
     */
    LocalDate caeExpiration,

    /**
     * Base64-encoded QR data for AFIP fiscal ticket.
     * Contains JSON with invoice details per AFIP specifications.
     */
    String qrData
) {
}
