package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderPaymentMethodDTO(

        @Schema(description = "ID of the payment method", example = "1")
        @NotNull(message = "Payment method ID cannot be null")
        Long paymentMethodId,

        @Schema(description = "Amount paid with this payment method", example = "500.50")
        @NotNull(message = "Amount cannot be null")
        @Min(value = 0, message = "Amount must be greater than or equal to 0")
        Double amount
) {}

