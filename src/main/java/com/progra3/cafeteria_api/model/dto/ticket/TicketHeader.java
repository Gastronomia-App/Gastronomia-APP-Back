package com.progra3.cafeteria_api.model.dto.ticket;

import com.progra3.cafeteria_api.model.enums.OrderType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * TicketHeader contains common header information
 * that can be rendered differently depending on the ticket type.
 */
@Builder
public record TicketHeader(

        /**
         * Business display name.
         * Used mainly for BILL and PAYMENT tickets.
         */
        String businessName,

        /**
         * Business tax identifier (CUIT).
         */
        Long businessCuit,

        /**
         * Business address (optional).
         */
        String businessAddress,

        /**
         * Business phone number (optional).
         */
        String businessPhone,

        /**
         * Internal order identifier.
         */
        Long orderId,

        /**
         * Seating/table number. Can be null for non-table orders.
         */
        Integer seatingNumber,

        /**
         * Order type: TABLE, TAKEAWAY, DELIVERY.
         */
        OrderType orderType,

        /**
         * Number of people for the order. Can be null.
         */
        Integer peopleCount,

        /**
         * Full name of the employee (waiter/cashier) handling the order.
         */
        String employeeName,

        /**
         * Full name of the customer.
         * If null, the renderer should avoid printing the "Cliente:" line.
         */
        String customerName,

        /**
         * Date and time associated with the ticket.
         * Usually the order creation or printing time.
         */
        LocalDateTime dateTime,

        /**
         * Optional title shown in the header section.
         * Examples:
         * - null or empty for KITCHEN
         * - "CUENTA / CONSUMO" for BILL
         * - "COMPROBANTE DE PAGO" for PAYMENT
         */
        String title
) {
}
