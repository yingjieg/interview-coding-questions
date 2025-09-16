package com.example.demo.payment.config;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paypal")
@Data
public class PayPalConfig {

    private String clientId;
    private String clientSecret;
    private String mode;
    private String baseUrl;
    private String returnUrl;
    private String cancelUrl;

    @Bean
    public PayPalHttpClient payPalHttpClient() {
        PayPalEnvironment environment;

        if ("sandbox".equalsIgnoreCase(mode)) {
            environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
        } else {
            environment = new PayPalEnvironment.Live(clientId, clientSecret);
        }

        return new PayPalHttpClient(environment);
    }
}