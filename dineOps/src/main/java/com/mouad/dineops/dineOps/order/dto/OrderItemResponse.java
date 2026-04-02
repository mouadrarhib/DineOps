package com.mouad.dineops.dineOps.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
		Long id,
		Long menuItemId,
		String menuItemName,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal totalPrice,
		String notes) {
}
