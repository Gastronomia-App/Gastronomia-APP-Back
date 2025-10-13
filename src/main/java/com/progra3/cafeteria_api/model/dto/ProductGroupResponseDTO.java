package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProductGroupResponseDTO(
        @Schema(description = "Unique identifier of the product group", example = "1")
        Long id,

        @Schema(description = "Name of the product group", example = "Beverages")
        String name,

        @Schema(description = "Minimum quantity of products allowed in the group", example = "1")
        Integer minQuantity,

        @Schema(description = "Maximum quantity of products allowed in the group", example = "3")
        Integer maxQuantity,

        @Schema(description = "List of product options available in this group")
        List<ProductOptionResponseDTO> options
) {}
