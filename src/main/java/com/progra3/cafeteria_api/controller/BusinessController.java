package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.model.dto.BusinessRequestDTO;
import com.progra3.cafeteria_api.model.dto.BusinessResponseDTO;
import com.progra3.cafeteria_api.service.impl.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/businesses")
@Tag(name = "Businesses", description = "Operations related to business registration and management")
public class BusinessController {

    private final BusinessService businessService;

    @Operation(summary = "Register a new business", description = "Creates and registers a new business entity in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Business created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BusinessResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BusinessResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Business data to register",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BusinessRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "name": "CoffeeCloud",
                                  "cuit": "20-12345678-9",
                                  "address": {
                                    "street": "Av. Corrientes 1234",
                                    "city": "Buenos Aires",
                                    "province": "Buenos Aires",
                                    "zipCode": "1406"
                                  },
                                  "owner": {
                                    "name": "Juan",
                                    "lastName": "Pérez",
                                    "dni": "12345678",
                                    "email": "juan.perez@example.com",
                                    "phoneNumber": "541123456789",
                                    "username": "juanperez",
                                    "password": "MySecret123",
                                    "role": "ADMIN"
                                  }
                                }
                                """)
                    )
            )
            @RequestBody @Valid BusinessRequestDTO dto) {

        BusinessResponseDTO responseDTO = businessService.createBusiness(dto);
        return ResponseEntity
                .created(URI.create("/api/businesses/" + responseDTO.id()))
                .body(responseDTO);
    }
}