package com.mouad.dineops.dineOps.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.auth.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByName(String name);

	boolean existsByName(String name);
}
