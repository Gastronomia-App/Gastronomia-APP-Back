package com.progra3.cafeteria_api.afip.wsfe;

import com.progra3.cafeteria_api.afip.wsass.AfipAuthService;
import com.progra3.cafeteria_api.afip.wsass.TaParser;
import com.progra3.cafeteria_api.afip.wsfe.generated.*;
import com.progra3.cafeteria_api.security.EmployeeContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * SOAP client for AFIP WSFEv1 (Electronic Invoice) service.
 * Uses Spring WS with JAXB2 marshaller for automatic Java ↔ XML conversion.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsfeClient extends WebServiceGatewaySupport {

    private final AfipAuthService afipAuthService;
    private final EmployeeContext context;
    private final Jaxb2Marshaller marshaller;

    @Value("${afip.wsfe.url}")
    private String wsfeUrl;

    /**
     * Post constructor configuration.
     * Spring injects dependencies → Lombok builds constructor → then we configure the SOAP template.
     */
    @PostConstruct
    private void init() {
        setDefaultUri(wsfeUrl);
        setMarshaller(marshaller);
        setUnmarshaller(marshaller);
    }

    public FECompUltimoAutorizadoResponse getLastAuthorized(int ptoVta, int cbteTipo) {
        log.debug("Requesting last authorized voucher for PtoVta={}, CbteTipo={}", ptoVta, cbteTipo);

        TaParser.TicketAccess ta = afipAuthService.getValidTicket();
        FEAuthRequest auth = buildAuthRequest(ta);

        FECompUltimoAutorizado request = new FECompUltimoAutorizado();
        request.setAuth(auth);
        request.setCbteTipo(cbteTipo);
        request.setPtoVta(ptoVta);

        FECompUltimoAutorizadoResponse response =
                (FECompUltimoAutorizadoResponse) getWebServiceTemplate().marshalSendAndReceive(request);

        log.debug("Last authorized voucher response received");
        return response;
    }

    public FECAESolicitarResponse solicitarCAE(FECAESolicitar request) {
        log.debug("Requesting CAE for {} vouchers",
                request.getFeCAEReq() != null && request.getFeCAEReq().getFeCabReq() != null
                        ? request.getFeCAEReq().getFeCabReq().getCantReg()
                        : 0);

        TaParser.TicketAccess ta = afipAuthService.getValidTicket();

        if (request.getAuth() == null) {
            request.setAuth(buildAuthRequest(ta));
        }

        FECAESolicitarResponse response =
                (FECAESolicitarResponse) getWebServiceTemplate().marshalSendAndReceive(request);

        log.debug("CAE solicitation response received");
        return response;
    }

    public FEDummyResponse checkServerStatus() {
        log.debug("Checking AFIP WSFE server status");

        FEDummy request = new FEDummy();

        FEDummyResponse response =
                (FEDummyResponse) getWebServiceTemplate().marshalSendAndReceive(request);

        log.debug("Server status check completed");
        return response;
    }

    private FEAuthRequest buildAuthRequest(TaParser.TicketAccess ta) {
        FEAuthRequest auth = new FEAuthRequest();
        auth.setCuit(context.getCurrentBusiness().getCuit());
        auth.setToken(ta.token());
        auth.setSign(ta.sign());
        return auth;
    }
}
