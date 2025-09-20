package com.example.demo.security;

import com.example.demo.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * Get the current authenticated user from SecurityContext
     *
     * @return UserEntity from the security context
     * @throws IllegalStateException if no user is authenticated
     */
    public static UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserEntity) {
            return (UserEntity) principal;
        }

        throw new IllegalStateException("User not found in security context");
    }

    /**
     * Get the current authenticated user's ID
     *
     * @return The authenticated user's ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Get the current authenticated user's email
     *
     * @return The authenticated user's email
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Check if a user is currently authenticated
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());
    }
}