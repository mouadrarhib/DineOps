package com.mouad.dineops.dineOps.inventory.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateInventoryItemRequest(
		@NotNull Long branchId,
		@NotNull Long ingredientId,
		@NotNull @DecimalMin(value = "0.0") BigDecimal quantityAvailable) {
}
