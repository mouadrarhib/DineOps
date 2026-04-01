package com.mouad.dineops.dineOps.auth.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.auth.dto.AuthTokensResponse;
import com.mouad.dineops.dineOps.auth.dto.CurrentUserResponse;
import com.mouad.dineops.dineOps.auth.dto.SeedDataResponse;
import com.mouad.dineops.dineOps.auth.entity.Permission;
import com.mouad.dineops.dineOps.auth.entity.RefreshToken;
import com.mouad.dineops.dineOps.auth.entity.Role;
import com.mouad.dineops.dineOps.auth.repository.PermissionRepository;
import com.mouad.dineops.dineOps.auth.repository.RefreshTokenRepository;
import com.mouad.dineops.dineOps.auth.repository.RoleRepository;
import com.mouad.dineops.dineOps.auth.security.AppUserPrincipal;
import com.mouad.dineops.dineOps.auth.security.JwtTokenService;
import com.mouad.dineops.dineOps.auth.security.PasswordHashService;
import com.mouad.dineops.dineOps.common.enums.UserStatus;
import com.mouad.dineops.dineOps.common.exception.UnauthorizedException;
import com.mouad.dineops.dineOps.user.entity.User;
import com.mouad.dineops.dineOps.user.repository.UserRepository;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenService jwtTokenService;
	private final RefreshTokenRepository refreshTokenRepository;
	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final PasswordHashService passwordHashService;
	private final UserRepository userRepository;

	public AuthService(
			AuthenticationManager authenticationManager,
			JwtTokenService jwtTokenService,
			RefreshTokenRepository refreshTokenRepository,
			RoleRepository roleRepository,
			PermissionRepository permissionRepository,
			PasswordHashService passwordHashService,
			UserRepository userRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenService = jwtTokenService;
		this.refreshTokenRepository = refreshTokenRepository;
		this.roleRepository = roleRepository;
		this.permissionRepository = permissionRepository;
		this.passwordHashService = passwordHashService;
		this.userRepository = userRepository;
	}

	@Transactional
	public SeedDataResponse seedVirtualAuthData() {
		List<String> permissionNames = List.of(
				"CREATE_BRANCH",
				"UPDATE_MENU",
				"VIEW_ALL_BRANCHES",
				"MANAGE_INVENTORY",
				"VIEW_REPORTS");

		Map<String, Set<String>> roleToPermissions = new LinkedHashMap<>();
		roleToPermissions.put("SUPER_ADMIN", Set.of("CREATE_BRANCH", "UPDATE_MENU", "VIEW_ALL_BRANCHES", "MANAGE_INVENTORY", "VIEW_REPORTS"));
		roleToPermissions.put("RESTAURANT_OWNER", Set.of("CREATE_BRANCH", "UPDATE_MENU", "VIEW_ALL_BRANCHES", "VIEW_REPORTS"));
		roleToPermissions.put("BRANCH_MANAGER", Set.of("UPDATE_MENU", "MANAGE_INVENTORY", "VIEW_REPORTS"));
		roleToPermissions.put("CASHIER", Set.of("VIEW_ALL_BRANCHES"));
		roleToPermissions.put("KITCHEN_STAFF", Set.of("MANAGE_INVENTORY"));

		Map<String, Permission> permissionsByName = permissionNames.stream()
				.map(this::findOrCreatePermission)
				.collect(Collectors.toMap(Permission::getName, permission -> permission));

		Map<String, Role> rolesByName = roleToPermissions.entrySet().stream()
				.map(entry -> findOrCreateRole(entry.getKey(), entry.getValue(), permissionsByName))
				.collect(Collectors.toMap(Role::getName, role -> role));

		createUserIfMissing("Super", "Admin", "super.admin@dineops.local", "+212600000001", "Admin@123", rolesByName.get("SUPER_ADMIN"));
		createUserIfMissing("Restaurant", "Owner", "owner@dineops.local", "+212600000002", "Owner@123", rolesByName.get("RESTAURANT_OWNER"));
		createUserIfMissing("Branch", "Manager", "manager@dineops.local", "+212600000003", "Manager@123", rolesByName.get("BRANCH_MANAGER"));
		createUserIfMissing("Cash", "ier", "cashier@dineops.local", "+212600000004", "Cashier@123", rolesByName.get("CASHIER"));
		createUserIfMissing("Kitchen", "Staff", "kitchen@dineops.local", "+212600000005", "Kitchen@123", rolesByName.get("KITCHEN_STAFF"));

		return new SeedDataResponse(
				List.copyOf(rolesByName.keySet()),
				permissionNames,
				List.of(
						"super.admin@dineops.local",
						"owner@dineops.local",
						"manager@dineops.local",
						"cashier@dineops.local",
						"kitchen@dineops.local"));
	}

	@Transactional
	public AuthTokensResponse login(String email, String password) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(email, password));
		AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();

		String accessToken = jwtTokenService.generateAccessToken(principal);
		String refreshTokenValue = jwtTokenService.generateRefreshToken(principal);
		Instant accessExpiresAt = jwtTokenService.extractExpiration(accessToken);
		Instant refreshExpiresAt = jwtTokenService.extractExpiration(refreshTokenValue);
		User user = userRepository.findByEmail(principal.getEmail())
				.orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setToken(refreshTokenValue);
		refreshToken.setExpiresAt(refreshExpiresAt);
		refreshToken.setRevoked(false);
		refreshTokenRepository.save(refreshToken);

		return new AuthTokensResponse("Bearer", accessToken, refreshTokenValue, accessExpiresAt, refreshExpiresAt);
	}

	@Transactional
	public AuthTokensResponse refresh(String refreshTokenValue) {
		RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
				.orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

		if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(Instant.now())) {
			throw new UnauthorizedException("Refresh token expired or revoked");
		}

		if (!jwtTokenService.isTokenValid(refreshTokenValue, "refresh")) {
			throw new UnauthorizedException("Invalid refresh token");
		}

		String tokenSubject = jwtTokenService.extractSubject(refreshTokenValue);
		if (!storedToken.getUser().getEmail().equals(tokenSubject)) {
			throw new UnauthorizedException("Refresh token does not match user");
		}

		AppUserPrincipal principal = new AppUserPrincipal(
				storedToken.getUser().getId(),
				storedToken.getUser().getFirstName(),
				storedToken.getUser().getLastName(),
				storedToken.getUser().getEmail(),
				storedToken.getUser().getPhone(),
				storedToken.getUser().getPasswordHash(),
				storedToken.getUser().getStatus(),
				storedToken.getUser().getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet()),
				storedToken.getUser().getRoles().stream()
						.flatMap(role -> role.getPermissions().stream())
						.map(permission -> permission.getName())
						.collect(java.util.stream.Collectors.toSet()),
				java.util.Set.of());

		String newAccessToken = jwtTokenService.generateAccessToken(principal);
		String newRefreshToken = jwtTokenService.generateRefreshToken(principal);
		Instant accessExpiresAt = jwtTokenService.extractExpiration(newAccessToken);
		Instant refreshExpiresAt = jwtTokenService.extractExpiration(newRefreshToken);

		storedToken.setRevoked(true);
		refreshTokenRepository.save(storedToken);

		RefreshToken rotatedToken = new RefreshToken();
		rotatedToken.setUser(storedToken.getUser());
		rotatedToken.setToken(newRefreshToken);
		rotatedToken.setExpiresAt(refreshExpiresAt);
		rotatedToken.setRevoked(false);
		refreshTokenRepository.save(rotatedToken);

		return new AuthTokensResponse("Bearer", newAccessToken, newRefreshToken, accessExpiresAt, refreshExpiresAt);
	}

	@Transactional
	public void logout(String refreshTokenValue) {
		RefreshToken token = refreshTokenRepository.findByToken(refreshTokenValue)
				.orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
		token.setRevoked(true);
		refreshTokenRepository.save(token);
	}

	public CurrentUserResponse currentUser(AppUserPrincipal principal) {
		return new CurrentUserResponse(
				principal.getId(),
				principal.getFirstName(),
				principal.getLastName(),
				principal.getEmail(),
				principal.getPhone(),
				principal.getStatus(),
				principal.getRoles(),
				principal.getPermissions());
	}

	private Permission findOrCreatePermission(String name) {
		return permissionRepository.findByName(name).orElseGet(() -> {
			Permission permission = new Permission();
			permission.setName(name);
			permission.setDescription(name.replace('_', ' '));
			return permissionRepository.save(permission);
		});
	}

	private Role findOrCreateRole(String roleName, Set<String> permissionNames, Map<String, Permission> permissionsByName) {
		Role role = roleRepository.findByName(roleName).orElseGet(() -> {
			Role created = new Role();
			created.setName(roleName);
			created.setDescription(roleName.replace('_', ' '));
			return roleRepository.save(created);
		});

		Set<Permission> assignedPermissions = permissionNames.stream()
				.map(permissionsByName::get)
				.collect(Collectors.toSet());
		role.setPermissions(assignedPermissions);
		return roleRepository.save(role);
	}

	private void createUserIfMissing(
			String firstName,
			String lastName,
			String email,
			String phone,
			String rawPassword,
			Role role) {
		if (userRepository.existsByEmail(email)) {
			return;
		}

		User user = new User();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);
		user.setPhone(phone);
		user.setPasswordHash(passwordHashService.hash(rawPassword));
		user.setStatus(UserStatus.ACTIVE);
		user.setRoles(Set.of(role));
		userRepository.save(user);
	}
}
