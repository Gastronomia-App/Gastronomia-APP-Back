package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductComponentResponseDTO(

        @Schema(description = "Unique identifier of the component", example = "12")
        Long id,

        @Schema(description = "Name of the product component", example = "Espresso Shot")
        String productName,

        @Schema(description = "Quantity of the component used", example = "2")
        Integer quantity

) { }
