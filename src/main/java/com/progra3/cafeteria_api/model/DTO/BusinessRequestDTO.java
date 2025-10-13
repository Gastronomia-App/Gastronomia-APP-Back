package com.progra3.cafeteria_api.model.dto;

import com.progra3.cafeteria_api.validation.ValidBusinessRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidBusinessRequest
public record BusinessRequestDTO(

        @Schema(description = "Name of the business", example = "Café Central")
        @NotBlank(message = "Name cannot be blank")
        String name,

        @Schema(description = "CUIT (tax ID) of the business", example = "30-12345678-9")
        @NotBlank(message = "CUIT cannot be blank")
        String cuit,

        @Schema(description = "Address details of the business")
        @Valid
        AddressRequestDTO address,

        @Schema(description = "Owner employee details")
        @NotNull(message = "Owner cannot be null")
        @Valid
        EmployeeRequestDTO owner

) {}
