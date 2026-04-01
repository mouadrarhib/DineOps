package com.mouad.dineops.dineOps.common.security;

import java.util.Optional;

public final class AuditMetadataContext {

	private static final ThreadLocal<AuditMetadata> CONTEXT = new ThreadLocal<>();

	private AuditMetadataContext() {
	}

	public static void set(AuditMetadata metadata) {
		CONTEXT.set(metadata);
	}

	public static Optional<AuditMetadata> get() {
		return Optional.ofNullable(CONTEXT.get());
	}

	public static void clear() {
		CONTEXT.remove();
	}
}
