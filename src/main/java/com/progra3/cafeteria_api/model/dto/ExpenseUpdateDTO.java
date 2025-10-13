package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record ExpenseUpdateDTO(

        @Schema(description = "ID of the supplier associated with the expense", example = "1", required = false)
        Long supplierId,

        @Positive(message = "Amount must be positive")
        @Schema(description = "Amount of the expense, must be positive", example = "1500.50", required = false)
        Double amount,

        @Schema(description = "Optional comment about the expense", example = "Office supplies purchase", required = false, nullable = true)
        String comment
) {}
