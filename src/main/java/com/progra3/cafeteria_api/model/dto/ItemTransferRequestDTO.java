package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ItemTransferRequestDTO(

        @Schema(description = "ID of the item to transfer", example = "123")
        @NotNull(message = "Item ID cannot be null")
        Long itemId,

        @Schema(description = "Quantity of the item to transfer", example = "2")
        @NotNull(message = "Quantity cannot be null")
        Integer quantity

) {}
