package com.example.demo.user;

import com.example.demo.user.dto.AuthenticationResponseDto;
import com.example.demo.user.dto.UserLoginDto;
import com.example.demo.user.dto.UserRegistrationDto;
import com.example.demo.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "JWT-based authentication endpoints")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register a new user", description = "Register a new user account")
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        log.info("Registration attempt for email: {}", registrationDto.getEmail());

        UserEntity user = userService.registerUser(registrationDto);

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .build();

        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Authenticate user with JWT", description = "Login user and return JWT tokens")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(@Valid @RequestBody UserLoginDto loginDto) {
        log.info("JWT authentication attempt for email: {}", loginDto.getEmail());

        AuthenticationResponseDto response = userService.authenticateUser(loginDto);

        if (response == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout", description = "Logout user (client-side token cleanup)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // TODO implement blacklist by Redis
        return ResponseEntity.ok().build();
    }
}