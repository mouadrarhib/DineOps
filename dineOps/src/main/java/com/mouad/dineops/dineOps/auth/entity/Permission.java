package com.mouad.dineops.dineOps.auth.entity;

import java.util.HashSet;
import java.util.Set;

import com.mouad.dineops.dineOps.common.entity.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(length = 255)
	private String description;

	@ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
	@ToString.Exclude
	private Set<Role> roles = new HashSet<>();
}
