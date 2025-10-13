package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SelectedProductOptionRequestDTO(

        @Schema(description = "ID of the selected product option", example = "10", required = true)
        @NotNull(message = "Option ID cannot be null")
        Long productOptionId,

        @Schema(description = "Quantity of the selected product option", example = "2", required = true)
        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Quantity must be positive")
        Integer quantity

) { }
