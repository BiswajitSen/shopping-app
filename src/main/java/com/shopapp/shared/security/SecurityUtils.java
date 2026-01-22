package com.shopapp.shared.security;

import com.shopapp.shared.domain.Role;
import com.shopapp.shared.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

/**
 * Utility class for security-related operations
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class, no instantiation
    }

    /**
     * Get the current authenticated user principal
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() 
                || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    /**
     * Get the current authenticated user's ID
     */
    public static String getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Get the current authenticated user's email
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Get the current authenticated user's roles
     */
    public static Set<Role> getCurrentUserRoles() {
        return getCurrentUser().getRoles();
    }

    /**
     * Check if the current user has a specific role
     */
    public static boolean hasRole(Role role) {
        return getCurrentUser().hasRole(role);
    }

    /**
     * Check if the current user is an admin
     */
    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Check if the current user is a vendor
     */
    public static boolean isVendor() {
        return hasRole(Role.VENDOR);
    }
}
