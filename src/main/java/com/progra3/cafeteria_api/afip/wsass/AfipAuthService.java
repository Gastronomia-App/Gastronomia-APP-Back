package com.progra3.cafeteria_api.afip.wsass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public  class AfipAuthService {

    private final TraBuilder traBuilder;
    private final TraSigner traSigner;
    private final WsassClient wsassClient;
    private final TaParser taParser;

    private TaParser.TicketAccess cachedTicket;

    /**
     * Gets a valid AFIP access ticket, either from cache or by requesting a new one
     */
    public TaParser.TicketAccess getValidTicket() {
        if (cachedTicket != null && !cachedTicket.isExpired()) {
            log.debug("Using cached AFIP ticket");
            return cachedTicket;
        }

        log.info("Requesting new AFIP authentication ticket");
        cachedTicket = authenticate();
        return cachedTicket;
    }

    /**
     * Forces a new authentication request to AFIP
     */
    public TaParser.TicketAccess authenticate() {
        try {
            // 1. Build TRA (Ticket Request Authentication) XML
            String traXml = traBuilder.buildTraXml();
            log.debug("Built TRA XML");

            // 2. Sign TRA with PKCS#12 certificate
            byte[] signedTra = traSigner.signTra(traXml);
            log.debug("Signed TRA with certificate");

            // 3. Send signed TRA to WSAA
            String loginTicketResponse = wsassClient.loginCms(signedTra);
            log.debug("Received LoginTicketResponse from AFIP");

            // 4. Parse response to extract token, sign, and expiration
            TaParser.TicketAccess ticket = taParser.parse(loginTicketResponse);
            log.info("Successfully authenticated with AFIP. Token expires at: {}", ticket.expirationTime());

            return ticket;

        } catch (Exception e) {
            log.error("Error authenticating with AFIP", e);
            throw new IllegalStateException("Failed to authenticate with AFIP WSAA", e);
        }
    }

    /**
     * Clears the cached ticket, forcing a new authentication on next request
     */
    public void invalidateCache() {
        log.info("Invalidating AFIP ticket cache");
        cachedTicket = null;
    }
}
