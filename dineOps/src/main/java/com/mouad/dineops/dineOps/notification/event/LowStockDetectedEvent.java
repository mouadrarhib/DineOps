package com.mouad.dineops.dineOps.notification.event;

import java.math.BigDecimal;

public record LowStockDetectedEvent(
		Long branchId,
		Long inventoryItemId,
		String ingredientName,
		BigDecimal quantityAvailable,
		String unit) {
}
