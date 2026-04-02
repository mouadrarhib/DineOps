package com.mouad.dineops.dineOps.menu.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MenuItemIngredientResponse(
		Long id,
		Long menuItemId,
		Long ingredientId,
		String ingredientName,
		String ingredientUnit,
		BigDecimal quantityRequired,
		Instant createdAt,
		Instant updatedAt) {
}
