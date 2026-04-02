package com.mouad.dineops.dineOps.inventory.entity;

import java.math.BigDecimal;

import com.mouad.dineops.dineOps.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "ingredients")
public class Ingredient extends BaseEntity {

	@Column(nullable = false, unique = true, length = 150)
	private String name;

	@Column(nullable = false, length = 30)
	private String unit;

	@Column(name = "min_threshold", precision = 12, scale = 3)
	private BigDecimal minThreshold;

	@Column(nullable = false)
	private boolean active = true;
}
