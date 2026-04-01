package com.mouad.dineops.dineOps.auth.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mouad.dineops.dineOps.common.enums.UserStatus;
import com.mouad.dineops.dineOps.common.exception.UnauthorizedException;
import com.mouad.dineops.dineOps.user.entity.User;
import com.mouad.dineops.dineOps.user.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findWithRolesAndPermissionsByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new UnauthorizedException("User account is not active");
		}

		Set<String> roles = user.getRoles().stream()
				.map(role -> role.getName())
				.collect(Collectors.toSet());

		Set<String> permissions = user.getRoles().stream()
				.flatMap(role -> role.getPermissions().stream())
				.map(permission -> permission.getName())
				.collect(Collectors.toSet());

		Set<GrantedAuthority> authorities = roles.stream()
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toSet());

		authorities.addAll(permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()));

		return new AppUserPrincipal(
				user.getId(),
				user.getFirstName(),
				user.getLastName(),
				user.getEmail(),
				user.getPhone(),
				user.getPasswordHash(),
				user.getStatus(),
				roles,
				permissions,
				authorities);
	}
}
