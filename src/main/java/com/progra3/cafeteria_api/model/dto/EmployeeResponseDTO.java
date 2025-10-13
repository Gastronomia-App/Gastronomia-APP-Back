package com.progra3.cafeteria_api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record EmployeeResponseDTO(
        @Schema(description = "Unique identifier of the employee", example = "123")
        Long id,

        @Schema(description = "First name of the employee", example = "Juan")
        String name,

        @Schema(description = "Last name of the employee", example = "Pérez")
        String lastName,

        @Schema(description = "DNI number (national ID)", example = "12345678")
        String dni,

        @Schema(description = "Phone number of the employee", example = "541123456789")
        String phoneNumber,

        @Schema(description = "Email address of the employee", example = "juan.perez@example.com")
        String email,

        @Schema(description = "Role assigned to the employee", example = "ADMIN")
        String role,

        @Schema(description = "Indicates if the employee is deleted or inactive", example = "false")
        Boolean deleted
){}
