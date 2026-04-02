package com.mouad.dineops.dineOps.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record IngredientResponse(
		Long id,
		String name,
		String unit,
		BigDecimal minThreshold,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
