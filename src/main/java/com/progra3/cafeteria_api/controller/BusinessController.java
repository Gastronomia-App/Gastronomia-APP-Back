package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.model.dto.BusinessRequestDTO;
import com.progra3.cafeteria_api.model.dto.BusinessResponseDTO;
import com.progra3.cafeteria_api.model.dto.BusinessUpdateDTO;
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
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Get business by ID", description = "Retrieves a business by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BusinessResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Business not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponseDTO> getById(@PathVariable Long id) {
        BusinessResponseDTO responseDTO = businessService.getBusinessById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Update business", description = "Updates the information of an existing business")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BusinessResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Business not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<BusinessResponseDTO> update(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated business data",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BusinessUpdateDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "name": "CoffeeCloud Premium",
                                  "cuit": "20-12345678-9",
                                  "address": {
                                    "street": "Av. Corrientes 5678",
                                    "city": "Buenos Aires",
                                    "province": "Buenos Aires",
                                    "zipCode": "1406"
                                  }
                                }
                                """)
                    )
            )
            @RequestBody @Valid BusinessUpdateDTO dto) {
        BusinessResponseDTO responseDTO = businessService.updateBusiness(id, dto);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Delete business", description = "Deletes a business from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Business deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Business not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }
}