package otvosuzlet.javitasnyilntarto.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import otvosuzlet.javitasnyilntarto.dto.LoginRequestDto;
import otvosuzlet.javitasnyilntarto.dto.LoginResponse;
import otvosuzlet.javitasnyilntarto.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService service;
    
    /**
     * Authenticates a user and generates access and refresh tokens.
     *
     * @param user The login request containing user credentials.
     * @param request The HTTP request to extract IP and user agent.
     * @return The login response consisting of the generated tokens and user authorities.
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequestDto user, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        return service.verify(user, ipAddress, userAgent);
    }

    /**
     * Returns a new token pair using a refresh token.
     *
     * @param refreshToken The raw refresh token string provided by the user.
     * @param request The HTTP request to extract IP and user agent.
     * @return The updated login response containing new tokens.
     */
    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody String refreshToken, HttpServletRequest request) {
        try {
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            return service.refresh(refreshToken, ipAddress, userAgent);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token", e);
        }
    }
    
    /**
     * Logs out the user by terminating the provided refresh token.
     *
     * @param refreshToken The refresh token corresponding to the session to invalidate.
     */
    @PostMapping("/logout")
    public void logout(@RequestBody String refreshToken) {
        this.service.invalidateRefreshTokenString(refreshToken);
    }

    /**
     * Invalidates all other refresh tokens for the same user, keeping only the provided token valid.
     * Effectively logs out from all other sessions/devices.
     * Only invalidates tokens for the authenticated user.
     *
     * @param refreshToken The refresh token to keep valid.
     */
    @PostMapping("/logout-others")
    public void logoutOthers(@RequestBody String refreshToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        this.service.invalidateOtherRefreshTokensByUsername(refreshToken, username);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
