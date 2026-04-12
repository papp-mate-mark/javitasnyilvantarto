package otvosuzlet.javitasnyilntarto.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import otvosuzlet.javitasnyilntarto.dto.RefreshTokenDataDto;
import otvosuzlet.javitasnyilntarto.exceptions.InvalidCredentialsException;
import otvosuzlet.javitasnyilntarto.model.RefreshToken;
import otvosuzlet.javitasnyilntarto.repository.RefreshTokenRepository;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RefreshTokenDataDto> getActiveTokens(String username) {
        return refreshTokenRepository
            .findByUserUsernameAndRevokedAtIsNullAndExpiresAtAfter(username, Instant.now())
            .stream()
            .map(token -> new RefreshTokenDataDto(token.getUserAgent(), token.getIpAddress(), token.getId()))
            .toList();
    }

    @Override
    @Transactional
    public void invalidateToken(Integer id, String username) {
        RefreshToken token = refreshTokenRepository.findByIdAndUserUsername(id, username)
            .orElseThrow(() -> new InvalidCredentialsException("Refresh token not found"));

        if (token.getRevokedAt() == null || token.getRevokedAt().isAfter(Instant.now())) {
            token.setRevokedAt(Instant.now());
        }

        refreshTokenRepository.save(token);
    }
}
