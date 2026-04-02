package com.mouad.dineops.dineOps.menu.entity;

import com.mouad.dineops.dineOps.branch.entity.Branch;
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
@Table(name = "menu_categories")
public class MenuCategory extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "branch_id", nullable = false)
	@ToString.Exclude
	private Branch branch;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(length = 255)
	private String description;

	@Column(name = "display_order")
	private Integer displayOrder;

	@Column(nullable = false)
	private boolean active = true;
}
