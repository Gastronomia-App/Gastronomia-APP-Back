package com.progra3.cafeteria_api.afip.wsfe;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class WsfeConfig {

/**
 * Configures JAXB2 marshaller for AFIP WSFE service.
 * Uses JAXB2-generated classes from the WSDL.
 */
    @Bean
    public Jaxb2Marshaller wsfeMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // Package where maven-jaxb2-plugin generated the classes
        marshaller.setContextPath("com.progra3.cafeteria_api.afip.wsfe.generated");
        return marshaller;
    }
}