package com.mouad.dineops.dineOps.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	@EntityGraph(attributePaths = {"roles", "roles.permissions"})
	Optional<User> findWithRolesAndPermissionsByEmail(String email);

	boolean existsByEmail(String email);
}
