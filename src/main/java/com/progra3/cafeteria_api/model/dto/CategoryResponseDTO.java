package com.progra3.cafeteria_api.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.progra3.cafeteria_api.model.enums.CategoryIcon;
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
        @JsonProperty("color")
        String color,

        @Schema(description = "Icon associated with the category", example = "BEVERAGES")
        CategoryIcon icon,

        @Schema(description = "Whether this category should be visible in the menu. " +
                "If null, the client may treat it as visible by default.")
        Boolean visibleInMenu,

        @Schema(description = "List of products under this category (only included when fetching categories)")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<ProductResponseDTO> products
) { }
