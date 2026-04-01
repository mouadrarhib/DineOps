package com.mouad.dineops.dineOps.common.security;

public record AuditMetadata(
		String requestId,
		String userId,
		String ipAddress,
		String userAgent,
		String requestPath,
		String httpMethod) {
}
