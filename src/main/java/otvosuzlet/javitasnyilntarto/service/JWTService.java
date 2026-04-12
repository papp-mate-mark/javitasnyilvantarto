package otvosuzlet.javitasnyilntarto.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import otvosuzlet.javitasnyilntarto.dto.TokensResponseDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    private static final Logger logger = LoggerFactory.getLogger("fileLogger");

    private final String secretKey;

    private final long accessTokenTtlMs;

    private final long refreshTokenTtlMs;

    public JWTService(
            @Value("${jwt.secret-base64:}") String secretKeyBase64,
            @Value("${jwt.access-token-ttl-ms}") long accessTokenTtlMs,
            @Value("${jwt.refresh-token-ttl-ms}") long refreshTokenTtlMs
    ) {
        this.accessTokenTtlMs = accessTokenTtlMs;
        this.refreshTokenTtlMs = refreshTokenTtlMs;

        if (secretKeyBase64 != null && !secretKeyBase64.isBlank()) {
            this.secretKey = secretKeyBase64.trim();
            logger.info("Using provided JWT token key.");

            return;
        }
        // Fallback: generate a random secret at startup (tokens won't survive restart).
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            logger.info("JWT token key not provided. Using auto generated key.");
            this.secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates both access and refresh tokens for a given username.
     *
     * @param username The subject for whom to generate the tokens.
     * @return A DTO containing the newly generated access and refresh tokens.
     */
    public TokensResponseDTO generateTokens(String username) {
        String accessToken = generateToken(username, accessTokenTtlMs, "access", getKey());
        String refreshToken = generateToken(username, refreshTokenTtlMs, "refresh", getKey());

        return new TokensResponseDTO(accessToken, refreshToken);
    }

    private String generateToken(String username, long expiryMillis, String type, SecretKey key) {
        long cMilis = System.currentTimeMillis();
        Date now = new Date(cMilis);
        Date expiry = new Date(cMilis + expiryMillis);
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", type);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the username (subject) from a given token.
     *
     * @param token The JWT string.
     * @return The username embedded in the token.
     */
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extracts the token type from a given token.
     *
     * @param token The JWT string.
     * @return The token type embedded in the token's claims.
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("token_type", String.class));
    }
    
    /**
     * Validates an access token against the provided UserDetails.
     * Ensures the token's subject matches the username, the token is not expired,
     * and it has the correct "access" type.
     *
     * @param token The JWT string to validate.
     * @param userDetails The user details to match against the token.
     * @return True if the token is valid, otherwise false.
     */
    public boolean validateAccessToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) 
                && !isTokenExpired(token) 
                && "access".equals(extractTokenType(token))); 
    }
    
    /**
     * Checks if a given token has expired.
     *
     * @param token The JWT string.
     * @return True if the token's expiration date is before the current date, false otherwise.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a given token.
     *
     * @param token The JWT string.
     * @return The expiration date of the token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
