package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OrderPaymentMethodResponseDTO(

        @Schema(description = "ID of the payment method", example = "1")
        Long paymentMethodId,

        @Schema(description = "Name of the payment method", example = "Cash")
        String paymentMethodName,

        @Schema(description = "Amount paid with this payment method", example = "500.50")
        Double amount
) {}

