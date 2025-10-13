package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.validation.ValidItemRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

@Builder
@ValidItemRequest
public record ItemRequestDTO(

        @NotNull(message = "productId cannot be null")
        @Schema(description = "ID of the selected product", example = "101", required = true)
        Long productId,

        @NotNull
        @Min(value = 1, message = "Minimum quantity is 1")
        @Schema(description = "Quantity of the selected product", example = "2", required = true, minimum = "1")
        Integer quantity,

        @Schema(description = "List of selected options for the product", required = false)
        List<SelectedProductOptionRequestDTO> selectedOptions,

        @Schema(description = "Optional comment about the item", example = "No onions", required = false)
        String comment
) {}