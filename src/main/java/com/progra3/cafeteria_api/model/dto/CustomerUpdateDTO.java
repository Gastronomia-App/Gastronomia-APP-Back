package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

public record CustomerUpdateDTO(
        @Schema(description = "First name of the customer", example = "Juan", minLength = 1, maxLength = 50, nullable = true)
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        @Pattern(regexp = "^\\s*\\S.*$", message = "Name cannot be blank or only spaces")
        String name,

        @Schema(description = "Last name of the customer", example = "Pérez", minLength = 1, maxLength = 50, nullable = true)
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        @Pattern(regexp = "^\\s*\\S.*$", message = "Last name cannot be blank or only spaces")
        String lastName,

        @Schema(description = "National ID number (DNI)", example = "12345678", pattern = "\\d{7,8}", nullable = true)
        @Pattern(regexp = "^\\d{7,8}$", message = "DNI must be between 7 and 8 numeric digits")
        String dni,

        @Schema(description = "Phone number", example = "541112345678", pattern = "\\d{10,13}", nullable = true)
        @Pattern(regexp = "^\\d{10,13}$", message = "Phone number must be between 10 and 13 numeric digits.")
        String phoneNumber,

        @Schema(description = "Email address", example = "juan.perez@example.com", nullable = true)
        @Email(message = "Invalid email format. Expected format: example@domain.com")
        String email,

        @Schema(description = "Discount percentage", example = "10", minimum = "0", maximum = "100", nullable = true)
        @Min(value = 0, message = "Discount must be between 0 and 100")
        @Max(value = 100, message = "Discount must be between 0 and 100")
        Integer discount
) {}