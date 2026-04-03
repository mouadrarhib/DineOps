package com.mouad.dineops.dineOps.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesReportRow(
		LocalDate date,
		Long branchId,
		String branchName,
		Long completedOrders,
		BigDecimal subtotal,
		BigDecimal tax,
		BigDecimal total) {
}
