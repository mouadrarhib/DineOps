package com.mouad.dineops.dineOps.inventory.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(
		name = "inventory_items",
		uniqueConstraints = @UniqueConstraint(columnNames = { "branch_id", "ingredient_id" }))
public class InventoryItem extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "branch_id", nullable = false)
	@ToString.Exclude
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ingredient_id", nullable = false)
	@ToString.Exclude
	private Ingredient ingredient;

	@Column(name = "quantity_available", nullable = false, precision = 12, scale = 3)
	private BigDecimal quantityAvailable;

	@Column(name = "last_restocked_at")
	private Instant lastRestockedAt;
}
