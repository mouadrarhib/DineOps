package com.mouad.dineops.dineOps.reservation.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.enums.ReservationStatus;
import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.reservation.dto.CreateReservationRequest;
import com.mouad.dineops.dineOps.reservation.dto.ReservationResponse;
import com.mouad.dineops.dineOps.reservation.service.ReservationService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER','CASHIER')")
	public ApiResponse<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
		return ApiResponse.success("Reservation created successfully", reservationService.createReservation(request));
	}

	@GetMapping
	public ApiResponse<List<ReservationResponse>> listReservations(
			@RequestParam Long branchId,
			@RequestParam(required = false) ReservationStatus status) {
		return ApiResponse.success("Reservations fetched successfully", reservationService.listReservations(branchId, status));
	}

	@PatchMapping("/{reservationId}/approve")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<ReservationResponse> approveReservation(@PathVariable Long reservationId) {
		return ApiResponse.success("Reservation approved successfully", reservationService.approveReservation(reservationId));
	}

	@PatchMapping("/{reservationId}/reject")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<ReservationResponse> rejectReservation(@PathVariable Long reservationId) {
		return ApiResponse.success("Reservation rejected successfully", reservationService.rejectReservation(reservationId));
	}
}
