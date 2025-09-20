package com.example.demo.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isBlank(secretKey) || StringUtils.equals(secretKey, "your_stripe_secret_key")) {
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
        return StringUtils.isNotBlank(secretKey) &&
                !StringUtils.equals(secretKey, "your_stripe_secret_key") &&
                StringUtils.isNotBlank(publishableKey) &&
                !StringUtils.equals(publishableKey, "your_stripe_publishable_key");
    }
}