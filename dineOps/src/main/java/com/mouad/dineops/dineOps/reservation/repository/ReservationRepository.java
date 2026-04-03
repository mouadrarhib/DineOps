package com.mouad.dineops.dineOps.reservation.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.common.enums.ReservationStatus;
import com.mouad.dineops.dineOps.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	List<Reservation> findByBranchIdOrderByReservationTimeAsc(Long branchId);

	List<Reservation> findByBranchIdAndStatusOrderByReservationTimeAsc(Long branchId, ReservationStatus status);

	List<Reservation> findByStatusAndReservationTimeBetweenOrderByReservationTimeAsc(
			ReservationStatus status,
			Instant from,
			Instant to);
}
