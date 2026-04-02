package com.mouad.dineops.dineOps.inventory.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.mouad.dineops.dineOps.branch.entity.Branch;
import com.mouad.dineops.dineOps.common.enums.InventoryMovementType;
import com.mouad.dineops.dineOps.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "branch_id", nullable = false)
	@ToString.Exclude
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ingredient_id", nullable = false)
	@ToString.Exclude
	private Ingredient ingredient;

	@Enumerated(EnumType.STRING)
	@Column(name = "movement_type", nullable = false, length = 30)
	private InventoryMovementType movementType;

	@Column(name = "quantity_changed", nullable = false, precision = 12, scale = 3)
	private BigDecimal quantityChanged;

	@Column(name = "reference_type", length = 50)
	private String referenceType;

	@Column(name = "reference_id")
	private Long referenceId;

	@Column(length = 255)
	private String notes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	@ToString.Exclude
	private User createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
	}
}
