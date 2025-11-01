package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductResponseDTO(

        @Schema(description = "Unique identifier of the product", example = "12")
        Long id,

        @Schema(description = "Name of the product", example = "Café Latte")
        String name,

        @Schema(description = "Description of the product", example = "Delicious coffee with milk")
        String description,

        @Schema(description = "Selling price of the product", example = "150.0")
        Double price,

        @Schema(description = "Cost price of the product", example = "100.0")
        Double cost,

        @Schema(description = "Flag indicating if stock control is enabled", example = "true")
        Boolean controlStock,

        @Schema(description = "Current stock quantity", example = "25")
        Integer stock,

        @Schema(description = "Category this product belongs to")
        CategoryResponseDTO category,

        @Schema(description = "Indicates if the product is active", example = "true")
        Boolean active,

        @Schema(description = "Indicates if the product is a composite product (made of components)", example = "false")
        Boolean composite,

        @Schema(description = "List of components if this is a composite product")
        List<ProductComponentResponseDTO> components,

        @Schema(description = "List of product groups the product belongs to", example = "[\"Promociones\", \"Especiales\"]")
        List<ProductGroupResponseDTO> productGroups

) { }
