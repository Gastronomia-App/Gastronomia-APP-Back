package com.progra3.cafeteria_api.afip.wsfe;

import com.progra3.cafeteria_api.afip.wsass.AfipAuthService;
import com.progra3.cafeteria_api.afip.wsass.TaParser;
import com.progra3.cafeteria_api.afip.wsfe.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public  class AfipWsfeClient {

    private final AfipAuthService authService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${afip.wsfe.url}")
    private String wsfeUrl;

    @Value("${afip.cuit}")
    private Long cuit;

    /**
     * Obtiene el último número de comprobante autorizado
     */
    public Long getLastAuthorizedVoucher(Integer ptoVta, Integer cbteTipo) {
        TaParser.TicketAccess ticket = authService.getValidTicket();

        String soapRequest = buildLastVoucherSoapRequest(ticket, ptoVta, cbteTipo);
        String response = callSoapService(soapRequest);

        return parseLastVoucherResponse(response);
    }

    /**
     * Solicita CAE (Código de Autorización Electrónico) para un comprobante
     */
    public FECAEResponse requestCAE(FECAERequest request) {
        TaParser.TicketAccess ticket = authService.getValidTicket();

        String soapRequest = buildCAERequestSoap(ticket, request);
        String response = callSoapService(soapRequest);

        return parseCAEResponse(response);
    }

    /**
     * Consulta el estado del servidor de AFIP
     */
    public boolean checkServerStatus() {
        try {
            String soapRequest = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                      xmlns:wsfe="http://ar.gov.afip.dif.FEV1/">
                      <soapenv:Header/>
                      <soapenv:Body>
                        <wsfe:FEDummy/>
                      </soapenv:Body>
                    </soapenv:Envelope>
                    """;

            String response = callSoapService(soapRequest);
            return response.contains("OK");
        } catch (Exception e) {
            log.error("Error checking AFIP server status", e);
            return false;
        }
    }

    private String buildLastVoucherSoapRequest(TaParser.TicketAccess ticket, Integer ptoVta, Integer cbteTipo) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:wsfe="http://ar.gov.afip.dif.FEV1/">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <wsfe:FECompUltimoAutorizado>
                      <wsfe:Auth>
                        <wsfe:Token>%s</wsfe:Token>
                        <wsfe:Sign>%s</wsfe:Sign>
                        <wsfe:Cuit>%d</wsfe:Cuit>
                      </wsfe:Auth>
                      <wsfe:PtoVta>%d</wsfe:PtoVta>
                      <wsfe:CbteTipo>%d</wsfe:CbteTipo>
                    </wsfe:FECompUltimoAutorizado>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(ticket.token(), ticket.sign(), cuit, ptoVta, cbteTipo);
    }

    private String buildCAERequestSoap(TaParser.TicketAccess ticket, FECAERequest request) {
        StringBuilder detReqs = new StringBuilder();

        for (FECAEDetRequest det : request.feDetReq()) {
            detReqs.append("""
                    <wsfe:FECAEDetRequest>
                      <wsfe:Concepto>%d</wsfe:Concepto>
                      <wsfe:DocTipo>%d</wsfe:DocTipo>
                      <wsfe:DocNro>%d</wsfe:DocNro>
                      <wsfe:CbteDesde>%d</wsfe:CbteDesde>
                      <wsfe:CbteHasta>%d</wsfe:CbteHasta>
                      <wsfe:CbteFch>%s</wsfe:CbteFch>
                      <wsfe:ImpTotal>%.2f</wsfe:ImpTotal>
                      <wsfe:ImpTotConc>%.2f</wsfe:ImpTotConc>
                      <wsfe:ImpNeto>%.2f</wsfe:ImpNeto>
                      <wsfe:ImpOpEx>%.2f</wsfe:ImpOpEx>
                      <wsfe:ImpTrib>%.2f</wsfe:ImpTrib>
                      <wsfe:ImpIVA>%.2f</wsfe:ImpIVA>
                      <wsfe:MonId>%s</wsfe:MonId>
                      <wsfe:MonCotiz>%.2f</wsfe:MonCotiz>
                    </wsfe:FECAEDetRequest>
                    """.formatted(
                    det.concepto(), det.docTipo(), det.docNro(),
                    det.cbteDesde(), det.cbteHasta(), det.cbteFch(),
                    det.impTotal(), det.impTotConc(), det.impNeto(),
                    det.impOpEx(), det.impTrib(), det.impIVA(),
                    det.monId(), det.monCotiz()
            ));
        }

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:wsfe="http://ar.gov.afip.dif.FEV1/">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <wsfe:FECAESolicitar>
                      <wsfe:Auth>
                        <wsfe:Token>%s</wsfe:Token>
                        <wsfe:Sign>%s</wsfe:Sign>
                        <wsfe:Cuit>%d</wsfe:Cuit>
                      </wsfe:Auth>
                      <wsfe:FeCAEReq>
                        <wsfe:FeCabReq>
                          <wsfe:CantReg>%d</wsfe:CantReg>
                          <wsfe:PtoVta>%d</wsfe:PtoVta>
                          <wsfe:CbteTipo>%d</wsfe:CbteTipo>
                        </wsfe:FeCabReq>
                        <wsfe:FeDetReq>
                          %s
                        </wsfe:FeDetReq>
                      </wsfe:FeCAEReq>
                    </wsfe:FECAESolicitar>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(
                ticket.token(), ticket.sign(), cuit,
                request.feCabReq().cantReg(),
                request.feCabReq().ptoVta(),
                request.feCabReq().cbteTipo(),
                detReqs.toString()
        );
    }

    private String callSoapService(String soapRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.set("SOAPAction", "");

        HttpEntity<String> requestEntity = new HttpEntity<>(soapRequest, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    wsfeUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling AFIP WSFEv1", e);
            throw new IllegalStateException("Failed to call AFIP WSFEv1", e);
        }
    }

    private Long parseLastVoucherResponse(String soapResponse) {
        try {
            // Extract CbteNro from response
            int start = soapResponse.indexOf("<CbteNro>");
            int end = soapResponse.indexOf("</CbteNro>");

            if (start == -1 || end == -1) {
                log.warn("CbteNro not found in response, returning 0");
                return 0L;
            }

            String cbteNro = soapResponse.substring(start + 9, end);
            return Long.parseLong(cbteNro);
        } catch (Exception e) {
            log.error("Error parsing last voucher response", e);
            throw new IllegalStateException("Error parsing AFIP response", e);
        }
    }

    private FECAEResponse parseCAEResponse(String soapResponse) {
        try {
            // This is a simplified parser. In production, you should use JAXB or a proper XML parser

            // Parse CAE
            String cae = extractXmlValue(soapResponse, "CAE");
            String caeFchVto = extractXmlValue(soapResponse, "CAEFchVto");
            String resultado = extractXmlValue(soapResponse, "Resultado");

            FECAEDetResponse detResponse = FECAEDetResponse.builder()
                    .cae(cae)
                    .caeFchVto(caeFchVto)
                    .resultado(resultado)
                    .build();

            return FECAEResponse.builder()
                    .feDetResp(java.util.List.of(detResponse))
                    .build();
        } catch (Exception e) {
            log.error("Error parsing CAE response", e);
            throw new IllegalStateException("Error parsing AFIP CAE response", e);
        }
    }

    private String extractXmlValue(String xml, String tagName) {
        int start = xml.indexOf("<" + tagName + ">");
        int end = xml.indexOf("</" + tagName + ">");

        if (start == -1 || end == -1) {
            return null;
        }

        return xml.substring(start + tagName.length() + 2, end);
    }
}
