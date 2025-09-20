package com.example.demo.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String jwtToken;

    public JwtAuthenticationToken(Object principal, String jwtToken, Collection<? extends GrantedAuthority> authorities) {
        super(principal, null, authorities);
        this.jwtToken = jwtToken;
    }

    public String getJwtToken() {
        return jwtToken;
    }
}