package com.mouad.dineops.dineOps.auth.bootstrap;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.entity.Role;
import com.mouad.dineops.dineOps.auth.repository.RoleRepository;
import com.mouad.dineops.dineOps.auth.security.PasswordHashService;
import com.mouad.dineops.dineOps.common.enums.UserStatus;
import com.mouad.dineops.dineOps.user.entity.User;
import com.mouad.dineops.dineOps.user.repository.UserRepository;

@Component
public class SuperAdminStartupSeed implements CommandLineRunner {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordHashService passwordHashService;
	private final String adminFirstName;
	private final String adminLastName;
	private final String adminEmail;
	private final String adminPhone;
	private final String adminPassword;

	public SuperAdminStartupSeed(
			UserRepository userRepository,
			RoleRepository roleRepository,
			PasswordHashService passwordHashService,
			@Value("${app.bootstrap.super-admin.first-name:Super}") String adminFirstName,
			@Value("${app.bootstrap.super-admin.last-name:Admin}") String adminLastName,
			@Value("${app.bootstrap.super-admin.email:super.admin@dineops.local}") String adminEmail,
			@Value("${app.bootstrap.super-admin.phone:+212600000001}") String adminPhone,
			@Value("${app.bootstrap.super-admin.password:Admin@123}") String adminPassword) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordHashService = passwordHashService;
		this.adminFirstName = adminFirstName;
		this.adminLastName = adminLastName;
		this.adminEmail = adminEmail;
		this.adminPhone = adminPhone;
		this.adminPassword = adminPassword;
	}

	@Override
	@Transactional
	public void run(String... args) {
		Role superAdminRole = roleRepository.findByName("SUPER_ADMIN").orElseGet(() -> {
			Role role = new Role();
			role.setName("SUPER_ADMIN");
			role.setDescription("System super administrator");
			return roleRepository.save(role);
		});

		if (userRepository.existsByEmail(adminEmail)) {
			return;
		}

		User user = new User();
		user.setFirstName(adminFirstName);
		user.setLastName(adminLastName);
		user.setEmail(adminEmail);
		user.setPhone(adminPhone);
		user.setPasswordHash(passwordHashService.hash(adminPassword));
		user.setStatus(UserStatus.ACTIVE);
		user.setRoles(Set.of(superAdminRole));

		userRepository.save(user);
	}
}
