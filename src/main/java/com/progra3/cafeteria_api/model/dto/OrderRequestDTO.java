package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.validation.ValidOrderRequest;
import com.progra3.cafeteria_api.model.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;


@Builder
@ValidOrderRequest
public record OrderRequestDTO(

        @Schema(description = "ID of the seating where the order is placed", example = "5")
        Long seatingId,

        @Schema(description = "ID of the employee who created the order", example = "2")
        Long employeeId,

        @Schema(description = "ID of the customer who made the order", example = "3")
        Long customerId,

        @Schema(description = "Type of the order (e.g., DINE_IN, TAKEAWAY, DELIVERY)", example = "DINE_IN", required = true)
        @NotNull(message = "Order type cannot be null")
        OrderType orderType,

        @Schema(description = "Number of people for the order", example = "4")
        @Min(value = 1, message = "It must be at least 1 person")
        Integer peopleCount

) {}