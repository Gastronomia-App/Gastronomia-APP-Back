package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.service.helper.SortUtils;
import com.progra3.cafeteria_api.model.dto.SupplierRequestDTO;
import com.progra3.cafeteria_api.model.dto.SupplierResponseDTO;
import com.progra3.cafeteria_api.model.dto.SupplierUpdateDTO;
import com.progra3.cafeteria_api.service.port.ISupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Operations related to supplier management")
public class SupplierController {

    private final ISupplierService supplierService;

    private final SortUtils sortUtils;

    @Operation(summary = "Create a new supplier", description = "Creates a new supplier with the given data")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Supplier created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<SupplierResponseDTO> createSupplier(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Supplier data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SupplierRequestDTO.class))
            )
            @Valid @RequestBody SupplierRequestDTO dto) {
        SupplierResponseDTO responseDTO = supplierService.create(dto);
        return ResponseEntity
                .created(URI.create("/api/suppliers/" + responseDTO.id()))
                .body(responseDTO);
    }

    @Operation(
            summary = "Get all suppliers",
            description = "Retrieves a paginated list of all suppliers, optionally filtered by trade name, legal name, or CUIT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of suppliers returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @GetMapping
    public ResponseEntity<Page<SupplierResponseDTO>> getSuppliers(
            @RequestParam(required = false)
            @Schema(description = "Filter by trade name (partial match)", example = "Comercial XYZ")
            String tradeName,

            @RequestParam(required = false)
            @Schema(description = "Filter by legal name (partial match)", example = "Empresa S.A.")
            String legalName,

            @RequestParam(required = false)
            @Schema(description = "Filter by CUIT in format XX-XXXXXXXX-X", example = "30-12345678-9")
            String cuit,

            @RequestParam(defaultValue = "0")
            @Schema(description = "Page number for pagination (0-based)", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Schema(description = "Number of suppliers per page", example = "10")
            int size,

            @RequestParam(defaultValue = "tradeName,asc")
            @Schema(description = "Sorting criteria in the format 'field,direction'", example = "tradeName,asc")
            String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, sortUtils.buildSort(sort));
        Page<SupplierResponseDTO> suppliers = supplierService.getSuppliers(tradeName, legalName, cuit, pageable);
        return ResponseEntity.ok(suppliers);
    }

    @Operation(summary = "Get supplier by ID", description = "Retrieves the supplier details for the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier found"),
            @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(
            @Parameter(description = "ID of the supplier to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @Operation(summary = "Update supplier", description = "Updates supplier data partially")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Supplier updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<SupplierResponseDTO> updateSupplier(
            @Parameter(description = "ID of the supplier to update", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Supplier update data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SupplierUpdateDTO.class))
            )
            @Valid @RequestBody SupplierUpdateDTO dto) {
        return ResponseEntity.ok(supplierService.update(id, dto));
    }

    @Operation(summary = "Delete supplier", description = "Deletes the supplier with the given ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Supplier deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Supplier not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "ID of the supplier to delete", required = true)
            @PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}