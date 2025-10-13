package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.model.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record EmployeeRequestDTO(

        @Schema(description = "First name of the employee", example = "Juan", minLength = 1, maxLength = 50, required = true)
        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        String name,

        @Schema(description = "Last name of the employee", example = "Pérez", minLength = 1, maxLength = 50, required = true)
        @NotBlank(message = "Lastname is required")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        String lastName,

        @Schema(description = "DNI number (national ID)", example = "12345678", pattern = "\\d{7,8}", required = true)
        @NotBlank(message = "DNI is required")
        @Pattern(regexp = "^\\d{7,8}$", message = "DNI must be between 7 and 8 numeric digits")
        String dni,

        @Schema(description = "Email address of the employee", example = "juan.perez@example.com", required = true)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format. Expected format: example@domain.com")
        String email,

        @Schema(description = "Phone number of the employee", example = "541123456789", pattern = "\\d{9,12}", required = true)
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\d{9,12}$", message = "Phone number must be between 9 and 12 numeric digits")
        String phoneNumber,

        @Schema(description = "Username for the employee account", example = "username123")
        @NotBlank(message = "Username is required")
        @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
        String username,

        @Schema(description = "Password for the employee account", minLength = 8, required = true)
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @Schema(description = "Role of the employee", required = true)
        @NotNull(message = "Role is required")
        Role role
) { }
