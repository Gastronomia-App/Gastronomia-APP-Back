package com.progra3.cafeteria_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "afip.wsass")
public class AfipConfig {
    private String url;
    private String service;
    private String certPath;
    private String certPassword;
    private int ticketTimeoutMinutes;
}