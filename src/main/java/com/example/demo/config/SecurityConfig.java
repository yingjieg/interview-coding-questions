package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CorsProperties corsProperties;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CorsProperties corsProperties) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.corsProperties = corsProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Set allowed origins from YAML configuration
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());

        // Set allowed methods from YAML configuration
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());

        // Set allowed headers
        configuration.addAllowedHeader(corsProperties.getAllowedHeaders());

        // Set allow credentials
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());

        // Set max age
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/auth/**", "/api/users/register", "/api/users/login", "/api/users/forgot-password", "/api/users/reset-password", "/api/users/verify").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/", "/users", "/orders", "/bookings", "/purchase", "/static/**", "/*.html", "/*.css", "/*.js").permitAll()
                        .requestMatchers("/api/stripe/**").permitAll() // Add Stripe endpoints as public for now
                        // Protected endpoints - require authentication
                        .requestMatchers("/api/orders/**", "/api/bookings/**", "/api/purchases/**", "/api/payments/**").authenticated()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}