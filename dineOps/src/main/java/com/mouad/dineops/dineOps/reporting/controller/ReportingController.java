package com.mouad.dineops.dineOps.reporting.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.reporting.dto.BranchPerformanceReportResponse;
import com.mouad.dineops.dineOps.reporting.dto.DailySalesReportRow;
import com.mouad.dineops.dineOps.reporting.dto.OrderSummaryReportResponse;
import com.mouad.dineops.dineOps.reporting.service.ReportingService;

@Validated
@RestController
@RequestMapping("/api/reports")
public class ReportingController {

	private final ReportingService reportingService;

	public ReportingController(ReportingService reportingService) {
		this.reportingService = reportingService;
	}

	@GetMapping("/daily-sales")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<List<DailySalesReportRow>> dailySales(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam(required = false) Long branchId) {
		return ApiResponse.success(
				"Daily sales report fetched successfully",
				reportingService.getDailySalesReport(fromDate, toDate, branchId));
	}

	@GetMapping("/order-summary")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<OrderSummaryReportResponse> orderSummary(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam(required = false) Long branchId) {
		return ApiResponse.success(
				"Order summary report fetched successfully",
				reportingService.getOrderSummaryReport(fromDate, toDate, branchId));
	}

	@GetMapping("/branch-performance")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<BranchPerformanceReportResponse> branchPerformance(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		return ApiResponse.success(
				"Branch performance report fetched successfully",
				reportingService.getBranchPerformanceReport(fromDate, toDate));
	}
}
