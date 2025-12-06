package com.progra3.cafeteria_api.afip.wsass;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TaParser {

    public record TicketAccess(
        String token,
        String sign,
        LocalDateTime expirationTime
    ) {
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expirationTime);
        }
    }

    public TicketAccess parse(String loginTicketResponseXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(loginTicketResponseXml.getBytes()));

            String token = getTextContent(doc, "token");
            String sign = getTextContent(doc, "sign");
            String expirationStr = getTextContent(doc, "expirationTime");

            LocalDateTime expirationTime = LocalDateTime.parse(
                    expirationStr,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
            );

            return new TicketAccess(token, sign, expirationTime);

        } catch (Exception e) {
            throw new IllegalStateException("Error parsing AFIP LoginTicketResponse", e);
        }
    }

    private String getTextContent(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        throw new IllegalStateException("Tag not found in XML: " + tagName);
    }
}
