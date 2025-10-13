package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductOptionRequestDTO(

        @Schema(description = "ID of the associated product", example = "10", required = true)
        @NotNull(message = "Product ID is required")
        Long productId,

        @Schema(description = "Maximum quantity allowed for this option", example = "5", required = true)
        @NotNull(message = "Max quantity is required")
        @Positive(message = "Max quantity must be positive")
        Integer maxQuantity,

        @Schema(description = "Additional price increase for this option (can be zero)", example = "2.5")
        @PositiveOrZero(message = "Price increase must be positive or zero")
        Double priceIncrease
) {}