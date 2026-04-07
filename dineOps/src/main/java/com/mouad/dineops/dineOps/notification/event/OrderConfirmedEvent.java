package com.mouad.dineops.dineOps.notification.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderConfirmedEvent(
		Long orderId,
		String orderNumber,
		Long branchId,
		BigDecimal totalAmount,
		Instant confirmedAt) {
}
