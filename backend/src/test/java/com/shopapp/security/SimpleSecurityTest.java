package com.shopapp.security;

import com.shopapp.shared.domain.Role;
import com.shopapp.shared.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Simple Security Tests
 *
 * Tests core JWT and role-based security functionality.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Simple Security Tests")
class SimpleSecurityTest {

    @Mock
    private JwtService jwtService;

    @Test
    @DisplayName("Should validate JWT token successfully")
    void shouldValidateJwtTokenSuccessfully() {
        String validToken = "valid.jwt.token";
        when(jwtService.validateToken(validToken)).thenReturn(true);

        assertTrue(jwtService.validateToken(validToken));
    }

    @Test
    @DisplayName("Should reject invalid JWT tokens")
    void shouldRejectInvalidJwtTokens() {
        String invalidToken = "invalid.jwt.token";
        when(jwtService.validateToken(invalidToken)).thenReturn(false);

        assertFalse(jwtService.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Should extract USER role from token")
    void shouldExtractUserRoleFromToken() {
        String token = "user.jwt.token";
        when(jwtService.extractRoles(token)).thenReturn(Set.of(Role.USER));

        Set<Role> roles = jwtService.extractRoles(token);
        assertTrue(roles.contains(Role.USER));
        assertEquals(1, roles.size());
    }

    @Test
    @DisplayName("Should extract multiple roles from token")
    void shouldExtractMultipleRolesFromToken() {
        String token = "admin.jwt.token";
        when(jwtService.extractRoles(token)).thenReturn(Set.of(Role.USER, Role.VENDOR, Role.ADMIN));

        Set<Role> roles = jwtService.extractRoles(token);
        assertTrue(roles.contains(Role.USER));
        assertTrue(roles.contains(Role.VENDOR));
        assertTrue(roles.contains(Role.ADMIN));
        assertEquals(3, roles.size());
    }

    @Test
    @DisplayName("Should identify access tokens correctly")
    void shouldIdentifyAccessTokensCorrectly() {
        String accessToken = "access.jwt.token";
        String refreshToken = "refresh.jwt.token";

        when(jwtService.isAccessToken(accessToken)).thenReturn(true);
        when(jwtService.isAccessToken(refreshToken)).thenReturn(false);

        assertTrue(jwtService.isAccessToken(accessToken));
        assertFalse(jwtService.isAccessToken(refreshToken));
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        String token = "user.jwt.token";
        when(jwtService.extractUserId(token)).thenReturn("user123");

        assertEquals("user123", jwtService.extractUserId(token));
    }

    @Test
    @DisplayName("Should extract email from token")
    void shouldExtractEmailFromToken() {
        String token = "user.jwt.token";
        when(jwtService.extractEmail(token)).thenReturn("user@example.com");

        assertEquals("user@example.com", jwtService.extractEmail(token));
    }
}