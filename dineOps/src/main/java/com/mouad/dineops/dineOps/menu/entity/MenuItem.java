package com.mouad.dineops.dineOps.menu.entity;

import java.math.BigDecimal;

import com.mouad.dineops.dineOps.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "menu_items")
public class MenuItem extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	@ToString.Exclude
	private MenuCategory category;

	@Column(nullable = false, length = 150)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private boolean available = true;

	@Column(name = "preparation_time_minutes")
	private Integer preparationTimeMinutes;

	@Column(nullable = false)
	private boolean active = true;
}
