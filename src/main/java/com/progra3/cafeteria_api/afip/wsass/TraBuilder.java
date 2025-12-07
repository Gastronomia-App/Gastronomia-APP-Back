package com.progra3.cafeteria_api.afip.wsass;

import com.progra3.cafeteria_api.config.AfipConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Builds TRA (Ticket Request Authentication) XML for AFIP WSAA.
 * The TRA must be signed with PKCS#7 before sending to AFIP.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TraBuilder {

    private final AfipConfig properties;
    private final Clock clock;

    // AFIP expects ISO-8601 format with timezone: 2025-12-06T14:30:00-03:00
    private static final ZoneId AFIP_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter AFIP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * Builds the TRA XML with current timestamp and configured service.
     *
     * @return TRA XML string ready to be signed
     */
    public String buildTraXml() {
        long uniqueId = System.currentTimeMillis() / 1000L;

        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(AFIP_ZONE);
        ZonedDateTime generationTime = now.minusMinutes(2);
        ZonedDateTime expirationTime = generationTime.plusMinutes(properties.getTicketTimeoutMinutes());

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <loginTicketRequest version="1.0">
                  <header>
                    <uniqueId>%d</uniqueId>
                    <generationTime>%s</generationTime>
                    <expirationTime>%s</expirationTime>
                  </header>
                  <service>%s</service>
                </loginTicketRequest>
                """.formatted(
                uniqueId,
                generationTime.format(AFIP_DATE_FORMAT),
                expirationTime.format(AFIP_DATE_FORMAT),
                properties.getService()
        );
    }
}

