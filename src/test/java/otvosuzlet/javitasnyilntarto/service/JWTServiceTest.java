package otvosuzlet.javitasnyilntarto.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import otvosuzlet.javitasnyilntarto.dto.TokensResponseDTO;

class JWTServiceTest {

    private JWTService jwtService;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService("dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHktdGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHk=", 60000, 3600000);
        testUser = new User("testuser", "", Collections.emptyList());
    }

    @Test
    void generateTokensShouldReturnValidTokens() {
        TokensResponseDTO tokens = jwtService.generateTokens(testUser);

        assertNotNull(tokens);
        assertNotNull(tokens.getAccessToken());
        assertNotNull(tokens.getRefreshToken());
    }

    @Test
    void extractUserNameShouldReturnCorrectUsername() {
        TokensResponseDTO tokens = jwtService.generateTokens(testUser);
        String username = jwtService.extractUserName(tokens.getAccessToken());

        assertEquals("testuser", username);
    }

    @Test
    void extractTokenTypeShouldReturnAccessForAccessToken() {
        TokensResponseDTO tokens = jwtService.generateTokens(testUser);
        String tokenType = jwtService.extractTokenType(tokens.getAccessToken());

        assertEquals("access", tokenType);
    }

    @Test
    void extractTokenTypeShouldReturnRefreshForRefreshToken() {
        TokensResponseDTO tokens = jwtService.generateTokens(testUser);
        String tokenType = jwtService.extractTokenType(tokens.getRefreshToken());

        assertEquals("refresh", tokenType);
    }

    @Test
    void isTokenExpiredShouldReturnFalseForValidToken() {
        TokensResponseDTO tokens = jwtService.generateTokens(testUser);
        boolean isExpired = jwtService.isTokenExpired(tokens.getAccessToken());

        assertFalse(isExpired);
    }

    @Test
    void extractExpirationShouldReturnFutureDate() {
        TokensResponseDTO tokens = jwtService.generateTokens(testUser);
        Date expiration = jwtService.extractExpiration(tokens.getAccessToken());

        assertTrue(expiration.after(new Date()));
    }
}
