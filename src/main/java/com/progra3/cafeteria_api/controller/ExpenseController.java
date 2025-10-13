package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.service.helper.SortUtils;
import com.progra3.cafeteria_api.model.dto.ExpenseRequestDTO;
import com.progra3.cafeteria_api.model.dto.ExpenseResponseDTO;
import com.progra3.cafeteria_api.model.dto.ExpenseUpdateDTO;
import com.progra3.cafeteria_api.service.port.IExpenseService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expenses")
@Tag(name = "Expenses", description = "Operations related to managing expenses")
public class ExpenseController {

    private final IExpenseService expenseService;

    private final SortUtils sortUtils;

    @Operation(summary = "Create a new expense", description = "Registers a new expense in the system, including supplier, amount, and optional comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpenseResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid expense data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Expense data to create, including supplier ID, amount, and optional comment.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ExpenseRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "supplierId": 12,
                                  "amount": 1500.50,
                                  "comment": "Office supplies purchase"
                                }
                                """)
                    )
            )
            @RequestBody @Valid ExpenseRequestDTO dto) {
        ExpenseResponseDTO responseDTO = expenseService.create(dto);
        return ResponseEntity
                .created(URI.create("/api/expenses/" + responseDTO.id()))
                .body(responseDTO);
    }

    @Operation(
            summary = "Get all expenses",
            description = "Retrieves a paginated list of expenses, optionally filtered by supplier, amount range, or date range."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ExpenseResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @GetMapping
    public ResponseEntity<Page<ExpenseResponseDTO>> getExpenses(
            @Parameter(description = "Filter by supplier ID (optional)", example = "12")
            @RequestParam(required = false) Long supplierId,

            @Parameter(description = "Minimum amount for filtering expenses (optional)", example = "100.00")
            @RequestParam(required = false) Double minAmount,

            @Parameter(description = "Maximum amount for filtering expenses (optional)", example = "5000.00")
            @RequestParam(required = false) Double maxAmount,

            @Parameter(description = "Start date for filtering expenses (optional, ISO 8601 format)", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "End date for filtering expenses (optional, ISO 8601 format)", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "Page number for pagination (default is 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page (default is 10)", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sorting criteria in the format 'field,direction' (default is 'dateTime,asc')", example = "dateTime,asc")
            @RequestParam(defaultValue = "dateTime,asc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, sortUtils.buildSort(sort));
        Page<ExpenseResponseDTO> expenses = expenseService.getExpenses(supplierId, minAmount, maxAmount, startDate, endDate, pageable);
        return ResponseEntity.ok(expenses);
    }

    @Operation(summary = "Get an expense by ID", description = "Retrieves an expense by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpenseResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Expense not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(
            @Parameter(description = "ID of the expense to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(id));
    }

    @Operation(summary = "Update an expense", description = "Updates details of an existing expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExpenseResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Expense not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @PatchMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(
            @Parameter(description = "ID of the expense to update") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Expense fields to update",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ExpenseUpdateDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "supplierId": 15,
                                  "amount": 1800.00,
                                  "comment": "Updated office supplies purchase"
                                }
                                """)
                    )
            )
            @RequestBody @Valid ExpenseUpdateDTO dto) {
        return ResponseEntity.ok(expenseService.update(id, dto));
    }

    @Operation(summary = "Delete an expense", description = "Deletes an expense by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Expense not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "ID of the expense to delete") @PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}