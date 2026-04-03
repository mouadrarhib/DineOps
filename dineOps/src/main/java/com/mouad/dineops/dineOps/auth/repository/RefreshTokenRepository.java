package com.mouad.dineops.dineOps.auth.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mouad.dineops.dineOps.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	@Modifying
	@Query("update RefreshToken rt set rt.revoked = true where rt.user.id = :userId")
	int revokeAllByUserId(@Param("userId") Long userId);

	@Modifying
	@Query("delete from RefreshToken rt where rt.expiresAt < :now")
	int deleteAllExpired(@Param("now") Instant now);

	@Modifying
	@Query("delete from RefreshToken rt where rt.revoked = true and rt.expiresAt < :now")
	int deleteAllRevokedAndExpired(@Param("now") Instant now);
}
