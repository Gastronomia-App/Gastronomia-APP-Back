package com.progra3.cafeteria_api.model.dto.ticket;

import com.progra3.cafeteria_api.model.enums.OrderType;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TicketHeader contains common header information
 * that can be rendered differently depending on the ticket type.
 */
@Builder
public record TicketHeader(
    // Business info
    String businessName,
    Long businessCuit,
    String businessAddress,
    String businessIvaCondition,

    // Fiscal-specific fields
    String businessIibb,
    LocalDate businessActivityStart,
    Integer invoiceCode,
    Integer puntoVenta,
    Long cbteNumero,
    String concept,
    String customerIvaCondition,

    // Order info
    Long orderId,
    Integer seatingNumber,
    OrderType orderType,
    Integer peopleCount,
    String employeeName,
    String customerName,
    LocalDateTime dateTime,
    String title
) {
}
