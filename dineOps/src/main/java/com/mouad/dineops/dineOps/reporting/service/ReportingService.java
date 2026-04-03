package com.mouad.dineops.dineOps.reporting.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.common.enums.OrderStatus;
import com.mouad.dineops.dineOps.common.exception.BadRequestException;
import com.mouad.dineops.dineOps.order.entity.CustomerOrder;
import com.mouad.dineops.dineOps.order.repository.CustomerOrderRepository;
import com.mouad.dineops.dineOps.reporting.dto.BranchPerformanceReportResponse;
import com.mouad.dineops.dineOps.reporting.dto.BranchPerformanceRow;
import com.mouad.dineops.dineOps.reporting.dto.DailySalesReportRow;
import com.mouad.dineops.dineOps.reporting.dto.OrderSummaryReportResponse;

@Service
public class ReportingService {

	private final CustomerOrderRepository customerOrderRepository;

	public ReportingService(CustomerOrderRepository customerOrderRepository) {
		this.customerOrderRepository = customerOrderRepository;
	}

	@Transactional(readOnly = true)
	public List<DailySalesReportRow> getDailySalesReport(LocalDate fromDate, LocalDate toDate, Long branchId) {
		validateDateRange(fromDate, toDate);
		Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant toExclusive = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

		List<CustomerOrder> completedOrders = branchId == null
				? customerOrderRepository.findByStatusAndCompletedAtBetween(OrderStatus.COMPLETED, from, toExclusive)
				: customerOrderRepository.findByBranchIdAndStatusAndCompletedAtBetween(branchId, OrderStatus.COMPLETED, from, toExclusive);

		Map<String, DailySalesAccumulator> acc = new HashMap<>();
		for (CustomerOrder order : completedOrders) {
			LocalDate date = order.getCompletedAt().atZone(ZoneOffset.UTC).toLocalDate();
			Long bId = order.getBranch().getId();
			String key = date + "#" + bId;
			DailySalesAccumulator row = acc.computeIfAbsent(
					key,
					ignored -> new DailySalesAccumulator(date, bId, order.getBranch().getName()));
			row.completedOrders += 1;
			row.subtotal = row.subtotal.add(order.getSubtotal());
			row.tax = row.tax.add(order.getTaxAmount());
			row.total = row.total.add(order.getTotalAmount());
		}

		return acc.values().stream()
				.map(DailySalesAccumulator::toRow)
				.sorted(Comparator.comparing(DailySalesReportRow::date).thenComparing(DailySalesReportRow::branchId))
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderSummaryReportResponse getOrderSummaryReport(LocalDate fromDate, LocalDate toDate, Long branchId) {
		validateDateRange(fromDate, toDate);
		Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant toExclusive = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

		List<CustomerOrder> orders = branchId == null
				? customerOrderRepository.findByCreatedAtBetween(from, toExclusive)
				: customerOrderRepository.findByBranchIdAndCreatedAtBetween(branchId, from, toExclusive);

		long pending = 0;
		long confirmed = 0;
		long inPreparation = 0;
		long ready = 0;
		long completed = 0;
		long canceled = 0;
		BigDecimal completedRevenue = BigDecimal.ZERO;

		for (CustomerOrder order : orders) {
			switch (order.getStatus()) {
				case PENDING -> pending++;
				case CONFIRMED -> confirmed++;
				case IN_PREPARATION -> inPreparation++;
				case READY -> ready++;
				case COMPLETED -> {
					completed++;
					completedRevenue = completedRevenue.add(order.getTotalAmount());
				}
				case CANCELED -> canceled++;
			}
		}

		BigDecimal average = completed == 0
				? BigDecimal.ZERO
				: completedRevenue.divide(BigDecimal.valueOf(completed), 2, java.math.RoundingMode.HALF_UP);

		return new OrderSummaryReportResponse(
				fromDate,
				toDate,
				(long) orders.size(),
				pending,
				confirmed,
				inPreparation,
				ready,
				completed,
				canceled,
				completedRevenue,
				average);
	}

	@Transactional(readOnly = true)
	public BranchPerformanceReportResponse getBranchPerformanceReport(LocalDate fromDate, LocalDate toDate) {
		validateDateRange(fromDate, toDate);
		Instant from = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant toExclusive = toDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

		List<CustomerOrder> orders = customerOrderRepository.findByCreatedAtBetween(from, toExclusive);
		Map<Long, BranchPerformanceAccumulator> acc = new HashMap<>();

		for (CustomerOrder order : orders) {
			Long branchId = order.getBranch().getId();
			BranchPerformanceAccumulator row = acc.computeIfAbsent(
					branchId,
					ignored -> new BranchPerformanceAccumulator(branchId, order.getBranch().getName()));
			row.totalOrders += 1;
			if (order.getStatus() == OrderStatus.COMPLETED) {
				row.completedOrders += 1;
				row.completedRevenue = row.completedRevenue.add(order.getTotalAmount());
			}
			if (order.getStatus() == OrderStatus.CANCELED) {
				row.canceledOrders += 1;
			}
		}

		List<BranchPerformanceRow> rows = acc.values().stream()
				.map(BranchPerformanceAccumulator::toRow)
				.sorted(Comparator.comparing(BranchPerformanceRow::completedRevenue).reversed())
				.toList();

		return new BranchPerformanceReportResponse(fromDate, toDate, rows);
	}

	private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
		if (fromDate == null || toDate == null) {
			throw new BadRequestException("Both fromDate and toDate are required");
		}
		if (toDate.isBefore(fromDate)) {
			throw new BadRequestException("toDate must be after or equal to fromDate");
		}
	}

	private static class DailySalesAccumulator {
		private final LocalDate date;
		private final Long branchId;
		private final String branchName;
		private long completedOrders = 0;
		private BigDecimal subtotal = BigDecimal.ZERO;
		private BigDecimal tax = BigDecimal.ZERO;
		private BigDecimal total = BigDecimal.ZERO;

		private DailySalesAccumulator(LocalDate date, Long branchId, String branchName) {
			this.date = date;
			this.branchId = branchId;
			this.branchName = branchName;
		}

		private DailySalesReportRow toRow() {
			return new DailySalesReportRow(date, branchId, branchName, completedOrders, subtotal, tax, total);
		}
	}

	private static class BranchPerformanceAccumulator {
		private final Long branchId;
		private final String branchName;
		private long totalOrders = 0;
		private long completedOrders = 0;
		private long canceledOrders = 0;
		private BigDecimal completedRevenue = BigDecimal.ZERO;

		private BranchPerformanceAccumulator(Long branchId, String branchName) {
			this.branchId = branchId;
			this.branchName = branchName;
		}

		private BranchPerformanceRow toRow() {
			return new BranchPerformanceRow(branchId, branchName, totalOrders, completedOrders, canceledOrders, completedRevenue);
		}
	}
}
