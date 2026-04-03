package com.mouad.dineops.dineOps.reporting.dto;

import java.math.BigDecimal;

public record BranchPerformanceRow(
		Long branchId,
		String branchName,
		Long totalOrders,
		Long completedOrders,
		Long canceledOrders,
		BigDecimal completedRevenue) {
}
