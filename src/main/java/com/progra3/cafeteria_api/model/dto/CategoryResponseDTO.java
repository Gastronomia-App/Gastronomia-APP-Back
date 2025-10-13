package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryResponseDTO(
        @Schema(description = "Unique identifier of the category", example = "1")
        Long id,

        @Schema(description = "Name of the category", example = "Beverages")
        String name,

        @Schema(description = "List of products under this category")
        List<ProductResponseDTO> products
) { }
