package com.mouad.dineops.dineOps.auth.dto;

import java.time.Instant;

public record AuthTokensResponse(
		String tokenType,
		String accessToken,
		String refreshToken,
		Instant accessTokenExpiresAt,
		Instant refreshTokenExpiresAt) {
}
