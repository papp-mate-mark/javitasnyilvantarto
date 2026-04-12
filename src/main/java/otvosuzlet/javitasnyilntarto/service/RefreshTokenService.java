package otvosuzlet.javitasnyilntarto.service;

import java.util.List;

import otvosuzlet.javitasnyilntarto.dto.RefreshTokenDataDto;

public interface RefreshTokenService {
    List<RefreshTokenDataDto> getActiveTokens(String username);

    void invalidateToken(Integer id, String username);
}
