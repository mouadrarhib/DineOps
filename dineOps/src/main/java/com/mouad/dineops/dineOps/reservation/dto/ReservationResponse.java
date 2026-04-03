package com.mouad.dineops.dineOps.reservation.dto;

import java.time.Instant;

import com.mouad.dineops.dineOps.common.enums.ReservationStatus;

public record ReservationResponse(
		Long id,
		Long branchId,
		String customerName,
		String customerPhone,
		String customerEmail,
		Instant reservationTime,
		Integer numberOfGuests,
		ReservationStatus status,
		String notes,
		Instant createdAt,
		Instant updatedAt) {
}
