package com.shopapp.shared.security;

import com.shopapp.shared.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "VGVzdFNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbk11c3RCZUF0TGVhc3QyNTZCaXRzTG9uZzEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 604800000L);
    }

    @Nested
    @DisplayName("Access Token Generation")
    class AccessTokenGeneration {

        @Test
        @DisplayName("Should generate valid access token with user details")
        void shouldGenerateValidAccessToken() {
            String userId = "user123";
            String email = "test@example.com";
            Set<Role> roles = Set.of(Role.USER);

            String token = jwtService.generateAccessToken(userId, email, roles);

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should include user ID in access token")
        void shouldIncludeUserIdInToken() {
            String userId = "user123";
            String token = jwtService.generateAccessToken(userId, "test@example.com", Set.of(Role.USER));

            assertEquals(userId, jwtService.extractUserId(token));
        }

        @Test
        @DisplayName("Should include email in access token")
        void shouldIncludeEmailInToken() {
            String email = "test@example.com";
            String token = jwtService.generateAccessToken("user123", email, Set.of(Role.USER));

            assertEquals(email, jwtService.extractEmail(token));
        }

        @Test
        @DisplayName("Should include single role in access token")
        void shouldIncludeSingleRoleInToken() {
            Set<Role> roles = Set.of(Role.USER);
            String token = jwtService.generateAccessToken("user123", "test@example.com", roles);

            Set<Role> extractedRoles = jwtService.extractRoles(token);
            assertEquals(1, extractedRoles.size());
            assertTrue(extractedRoles.contains(Role.USER));
        }

        @Test
        @DisplayName("Should include multiple roles in access token")
        void shouldIncludeMultipleRolesInToken() {
            Set<Role> roles = Set.of(Role.USER, Role.VENDOR, Role.ADMIN);
            String token = jwtService.generateAccessToken("user123", "test@example.com", roles);

            Set<Role> extractedRoles = jwtService.extractRoles(token);
            assertEquals(3, extractedRoles.size());
            assertTrue(extractedRoles.contains(Role.USER));
            assertTrue(extractedRoles.contains(Role.VENDOR));
            assertTrue(extractedRoles.contains(Role.ADMIN));
        }

        @Test
        @DisplayName("Should mark token as access type")
        void shouldMarkTokenAsAccessType() {
            String token = jwtService.generateAccessToken("user123", "test@example.com", Set.of(Role.USER));

            assertTrue(jwtService.isAccessToken(token));
            assertFalse(jwtService.isRefreshToken(token));
        }
    }

    @Nested
    @DisplayName("Refresh Token Generation")
    class RefreshTokenGeneration {

        @Test
        @DisplayName("Should generate valid refresh token")
        void shouldGenerateValidRefreshToken() {
            String userId = "user123";

            String token = jwtService.generateRefreshToken(userId);

            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.split("\\.").length == 3);
        }

        @Test
        @DisplayName("Should include user ID in refresh token")
        void shouldIncludeUserIdInRefreshToken() {
            String userId = "user123";
            String token = jwtService.generateRefreshToken(userId);

            assertEquals(userId, jwtService.extractUserId(token));
        }

        @Test
        @DisplayName("Should mark token as refresh type")
        void shouldMarkTokenAsRefreshType() {
            String token = jwtService.generateRefreshToken("user123");

            assertTrue(jwtService.isRefreshToken(token));
            assertFalse(jwtService.isAccessToken(token));
        }

        @Test
        @DisplayName("Should return empty roles for refresh token")
        void shouldReturnEmptyRolesForRefreshToken() {
            String token = jwtService.generateRefreshToken("user123");

            Set<Role> roles = jwtService.extractRoles(token);
            assertTrue(roles.isEmpty());
        }
    }

    @Nested
    @DisplayName("Token Validation")
    class TokenValidation {

        @Test
        @DisplayName("Should validate correct access token")
        void shouldValidateCorrectAccessToken() {
            String token = jwtService.generateAccessToken("user123", "test@example.com", Set.of(Role.USER));

            assertTrue(jwtService.validateToken(token));
        }

        @Test
        @DisplayName("Should validate correct refresh token")
        void shouldValidateCorrectRefreshToken() {
            String token = jwtService.generateRefreshToken("user123");

            assertTrue(jwtService.validateToken(token));
        }

        @Test
        @DisplayName("Should reject null token")
        void shouldRejectNullToken() {
            assertFalse(jwtService.validateToken(null));
        }

        @Test
        @DisplayName("Should reject empty token")
        void shouldRejectEmptyToken() {
            assertFalse(jwtService.validateToken(""));
        }

        @Test
        @DisplayName("Should reject malformed token")
        void shouldRejectMalformedToken() {
            assertFalse(jwtService.validateToken("invalid.token"));
        }

        @Test
        @DisplayName("Should reject token with invalid signature")
        void shouldRejectTokenWithInvalidSignature() {
            String token = jwtService.generateAccessToken("user123", "test@example.com", Set.of(Role.USER));
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

            assertFalse(jwtService.validateToken(tamperedToken));
        }

        @Test
        @DisplayName("Should reject random string as token")
        void shouldRejectRandomString() {
            assertFalse(jwtService.validateToken("randomstring"));
        }

        @Test
        @DisplayName("Should reject token with wrong secret")
        void shouldRejectTokenWithWrongSecret() {
            String token = jwtService.generateAccessToken("user123", "test@example.com", Set.of(Role.USER));

            // Change the secret to a different valid 256-bit key
            ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "V3JvbmdTZWNyZXRfa2V5X2Zvcl9KV1RfdG9rZW5fR2VuZXJhdGlvbl9NdXN0QmVBdExlYXN0MjU2Qml0c0xvbmdfMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MA==");

            assertFalse(jwtService.validateToken(token));
        }
    }

    @Nested
    @DisplayName("Token Type Identification")
    class TokenTypeIdentification {

        @Test
        @DisplayName("Access token should not be identified as refresh token")
        void accessTokenShouldNotBeRefreshToken() {
            String accessToken = jwtService.generateAccessToken("user123", "test@example.com", Set.of(Role.USER));

            assertFalse(jwtService.isRefreshToken(accessToken));
        }

        @Test
        @DisplayName("Refresh token should not be identified as access token")
        void refreshTokenShouldNotBeAccessToken() {
            String refreshToken = jwtService.generateRefreshToken("user123");

            assertFalse(jwtService.isAccessToken(refreshToken));
        }
    }

    @Nested
    @DisplayName("Token Expiration")
    class TokenExpiration {

        @Test
        @DisplayName("Should reject expired access token")
        void shouldRejectExpiredAccessToken() {
            // Set very short expiration
            ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 1L);
            
            String token = jwtService.generateAccessToken("user123", "test@example.com", Set.of(Role.USER));
            
            // Wait for token to expire
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertFalse(jwtService.validateToken(token));
        }
    }
}
