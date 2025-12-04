package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.validation.ValidProductRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

@Builder
@ValidProductRequest
public record ProductRequestDTO(

        @NotNull(message = "Category id cannot be null")
        @Schema(description = "ID of the category this product belongs to", example = "3", required = true)
        Long categoryId,

        @NotNull(message = "Name cannot be null")
        @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
        @Schema(description = "Name of the product", example = "Café Latte", required = true, minLength = 1, maxLength = 100)
        String name,

        @Size(min = 1, max = 255, message = "Description must be between 1 and 255 characters")
        @Schema(description = "Description of the product", example = "Delicious coffee with milk", minLength = 1, maxLength = 255)
        String description,

        @NotNull(message = "Price cannot be null")
        @Positive(message = "Price must be positive")
        @Schema(description = "Selling price of the product", example = "150.0", required = true, minimum = "0.01")
        Double price,

        @Positive(message = "Cost must be positive")
        @Schema(description = "Cost price of the product", example = "100.0", minimum = "0.01")
        Double cost,

        @NotNull(message = "Active status cannot be null")
        @Schema(description = "Indicates if the product is active", example = "true")
        Boolean active,

        @NotNull(message = "Control stock cannot be null")
        @Schema(description = "Indicates if stock control is enabled", example = "true", required = true)
        Boolean controlStock,

        @PositiveOrZero(message = "Stock can't be less than 0")
        @Schema(description = "Current stock quantity", example = "25", minimum = "0")
        Integer stock,

        @Schema(description = "List of components if the product is composed of other products (empty if not composite)",
                example = "[{\"productId\": 1, \"quantity\": 2}]")
        List<ProductComponentRequestDTO> components,

        @Schema(description = "List of product group IDs this product belongs to",
                example = "[1, 2, 3]")
        List<Long> productGroups,

        @Schema(description = "URL of the product image previously uploaded",
                example = "/uploads/products/cafe-latte-123.jpg")
        String imageUrl

) { }

