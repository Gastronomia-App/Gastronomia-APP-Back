package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SelectedProductOptionResponseDTO(

        @Schema(description = "Unique identifier of the selected product option", example = "15")
        Long id,

        @Schema(description = "ID of the product option", example = "10")
        Long productOptionId,

        @Schema(description = "Quantity selected", example = "3")
        Long quantity

) { }
