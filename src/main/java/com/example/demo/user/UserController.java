package com.example.demo.user;

import com.example.demo.user.dto.UserRegistrationDto;
import com.example.demo.user.dto.UserLoginDto;
import com.example.demo.user.dto.PasswordResetDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Email already exists")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserEntity user = userService.registerUser(registrationDto);
        return ResponseEntity.ok("User registered successfully. Check your email for verification.");
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginDto loginDto) {
        boolean loginSuccess = userService.loginUser(loginDto);

        if (loginSuccess) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    @ApiResponse(responseCode = "200", description = "Password reset email sent")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody PasswordResetDto resetDto) {
        userService.requestPasswordReset(resetDto);
        return ResponseEntity.ok("If the email exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    public ResponseEntity<String> resetPassword(@RequestParam String token,
                                               @RequestParam String newPassword) {
        boolean resetSuccess = userService.resetPassword(token, newPassword);

        if (resetSuccess) {
            return ResponseEntity.ok("Password reset successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email address")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid verification token")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean verificationSuccess = userService.verifyEmail(token);

        if (verificationSuccess) {
            return ResponseEntity.ok("Email verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid verification token");
        }
    }
}