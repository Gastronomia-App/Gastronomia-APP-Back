package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.service.helper.SortUtils;
import com.progra3.cafeteria_api.model.dto.ProductComponentRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.service.port.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Operations related to product management")
public class ProductController {

    private final IProductService productService;

    private final SortUtils sortUtils;

    @Operation(summary = "Create a new product", description = "Registers a new product in the system")
    @ApiResponse(responseCode = "201", description = "Product created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProductResponseDTO.class)))
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product data to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "categoryId": 3,
                                  "name": "Café Latte",
                                  "description": "Delicious coffee with milk",
                                  "price": 150.0,
                                  "cost": 100.0,
                                  "controlStock": true,
                                  "stock": 25
                                }
                                """)
                    )
            )
            @RequestBody @Valid ProductRequestDTO productRequestDTO) {
        return new ResponseEntity<>(productService.createProduct(productRequestDTO), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all products",
            description = "Retrieves a paginated list of all registered products, optionally filtered by name, category, stock range, or composite status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product list retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getProducts(
            @RequestParam(required = false)
            @Schema(description = "Filter by product name (partial match)", example = "Café Latte")
            String name,

            @RequestParam(required = false)
            @Schema(description = "Filter by category ID", example = "3")
            Long categoryId,

            @RequestParam(required = false)
            @Schema(description = "Minimum stock quantity for filtering", example = "10")
            Integer minStock,

            @RequestParam(required = false)
            @Schema(description = "Maximum stock quantity for filtering", example = "100")
            Integer maxStock,

            @RequestParam(required = false)
            @Schema(description = "Filter by composite product status (true for composite, false for non-composite)", example = "false")
            boolean composite,

            @RequestParam(defaultValue = "0")
            @Schema(description = "Page number for pagination (0-based)", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Schema(description = "Number of products per page", example = "10")
            int size,

            @RequestParam(defaultValue = "name,asc")
            @Schema(description = "Sorting criteria in the format 'field,direction'", example = "name,asc")
            String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, sortUtils.buildSort(sort));
        Page<ProductResponseDTO> products = productService.getProducts(name, categoryId, minStock, maxStock, composite, pageable);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get a product by ID", description = "Retrieves a product by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @Parameter(description = "ID of the product to retrieve") @PathVariable @NotNull Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Update an existing product", description = "Modifies the details of an existing product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Parameter(description = "ID of the product to update") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated product data",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProductRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "categoryId": 3,
                                  "name": "Smartphone Pro",
                                  "description": "Updated model with better features",
                                  "price": 799.99,
                                  "cost": 500.0,
                                  "controlStock": true,
                                  "stock": 30
                                }
                                """)
                    )
            )
            @RequestBody @Valid ProductRequestDTO productRequestDTO) {
        return new ResponseEntity<>(productService.updateProduct(id, productRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "Delete a product", description = "Deletes a product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> deleteProduct(
            @Parameter(description = "ID of the product to delete") @PathVariable @NotNull Long id) {
        ProductResponseDTO response = productService.deleteProduct(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Assign a group to a product", description = "Links a group to a product by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group assigned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product or group not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PutMapping("/{id}/groups/{groupId}")
    public ResponseEntity<ProductResponseDTO> assignGroupToProduct(
            @Parameter(description = "ID of the product") @PathVariable Long id,
            @Parameter(description = "ID of the group to assign") @PathVariable Long groupId) {
        ProductResponseDTO response = productService.assignGroupToProduct(id, groupId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove a group from a product", description = "Unlinks a group from a product by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group removed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product or group not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}/groups/{groupId}")
    public ResponseEntity<ProductResponseDTO> removeGroupFromProduct(
            @Parameter(description = "ID of the product") @PathVariable Long id,
            @Parameter(description = "ID of the group to remove") @PathVariable Long groupId) {
        ProductResponseDTO response = productService.removeGroupFromProduct(id, groupId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add components to a product", description = "Adds one or more components to a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Components added successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PutMapping("/{id}/components")
    public ResponseEntity<ProductResponseDTO> addComponents(
            @Parameter(description = "ID of the product") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of components to add",
                    required = true,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductComponentRequestDTO.class)))
            )
            @RequestBody List<@Valid ProductComponentRequestDTO> components
    ) {
        ProductResponseDTO response = productService.addComponentsToProduct(id, components);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a product component", description = "Updates the quantity of a specific component in a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Component updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product or component not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PutMapping("/{id}/components/{componentId}")
    public ResponseEntity<ProductResponseDTO> updateComponent(
            @Parameter(description = "ID of the product") @PathVariable Long id,
            @Parameter(description = "ID of the component to update") @PathVariable Long componentId,
            @Parameter(description = "New quantity for the component") @RequestParam Integer quantity
    ) {
        ProductResponseDTO response = productService.updateProductComponent(id, componentId, quantity);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove a component from a product", description = "Deletes a specific component from a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Component removed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Product or component not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}/components/{componentId}")
    public ResponseEntity<ProductResponseDTO> removeComponent(
            @Parameter(description = "ID of the product") @PathVariable Long id,
            @Parameter(description = "ID of the component to remove") @PathVariable Long componentId
    ) {
        ProductResponseDTO response = productService.removeComponentFromProduct(id, componentId);
        return ResponseEntity.ok(response);
    }
}