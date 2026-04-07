package com.mouad.dineops.dineOps.notification.event;

import java.time.Instant;

public record ReservationApprovedEvent(
		Long reservationId,
		String customerName,
		String customerEmail,
		Instant reservationTime) {
}
