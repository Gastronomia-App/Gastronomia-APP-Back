package com.progra3.cafeteria_api.afip.wsfe;

import com.progra3.cafeteria_api.afip.wsfe.generated.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service for AFIP electronic invoicing using WSFEv1.
 * Uses JAXB2-generated classes for type-safe SOAP communication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AfipInvoiceService {

    private final WsfeClient wsfeClient;

    @Value("${afip.punto-venta:1}")
    private Integer puntoVenta;

    /**
     * Generates an electronic Invoice B (for monotributistas/consumidores finales).
     *
     * @param customerDocNumber Customer's document number (CUIT/DNI)
     * @param totalAmount Total amount including VAT
     * @param netAmount Net amount (taxable base)
     * @param ivaAmount VAT amount
     * @return AFIP response with CAE and authorization details
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
     * Generates an electronic Invoice C (for monotributistas).
     *
     * @param customerDocNumber Customer's document number
     * @param totalAmount Total amount (no VAT discrimination)
     * @return AFIP response with CAE and authorization details
     */
    public FECAEResponse generateInvoiceC(
            Long customerDocNumber,
            BigDecimal totalAmount
    ) {
        // Invoice C does not discriminate VAT
        return generateInvoice(11, customerDocNumber, totalAmount, totalAmount, BigDecimal.ZERO);
    }

    /**
     * Generates an electronic invoice.
     *
     * @param cbteTipo Voucher type (6=Invoice B, 11=Invoice C)
     * @param customerDocNumber Customer's document number
     * @param totalAmount Total amount
     * @param netAmount Net amount (taxable base)
     * @param ivaAmount VAT amount
     * @return AFIP response with CAE and authorization details
     */
    private FECAEResponse generateInvoice(
            Integer cbteTipo,
            Long customerDocNumber,
            BigDecimal totalAmount,
            BigDecimal netAmount,
            BigDecimal ivaAmount
    ) {
        try {
            // Get last authorized voucher number
            Long lastVoucher = getLastAuthorizedVoucher(puntoVenta, cbteTipo);
            Long nextVoucher = lastVoucher + 1;

            log.info("Generating invoice type {} number {}", cbteTipo, nextVoucher);

            // Build request using JAXB2-generated classes
            FECAESolicitar request = buildInvoiceRequest(
                    cbteTipo,
                    customerDocNumber,
                    nextVoucher,
                    totalAmount,
                    netAmount,
                    ivaAmount
            );

            // Call AFIP WSFEv1
            FECAESolicitarResponse response = wsfeClient.solicitarCAE(request);

            // Extract result
            FECAEResponse result = response.getFECAESolicitarResult();

            if (result != null && result.getFeDetResp() != null
                    && !result.getFeDetResp().getFECAEDetResponse().isEmpty()) {
                FECAEDetResponse detResp = result.getFeDetResp().getFECAEDetResponse().get(0);
                if ("A".equals(detResp.getResultado())) {
                    log.info("Invoice authorized. CAE: {}, Expiration: {}",
                            detResp.getCAE(), detResp.getCAEFchVto());
                } else {
                    log.error("Invoice rejected. Result: {}", detResp.getResultado());
                    if (detResp.getObservaciones() != null) {
                        detResp.getObservaciones().getObs().forEach(obs ->
                                log.error("Observation {}: {}", obs.getCode(), obs.getMsg())
                        );
                    }
                }
            }

            return result;

        } catch (Exception e) {
            log.error("Error generating invoice", e);
            throw new IllegalStateException("Failed to generate AFIP invoice", e);
        }
    }

    /**
     * Checks AFIP WSFE server status.
     *
     * @return true if server is available, false otherwise
     */
    public boolean checkAfipStatus() {
        try {
            FEDummyResponse response = wsfeClient.checkServerStatus();
            return response != null && response.getFEDummyResult() != null;
        } catch (Exception e) {
            log.error("Error checking AFIP status", e);
            return false;
        }
    }

    /**
     * Gets the next available voucher number for a given voucher type.
     *
     * @param cbteTipo Voucher type
     * @return Next voucher number
     */
    public Long getNextVoucherNumber(Integer cbteTipo) {
        Long last = getLastAuthorizedVoucher(puntoVenta, cbteTipo);
        return last + 1;
    }

    /**
     * Gets the last authorized voucher number from AFIP.
     */
    private Long getLastAuthorizedVoucher(Integer ptoVta, Integer cbteTipo) {
        FECompUltimoAutorizadoResponse response = wsfeClient.getLastAuthorized(ptoVta, cbteTipo);

        if (response != null && response.getFECompUltimoAutorizadoResult() != null) {
            Integer cbteNro = response.getFECompUltimoAutorizadoResult().getCbteNro();
            return cbteNro.longValue();
        }

        log.warn("Could not retrieve last authorized voucher, returning 0");
        return 0L;
    }

    /**
     * Builds a complete FECAESolicitar request using JAXB2-generated classes.
     */
    private FECAESolicitar buildInvoiceRequest(
            Integer cbteTipo,
            Long customerDocNumber,
            Long voucherNumber,
            BigDecimal totalAmount,
            BigDecimal netAmount,
            BigDecimal ivaAmount
    ) {
        // Build header (FECAECabRequest)
        FECAECabRequest cabReq = new FECAECabRequest();
        cabReq.setCantReg(1);
        cabReq.setPtoVta(puntoVenta);
        cabReq.setCbteTipo(cbteTipo);

        // Build detail (FECAEDetRequest)
        FECAEDetRequest detReq = new FECAEDetRequest();
        detReq.setConcepto(1); // 1=Products, 2=Services, 3=Both
        detReq.setDocTipo(80); // 80=CUIT, 96=DNI
        detReq.setDocNro(customerDocNumber);
        detReq.setCbteDesde(voucherNumber);
        detReq.setCbteHasta(voucherNumber);
        detReq.setCbteFch(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        detReq.setImpTotal(totalAmount.doubleValue());
        detReq.setImpTotConc(0.0); // Non-taxable amount
        detReq.setImpNeto(netAmount.doubleValue());
        detReq.setImpOpEx(0.0); // Exempt amount
        detReq.setImpTrib(0.0); // Other taxes
        detReq.setImpIVA(ivaAmount.doubleValue());
        detReq.setMonId("PES"); // Argentine Pesos
        detReq.setMonCotiz(1.0); // Exchange rate

        // Add VAT aliquots if needed (for Invoice B)
        if (ivaAmount.compareTo(BigDecimal.ZERO) > 0) {
            AlicIva alicIva = new AlicIva();
            alicIva.setId(5); // 5 = 21% VAT
            alicIva.setBaseImp(netAmount.doubleValue());
            alicIva.setImporte(ivaAmount.doubleValue());

            ArrayOfAlicIva arrayIva = new ArrayOfAlicIva();
            arrayIva.getAlicIva().add(alicIva);
            detReq.setIva(arrayIva);
        }

        // Wrap detail in array
        ArrayOfFECAEDetRequest arrayDet = new ArrayOfFECAEDetRequest();
        arrayDet.getFECAEDetRequest().add(detReq);

        // Build complete request (FECAERequest)
        FECAERequest fecaeReq = new FECAERequest();
        fecaeReq.setFeCabReq(cabReq);
        fecaeReq.setFeDetReq(arrayDet);

        // Build SOAP request wrapper
        FECAESolicitar solicitar = new FECAESolicitar();
        solicitar.setFeCAEReq(fecaeReq);

        return solicitar;
    }
}

