package com.mouad.dineops.dineOps.menu.dto;

import java.time.Instant;

public record MenuCategoryResponse(
		Long id,
		Long branchId,
		String name,
		String description,
		Integer displayOrder,
		boolean active,
		Instant createdAt,
		Instant updatedAt) {
}
