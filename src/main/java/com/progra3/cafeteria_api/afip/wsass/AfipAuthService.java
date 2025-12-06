package com.progra3.cafeteria_api.afip.wsass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Service for managing AFIP WSAA authentication.
 * Implements thread-safe caching of TicketAccess to avoid unnecessary calls to AFIP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AfipAuthService {

    private final TraBuilder traBuilder;
    private final TraSigner traSigner;
    private final WsassClient wsassClient;
    private final TaParser taParser;

    // Thread-safe cache using synchronized access
    private volatile TaParser.TicketAccess cachedTicket;
    private final Object cacheLock = new Object();

    /**
     * Gets a valid AFIP access ticket, either from cache or by requesting a new one.
     * This method is thread-safe and will reuse cached tickets when possible.
     *
     * @return A valid TicketAccess with token, sign, and expiration time
     * @throws IllegalStateException if authentication with AFIP fails
     */
    public TaParser.TicketAccess getValidTicket() {
        // Fast path: check without locking if cache is valid (volatile read)
        TaParser.TicketAccess current = cachedTicket;
        if (current != null && !current.isExpired()) {
            Duration timeUntilExpiry = Duration.between(ZonedDateTime.now(), current.expirationTime());
            log.debug("Reusing cached AFIP ticket. Expires in {} minutes", timeUntilExpiry.toMinutes());
            return current;
        }

        // Slow path: acquire lock and re-check (double-checked locking pattern)
        synchronized (cacheLock) {
            // Re-check after acquiring lock (another thread might have refreshed it)
            current = cachedTicket;
            if (current != null && !current.isExpired()) {
                Duration timeUntilExpiry = Duration.between(ZonedDateTime.now(), current.expirationTime());
                log.debug("Another thread refreshed the ticket. Expires in {} minutes", timeUntilExpiry.toMinutes());
                return current;
            }

            // Cache is invalid or expired, request new ticket
            if (current == null) {
                log.info("No cached AFIP ticket found. Requesting new authentication...");
            } else {
                log.info("Cached AFIP ticket expired at {}. Requesting new authentication...", current.expirationTime());
            }

            TaParser.TicketAccess newTicket = authenticate();
            cachedTicket = newTicket;

            Duration validity = Duration.between(ZonedDateTime.now(), newTicket.expirationTime());
            log.info("New AFIP ticket obtained. Valid for {} hours ({} minutes)",
                    validity.toHours(), validity.toMinutes());

            return newTicket;
        }
    }

    /**
     * Forces a new authentication request to AFIP WSAA.
     * This method performs the complete authentication flow:
     * 1. Build TRA (Ticket Request Authentication) XML
     * 2. Sign TRA with PKCS#12 certificate
     * 3. Send signed TRA to WSAA
     * 4. Parse response to extract token, sign, and expiration
     *
     * @return A new TicketAccess from AFIP
     * @throws IllegalStateException if any step of the authentication fails
     */
    private TaParser.TicketAccess authenticate() {
        try {
            log.debug("Starting AFIP authentication flow");

            // 1. Build TRA (Ticket Request Authentication) XML
            String traXml = traBuilder.buildTraXml();
            log.debug("Built TRA XML for service authentication");

            // 2. Sign TRA with PKCS#12 certificate
            byte[] signedTra = traSigner.signTra(traXml);
            log.debug("Signed TRA with certificate. CMS size: {} bytes", signedTra.length);

            // 3. Send signed TRA to WSAA
            String soapResponse = wsassClient.loginCms(signedTra);
            log.debug("Received SOAP response from AFIP WSAA");

            // 4. Parse response to extract token, sign, and expiration
            TaParser.TicketAccess ticket = taParser.parse(soapResponse);
            log.info("Successfully authenticated with AFIP. Token expires at: {}", ticket.expirationTime());

            return ticket;

        } catch (IllegalStateException e) {
            // Re-throw validation errors from TraBuilder, TraSigner, WsassClient or TaParser
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during AFIP authentication", e);
            throw new IllegalStateException("Failed to authenticate with AFIP WSAA: " + e.getMessage(), e);
        }
    }

    /**
     * Clears the cached ticket, forcing a new authentication on next request.
     * This is useful for testing or when you need to force token refresh.
     * This method is thread-safe.
     */
    public void invalidateCache() {
        synchronized (cacheLock) {
            if (cachedTicket != null) {
                log.info("Invalidating cached AFIP ticket (was valid until: {})", cachedTicket.expirationTime());
                cachedTicket = null;
            } else {
                log.debug("Cache invalidation requested but no ticket was cached");
            }
        }
    }

    /**
     * Checks if there is a valid cached ticket available.
     * This method is useful for monitoring or health checks.
     *
     * @return true if a valid (non-expired) ticket is cached, false otherwise
     */
    public boolean hasValidCachedTicket() {
        TaParser.TicketAccess current = cachedTicket;
        return current != null && !current.isExpired();
    }

    /**
     * Gets information about the current cached ticket for monitoring purposes.
     *
     * @return String with cache status information, or "No ticket cached" if empty
     */
    public String getCacheStatus() {
        TaParser.TicketAccess current = cachedTicket;
        if (current == null) {
            return "No ticket cached";
        }

        if (current.isExpired()) {
            return String.format("Ticket expired at %s", current.expirationTime());
        }

        Duration timeUntilExpiry = Duration.between(ZonedDateTime.now(), current.expirationTime());
        return String.format("Valid ticket. Expires in %d hours %d minutes",
                timeUntilExpiry.toHours(),
                timeUntilExpiry.toMinutesPart());
    }
}
