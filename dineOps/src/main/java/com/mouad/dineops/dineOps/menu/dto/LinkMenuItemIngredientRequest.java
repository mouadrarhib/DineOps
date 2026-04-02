package com.mouad.dineops.dineOps.menu.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record LinkMenuItemIngredientRequest(
		@NotNull Long menuItemId,
		@NotNull Long ingredientId,
		@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantityRequired) {
}
