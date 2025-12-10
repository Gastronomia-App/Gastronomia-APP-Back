package com.progra3.cafeteria_api.afip.wsass;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Parses AFIP WSAA SOAP responses to extract the LoginTicketResponse (TA).
 * Handles XML unescaping and date parsing according to AFIP specifications.
 */
@Component
@Slf4j
public class TaParser {

    /**
     * Ticket Access record containing authentication credentials from AFIP.
     *
     * @param token Authentication token (Base64 encoded XML)
     * @param sign Digital signature (Base64 encoded)
     * @param expirationTime When this ticket expires
     */
    public record TicketAccess(
            String token,
            String sign,
            ZonedDateTime expirationTime
    ) {
        /**
         * Checks if this ticket has expired.
         *
         * @return true if the ticket is expired, false otherwise
         */
        public boolean isExpired() {
            return ZonedDateTime.now().isAfter(expirationTime);
        }
    }

    /**
     * Parses the SOAP response from AFIP WSAA to extract the LoginTicketResponse (TA).
     * The response contains escaped XML inside the <loginCmsReturn> node.
     *
     * @param soapResponse Full SOAP response from AFIP WSAA
     * @return Parsed TicketAccess with token, sign, and expiration time
     * @throws IllegalStateException if parsing fails or required fields are missing
     */
    public TicketAccess parse(String soapResponse) {
        try {
            log.debug("Parsing WSAA SOAP response");

            // Step 1: Parse SOAP envelope
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document soapDoc = builder.parse(
                new ByteArrayInputStream(soapResponse.getBytes(StandardCharsets.UTF_8))
            );

            // Step 2: Extract <loginCmsReturn> node content (comes escaped)
            String escapedLoginTicketResponse = extractLoginCmsReturn(soapDoc);
            if (escapedLoginTicketResponse.trim().isEmpty()) {
                throw new IllegalStateException("loginCmsReturn node is empty");
            }

            log.debug("Extracted loginCmsReturn content (escaped), length: {}", escapedLoginTicketResponse.length());

            // Step 3: Unescape XML entities (&lt; → <, &gt; → >, &amp; → &, etc.)
            String unescapedLoginTicketResponse = unescapeXml(escapedLoginTicketResponse);
            log.debug("Unescaped loginTicketResponse XML, length: {}", unescapedLoginTicketResponse.length());

            // Step 4: Parse the unescaped LoginTicketResponse XML
            Document loginTicketDoc = builder.parse(
                new ByteArrayInputStream(unescapedLoginTicketResponse.getBytes(StandardCharsets.UTF_8))
            );

            // Step 5: Extract token, sign, and expirationTime
            String token = getTextContent(loginTicketDoc, "token");
            String sign = getTextContent(loginTicketDoc, "sign");
            String expirationStr = getTextContent(loginTicketDoc, "expirationTime");

            if (token == null || token.trim().isEmpty()) {
                throw new IllegalStateException("Token not found or empty in LoginTicketResponse");
            }
            if (sign == null || sign.trim().isEmpty()) {
                throw new IllegalStateException("Sign not found or empty in LoginTicketResponse");
            }
            if (expirationStr == null || expirationStr.trim().isEmpty()) {
                throw new IllegalStateException("ExpirationTime not found or empty in LoginTicketResponse");
            }

            log.debug("Successfully extracted token (length: {}), sign (length: {})", token.length(), sign.length());

            // Parse expiration time (AFIP uses ISO-8601 with timezone)
            ZonedDateTime expirationTime = ZonedDateTime.parse(
                    expirationStr,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
            );

            log.info("LoginTicketResponse parsed successfully. Expires at: {}", expirationTime);

            return new TicketAccess(token, sign, expirationTime);

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error parsing AFIP LoginTicketResponse", e);
            throw new IllegalStateException("Error parsing AFIP LoginTicketResponse: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the content of <loginCmsReturn> node from the SOAP response.
     * The content is escaped XML.
     */
    private String extractLoginCmsReturn(Document soapDoc) {
        // Try with namespace-aware search first
        NodeList nodeList = soapDoc.getElementsByTagNameNS("*", "loginCmsReturn");

        if (nodeList.getLength() == 0) {
            // Fallback: try without namespace
            nodeList = soapDoc.getElementsByTagName("loginCmsReturn");
        }

        if (nodeList.getLength() == 0) {
            throw new IllegalStateException("loginCmsReturn node not found in SOAP response");
        }

        Node loginCmsReturnNode = nodeList.item(0);
        String content = loginCmsReturnNode.getTextContent();

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalStateException("loginCmsReturn node is empty");
        }

        return content.trim();
    }

    /**
     * Unescapes XML entities from the escaped XML string.
     * Converts: &lt; → <, &gt; → >, &amp; → &, &quot; → ", &apos; → '
     */
    private String unescapeXml(String escapedXml) {
        return escapedXml
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&"); // Must be last to avoid double-unescaping
    }

    /**
     * Extracts text content from a specific XML tag.
     *
     * @param doc XML document
     * @param tagName Tag name to extract
     * @return Text content of the tag
     * @throws IllegalStateException if tag is not found
     */
    private String getTextContent(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        throw new IllegalStateException("Tag not found in LoginTicketResponse XML: " + tagName);
    }
}
