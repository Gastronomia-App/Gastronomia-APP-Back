package com.progra3.cafeteria_api.service.impl.tickets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.progra3.cafeteria_api.model.dto.ticket.Ticket;
import com.progra3.cafeteria_api.model.dto.ticket.TicketContext;
import com.progra3.cafeteria_api.model.dto.ticket.TicketHeader;
import com.progra3.cafeteria_api.model.dto.ticket.TicketTotals;
import com.progra3.cafeteria_api.model.entity.Business;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.enums.IvaCondition;
import com.progra3.cafeteria_api.model.enums.TicketType;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.tickets.TicketBuilderStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FiscalTicketBuilder implements TicketBuilderStrategy {

    private final EmployeeContext context;
    private final TicketBuilderHelper helper;
    private final ObjectMapper objectMapper;

    @Value("${afip.punto-venta:1}")
    private Integer puntoVenta;

    private static final String AFIP_QR_URL = "https://www.afip.gob.ar/fe/qr/?p=";

    @Override
    public Ticket build(Order order, TicketContext ticketContext) {
        return Ticket.builder()
                .type(TicketType.FISCAL_TICKET)
                .header(buildHeader(order, ticketContext))
                .items(helper.buildTicketItems(order))
                .totals(buildTotals(order, ticketContext))
                .build();
    }

    private TicketHeader buildHeader(Order order, TicketContext ticketContext) {
        Business business = context.getCurrentBusiness();

        String invoiceTitle = ticketContext.invoiceType() != null
                ? ticketContext.invoiceType().name().replace("_", " ")
                : "FACTURA";

        String customerIvaConditionStr = ticketContext.customerIvaCondition() != null
                ? ticketContext.customerIvaCondition().name().replace("_", " ")
                : "CONSUMIDOR FINAL";

        return TicketHeader.builder()
                // Business fiscal data
                .businessName(business.getName())
                .businessAddress(business.getAddress() != null ? business.getAddress().getStreet() : null)
                .businessCuit(business.getCuit())
                .businessIvaCondition(IvaCondition.RESPONSABLE_INSCRIPTO.name().replace("_", " "))
                .businessIibb(business.getIibb())
                .businessActivityStart(business.getActivityStartDate())
                // Invoice data
                .invoiceCode(ticketContext.invoiceType() != null ? ticketContext.invoiceType().getInvoiceCode() : null)
                .puntoVenta(ticketContext.puntoVenta() != null ? ticketContext.puntoVenta() : puntoVenta)
                .cbteNumero(ticketContext.cbteNumero())
                .concept(ticketContext.concept() != null ? ticketContext.concept() : "Productos")
                .customerIvaCondition(customerIvaConditionStr)
                // Order data
                .dateTime(order.getEndDateTime() != null ? order.getEndDateTime() : order.getStartDateTime())
                .title(invoiceTitle)
                .build();
    }

    private TicketTotals buildTotals(Order order, TicketContext ticketContext) {
        Double subtotal = helper.defaultZero(order.getSubtotal());
        Integer discountPercent = order.getDiscount() != null ? order.getDiscount() : 0;
        double discountAmount = helper.roundToTwoDecimals(subtotal * discountPercent / 100.0);
        Double total = helper.defaultZero(order.getTotal());

        String qrData = generateQrData(order, ticketContext);

        return TicketTotals.builder()
                .subtotal(subtotal)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .total(total)
                .printInvoiceWarning(false)
                .cae(ticketContext.cae())
                .caeExpiration(ticketContext.caeExpiration())
                .qrData(qrData)
                .build();
    }

    /**
     * Generates the QR data URL according to AFIP specifications (RG 4291).
     * The QR contains a Base64-encoded JSON with invoice details.
     */
    private String generateQrData(Order order, TicketContext ticketContext) {
        if (ticketContext.cae() == null || ticketContext.invoiceType() == null) {
            return null;
        }

        try {
            Business business = context.getCurrentBusiness();

            Map<String, Object> qrJson = new LinkedHashMap<>();
            qrJson.put("ver", 1);
            qrJson.put("fecha", order.getEndDateTime() != null
                    ? order.getEndDateTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    : (order.getStartDateTime() != null
                        ? order.getStartDateTime().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)));
            qrJson.put("cuit", business.getCuit());
            qrJson.put("ptoVta", ticketContext.puntoVenta() != null ? ticketContext.puntoVenta() : puntoVenta);
            qrJson.put("tipoCmp", ticketContext.invoiceType().getInvoiceCode());
            qrJson.put("nroCmp", ticketContext.cbteNumero());
            qrJson.put("importe", helper.defaultZero(order.getTotal()));
            qrJson.put("moneda", "PES");
            qrJson.put("ctz", 1);

            // Document type and number
            int tipoDocRec = ticketContext.documentType() != null
                    ? ticketContext.documentType().getCode()
                    : 99; // 99 = Consumer without ID
            qrJson.put("tipoDocRec", tipoDocRec);
            qrJson.put("nroDocRec", ticketContext.documentNumber() != null ? ticketContext.documentNumber() : 0);

            // Invoice type code (Letter: A=1, B=6, C=11)
            qrJson.put("tipoCodAut", "E"); // E = CAE
            qrJson.put("codAut", Long.parseLong(ticketContext.cae()));

            String jsonString = objectMapper.writeValueAsString(qrJson);
            String base64Encoded = Base64.getEncoder().encodeToString(jsonString.getBytes(StandardCharsets.UTF_8));

            return AFIP_QR_URL + base64Encoded;

        } catch (Exception e) {
            log.error("Error generating QR data for fiscal ticket", e);
            return null;
        }
    }
}
