package com.example.demo.security;

import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        log.debug("JWT Filter - Processing request: {} {}", request.getMethod(), request.getServletPath());

        if (request.getServletPath().contains("/api/auth")) {
            log.debug("JWT Filter - Skipping authentication for public endpoint: {}", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        log.debug("JWT Filter - Authorization header: {}", authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null");

        final String jwt;
        final String userEmail;

        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("JWT Filter - No valid Authorization header found for protected endpoint: {}", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userEmail = jwtService.extractUsername(jwt);
            log.debug("JWT Filter - Extracted username from token: {}", userEmail);

            if (StringUtils.isNotBlank(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<UserEntity> userOpt = userRepository.findByEmail(userEmail);
                log.debug("JWT Filter - User found in database: {}", userOpt.isPresent());

                if (userOpt.isPresent()) {
                    boolean isTokenValid = jwtService.isTokenValid(jwt, userEmail);
                    log.debug("JWT Filter - Token validation result: {}", isTokenValid);

                    if (isTokenValid) {
                        UserEntity user = userOpt.get();

                        // Create authentication token with the UserEntity as principal
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user, // Use UserEntity directly as principal
                                null,
                                List.of(() -> "ROLE_USER") // Simple authority
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("JWT authentication successful for user: {}", userEmail);
                    } else {
                        log.warn("JWT Filter - Token validation failed for user: {}", userEmail);
                    }
                } else {
                    log.warn("JWT Filter - User not found for email: {}", userEmail);
                }
            } else {
                log.debug("JWT Filter - Username blank or already authenticated");
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}