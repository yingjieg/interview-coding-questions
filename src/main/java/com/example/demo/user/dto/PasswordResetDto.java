package com.example.demo.user.dto;

import lombok.Data;

@Data
public class PasswordResetDto {
    private String email;
}

@Data
class PasswordResetConfirmDto {
    private String token;
    private String newPassword;
}