package com.progra3.cafeteria_api.service.impl.tickets;

import com.progra3.cafeteria_api.afip.wsfe.AfipInvoiceService;
import com.progra3.cafeteria_api.afip.wsfe.generated.*;
import com.progra3.cafeteria_api.exception.order.FailedToPrintException;
import com.progra3.cafeteria_api.model.dto.ticket.FiscalTicketRequest;
import com.progra3.cafeteria_api.model.dto.ticket.Ticket;
import com.progra3.cafeteria_api.model.dto.ticket.TicketContext;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.service.port.IOrderService;
import com.progra3.cafeteria_api.service.port.tickets.ITicketBuilderService;
import com.progra3.cafeteria_api.service.port.tickets.ITicketPdfService;
import com.progra3.cafeteria_api.service.port.tickets.ITicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for ticket generation and electronic invoicing operations.
 * Handles pre-tickets, fiscal tickets, kitchen tickets, and AFIP WSFEv1 integration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService implements ITicketService {

    private final AfipInvoiceService afipInvoiceService;
    private final ITicketBuilderService ticketBuilderService;
    private final ITicketPdfService ticketPdfService;
    private final IOrderService orderService;

    @Value("${billing.iva:21.0}")
    private Double ivaPercentage;

    @Value("${afip.punto-venta:1}")
    private Integer puntoVenta;

    // ===========================
    // Public API
    // ===========================

    @Override
    public byte[] generatePreTicket(Long orderId) {
        try {
            Order order = orderService.getEntityById(orderId);

            Ticket ticket = ticketBuilderService.build(TicketType.PRE_TICKET, order, null);

            if (order.getStatus() == OrderStatus.ACTIVE) {
                orderService.updateStatus(orderId, OrderStatus.BILLED);
            }

            return ticketPdfService.generateTicketPdf(ticket);
        } catch (Exception e) {
            log.error("Failed to generate pre-ticket for order ID: {}", orderId, e);
            throw new FailedToPrintException("Failed to generate pre-ticket", e);
        }
    }

    @Override
    public byte[] generateFiscalTicket(Long orderId, FiscalTicketRequest fiscalTicketRequest) {
        try {
            Order order = orderService.getEntityById(orderId);
            FECAEResponse response = generateElectronicInvoice(order, fiscalTicketRequest);

            FECAEDetResponse detResp = extractValidDetail(response);
            if (detResp == null) {
                throw new IllegalStateException("AFIP rejected the invoice or returned an invalid response");
            }

            TicketContext ticketContext = TicketContext.builder()
                    // Invoice type and AFIP data
                    .invoiceType(fiscalTicketRequest.invoiceType())
                    .puntoVenta(puntoVenta)
                    .cbteNumero(detResp.getCbteDesde())
                    .concept("Productos")
                    .cae(detResp.getCAE())
                    .caeExpiration(extractCaeExpirationFromResponse(response))
                    // Customer data
                    .customerIvaCondition(fiscalTicketRequest.ivaCondition())
                    .documentType(fiscalTicketRequest.documentType())
                    .documentNumber(fiscalTicketRequest.documentNumber())
                    .customerName(fiscalTicketRequest.customerName())
                    .customerAddress(fiscalTicketRequest.customerAddress())
                    .build();

            Ticket ticket = ticketBuilderService.build(TicketType.FISCAL_TICKET, order, ticketContext);
            return ticketPdfService.generateTicketPdf(ticket);
        } catch (Exception e) {
            log.error("Failed to generate fiscal ticket for order ID: {}", orderId, e);
            throw new IllegalStateException("Failed to generate fiscal ticket", e);
        }
    }

    @Override
    public byte[] generateKitchenTicket(Long orderId, List<Long> itemIds) {
        Order order = orderService.getEntityById(orderId);

        List<Item> addedItems = order.getItems().stream()
                .filter(item -> itemIds.contains(item.getId()))
                .toList();

        TicketContext ticketContext = TicketContext.builder()
                .kitchenItems(addedItems)
                .build();

        try {
            Ticket ticket = ticketBuilderService.build(TicketType.KITCHEN_TICKET, order, ticketContext);
            return ticketPdfService.generateTicketPdf(ticket);
        } catch (Exception e) {
            log.error("Failed to generate kitchen ticket for order ID: {}", order, e);
            throw new FailedToPrintException("Failed to generate kitchen ticket", e);
        }
    }

    // ===========================
    // Electronic Invoice Generation
    // ===========================

    /**
     * Generates an electronic invoice via AFIP WSFEv1.
     *
     * @param order                  The order to invoice
     * @param fiscalTicketRequest The fiscal ticket request data
     * @return The CAE (Código de Autorización Electrónico) received from AFIP
     * @throws IllegalStateException if AFIP service is unavailable or CAE is not received
     */
    private FECAEResponse generateElectronicInvoice(Order order, FiscalTicketRequest fiscalTicketRequest) {

        log.error(fiscalTicketRequest.toString());
        // Validate AFIP availability
        if (!checkAfipAvailability()) {
            throw new IllegalStateException("AFIP service is not available");
        }

        // Extract and validate customer data
        if (order.getCustomer() == null || order.getCustomer().getDni() == null) {
            throw new IllegalArgumentException("Order must have a customer with DNI for electronic invoicing");
        }

        // Calculate amounts
        BigDecimal totalAmount = BigDecimal.valueOf(order.getTotal());
        BigDecimal netAmount = calculateNetAmount(totalAmount);
        BigDecimal ivaAmount = calculateIvaAmount(totalAmount);

        // Generate invoice based on type
        return switch (fiscalTicketRequest.invoiceType()) {
            case FACTURA_A -> {
                log.info("Generating Invoice A for order ID: {}", order.getId());
                yield afipInvoiceService.generateInvoice(fiscalTicketRequest, totalAmount, netAmount, ivaAmount);
            }
            case FACTURA_B -> {
                log.info("Generating Invoice B for order ID: {}", order.getId());
                yield afipInvoiceService.generateInvoice(fiscalTicketRequest, totalAmount, netAmount, ivaAmount);
            }
            case FACTURA_C -> {
                log.info("Generating Invoice C for order ID: {}", order.getId());
                yield afipInvoiceService.generateInvoice(fiscalTicketRequest, totalAmount, null, null);
            }
            case NOTA_CREDITO_A -> null; //TODO: Implement credit note generation
            case NOTA_CREDITO_B -> null;
            case NOTA_CREDITO_C -> null;
        };
    }

    /**
     * Checks AFIP service availability.
     */
    private boolean checkAfipAvailability() {
        try {
            return afipInvoiceService.checkAfipStatus();
        } catch (Exception e) {
            log.error("Error checking AFIP availability", e);
            return false;
        }
    }

    // ===========================
    // Amount Calculations
    // ===========================

    /**
     * Calculates net amount (taxable base) from total amount.
     * Formula: netAmount = totalAmount / (1 + IVA/100)
     */
    private BigDecimal calculateNetAmount(BigDecimal totalAmount) {
        BigDecimal ivaFactor = BigDecimal.valueOf(ivaPercentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal divisor = BigDecimal.ONE.add(ivaFactor);
        return totalAmount.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates IVA amount from total amount.
     * Formula: ivaAmount = totalAmount - netAmount
     */
    private BigDecimal calculateIvaAmount(BigDecimal totalAmount) {
        return totalAmount.subtract(calculateNetAmount(totalAmount));
    }

    // ===========================
    // Response Processing
    // ===========================

    /**
     * Extracts and validates the detail response from AFIP response.
     *
     * @param response The AFIP response
     * @return The valid FECAEDetResponse if approved, null otherwise
     */
    private FECAEDetResponse extractValidDetail(FECAEResponse response) {
        if (response == null || response.getFeDetResp() == null) {
            log.error("Invalid AFIP response: null response or detail");
            return null;
        }

        ArrayOfFECAEDetResponse detRespArray = response.getFeDetResp();
        if (detRespArray.getFECAEDetResponse().isEmpty()) {
            log.error("Invalid AFIP response: empty detail response");
            return null;
        }

        FECAEDetResponse detResp = detRespArray.getFECAEDetResponse().getFirst();

        // Must be approved
        if (!"A".equals(detResp.getResultado())) {
            log.error("Invoice rejected by AFIP. Result: {}", detResp.getResultado());
            if (detResp.getObservaciones() != null) {
                detResp.getObservaciones().getObs().forEach(obs ->
                        log.error("AFIP Observation [{}]: {}", obs.getCode(), obs.getMsg())
                );
            }
            return null;
        }

        return detResp;
    }


    /**
     * Extracts CAE from AFIP response and validates approval.
     *
     * @param response The AFIP response
     * @return The CAE if approved, null otherwise
     */
    private String extractCaeFromResponse(FECAEResponse response) {
        FECAEDetResponse detResp = extractValidDetail(response);
        return detResp != null ? detResp.getCAE() : null;
    }


    /**
     * Extracts CAE expiration date from AFIP response and parses it.
     *
     * @param response The AFIP response
     * @return The CAE expiration date as LocalDate, or null if invalid
     */
    private LocalDate extractCaeExpirationFromResponse(FECAEResponse response) {
        FECAEDetResponse detResp = extractValidDetail(response);
        if (detResp == null) return null;

        String caeExpirationStr = detResp.getCAEFchVto();
        if (caeExpirationStr == null || caeExpirationStr.length() != 8) {
            log.error("Invalid or missing CAE expiration date: {}", caeExpirationStr);
            return null;
        }

        try {
            return LocalDate.parse(
                    caeExpirationStr,
                    DateTimeFormatter.ofPattern("yyyyMMdd")
            );
        } catch (Exception e) {
            log.error("Failed to parse CAE expiration '{}': {}", caeExpirationStr, e.getMessage());
            return null;
        }
    }

}
