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

    @Operation(summary = "Get my business", description = "Retrieves the business associated with the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BusinessResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Business not found for the current user", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<BusinessResponseDTO> getMyBusiness() {
        BusinessResponseDTO responseDTO = businessService.getBusinessForCurrentUser();
        return ResponseEntity.ok(responseDTO);
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

    @Operation(summary = "Update business", description = "Updates the information of an existing business. Only the owner of the business can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BusinessResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Business not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - You don't have permission to modify this business", content = @Content)
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

    @Operation(summary = "Delete business", description = "Performs a soft delete on a business. Only the owner of the business can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Business deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Business not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - You don't have permission to delete this business", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get business by slug (public)",
            description = """
                    Public endpoint used mainly by the public menu.
                    It returns business information based on its slug, 
                    as long as the business is not deleted.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BusinessResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Business not found", content = @Content)
    })
    @GetMapping("/public/{slug}")
    public ResponseEntity<BusinessResponseDTO> getBySlug(@PathVariable String slug) {
        BusinessResponseDTO responseDTO = businessService.getBusinessBySlugPublic(slug);
        return ResponseEntity.ok(responseDTO);
    }
}