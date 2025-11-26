package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.model.entity.ProductOption;
import io.swagger.v3.oas.annotations.media.Schema;

public record SelectedProductOptionResponseDTO(

        @Schema(description = "Unique identifier of the selected product option", example = "15")
        Long id,

        @Schema(description = "Details of the selected product option")
        ProductOptionResponseDTO productOption,

        @Schema(description = "Quantity selected", example = "3")
        Integer quantity

) { }
