package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.model.dto.AuditFinalizeRequestDTO;
import com.progra3.cafeteria_api.service.helper.SortUtils;
import com.progra3.cafeteria_api.model.dto.AuditRequestDTO;
import com.progra3.cafeteria_api.model.dto.AuditResponseDTO;
import com.progra3.cafeteria_api.service.port.IAuditService;
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
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audits")
@Tag(name = "Audits", description = "Operations related to audit lifecycle management")
public class AuditController {

    private final IAuditService auditService;


    private final SortUtils sortUtils;


    @Operation(summary = "Create a new audit", description = "Registers a new audit entry in the system")
    @ApiResponse(responseCode = "201", description = "Audit created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AuditResponseDTO.class)))
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @PostMapping
    public ResponseEntity<AuditResponseDTO> createAudit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Audit data to create",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuditRequestDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "initialCash": 1000.0
                                }
                                """)
                    )
            )
            @Valid @RequestBody AuditRequestDTO dto) {
        AuditResponseDTO auditResponseDTO = auditService.create(dto);
        return ResponseEntity.created(URI.create("/api/audits/" + auditResponseDTO.id()))
                .body(auditResponseDTO);
    }


    @Operation(
            summary = "Get all audits",
            description = "Retrieves a paginated list of all audit entries. Supports filtering by start and end dates, and sorting by specific fields."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Audit list retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AuditResponseDTO.class)),
                            examples = @ExampleObject(value = """
                {
                  "content": [
                    {
                      "id": 1,
                      "startTime": "2025-06-14T10:15:30",
                      "closeTime": "2025-06-14T18:00:00",
                      "initialCash": 1000.0,
                      "orders": [],
                      "expenses": [],
                      "auditStatus": "OPEN",
                      "totalExpensed": 500.0,
                      "total": 1500.0,
                      "balanceGap": 0.0,
                      "deleted": false
                    }
                  ],
                  "pageable": {
                    "pageNumber": 0,
                    "pageSize": 10
                  },
                  "totalElements": 1,
                  "totalPages": 1
                }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @GetMapping
    public ResponseEntity<Page<AuditResponseDTO>> getAudits(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Filter audits starting from this date (inclusive). Format: yyyy-MM-dd")
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Filter audits ending at this date (inclusive). Format: yyyy-MM-dd")
            LocalDate endDate,

            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number for pagination. Default is 0.")
            int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of items per page. Default is 10.")
            int size,

            @RequestParam(defaultValue = "closeTime,asc")
            @Parameter(description = "Sorting criteria in the format 'field,direction'. Default is 'closeTime,asc'.")
            String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, sortUtils.buildSort(sort));
        Page<AuditResponseDTO> audits = auditService.getAudits(startDate, endDate, pageable);
        return ResponseEntity.ok(audits);
    }


    @Operation(summary = "Get an audit by ID", description = "Retrieves a specific audit entry by its unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuditResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audit not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @GetMapping("/{id}")
    public ResponseEntity<AuditResponseDTO> getAuditById(
            @Parameter(description = "ID of the audit to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(auditService.getById(id));
    }


    @Operation(summary = "Finalize an audit", description = "Marks an audit entry as finalized")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit finalized successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuditResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audit not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @PatchMapping("/{id}/finalize")
    public ResponseEntity<AuditResponseDTO> finalizeAudit(
            @Parameter(description = "ID of the audit to finalize") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Audit data to finalize",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuditFinalizeRequestDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "realCash": 150000.0
                                    }
                                    """)
                    )
            ) @Valid @RequestBody AuditFinalizeRequestDTO dto) {
        return ResponseEntity.ok(auditService.finalize(id, dto));
    }


    @Operation(summary = "Cancel an audit", description = "Marks an audit entry as cancelled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit cancelled successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuditResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Audit not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<AuditResponseDTO> cancelAudit(
            @Parameter(description = "ID of the audit to cancel") @PathVariable Long id) {
        return ResponseEntity.ok(auditService.cancel(id));
    }
}