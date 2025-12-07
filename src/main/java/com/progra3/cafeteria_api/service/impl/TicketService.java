package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.afip.wsfe.AfipInvoiceService;
import com.progra3.cafeteria_api.afip.wsfe.generated.*;
import com.progra3.cafeteria_api.exception.order.FailedToPrintException;
import com.progra3.cafeteria_api.model.dto.ticket.FiscalTicketRequestDTO;
import com.progra3.cafeteria_api.model.dto.ticket.Ticket;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.service.port.IOrderService;
import com.progra3.cafeteria_api.service.port.ITicketBuilderService;
import com.progra3.cafeteria_api.service.port.ITicketPdfService;
import com.progra3.cafeteria_api.service.port.ITicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    // ===========================
    // Public API
    // ===========================

    @Override
    public byte[] generatePreTicket(Long orderId) {
        try {
            Order order = orderService.getEntityById(orderId);
            Ticket ticket = ticketBuilderService.buildPreTicket(order);
            return ticketPdfService.generateTicketPdf(ticket);
        } catch (Exception e) {
            log.error("Failed to generate pre-ticket for order ID: {}", orderId, e);
            throw new FailedToPrintException("Failed to generate pre-ticket", e);
        }
    }

    @Override
    public byte[] generateFiscalTicket(Long orderId, FiscalTicketRequestDTO fiscalTicketRequestDTO) {
        try {
            Order order = orderService.getEntityById(orderId);
            String cae = generateElectronicInvoice(order, fiscalTicketRequestDTO);

            // TODO: Store CAE in order or invoice entity for future reference

            Ticket ticket = ticketBuilderService.buildFiscalTicket(order, cae);
            return ticketPdfService.generateTicketPdf(ticket);
        } catch (Exception e) {
            log.error("Failed to generate fiscal ticket for order ID: {}", orderId, e);
            throw new IllegalStateException("Failed to generate fiscal ticket", e);
        }
    }

    @Override
    public byte[] generateKitchenTicket(Order order, List<Item> items) {
        try {
            Ticket ticket = ticketBuilderService.buildKitchenTicket(order, items);
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
     * @param order       The order to invoice
     * @param fiscalTicketRequestDTO The fiscal ticket request data
     * @return The CAE (Código de Autorización Electrónico) received from AFIP
     * @throws IllegalStateException if AFIP service is unavailable or CAE is not received
     */
    private String generateElectronicInvoice(Order order, FiscalTicketRequestDTO fiscalTicketRequestDTO) {

        log.error(fiscalTicketRequestDTO.toString());
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
        FECAEResponse response = switch (fiscalTicketRequestDTO.invoiceType()) {
            case FACTURA_A -> {
                log.info("Generating Invoice A for order ID: {}", order.getId());
                yield afipInvoiceService.generateInvoice(fiscalTicketRequestDTO, totalAmount, netAmount, ivaAmount);
            }
            case FACTURA_B -> {
                log.info("Generating Invoice B for order ID: {}", order.getId());
                yield afipInvoiceService.generateInvoice(fiscalTicketRequestDTO, totalAmount, netAmount, ivaAmount);
            }
            case FACTURA_C -> {
                log.info("Generating Invoice C for order ID: {}", order.getId());
                yield afipInvoiceService.generateInvoice(fiscalTicketRequestDTO, totalAmount, null, null);
            }
            case NOTA_CREDITO_A -> null; //TODO: Implement credit note generation
            case NOTA_CREDITO_B -> null;
            case NOTA_CREDITO_C -> null;
        };

        // Extract and validate CAE from response
        String cae = extractCaeFromResponse(response);
        if (cae == null) {
            log.error("Failed to obtain CAE from AFIP for order ID: {}", order.getId());
            throw new IllegalStateException("No CAE received from AFIP");
        }

        log.info("Successfully generated electronic invoice with CAE: {} for order ID: {}", cae, order.getId());
        return cae;
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
     * Extracts CAE from AFIP response and validates approval.
     *
     * @param response The AFIP response
     * @return The CAE if approved, null otherwise
     */
    private String extractCaeFromResponse(FECAEResponse response) {
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

        // Check if approved ("A" = Aprobado)
        if (!"A".equals(detResp.getResultado())) {
            log.error("Invoice rejected by AFIP. Result: {}", detResp.getResultado());
            if (detResp.getObservaciones() != null) {
                detResp.getObservaciones().getObs().forEach(obs ->
                        log.error("AFIP Observation [{}]: {}", obs.getCode(), obs.getMsg())
                );
            }
            return null;
        }

        return detResp.getCAE();
    }
}
