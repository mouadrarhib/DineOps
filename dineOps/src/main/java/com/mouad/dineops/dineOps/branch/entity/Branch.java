package com.mouad.dineops.dineOps.branch.entity;

import java.time.LocalTime;

import com.mouad.dineops.dineOps.common.entity.BaseEntity;
import com.mouad.dineops.dineOps.common.enums.BranchStatus;
import com.mouad.dineops.dineOps.restaurant.entity.Restaurant;

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
@Table(name = "branches")
public class Branch extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "restaurant_id", nullable = false)
	@ToString.Exclude
	private Restaurant restaurant;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(nullable = false, length = 255)
	private String address;

	@Column(nullable = false, length = 100)
	private String city;

	@Column(length = 30)
	private String phone;

	@Column(name = "opening_time")
	private LocalTime openingTime;

	@Column(name = "closing_time")
	private LocalTime closingTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private BranchStatus status = BranchStatus.ACTIVE;
}
