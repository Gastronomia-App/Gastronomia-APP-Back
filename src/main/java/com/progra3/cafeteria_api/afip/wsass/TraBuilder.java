package com.progra3.cafeteria_api.afip.wsass;

import com.progra3.cafeteria_api.config.AfipConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TraBuilder {

    private final AfipConfig properties;
    private final Clock clock;

    public String buildTraXml() {
        long uniqueId = System.currentTimeMillis() / 1000L;

        LocalDateTime generationTime = LocalDateTime.now(clock);
        LocalDateTime expirationTime = generationTime.plusMinutes(properties.getTicketTimeoutMinutes());

        return """
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
                generationTime,
                expirationTime,
                properties.getService()
        );
    }
}

