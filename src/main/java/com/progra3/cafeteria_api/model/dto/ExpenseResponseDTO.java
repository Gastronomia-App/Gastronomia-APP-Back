package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Builder
public record ExpenseResponseDTO (
        @Schema(description = "Unique identifier of the expense", example = "1")
        Long id,

        @Schema(description = "Supplier associated with the expense")
        SupplierResponseDTO supplier,

        @Schema(description = "Amount of the expense", example = "1500.50")
        Double amount,

        @Schema(description = "Optional comment about the expense", example = "Office supplies purchase", nullable = true)
        String comment,

        @Schema(description = "Date and time when the expense was recorded", example = "2025-06-14T15:30:00")
        LocalDateTime date,

        @Schema(description = "Indicates whether the expense is marked as deleted", example = "false")
        Boolean deleted
){}