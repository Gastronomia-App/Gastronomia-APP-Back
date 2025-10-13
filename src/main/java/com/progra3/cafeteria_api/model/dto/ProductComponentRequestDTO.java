package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProductComponentRequestDTO(

        @Schema(description = "ID of the product that is a component", example = "5")
        @NotNull(message = "Product ID is required")
        Long productId,

        @Schema(description = "Quantity of the component used in the product", example = "2")
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity

) { }
