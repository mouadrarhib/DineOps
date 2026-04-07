package com.mouad.dineops.dineOps.notification.job;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.repository.RefreshTokenRepository;
import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.branch.repository.BranchRepository;
import com.mouad.dineops.dineOps.common.enums.BranchStatus;
import com.mouad.dineops.dineOps.common.enums.NotificationType;
import com.mouad.dineops.dineOps.common.enums.OrderStatus;
import com.mouad.dineops.dineOps.common.enums.ReservationStatus;
import com.mouad.dineops.dineOps.common.messaging.EventPublisher;
import com.mouad.dineops.dineOps.inventory.entity.InventoryItem;
import com.mouad.dineops.dineOps.inventory.repository.InventoryItemRepository;
import com.mouad.dineops.dineOps.notification.repository.NotificationRepository;
import com.mouad.dineops.dineOps.notification.service.NotificationService;
import com.mouad.dineops.dineOps.notification.event.LowStockDetectedEvent;
import com.mouad.dineops.dineOps.order.entity.CustomerOrder;
import com.mouad.dineops.dineOps.order.repository.CustomerOrderRepository;
import com.mouad.dineops.dineOps.reservation.entity.Reservation;
import com.mouad.dineops.dineOps.reservation.repository.ReservationRepository;

@Component
public class OperationsScheduledJobs {

	private static final Logger log = LoggerFactory.getLogger(OperationsScheduledJobs.class);

	private final BranchRepository branchRepository;
	private final InventoryItemRepository inventoryItemRepository;
	private final ReservationRepository reservationRepository;
	private final CustomerOrderRepository customerOrderRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final NotificationRepository notificationRepository;
	private final NotificationService notificationService;
	private final EventPublisher eventPublisher;

	public OperationsScheduledJobs(
			BranchRepository branchRepository,
			InventoryItemRepository inventoryItemRepository,
			ReservationRepository reservationRepository,
			CustomerOrderRepository customerOrderRepository,
			RefreshTokenRepository refreshTokenRepository,
			NotificationRepository notificationRepository,
			NotificationService notificationService,
			EventPublisher eventPublisher) {
		this.branchRepository = branchRepository;
		this.inventoryItemRepository = inventoryItemRepository;
		this.reservationRepository = reservationRepository;
		this.customerOrderRepository = customerOrderRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.notificationRepository = notificationRepository;
		this.notificationService = notificationService;
		this.eventPublisher = eventPublisher;
	}

	@Scheduled(cron = "${app.jobs.low-stock-cron:0 */15 * * * *}")
	@Transactional
	public void lowStockJob() {
		List<Branch> branches = branchRepository.findByStatus(BranchStatus.ACTIVE);
		for (Branch branch : branches) {
			List<InventoryItem> lowStock = inventoryItemRepository.findByBranchIdOrderByIngredientNameAsc(branch.getId())
					.stream()
					.filter(item -> {
						BigDecimal threshold = item.getIngredient().getMinThreshold() == null
								? BigDecimal.ZERO
								: item.getIngredient().getMinThreshold();
						return item.getQuantityAvailable().compareTo(threshold) <= 0;
					})
					.toList();

			for (InventoryItem item : lowStock) {
				eventPublisher.publish(
						"dineops.event.low-stock.detected",
						new LowStockDetectedEvent(
								branch.getId(),
								item.getId(),
								item.getIngredient().getName(),
								item.getQuantityAvailable(),
								item.getIngredient().getUnit()));
			}
		}
	}

	@Scheduled(cron = "${app.jobs.reservation-reminder-cron:0 */10 * * * *}")
	@Transactional
	public void reservationReminderJob() {
		Instant now = Instant.now();
		Instant reminderEdge = now.plusSeconds(60L * 60L);
		List<Reservation> upcoming = reservationRepository
				.findByStatusAndReservationTimeBetweenOrderByReservationTimeAsc(
						ReservationStatus.APPROVED,
						now,
						reminderEdge);

		for (Reservation reservation : upcoming) {
			boolean alreadyNotified = notificationRepository.existsByTypeAndRelatedEntityTypeAndRelatedEntityId(
					NotificationType.EMAIL,
					"RESERVATION_REMINDER",
					reservation.getId());
			if (alreadyNotified) {
				continue;
			}

			String message = "Reminder: your reservation is scheduled at " + reservation.getReservationTime() + ".";
			if (reservation.getCustomerEmail() != null) {
				notificationService.sendEmail(
						reservation.getCustomerEmail(),
						"Reservation reminder",
						message,
						"RESERVATION_REMINDER",
						reservation.getId());
			} else {
				notificationService.sendInternal(
						"reservation:" + reservation.getId(),
						"Reservation reminder",
						message,
						"RESERVATION_REMINDER",
						reservation.getId());
			}
		}
	}

	@Scheduled(cron = "${app.jobs.daily-sales-summary-cron:0 10 0 * * *}")
	@Transactional
	public void dailySalesSummaryJob() {
		LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
		Instant from = todayUtc.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant to = todayUtc.atStartOfDay().toInstant(ZoneOffset.UTC);

		for (Branch branch : branchRepository.findByStatus(BranchStatus.ACTIVE)) {
			List<CustomerOrder> completed = customerOrderRepository.findByBranchIdAndStatusAndCompletedAtBetween(
					branch.getId(),
					OrderStatus.COMPLETED,
					from,
					to);
			if (completed.isEmpty()) {
				continue;
			}

			BigDecimal revenue = completed.stream()
					.map(CustomerOrder::getTotalAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			String summary = "Daily sales summary for branch " + branch.getName()
					+ ": orders=" + completed.size() + ", revenue=" + revenue;

			notificationService.sendInternal(
					"restaurant-owner:" + branch.getRestaurant().getId(),
					"Daily sales summary",
					summary,
					"DAILY_SALES_SUMMARY",
					branch.getId());

			String ownerEmail = branch.getRestaurant().getEmail();
			if (ownerEmail != null && !ownerEmail.isBlank()) {
				notificationService.sendEmail(
						ownerEmail,
						"Daily sales summary",
						summary,
						"DAILY_SALES_SUMMARY",
						branch.getId());
			}
		}
	}

	@Scheduled(cron = "${app.jobs.refresh-token-cleanup-cron:0 0 */6 * * *}")
	@Transactional
	public void refreshTokenCleanupJob() {
		Instant now = Instant.now();
		int expired = refreshTokenRepository.deleteAllExpired(now);
		int revokedExpired = refreshTokenRepository.deleteAllRevokedAndExpired(now);
		log.info("Refresh token cleanup done: expired={}, revokedExpired={}", expired, revokedExpired);
	}

}
