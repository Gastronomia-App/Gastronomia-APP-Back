package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.service.helper.SortUtils;
import com.progra3.cafeteria_api.model.dto.CustomerRequestDTO;
import com.progra3.cafeteria_api.model.dto.CustomerResponseDTO;
import com.progra3.cafeteria_api.model.dto.CustomerUpdateDTO;
import com.progra3.cafeteria_api.service.port.ICustomerService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Operations related to customer management")
public class CustomerController {

    private final ICustomerService customerService;

    private final SortUtils sortUtils;

    @Operation(summary = "Create a new customer", description = "Registers a new customer in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid customer data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> createCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer data to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CustomerRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "name": "John",
                                  "lastName": "Doe",
                                  "dni": "12345678",
                                  "phoneNumber": "541112345678",
                                  "email": "john.doe@example.com"
                                }
                                """)
                    )
            )
            @Valid @RequestBody CustomerRequestDTO dto) {
        CustomerResponseDTO responseDTO = customerService.create(dto);
        return ResponseEntity
                .created(URI.create("/api/customers/" + responseDTO.id()))
                .body(responseDTO);
    }

    @Operation(
            summary = "Get all customers",
            description = "Retrieves a paginated list of all registered customers, optionally filtered by name, last name, DNI, or email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer list retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CustomerResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @GetMapping
    public ResponseEntity<Page<CustomerResponseDTO>> getCustomers(
            @Parameter(description = "Filter by customer name (optional)", example = "John")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by customer last name (optional)", example = "Doe")
            @RequestParam(required = false) String lastName,

            @Parameter(description = "Filter by customer DNI (optional)", example = "12345678")
            @RequestParam(required = false) String dni,

            @Parameter(description = "Filter by customer email (optional)", example = "john.doe@example.com")
            @RequestParam(required = false) String email,

            @Parameter(description = "Page number for pagination (default is 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page (default is 10)", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sorting criteria in the format 'field,direction' (default is 'name,asc')", example = "name,asc")
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, sortUtils.buildSort(sort));
        Page<CustomerResponseDTO> customers = customerService.getCustomers(name, lastName, dni, email, pageable);
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Get a customer by ID", description = "Retrieves a customer by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(
            @Parameter(description = "ID of the customer to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @Operation(summary = "Update a customer", description = "Updates customer data partially (e.g., contact information)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid update data", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @Parameter(description = "ID of the customer to update") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer fields to update",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CustomerUpdateDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "email": "new.email@example.com",
                                  "phoneNumber": "541198765432",
                                  "discount": 15
                                }
                                """)
                    )
            )
            @Valid @RequestBody CustomerUpdateDTO dto) {
        return ResponseEntity.ok(customerService.update(id, dto));
    }

    @Operation(summary = "Delete a customer", description = "Deletes a customer by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "ID of the customer to delete") @PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}