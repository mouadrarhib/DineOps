package com.mouad.dineops.dineOps.menu.entity;

import java.math.BigDecimal;

import com.mouad.dineops.dineOps.common.entity.BaseEntity;
import com.mouad.dineops.dineOps.inventory.entity.Ingredient;

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
		name = "menu_item_ingredients",
		uniqueConstraints = @UniqueConstraint(columnNames = { "menu_item_id", "ingredient_id" }))
public class MenuItemIngredient extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "menu_item_id", nullable = false)
	@ToString.Exclude
	private MenuItem menuItem;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ingredient_id", nullable = false)
	@ToString.Exclude
	private Ingredient ingredient;

	@Column(name = "quantity_required", nullable = false, precision = 12, scale = 3)
	private BigDecimal quantityRequired;
}
