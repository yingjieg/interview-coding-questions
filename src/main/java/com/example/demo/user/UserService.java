package com.example.demo.user;

import com.example.demo.common.exception.BusinessRuleCode;
import com.example.demo.common.exception.BusinessRuleViolationException;
import com.example.demo.user.dto.UserRegistrationDto;
import com.example.demo.user.dto.UserLoginDto;
import com.example.demo.user.dto.PasswordResetDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public UserEntity registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new BusinessRuleViolationException(BusinessRuleCode.EMAIL_ALREADY_EXISTS);
        }

        UserEntity user = new UserEntity();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        UserEntity savedUser = userRepository.save(user);

        // Send verification email
        String verificationToken = UUID.randomUUID().toString();
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        log.info("User registered successfully: {}", savedUser.getEmail());
        return savedUser;
    }

    public boolean verifyEmail(String token) {
        // Note: This is a simplified implementation
        // In production, you'd store the verification token in the database
        Optional<UserEntity> userOpt = userRepository.findByResetToken(token);

        if (userOpt.isEmpty()) {
            return false;
        }

        UserEntity user = userOpt.get();
        user.setEmailVerified(true);
        user.setResetToken(null);
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        log.info("Email verified successfully for: {}", user.getEmail());
        return true;
    }

    public boolean loginUser(UserLoginDto loginDto) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(loginDto.getEmail());

        if (userOpt.isEmpty()) {
            return false;
        }

        UserEntity user = userOpt.get();
        if (!user.getEnabled()) {
            return false;
        }

        boolean passwordMatch = passwordEncoder.matches(loginDto.getPassword(), user.getPassword());

        if (passwordMatch) {
            log.info("User logged in successfully: {}", user.getEmail());
        }

        return passwordMatch;
    }

    public void requestPasswordReset(PasswordResetDto resetDto) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(resetDto.getEmail());

        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", resetDto.getEmail());
            return;
        }

        UserEntity user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

        log.info("Password reset requested for: {}", user.getEmail());
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<UserEntity> userOpt = userRepository.findByResetToken(token);

        if (userOpt.isEmpty()) {
            return false;
        }

        UserEntity user = userOpt.get();

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        log.info("Password reset successfully for: {}", user.getEmail());
        return true;
    }
}