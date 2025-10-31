package com.progra3.cafeteria_api.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryResponseDTO(
        @Schema(description = "Unique identifier of the category", example = "1")
        Long id,

        @Schema(description = "Name of the category", example = "Beverages")
        String name,

        @Schema(description = "Color associated with the category in HEX format",
                example = "#FF5733")
        String color,

        @Schema(description = "List of products under this category (only included when fetching categories)")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<ProductResponseDTO> products
) { }
