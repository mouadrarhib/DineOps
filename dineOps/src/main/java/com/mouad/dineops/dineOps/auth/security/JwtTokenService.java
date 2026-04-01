package com.mouad.dineops.dineOps.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

	private final SecretKey signingKey;
	private final long accessTokenExpirationMinutes;
	private final long refreshTokenExpirationDays;

	public JwtTokenService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes,
			@Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
		this.refreshTokenExpirationDays = refreshTokenExpirationDays;
	}

	public String generateAccessToken(AppUserPrincipal principal) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

		return Jwts.builder()
				.subject(principal.getEmail())
				.claim("uid", principal.getId())
				.claim("roles", principal.getRoles())
				.claim("type", "access")
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				.signWith(signingKey)
				.compact();
	}

	public String generateRefreshToken(AppUserPrincipal principal) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

		return Jwts.builder()
				.subject(principal.getEmail())
				.claim("uid", principal.getId())
				.claim("type", "refresh")
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				.signWith(signingKey)
				.compact();
	}

	public boolean isTokenValid(String token, String expectedType) {
		Claims claims = extractAllClaims(token);
		String tokenType = claims.get("type", String.class);
		return expectedType.equals(tokenType) && claims.getExpiration().after(new Date());
	}

	public String extractSubject(String token) {
		return extractAllClaims(token).getSubject();
	}

	public Instant extractExpiration(String token) {
		return extractAllClaims(token).getExpiration().toInstant();
	}

	public Map<String, Object> extractClaims(String token) {
		Claims claims = extractAllClaims(token);
		return Map.copyOf(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
	}
}
