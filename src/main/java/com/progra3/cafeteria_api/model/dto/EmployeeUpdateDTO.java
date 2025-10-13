package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import com.progra3.cafeteria_api.model.enums.*;

@Builder
public record EmployeeUpdateDTO(

        @Schema(description = "First name of the employee", example = "Juan",
                minLength = 1, maxLength = 50,
                pattern = "^\\s*\\S.*$",
                nullable = true)
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        @Pattern(regexp = "^\\s*\\S.*$", message = "Name cannot be blank or only spaces")
        String name,

        @Schema(description = "Last name of the employee", example = "Pérez",
                minLength = 1, maxLength = 50,
                pattern = "^\\s*\\S.*$",
                nullable = true)
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        @Pattern(regexp = "^\\s*\\S.*$", message = "Last name cannot be blank or only spaces")
        String lastName,

        @Schema(description = "DNI (national ID) number", example = "12345678",
                pattern = "^\\d{7,8}$", nullable = true)
        @Pattern(regexp = "^\\d{7,8}$", message = "DNI must be between 7 and 8 numeric digits")
        String dni,

        @Schema(description = "Email address of the employee", example = "juan.perez@example.com", nullable = true)
        @Email(message = "Invalid email format. Expected format: example@domain.com")
        String email,

        @Schema(description = "Phone number of the employee", example = "541123456789",
                pattern = "^\\d{10,13}$", nullable = true)
        @Pattern(regexp = "^\\d{10,13}$", message = "Phone number must be between 10 and 13 numeric digits")
        String phoneNumber,

        @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
        String username,

        @Schema(description = "Password for the employee account, minimum 8 characters", example = "password123", nullable = true)
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @Schema(description = "Role assigned to the employee", example = "ADMIN", nullable = true)
        Role role
){}



