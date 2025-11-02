package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ExpenseUpdateDTO(

        @Schema(description = "ID of the supplier associated with the expense", example = "1", required = false)
        Long supplierId,

        @Positive(message = "Amount must be positive")
        @Schema(description = "Amount of the expense, must be positive", example = "1500.50", required = false)
        Double amount,

        @Schema(description = "Optional comment about the expense", example = "Office supplies purchase", required = false, nullable = true)
        String comment,

        @Schema(description = "Date and time of the expense", example = "2025-10-31T14:30:00", required = false)
        LocalDateTime dateTime
) {}
