package com.example.demo.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtUserInfo {
    private Long userId;
    private String email;
    private String fullName;
}