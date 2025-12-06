package com.progra3.cafeteria_api.controller;

import com.progra3.cafeteria_api.afip.wsass.AfipAuthService;
import com.progra3.cafeteria_api.afip.wsass.TaParser;
import com.progra3.cafeteria_api.afip.wsfe.AfipInvoiceService;
import com.progra3.cafeteria_api.afip.wsfe.dto.FECAEResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controller for AFIP integration testing and operations
 */
@RestController
@RequestMapping("/api/afip")
@RequiredArgsConstructor
@Slf4j
public class AfipController {

    private final AfipAuthService authService;
    private final AfipInvoiceService invoiceService;

    /**
     * Test AFIP authentication
     */
    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuthentication() {
        try {
            TaParser.TicketAccess ticket = authService.authenticate();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Authentication successful",
                "token", ticket.token().substring(0, 50) + "...",
                "expirationTime", ticket.expirationTime().toString()
            ));
        } catch (Exception e) {
            log.error("Authentication test failed", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Authentication failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Check AFIP server status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> checkStatus() {
        try {
            boolean status = invoiceService.checkAfipStatus();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "afipServerOnline", status
            ));
        } catch (Exception e) {
            log.error("Status check failed", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Status check failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Get next voucher number for a given voucher type
     */
    @GetMapping("/next-voucher/{cbteTipo}")
    public ResponseEntity<Map<String, Object>> getNextVoucher(@PathVariable Integer cbteTipo) {
        try {
            Long nextVoucher = invoiceService.getNextVoucherNumber(cbteTipo);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "cbteTipo", cbteTipo,
                "nextVoucherNumber", nextVoucher
            ));
        } catch (Exception e) {
            log.error("Failed to get next voucher", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to get next voucher: " + e.getMessage()
            ));
        }
    }

    /**
     * Test invoice generation - Factura B
     */
    @PostMapping("/test-invoice-b")
    public ResponseEntity<Map<String, Object>> testInvoiceB(
            @RequestBody TestInvoiceRequest request
    ) {
        try {
            FECAEResponse response = invoiceService.generateInvoiceB(
                request.customerDoc(),
                request.totalAmount(),
                request.netAmount(),
                request.ivaAmount()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invoice B generated successfully",
                "response", response
            ));
        } catch (Exception e) {
            log.error("Failed to generate invoice B", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to generate invoice: " + e.getMessage()
            ));
        }
    }

    /**
     * Test invoice generation - Factura C
     */
    @PostMapping("/test-invoice-c")
    public ResponseEntity<Map<String, Object>> testInvoiceC(
            @RequestBody TestInvoiceRequest request
    ) {
        try {
            FECAEResponse response = invoiceService.generateInvoiceC(
                request.customerDoc(),
                request.totalAmount()
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Invoice C generated successfully",
                "response", response
            ));
        } catch (Exception e) {
            log.error("Failed to generate invoice C", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to generate invoice: " + e.getMessage()
            ));
        }
    }

    /**
     * Invalidate cached authentication ticket
     */
    @PostMapping("/invalidate-cache")
    public ResponseEntity<Map<String, String>> invalidateCache() {
        authService.invalidateCache();
        return ResponseEntity.ok(Map.of(
            "success", "true",
            "message", "Authentication cache invalidated"
        ));
    }

    // DTO for test requests
    public record TestInvoiceRequest(
        Long customerDoc,
        BigDecimal totalAmount,
        BigDecimal netAmount,
        BigDecimal ivaAmount
    ) {}
}

