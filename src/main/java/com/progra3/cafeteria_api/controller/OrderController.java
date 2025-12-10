package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.model.dto.*;
import com.progra3.cafeteria_api.model.dto.ticket.FiscalTicketRequest;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import com.progra3.cafeteria_api.model.enums.OrderType;
import com.progra3.cafeteria_api.service.helper.SortUtils;
import com.progra3.cafeteria_api.service.port.IOrderService;
import com.progra3.cafeteria_api.service.port.tickets.ITicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Operations related to orders")
public class OrderController {

    private final IOrderService orderService;
    private final ITicketService ticketService;
    private final SortUtils sortUtils;

    @Operation(summary = "Create a new order", description = "Creates a new order with optional customer and employee IDs. The order starts in ACTIVE state.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER', 'OWNER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order creation data",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderRequestDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "seatingId": 5,
                                      "employeeId": 45,
                                      "customerId": 123,
                                      "orderType": "TABLE",
                                      "peopleCount": 4
                                    }
                                    """)
                    )
            )
            @RequestBody @Valid OrderRequestDTO dto) {
        OrderResponseDTO responseDTO = orderService.create(dto);
        return ResponseEntity
                .created(URI.create("/api/orders/" + responseDTO.id()))
                .body(responseDTO);
    }

    @Operation(summary = "Add multiple items to an order", description = "Adds multiple items to the order. The items will be included in the total and subtotal calculations.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Items successfully added to the order"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER', 'OWNER', 'ADMIN')")
    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponseDTO> addItems(
            @Parameter(description = "ID of the order to add items to")
            @PathVariable @NotNull Long id,
            @RequestBody List<@Valid ItemRequestDTO> items) {
        OrderResponseDTO orderResponseDTO = orderService.addItems(id, items);
        return ResponseEntity
                .created(URI.create("/api/orders/" + id + "/items"))
                .body(orderResponseDTO);
    }

    @Operation(
            summary = "Get all orders",
            description = "Retrieves a paginated list of all orders in the system, optionally filtered by date range, customer, employee, status, seating, order type, or total range."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of orders returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'OWNER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<OrderResponseDTO>> getOrders(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Schema(description = "Start date for filtering orders (inclusive)", example = "2024-01-01")
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Schema(description = "End date for filtering orders (inclusive)", example = "2024-12-31")
            LocalDate endDate,

            @RequestParam(required = false)
            @Schema(description = "Name of the customer to filter orders by", example = "Juan")
            String customerName,

            @RequestParam(required = false)
            @Schema(description = "Name of the employee to filter orders by", example = "Maria")
            String employeeName,

            @RequestParam(required = false)
            @Schema(description = "Status of the orders to filter by", example = "COMPLETED")
            OrderStatus status,

            @RequestParam(required = false)
            @Schema(description = "Number of the seating/table to filter orders by", example = "5")
            Integer seatingNumber,

            @RequestParam(required = false)
            @Schema(description = "Type of the orders to filter by", example = "TABLE")
            OrderType orderType,

            @RequestParam(required = false)
            @Schema(description = "Minimum total amount for filtering orders", example = "100.0")
            Double minTotal,

            @RequestParam(required = false)
            @Schema(description = "Maximum total amount for filtering orders", example = "500.0")
            Double maxTotal,

            @RequestParam(defaultValue = "0")
            @Schema(description = "Page number for pagination (0-based)", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Schema(description = "Number of orders per page", example = "10")
            int size,

            @RequestParam(defaultValue = "dateTime,asc")
            @Schema(description = "Sorting criteria in the format 'field,direction'", example = "dateTime,asc")
            String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, sortUtils.buildSort(sort));
        Page<OrderResponseDTO> orders = orderService.getOrders(startDate, endDate, customerName, employeeName, status, seatingNumber, orderType, minTotal, maxTotal, pageable);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get an order by ID", description = "Retrieves the details of a specific order based on its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found and returned"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'OWNER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID of the order to retrieve")
            @PathVariable @NotNull Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Operation(summary = "Get items from an order", description = "Returns all items associated with a given order ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of items returned successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'OWNER', 'ADMIN')")
    @GetMapping("/{id}/items")
    public ResponseEntity<List<ItemResponseDTO>> getItems(
            @Parameter(description = "ID of the order to retrieve items from")
            @PathVariable @NotNull Long id) {
        return ResponseEntity.ok(orderService.getItems(id));
    }

    @Operation(summary = "Update an order", description = "Updates basic fields of an existing order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER', 'OWNER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @Parameter(description = "ID of the order to update")
            @PathVariable @NotNull Long id,
            @RequestBody @Valid OrderRequestDTO dto) {
        return ResponseEntity.ok(orderService.update(id, dto));
    }

    @Operation(summary = "Update the discount of an order", description = "Updates the discount value of an order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Discount updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid discount value", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'OWNER', 'ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateDiscount(
            @Parameter(description = "ID of the order to update discount")
            @PathVariable @NotNull Long id,
            @RequestParam @Min(0) @Max(100) Integer discount) {
        return ResponseEntity.ok(orderService.updateDiscount(id, discount));
    }

    @Operation(summary = "Update an item in an order", description = "Modifies the comment and quantity of an existing item within an order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order or item not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER', 'OWNER', 'ADMIN')")
    @PutMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<ItemResponseDTO> updateItem(
            @Parameter(description = "ID of the order")
            @PathVariable @NotNull Long orderId,
            @Parameter(description = "ID of the item to update")
            @PathVariable @NotNull Long itemId,
            @RequestBody @Valid ItemRequestDTO dto) {
        return ResponseEntity.ok(orderService.updateItem(orderId, itemId, dto));
    }

    @Operation(summary = "Remove an item from an order", description = "Performs a logical delete of the item from the order. It will no longer affect the total.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item marked as deleted"),
            @ApiResponse(responseCode = "404", description = "Order or item not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER', 'OWNER', 'ADMIN')")
    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<ItemResponseDTO> removeItem(
            @Parameter(description = "ID of the order")
            @PathVariable @NotNull Long orderId,
            @Parameter(description = "ID of the item to remove")
            @PathVariable @NotNull Long itemId) {
        return ResponseEntity.ok(orderService.removeItem(orderId, itemId));
    }

    @Operation(summary = "Split an order", description = "Splits an order into two separate orders based on the provided items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order split successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'OWNER', 'ADMIN')")
    @PatchMapping("/{orderId}/split")
    public ResponseEntity<List<OrderResponseDTO>> splitOrder(
            @Parameter(description = "ID of the order to split")
            @PathVariable @NotNull Long orderId,
            @RequestBody @Valid OrderSplitRequestDTO dto) {
        return ResponseEntity.ok(orderService.transferItemsBetweenOrders(orderId, dto));
    }

    @Operation(
            summary = "Finalize an order",
            description = "Marks the order as finalized with one or more payment methods. The sum of all payment amounts must equal the order total."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order finalized successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment methods or amount mismatch", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order or payment method not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'OWNER', 'ADMIN')")
    @PatchMapping("/{id}/finalize")
    public ResponseEntity<OrderResponseDTO> finalizeOrder(
            @Parameter(description = "ID of the order to finalize")
            @PathVariable @NotNull Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of payment methods with amounts",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderPaymentMethodDTO.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "paymentMethodId": 1,
                                        "amount": 500.00
                                      },
                                      {
                                        "paymentMethodId": 2,
                                        "amount": 250.50
                                      }
                                    ]
                                    """)
                    )
            )
            @RequestBody @Valid List<OrderPaymentMethodDTO> paymentMethods) {
        return ResponseEntity.ok(orderService.finalizeOrder(id, paymentMethods));
    }

    @Operation(summary = "Cancel an order", description = "Marks the order as CANCELED and excludes it from further operations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order canceled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'OWNER', 'ADMIN')")
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @Parameter(description = "ID of the order to cancel")
            @PathVariable @NotNull Long id) {
        return ResponseEntity.ok(orderService.updateStatus(id, OrderStatus.CANCELED));
    }

    @Operation(
            summary = "Generate kitchen ticket (comanda) for an order",
            description = "Generates a PDF kitchen ticket with the specified items for kitchen staff to prepare."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kitchen ticket generated successfully",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to generate kitchen ticket", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER', 'OWNER', 'ADMIN')")
    @PostMapping(value = "/{id}/tickets/kitchen", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateKitchenTicket(
            @Parameter(description = "ID of the order to generate kitchen ticket for")
            @PathVariable @NotNull Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "List of items to include in the kitchen ticket",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ItemRequestDTO.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "productName": "Hamburguesa Completa",
                                        "quantity": 2,
                                        "comment": "Sin cebolla",
                                        "options": ["Extra queso", "Sin tomate"]
                                      },
                                      {
                                        "productName": "Papas Fritas",
                                        "quantity": 1,
                                        "comment": "",
                                        "options": []
                                      }
                                    ]
                                    """)
                    )
            )
            @RequestBody @Valid List<Long> itemIds) {

        byte[] pdf = ticketService.generateKitchenTicket(id, itemIds);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=comanda_orden_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    @Operation(
            summary = "Generate pre-ticket for an order",
            description = "Generates a PDF pre-ticket (cuenta) for the customer showing items and totals, without payment details or CAE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pre-ticket generated successfully",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to generate pre-ticket", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'WAITER', 'OWNER', 'ADMIN')")
    @GetMapping(value = "/{id}/tickets/pre-ticket", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generatePreTicket(
            @Parameter(description = "ID of the order to generate pre-ticket for")
            @PathVariable @NotNull Long id) {

        Order order = orderService.getEntityById(id);
        if (order.getStatus() != OrderStatus.ACTIVE) {
            order.setStatus(OrderStatus.BILLED);
        }
        byte[] pdf = ticketService.generatePreTicket(id);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=order-" + id + "-pre-ticket.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    @Operation(
            summary = "Generate fiscal ticket (electronic invoice) for an order",
            description = "Generates an electronic invoice via AFIP and returns a PDF fiscal ticket with CAE. Supports Invoice types A, B, and C."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fiscal ticket generated successfully",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid invoice type or order missing customer data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to generate fiscal ticket or AFIP service unavailable", content = @Content)
    })
    @PreAuthorize("hasAnyRole('CASHIER', 'OWNER', 'ADMIN')")
    @PostMapping(value = "/{id}/tickets/fiscal-ticket", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateFiscalTicket(
            @Parameter(description = "ID of the order to generate fiscal ticket for")
            @PathVariable @NotNull Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fiscal ticket request data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FiscalTicketRequest.class))
            )
            @RequestBody @Valid FiscalTicketRequest fiscalTicketRequest) {

        byte[] pdf = ticketService.generateFiscalTicket(id, fiscalTicketRequest);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=order-" + id + "-fiscal-ticket.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

}
