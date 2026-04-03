package com.mouad.dineops.dineOps.reservation.dto;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReservationRequest(
		@NotNull Long branchId,
		@NotBlank @Size(max = 150) String customerName,
		@NotBlank @Size(max = 30) String customerPhone,
		@Email @Size(max = 150) String customerEmail,
		@NotNull Instant reservationTime,
		@NotNull @Min(1) Integer numberOfGuests,
		@Size(max = 255) String notes) {
}
