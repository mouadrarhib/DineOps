package com.mouad.dineops.dineOps.user.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.entity.Role;
import com.mouad.dineops.dineOps.auth.repository.RoleRepository;
import com.mouad.dineops.dineOps.auth.security.PasswordHashService;
import com.mouad.dineops.dineOps.common.enums.SystemRole;
import com.mouad.dineops.dineOps.common.enums.UserStatus;
import com.mouad.dineops.dineOps.common.exception.BadRequestException;
import com.mouad.dineops.dineOps.common.exception.ConflictException;
import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.user.dto.CreateStaffUserRequest;
import com.mouad.dineops.dineOps.user.dto.StaffUserResponse;
import com.mouad.dineops.dineOps.user.entity.User;
import com.mouad.dineops.dineOps.user.repository.UserRepository;

@Service
public class UserManagementService {

	private static final Set<SystemRole> ALLOWED_STAFF_ROLES = Set.of(
			SystemRole.BRANCH_MANAGER,
			SystemRole.CASHIER,
			SystemRole.KITCHEN_STAFF);

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordHashService passwordHashService;

	public UserManagementService(
			UserRepository userRepository,
			RoleRepository roleRepository,
			PasswordHashService passwordHashService) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordHashService = passwordHashService;
	}

	@Transactional
	public StaffUserResponse createStaffUser(CreateStaffUserRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ConflictException("User with this email already exists");
		}

		Set<SystemRole> requestedRoles = request.roles().stream()
				.map(this::parseRole)
				.collect(Collectors.toSet());

		if (requestedRoles.isEmpty()) {
			throw new BadRequestException("At least one staff role is required");
		}

		if (!ALLOWED_STAFF_ROLES.containsAll(requestedRoles)) {
			throw new BadRequestException("Only staff roles are allowed: BRANCH_MANAGER, CASHIER, KITCHEN_STAFF");
		}

		Set<Role> roles = requestedRoles.stream()
				.map(Enum::name)
				.map(this::findRoleByName)
				.collect(Collectors.toSet());

		User user = new User();
		user.setFirstName(request.firstName());
		user.setLastName(request.lastName());
		user.setEmail(request.email());
		user.setPhone(request.phone());
		user.setPasswordHash(passwordHashService.hash(request.password()));
		user.setStatus(UserStatus.ACTIVE);
		user.setRoles(roles);

		User saved = userRepository.save(user);

		Set<String> roleNames = saved.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
		return new StaffUserResponse(
				saved.getId(),
				saved.getFirstName(),
				saved.getLastName(),
				saved.getEmail(),
				saved.getPhone(),
				saved.getStatus(),
				roleNames);
	}

	private SystemRole parseRole(String role) {
		try {
			return SystemRole.valueOf(role);
		} catch (IllegalArgumentException ex) {
			throw new BadRequestException("Unknown role: " + role);
		}
	}

	private Role findRoleByName(String roleName) {
		return roleRepository.findByName(roleName)
				.orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
	}
}
