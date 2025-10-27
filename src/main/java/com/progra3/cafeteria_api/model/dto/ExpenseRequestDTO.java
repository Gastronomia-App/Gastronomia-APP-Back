package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
public record ExpenseRequestDTO(

        @Schema(description = "ID of the supplier for the expense", example = "12", required = true)
        @NotNull(message = "Each expense must have a supplier")
        Long supplierId,

        @Schema(description = "Amount of the expense, must be positive", example = "1500.50", required = true)
        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be positive")
        Double amount,

        @Schema(description = "Optional comment about the expense", example = "Office supplies purchase", maxLength = 255, nullable = true)
        @Size(max = 255, message = "Comment must have max 255 characters")
        String comment,

        @Schema(description = "Date and time of the expense", example = "2025-10-26T14:30:00", required = true)
        @NotNull(message = "Date and time cannot be null")
        LocalDateTime dateTime
){}