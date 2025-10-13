package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductOptionResponseDTO(

        @Schema(description = "Unique identifier of the product option", example = "1")
        Long id,

        @Schema(description = "ID of the associated product", example = "10")
        Long productId,

        @Schema(description = "Maximum quantity allowed for this product option", example = "5")
        Integer maxQuantity,

        @Schema(description = "Additional price increase applied for this option", example = "2.5")
        Double priceIncrease
) {}