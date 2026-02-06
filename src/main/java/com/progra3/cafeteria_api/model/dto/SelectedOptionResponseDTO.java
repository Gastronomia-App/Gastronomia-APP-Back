package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record SelectedOptionResponseDTO(

        @Schema(description = "Unique identifier of the selected product option", example = "15")
        Long id,

        @Schema(description = "Details of the selected product option")
        ProductOptionResponseDTO productOption,

        @Schema(description = "Quantity selected", example = "3")
        Integer quantity,

        @Schema(description = "List of child selected options")
        List<SelectedOptionResponseDTO> selectedOptions

) {
    public SelectedOptionResponseDTO {
        if (selectedOptions == null) selectedOptions = List.of();
    }
}
