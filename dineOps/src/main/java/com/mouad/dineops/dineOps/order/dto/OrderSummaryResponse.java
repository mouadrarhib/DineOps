package com.mouad.dineops.dineOps.order.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.mouad.dineops.dineOps.common.enums.OrderStatus;

public record OrderSummaryResponse(
		Long id,
		Long branchId,
		String orderNumber,
		OrderStatus status,
		String source,
		BigDecimal subtotal,
		BigDecimal taxAmount,
		BigDecimal totalAmount,
		String notes,
		Long createdByUserId,
		Instant createdAt,
		Instant confirmedAt,
		Instant completedAt) {
}
