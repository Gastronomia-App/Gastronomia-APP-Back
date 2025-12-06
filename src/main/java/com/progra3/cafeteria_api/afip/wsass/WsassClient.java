package com.progra3.cafeteria_api.afip.wsass;

import com.progra3.cafeteria_api.config.AfipConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * SOAP client for AFIP WSAA (Web Service Authentication and Authorization).
 * Handles loginCms operation to obtain authentication tickets.
 *
 * Note: WSAA doesn't provide a standard WSDL, so we use RestTemplate
 * with manual SOAP envelope construction instead of Spring WS.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WsassClient {

    private final AfipConfig afipConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Calls AFIP WSAA loginCms service with CMS signed data.
     *
     * @param cmsSignedData PKCS#7 signed TRA (Ticket Request Authentication)
     * @return Full SOAP response containing the LoginTicketResponse
     * @throws IllegalStateException if the call fails or SOAP fault is received
     */
    public String loginCms(byte[] cmsSignedData) {
        String cms = Base64.getEncoder().encodeToString(cmsSignedData);
        log.debug("CMS signed data size: {} bytes, Base64 size: {} chars", cmsSignedData.length, cms.length());

        String soapRequest = buildSoapRequest(cms);

        log.debug("Calling AFIP WSAA at: {}", afipConfig.getUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        headers.set("SOAPAction", "");

        HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);

        try {
            log.debug("Sending SOAP request to AFIP WSAA");
            ResponseEntity<String> response = restTemplate.exchange(
                    afipConfig.getUrl(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            String responseBody = response.getBody();

            validateResponse(response, responseBody);

            log.debug("WSAA response received successfully. Status: {}, Body length: {} chars",
                     response.getStatusCode(),
                     responseBody.length());

            return responseBody;

        } catch (RestClientException e) {
            log.error("REST client error calling AFIP WSAA: {}", e.getMessage(), e);
            throw new IllegalStateException("Error calling AFIP WSAA loginCms: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling AFIP WSAA", e);
            throw new IllegalStateException("Unexpected error calling AFIP WSAA loginCms: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the SOAP request envelope for loginCms operation.
     */
    private String buildSoapRequest(String base64CmsData) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="https://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:wsaa="https://wsaa.view.sua.dvadac.desein.afip.gov">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <wsaa:loginCms>
                      <wsaa:in0>%s</wsaa:in0>
                    </wsaa:loginCms>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(base64CmsData);
    }

    /**
     * Validates the SOAP response from AFIP WSAA.
     *
     * @throws IllegalStateException if response is invalid or contains SOAP fault
     */
    private void validateResponse(ResponseEntity<String> response, String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            throw new IllegalStateException("Empty response from AFIP WSAA");
        }

        // Check HTTP status
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("AFIP WSAA returned non-success status: {}", response.getStatusCode());
            throw new IllegalStateException("AFIP WSAA returned error status: " + response.getStatusCode());
        }

        // Check for SOAP faults
        if (responseBody.contains("faultstring") || responseBody.contains("soap:Fault")) {
            String faultMessage = extractSoapFault(responseBody);
            log.error("SOAP Fault received from AFIP WSAA: {}", faultMessage);
            throw new IllegalStateException("SOAP Fault from AFIP WSAA: " + faultMessage);
        }

        // Verify loginCmsReturn exists
        if (!responseBody.contains("loginCmsReturn")) {
            log.error("Response does not contain loginCmsReturn node");
            throw new IllegalStateException("Invalid WSAA response: loginCmsReturn not found");
        }
    }

    /**
     * Extracts fault message from SOAP fault response.
     */
    private String extractSoapFault(String soapResponse) {
        try {
            // Try faultstring first
            int start = soapResponse.indexOf("<faultstring>");
            int end = soapResponse.indexOf("</faultstring>");

            if (start == -1 || end == -1) {
                // Try soap:Fault/faultstring
                start = soapResponse.indexOf("<faultstring>", soapResponse.indexOf("Fault"));
                end = soapResponse.indexOf("</faultstring>", start);
            }

            if (start != -1 && end != -1) {
                return soapResponse.substring(start + 13, end).trim();
            }

            // Return generic message if we can't parse it
            return "Could not parse SOAP fault details";
        } catch (Exception e) {
            log.warn("Error extracting SOAP fault message", e);
            return "Unknown SOAP fault";
        }
    }
}
