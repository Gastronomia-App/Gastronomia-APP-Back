package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.service.helper.SortUtils;
import com.progra3.cafeteria_api.model.dto.EmployeeRequestDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeResponseDTO;
import com.progra3.cafeteria_api.model.dto.EmployeeUpdateDTO;
import com.progra3.cafeteria_api.service.port.IEmployeeService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.progra3.cafeteria_api.model.enums.Role;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Operations related to employee management")
public class EmployeeController {

    private final IEmployeeService employeeService;

    private final SortUtils sortUtils;

    @Operation(summary = "Create a new employee", description = "Registers a new employee in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid employee data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Employee data to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EmployeeRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "name": "Alice",
                                  "lastName": "Smith",
                                  "dni": "12345678",
                                  "email": "alice.smith@example.com",
                                  "phoneNumber": "541123456789",
                                  "role": "WAITER",
                                  "password": "securePassword"
                                }
                                """)
                    )
            )
            @RequestBody @Valid EmployeeRequestDTO dto) {
        EmployeeResponseDTO response = employeeService.createEmployee(dto);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all employees",
            description = "Retrieves a paginated list of employees, optionally filtered by name, last name, DNI, email, or role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EmployeeResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponseDTO>> getEmployees(
            @Parameter(description = "Filter by employee name (optional)", example = "Alice")
            @RequestParam(required = false) String name,

            @Parameter(description = "Filter by employee last name (optional)", example = "Smith")
            @RequestParam(required = false) String lastName,

            @Parameter(description = "Filter by employee DNI (optional)", example = "12345678")
            @RequestParam(required = false) String dni,

            @Parameter(description = "Filter by employee email (optional)", example = "alice.smith@example.com")
            @RequestParam(required = false) String email,

            @Parameter(description = "Filter by employee role (optional)", example = "WAITER")
            @RequestParam(required = false) Role role,

            @Parameter(description = "Page number for pagination (default is 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page (default is 10)", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sorting criteria in the format 'field,direction' (default is 'name,asc')", example = "name,asc")
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, sortUtils.buildSort(sort));
        Page<EmployeeResponseDTO> employees = employeeService.getEmployees(name, lastName, dni, email, role, pageable);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Get an employee by ID", description = "Retrieves a specific employee by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
            @Parameter(description = "ID of the employee to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Operation(summary = "Update an employee", description = "Partially updates an employee's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @Parameter(description = "ID of the employee to update") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Employee fields to update",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EmployeeUpdateDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "name": "John",
                                  "lastName": "Doe",
                                  "dni": "87654321",
                                  "email": "john.doe@example.com",
                                  "phoneNumber": "541198765432",
                                  "username": "johndoe",
                                  "password": "newSecurePassword",
                                  "role": "ADMIN"
                                }
                                """)
                    )
            )
            @RequestBody @Valid EmployeeUpdateDTO dto) {
        EmployeeResponseDTO response = employeeService.updateEmployee(id, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an employee", description = "Marks an employee as deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee deleted successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> deleteEmployee(@PathVariable @NotNull Long id){
        EmployeeResponseDTO response = employeeService.deleteEmployee(id);
        return ResponseEntity.ok(response);
    }
}