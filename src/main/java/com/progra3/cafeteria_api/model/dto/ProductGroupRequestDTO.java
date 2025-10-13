package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductGroupRequestDTO(

        @Schema(description = "Name of the product group", example = "Beverages")
        @NotBlank(message = "Name cannot be blank")
        String name,

        @Schema(description = "Minimum number of products allowed in the group", example = "1")
        @NotNull(message = "Min quantity is required")
        @PositiveOrZero(message = "Min quantity must be zero or positive")
        Integer minQuantity,

        @Schema(description = "Maximum number of products allowed in the group", example = "3")
        @NotNull(message = "Max quantity is required")
        @PositiveOrZero(message = "Max quantity must be zero or positive")
        Integer maxQuantity

) {}
