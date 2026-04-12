package otvosuzlet.javitasnyilntarto.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import otvosuzlet.javitasnyilntarto.dto.RefreshTokenDataDto;
import otvosuzlet.javitasnyilntarto.service.RefreshTokenService;

@RestController
@RequestMapping("/api/refresh-tokens")
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Gets all active (non-revoked and non-expired) refresh tokens for the authenticated user.
     *
     * @param principal The authenticated principal.
     * @return Active refresh token records containing user agent, IP address and record ID.
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RefreshTokenDataDto>> getActiveTokens(Principal principal) {
        return ResponseEntity.ok(refreshTokenService.getActiveTokens(principal.getName()));
    }

    /**
     * Invalidates a specific refresh token by ID for the authenticated user.
     *
     * @param id The refresh token record ID.
     * @param principal The authenticated principal.
     * @return Empty response indicating successful invalidation.
     */
    @PostMapping("/{id}/invalidate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> invalidateToken(@PathVariable Integer id, Principal principal) {
        refreshTokenService.invalidateToken(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
