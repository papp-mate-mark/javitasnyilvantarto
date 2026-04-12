package otvosuzlet.javitasnyilntarto.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import otvosuzlet.javitasnyilntarto.dto.RefreshTokenDataDto;
import otvosuzlet.javitasnyilntarto.exceptions.InvalidCredentialsException;
import otvosuzlet.javitasnyilntarto.model.RefreshToken;
import otvosuzlet.javitasnyilntarto.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void getActiveTokensShouldReturnMappedData() {
        RefreshToken first = new RefreshToken();
        first.setId(11);
        first.setUserAgent("Firefox");
        first.setIpAddress("127.0.0.1");

        RefreshToken second = new RefreshToken();
        second.setId(12);
        second.setUserAgent("Chrome");
        second.setIpAddress("10.0.0.2");

        Mockito.when(refreshTokenRepository.findByUserUsernameAndRevokedAtIsNullAndExpiresAtAfter(Mockito.eq("john"), Mockito.any(Instant.class)))
            .thenReturn(List.of(first, second));

        List<RefreshTokenDataDto> result = refreshTokenService.getActiveTokens("john");

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("Firefox", result.get(0).getUserAgent());
        Assertions.assertEquals("127.0.0.1", result.get(0).getIpAddress());
        Assertions.assertEquals(11, result.get(0).getId());
        Assertions.assertEquals("Chrome", result.get(1).getUserAgent());
        Assertions.assertEquals("10.0.0.2", result.get(1).getIpAddress());
        Assertions.assertEquals(12, result.get(1).getId());
    }

    @Test
    void invalidateTokenShouldSetRevokedAtWhenNull() {
        RefreshToken token = new RefreshToken();
        token.setId(1);
        token.setRevokedAt(null);

        Mockito.when(refreshTokenRepository.findByIdAndUserUsername(1, "john")).thenReturn(Optional.of(token));

        refreshTokenService.invalidateToken(1, "john");

        Assertions.assertNotNull(token.getRevokedAt());
        Mockito.verify(refreshTokenRepository).save(token);
    }

    @Test
    void invalidateTokenShouldSetRevokedAtWhenInFuture() {
        RefreshToken token = new RefreshToken();
        token.setId(2);
        Instant future = Instant.now().plusSeconds(3600);
        token.setRevokedAt(future);

        Mockito.when(refreshTokenRepository.findByIdAndUserUsername(2, "john")).thenReturn(Optional.of(token));

        refreshTokenService.invalidateToken(2, "john");

        Assertions.assertNotNull(token.getRevokedAt());
        Assertions.assertTrue(token.getRevokedAt().isBefore(future));
        Mockito.verify(refreshTokenRepository).save(token);
    }

    @Test
    void invalidateTokenShouldKeepPastRevokedAt() {
        RefreshToken token = new RefreshToken();
        token.setId(3);
        Instant past = Instant.now().minusSeconds(3600);
        token.setRevokedAt(past);

        Mockito.when(refreshTokenRepository.findByIdAndUserUsername(3, "john")).thenReturn(Optional.of(token));

        refreshTokenService.invalidateToken(3, "john");

        Assertions.assertEquals(past, token.getRevokedAt());
        Mockito.verify(refreshTokenRepository).save(token);
    }

    @Test
    void invalidateTokenShouldThrowWhenNotFoundForUser() {
        Mockito.when(refreshTokenRepository.findByIdAndUserUsername(99, "john")).thenReturn(Optional.empty());

        Assertions.assertThrows(
            InvalidCredentialsException.class,
            () -> refreshTokenService.invalidateToken(99, "john")
        );
    }

    @Test
    void getActiveTokensShouldQueryWithCurrentTimestamp() {
        Mockito.when(refreshTokenRepository.findByUserUsernameAndRevokedAtIsNullAndExpiresAtAfter(Mockito.eq("john"), Mockito.any(Instant.class)))
            .thenReturn(List.of());

        refreshTokenService.getActiveTokens("john");

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        Mockito.verify(refreshTokenRepository)
            .findByUserUsernameAndRevokedAtIsNullAndExpiresAtAfter(Mockito.eq("john"), instantCaptor.capture());
        Assertions.assertNotNull(instantCaptor.getValue());
    }
}
