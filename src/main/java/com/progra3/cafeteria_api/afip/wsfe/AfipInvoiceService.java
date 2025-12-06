package com.progra3.cafeteria_api.afip.wsfe;

import com.progra3.cafeteria_api.afip.wsfe.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service for AFIP electronic invoicing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AfipInvoiceService {

    private final AfipWsfeClient wsfeClient;

    @Value("${afip.punto-venta:1}")
    private Integer puntoVenta;

    /**
     * Genera una factura electrónica tipo B
     */
    public FECAEResponse generateInvoiceB(
            Long customerDocNumber,
            BigDecimal totalAmount,
            BigDecimal netAmount,
            BigDecimal ivaAmount
    ) {
        return generateInvoice(6, customerDocNumber, totalAmount, netAmount, ivaAmount);
    }

    /**
     * Genera una factura electrónica tipo C
     */
    public FECAEResponse generateInvoiceC(
            Long customerDocNumber,
            BigDecimal totalAmount
    ) {
        // Factura C no discrimina IVA
        return generateInvoice(11, customerDocNumber, totalAmount, totalAmount, BigDecimal.ZERO);
    }

    /**
     * Genera una factura electrónica
     * @param cbteTipo Tipo de comprobante (6=Factura B, 11=Factura C)
     */
    private FECAEResponse generateInvoice(
            Integer cbteTipo,
            Long customerDocNumber,
            BigDecimal totalAmount,
            BigDecimal netAmount,
            BigDecimal ivaAmount
    ) {
        try {
            // Obtener el último número de comprobante
            Long lastVoucher = wsfeClient.getLastAuthorizedVoucher(puntoVenta, cbteTipo);
            Long nextVoucher = lastVoucher + 1;

            log.info("Generating invoice type {} number {}", cbteTipo, nextVoucher);

            // Crear la solicitud
            FECAECabRequest cabReq = FECAECabRequest.builder()
                    .cantReg(1)
                    .ptoVta(puntoVenta)
                    .cbteTipo(cbteTipo)
                    .build();

            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            FECAEDetRequest detReq = FECAEDetRequest.builder()
                    .concepto(1) // 1=Productos
                    .docTipo(80) // 80=CUIT, 96=DNI
                    .docNro(customerDocNumber)
                    .cbteDesde(nextVoucher)
                    .cbteHasta(nextVoucher)
                    .cbteFch(currentDate)
                    .impTotal(totalAmount)
                    .impTotConc(BigDecimal.ZERO) // Importe no gravado
                    .impNeto(netAmount)
                    .impOpEx(BigDecimal.ZERO) // Importe exento
                    .impTrib(BigDecimal.ZERO) // Tributos
                    .impIVA(ivaAmount)
                    .monId("PES") // Pesos argentinos
                    .monCotiz(BigDecimal.ONE)
                    .build();

            FECAERequest request = FECAERequest.builder()
                    .feCabReq(cabReq)
                    .feDetReq(java.util.List.of(detReq))
                    .build();

            FECAEResponse response = wsfeClient.requestCAE(request);

            if (response.feDetResp() != null && !response.feDetResp().isEmpty()) {
                FECAEDetResponse detResp = response.feDetResp().getFirst();
                if ("A".equals(detResp.resultado())) {
                    log.info("Invoice authorized. CAE: {}, Expiration: {}",
                            detResp.cae(), detResp.caeFchVto());
                } else {
                    log.error("Invoice rejected. Observations: {}", detResp.observaciones());
                }
            }

            return response;

        } catch (Exception e) {
            log.error("Error generating invoice", e);
            throw new IllegalStateException("Failed to generate AFIP invoice", e);
        }
    }

    /**
     * Verifica el estado del servidor de AFIP
     */
    public boolean checkAfipStatus() {
        return wsfeClient.checkServerStatus();
    }

    /**
     * Obtiene el próximo número de comprobante disponible
     */
    public Long getNextVoucherNumber(Integer cbteTipo) {
        Long last = wsfeClient.getLastAuthorizedVoucher(puntoVenta, cbteTipo);
        return last + 1;
    }
}

