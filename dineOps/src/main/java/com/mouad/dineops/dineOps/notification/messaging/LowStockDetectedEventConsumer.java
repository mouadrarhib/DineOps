package com.mouad.dineops.dineOps.notification.messaging;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mouad.dineops.dineOps.notification.event.LowStockDetectedEvent;
import com.mouad.dineops.dineOps.notification.service.NotificationService;
import com.mouad.dineops.dineOps.staff.entity.StaffAssignment;
import com.mouad.dineops.dineOps.staff.repository.StaffAssignmentRepository;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
public class LowStockDetectedEventConsumer {

	private final StaffAssignmentRepository staffAssignmentRepository;
	private final NotificationService notificationService;

	public LowStockDetectedEventConsumer(
			StaffAssignmentRepository staffAssignmentRepository,
			NotificationService notificationService) {
		this.staffAssignmentRepository = staffAssignmentRepository;
		this.notificationService = notificationService;
	}

	@RabbitListener(queues = "${app.messaging.events.low-stock-detected.queue:dineops.events.low-stock-detected.queue}")
	public void onLowStockDetected(LowStockDetectedEvent event) {
		List<StaffAssignment> managers = staffAssignmentRepository.findActiveByBranchIdAndRoleNameWithUser(
				event.branchId(),
				"BRANCH_MANAGER");

		String message = "Low stock alert: " + event.ingredientName()
				+ " is at " + event.quantityAvailable() + " " + event.unit();
		for (StaffAssignment manager : managers) {
			notificationService.sendInternal(
					manager.getUser().getEmail(),
					"Low stock alert",
					message,
					"LOW_STOCK",
					event.inventoryItemId());
		}
	}
}
