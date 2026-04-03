package com.mouad.dineops.dineOps.reporting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderSummaryReportResponse(
		LocalDate fromDate,
		LocalDate toDate,
		Long totalOrders,
		Long pending,
		Long confirmed,
		Long inPreparation,
		Long ready,
		Long completed,
		Long canceled,
		BigDecimal completedRevenue,
		BigDecimal averageCompletedOrderValue) {
}
