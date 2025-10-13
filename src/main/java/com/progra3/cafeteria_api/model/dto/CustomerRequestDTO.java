package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Builder
public record CustomerRequestDTO (
        @Schema(description = "First name of the customer", example = "Juan", required = true)
        @NotBlank(message = "Name cannot be null")
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        String name,

        @Schema(description = "Last name of the customer", example = "Pérez", required = true)
        @NotBlank(message = "Last name cannot be null")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        String lastName,

        @Schema(description = "National ID number (DNI)", example = "12345678", required = true)
        @NotBlank(message = "DNI cannot be null")
        @Pattern(regexp = "^\\d{7,8}$", message = "DNI must be between 7 and 8 numeric digits")
        String dni,

        @Schema(description = "Phone number of the customer", example = "541112345678", required = true)
        @NotBlank(message = "Phone number cannot be null")
        @Pattern(regexp = "^\\d{10,13}$", message = "Phone number must be between 10 and 13 numeric digits.")
        String phoneNumber,

        @Schema(description = "Email address", example = "juan.perez@example.com", required = true)
        @NotBlank(message = "Email cannot be null")
        @Email(message = "Invalid email format. Expected format: example@domain.com")
        String email,

        @Schema(description = "Discount percentage applicable to the customer", example = "10", minimum = "0", maximum = "100")
        @Min(value = 0, message = "Discount must be between 0 and 100")
        @Max(value = 100, message = "Discount must be between 0 and 100")
        Integer discount
){}