package com.mouad.dineops.dineOps.reporting.dto;

import java.time.LocalDate;
import java.util.List;

public record BranchPerformanceReportResponse(
		LocalDate fromDate,
		LocalDate toDate,
		List<BranchPerformanceRow> branches) {
}
