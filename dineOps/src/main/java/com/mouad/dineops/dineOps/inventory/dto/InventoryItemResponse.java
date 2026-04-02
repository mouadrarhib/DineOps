package com.mouad.dineops.dineOps.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryItemResponse(
		Long id,
		Long branchId,
		Long ingredientId,
		String ingredientName,
		String ingredientUnit,
		BigDecimal quantityAvailable,
		BigDecimal minThreshold,
		Instant lastRestockedAt,
		Instant createdAt,
		Instant updatedAt) {
}
