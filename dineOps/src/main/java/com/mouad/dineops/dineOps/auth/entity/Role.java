package com.mouad.dineops.dineOps.auth.entity;

import java.util.HashSet;
import java.util.Set;

import com.mouad.dineops.dineOps.common.entity.BaseEntity;
import com.mouad.dineops.dineOps.user.entity.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(length = 255)
	private String description;

	@ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
	@ToString.Exclude
	private Set<User> users = new HashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = "role_permissions",
			joinColumns = @JoinColumn(name = "role_id"),
			inverseJoinColumns = @JoinColumn(name = "permission_id"),
			uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id"}))
	@ToString.Exclude
	private Set<Permission> permissions = new HashSet<>();
}
