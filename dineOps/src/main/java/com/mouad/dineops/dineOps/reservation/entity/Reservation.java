package com.mouad.dineops.dineOps.reservation.entity;

import java.time.Instant;

import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.common.entity.BaseEntity;
import com.mouad.dineops.dineOps.common.enums.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "reservations")
public class Reservation extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "branch_id", nullable = false)
	@ToString.Exclude
	private Branch branch;

	@Column(name = "customer_name", nullable = false, length = 150)
	private String customerName;

	@Column(name = "customer_phone", nullable = false, length = 30)
	private String customerPhone;

	@Column(name = "customer_email", length = 150)
	private String customerEmail;

	@Column(name = "reservation_time", nullable = false)
	private Instant reservationTime;

	@Column(name = "number_of_guests", nullable = false)
	private Integer numberOfGuests;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ReservationStatus status = ReservationStatus.PENDING;

	@Column(length = 255)
	private String notes;
}
