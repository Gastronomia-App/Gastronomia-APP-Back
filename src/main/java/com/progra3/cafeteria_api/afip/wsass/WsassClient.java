package com.progra3.cafeteria_api.afip.wsass;

import com.progra3.cafeteria_api.config.AfipConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class WsassClient {

    private final AfipConfig afipConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public String loginCms(byte[] cmsSignedData) {
        String cms = Base64.getEncoder().encodeToString(cmsSignedData);

        String soapRequest = """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:wsaa="http://wsaa.view.sua.dvadac.desein.afip.gov">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <wsaa:loginCms>
                      <wsaa:in0>%s</wsaa:in0>
                    </wsaa:loginCms>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(cms);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.set("SOAPAction", "");

        HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    afipConfig.getUrl(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return extractLoginCmsReturn(response.getBody());

        } catch (Exception e) {
            throw new IllegalStateException("Error calling AFIP WSAA loginCms", e);
        }
    }

    private String extractLoginCmsReturn(String soapResponse) {
        try {
            // Extract the loginCmsReturn content from SOAP response
            int start = soapResponse.indexOf("<loginCmsReturn>");
            int end = soapResponse.indexOf("</loginCmsReturn>");

            if (start == -1 || end == -1) {
                throw new IllegalStateException("Invalid SOAP response: loginCmsReturn not found");
            }

            return soapResponse.substring(start + 16, end);
        } catch (Exception e) {
            throw new IllegalStateException("Error extracting loginCmsReturn from SOAP response", e);
        }
    }
}
