package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record ItemResponseDTO(

        @Schema(description = "Unique identifier of the item", example = "2001")
        Long id,

        @Schema(description = "Identifier of the associated order", example = "1005")
        Long orderId,

        @Schema(description = "Identifier of the associated product", example = "301")
        Long productId,

        @Schema(description = "List of selected options for the product")
        List<SelectedProductOptionResponseDTO> selectedOptions,

        @Schema(description = "Price per unit of the product", example = "12.50")
        Double unitPrice,

        @Schema(description = "Quantity of the product", example = "3")
        Integer quantity,

        @Schema(description = "Comment related to the item", example = "Extra cheese")
        String comment,

        @Schema(description = "Total price for the item", example = "37.50")
        Double totalPrice,

        @Schema(description = "Indicates whether the item is marked as deleted", example = "false")
        Boolean deleted
) {}