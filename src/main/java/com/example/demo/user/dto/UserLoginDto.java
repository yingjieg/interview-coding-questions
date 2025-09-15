package com.example.demo.user.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UserLoginDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}