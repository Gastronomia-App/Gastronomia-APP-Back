package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.model.dto.CategoryRequestDTO;
import com.progra3.cafeteria_api.model.dto.CategoryResponseDTO;
import com.progra3.cafeteria_api.service.port.ICategoryService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Operations related to product categories")
public class CategoryController {

    private final ICategoryService categoryService;

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Create a new category", description = "Registers a new category in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid category data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Category data to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CategoryRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "name": "Electronics"
                                }
                                """)
                    )
            )
            @RequestBody @Valid CategoryRequestDTO categoryRequestDTO) {
        return new ResponseEntity<>(categoryService.createCategory(categoryRequestDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @Operation(summary = "Get all categories", description = "Returns a list of all registered categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category list retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CategoryResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @Operation(summary = "Get a category by ID", description = "Retrieves a category by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @Parameter(description = "ID of the category to retrieve") @PathVariable @NotNull Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update an existing category", description = "Modifies the details of an existing category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @Parameter(description = "ID of the category to update") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated category data",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CategoryRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "name": "Accessories"
                                }
                                """)
                    )
            )
            @RequestBody @Valid CategoryRequestDTO categoryRequestDTO) {
        return new ResponseEntity<>(categoryService.updateCategory(id, categoryRequestDTO), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Delete a category", description = "Deletes a category by its ID, if it has no associated products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Cannot delete category with associated products", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID of the category to delete") @PathVariable @NotNull Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

