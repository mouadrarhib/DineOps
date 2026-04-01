package com.mouad.dineops.dineOps.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.auth.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

	Optional<Permission> findByName(String name);

	boolean existsByName(String name);
}
