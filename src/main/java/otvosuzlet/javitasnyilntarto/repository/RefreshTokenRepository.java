package otvosuzlet.javitasnyilntarto.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import otvosuzlet.javitasnyilntarto.model.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByUserUsernameAndRevokedAtIsNullAndExpiresAtAfter(String username, Instant now);
    Optional<RefreshToken> findByIdAndUserUsername(Integer id, String username);
}
