package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.model.dto.SeatingRequestDTO;
import com.progra3.cafeteria_api.model.dto.SeatingResponseDTO;
import com.progra3.cafeteria_api.service.port.ISeatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seating")
@Tag(name = "Seating", description = "Operations related to seating management")
public class SeatingController {

    private final ISeatingService seatingService;

    @Operation(summary = "Create a new seating", description = "Creates a new seating entry")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Seating created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<SeatingResponseDTO> createSeating(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Seating data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SeatingRequestDTO.class))
            )
            @RequestBody @Valid SeatingRequestDTO dto) {
        SeatingResponseDTO seatingResponseDTO = seatingService.create(dto);
        return ResponseEntity
                .created(URI.create("/api/seating/" + seatingResponseDTO.id()))
                .body(seatingResponseDTO);
    }

    @Operation(summary = "Get all seatings", description = "Retrieves all seating entries")
    @ApiResponse(responseCode = "200", description = "List of seatings retrieved successfully")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @GetMapping
    public ResponseEntity<List<SeatingResponseDTO>> getAllSeating() {
        return ResponseEntity.ok(seatingService.getAll());
    }

    @Operation(summary = "Get seating by ID", description = "Retrieves a seating entry by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seating found"),
            @ApiResponse(responseCode = "404", description = "Seating not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @GetMapping("/{id}")
    public ResponseEntity<SeatingResponseDTO> getSeatingById(
            @Parameter(description = "ID of the seating to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(seatingService.getById(id));
    }

    @Operation(summary = "Get seating by number", description = "Retrieves a seating entry by its number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seating found"),
            @ApiResponse(responseCode = "404", description = "Seating not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'CASHIER', 'WAITER')")
    @GetMapping("/number")
    public ResponseEntity<SeatingResponseDTO> getSeatingByNumber(
            @Parameter(description = "Number of the seating to retrieve", required = true)
            @RequestParam Integer number) {
        return ResponseEntity.ok(seatingService.getByNumber(number));
    }

    @Operation(summary = "Update a seating entry", description = "Updates the seating number of an existing seating")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seating updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Seating not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SeatingResponseDTO> updateSeating(
            @Parameter(description = "ID of the seating to update", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated seating data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SeatingRequestDTO.class))
            )
            @RequestBody @Valid SeatingRequestDTO dto) {
        SeatingResponseDTO seatingResponseDTO = seatingService.updateNumber(id, dto);
        return ResponseEntity.ok(seatingResponseDTO);
    }

    @Operation(summary = "Delete a seating entry", description = "Deletes a seating entry by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seating deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Seating not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SeatingResponseDTO> deleteSeating(
            @Parameter(description = "ID of the seating to delete", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(seatingService.delete(id));
    }
}
