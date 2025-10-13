package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.model.dto.ProductOptionRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductOptionResponseDTO;
import com.progra3.cafeteria_api.model.dto.ProductGroupRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductGroupResponseDTO;
import com.progra3.cafeteria_api.service.port.IProductGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
@Tag(name = "Product Groups", description = "Operations related to product groups and their options")
public class ProductGroupController {

    private final IProductGroupService productGroupService;

    @Operation(summary = "Create a new product group", description = "Creates a new product group with the given details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ProductGroupResponseDTO> createProductGroup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product group data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductGroupRequestDTO.class))
            )
            @RequestBody @Valid ProductGroupRequestDTO dto) {
        ProductGroupResponseDTO response = productGroupService.createProductGroup(dto);
        return ResponseEntity.created(URI.create("/api/groups/" + response.id())).body(response);
    }

    @Operation(summary = "Get all product groups", description = "Returns a list of all product groups")
    @ApiResponse(responseCode = "200", description = "List of product groups retrieved successfully")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @GetMapping
    public ResponseEntity<List<ProductGroupResponseDTO>> getAllProductGroups() {
        return ResponseEntity.ok(productGroupService.getAllProductGroups());
    }

    @Operation(summary = "Get a product group by ID", description = "Retrieves a product group by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product group found"),
            @ApiResponse(responseCode = "404", description = "Product group not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductGroupResponseDTO> getProductGroupById(
            @Parameter(description = "ID of the product group to retrieve", required = true)
            @PathVariable @NotNull Long id) {
        return ResponseEntity.ok(productGroupService.getProductGroupById(id));
    }

    @Operation(summary = "Update a product group", description = "Updates an existing product group with new data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product group updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product group not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductGroupResponseDTO> updateProductGroup(
            @Parameter(description = "ID of the product group to update", required = true)
            @PathVariable @NotNull Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated product group data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductGroupRequestDTO.class))
            )
            @RequestBody @Valid ProductGroupRequestDTO dto) {
        return ResponseEntity.ok(productGroupService.updateProductGroup(id, dto));
    }

    @Operation(summary = "Delete a product group", description = "Deletes a product group by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product group not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductGroup(
            @Parameter(description = "ID of the product group to delete", required = true)
            @PathVariable @NotNull Long id) {
        productGroupService.deleteProductGroup(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add options to a product group", description = "Adds a list of options to the specified product group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Options added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product group not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PostMapping("/{id}/options")
    public ResponseEntity<List<ProductOptionResponseDTO>> addOptions(
            @Parameter(description = "ID of the product group to add options to", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of product options to add",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductOptionRequestDTO.class)))
            )
            @RequestBody List<@Valid ProductOptionRequestDTO> options) {
        return ResponseEntity.ok(productGroupService.addProductOptions(id, options));
    }

    @Operation(summary = "Get options of a product group", description = "Returns all options of the specified product group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Options retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product group not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @GetMapping("/{id}/options")
    public ResponseEntity<List<ProductOptionResponseDTO>> getOptions(
            @Parameter(description = "ID of the product group to retrieve options from", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(productGroupService.getProductOptions(id));
    }

    @Operation(summary = "Update a product option", description = "Updates a specific product option in a product group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product option updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product group or option not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PutMapping("/{id}/options/{optionId}")
    public ResponseEntity<ProductOptionResponseDTO> updateOption(
            @Parameter(description = "ID of the product group", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID of the product option to update", required = true)
            @PathVariable Long optionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated product option data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductOptionRequestDTO.class))
            )
            @RequestBody @Valid ProductOptionRequestDTO dto) {
        return ResponseEntity.ok(productGroupService.updateProductOption(id, optionId, dto));
    }

    @Operation(summary = "Remove a product option", description = "Removes a product option from the specified product group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product option removed successfully"),
            @ApiResponse(responseCode = "404", description = "Product group or option not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}/options/{optionId}")
    public ResponseEntity<ProductOptionResponseDTO> removeOption(
            @Parameter(description = "ID of the product group", required = true)
            @PathVariable Long id,
            @Parameter(description = "ID of the product option to remove", required = true)
            @PathVariable Long optionId) {
        return ResponseEntity.ok(productGroupService.removeProductOption(id, optionId));
    }
}