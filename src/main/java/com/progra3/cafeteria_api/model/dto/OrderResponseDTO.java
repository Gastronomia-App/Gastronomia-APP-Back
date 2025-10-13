package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.model.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponseDTO (

        @Schema(description = "Unique identifier of the order", example = "101")
        Long id,

        @Schema(description = "Full name of the employee who handled the order", example = "Juan Pérez")
        String employeeName,

        @Schema(description = "Full name of the customer who placed the order", example = "María Gómez")
        String customerName,

        @Schema(description = "Number of the seating/table", example = "7")
        Integer seatingNumber,

        @Schema(description = "Type of the order", example = "DINE_IN")
        OrderType orderType,

        @Schema(description = "List of items included in the order")
        List<ItemResponseDTO> items,

        @Schema(description = "Date and time when the order was placed", example = "2024-06-14T12:45:00")
        LocalDateTime dateTime,

        @Schema(description = "Number of people for whom the order was made", example = "3")
        Integer peopleCount,

        @Schema(description = "Discount applied to the order", example = "10")
        Integer discount,

        @Schema(description = "Current status of the order", example = "COMPLETED")
        String status,

        @Schema(description = "Subtotal amount before discount", example = "2500.0")
        Double subtotal,

        @Schema(description = "Total amount after applying discount", example = "2250.0")
        Double total

) {}
