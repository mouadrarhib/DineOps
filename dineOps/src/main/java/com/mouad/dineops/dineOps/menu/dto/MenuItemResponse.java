package com.mouad.dineops.dineOps.menu.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MenuItemResponse(
		Long id,
		Long categoryId,
		Long branchId,
		String name,
		String description,
		BigDecimal price,
		boolean available,
		Integer preparationTimeMinutes,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
