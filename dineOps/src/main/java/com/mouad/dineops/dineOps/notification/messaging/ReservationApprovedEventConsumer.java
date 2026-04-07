package com.mouad.dineops.dineOps.notification.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mouad.dineops.dineOps.notification.event.ReservationApprovedEvent;
import com.mouad.dineops.dineOps.notification.service.NotificationService;

@Component
@ConditionalOnProperty(prefix = "app.messaging.rabbit", name = "enabled", havingValue = "true")
public class ReservationApprovedEventConsumer {

	private final NotificationService notificationService;

	public ReservationApprovedEventConsumer(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@RabbitListener(queues = "${app.messaging.events.reservation-approved.queue:dineops.events.reservation-approved.queue}")
	public void onReservationApproved(ReservationApprovedEvent event) {
		notificationService.sendInternal(
				"reservation:" + event.reservationId(),
				"Reservation approved",
				"Reservation for " + event.customerName() + " has been approved.",
				"RESERVATION",
				event.reservationId());

		if (event.customerEmail() != null && !event.customerEmail().isBlank()) {
			notificationService.sendEmail(
					event.customerEmail(),
					"Your reservation is approved",
					"Hello " + event.customerName() + ", your reservation at " + event.reservationTime()
							+ " has been approved.",
					"RESERVATION",
					event.reservationId());
		}
	}
}
