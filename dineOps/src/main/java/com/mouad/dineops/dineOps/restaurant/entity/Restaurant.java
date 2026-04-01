package com.mouad.dineops.dineOps.restaurant.entity;

import com.mouad.dineops.dineOps.common.entity.BaseEntity;
import com.mouad.dineops.dineOps.common.enums.RestaurantStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "restaurants")
public class Restaurant extends BaseEntity {

	@Column(nullable = false, length = 150)
	private String name;

	@Column(name = "legal_name", length = 200)
	private String legalName;

	@Column(name = "tax_id", length = 100)
	private String taxId;

	@Column(length = 150)
	private String email;

	@Column(length = 30)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private RestaurantStatus status = RestaurantStatus.ACTIVE;
}
