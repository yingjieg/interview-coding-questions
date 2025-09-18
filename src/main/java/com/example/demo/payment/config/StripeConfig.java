package com.example.demo.payment.config;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty() || secretKey.equals("your_stripe_secret_key")) {
            log.warn("Stripe secret key not configured. Stripe payments will not work.");
            return;
        }

        try {
            Stripe.apiKey = secretKey;
            log.info("Stripe configuration initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Stripe configuration: {}", e.getMessage());
            throw new IllegalStateException("Stripe configuration failed", e);
        }
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public boolean isConfigured() {
        return secretKey != null &&
                !secretKey.isEmpty() &&
                !secretKey.equals("your_stripe_secret_key") &&
                publishableKey != null &&
                !publishableKey.isEmpty() &&
                !publishableKey.equals("your_stripe_publishable_key");
    }
}