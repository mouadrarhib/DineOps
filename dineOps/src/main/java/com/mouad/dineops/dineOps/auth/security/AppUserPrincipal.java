package com.mouad.dineops.dineOps.auth.security;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mouad.dineops.dineOps.common.enums.UserStatus;

public class AppUserPrincipal implements UserDetails {

	private final Long id;
	private final String firstName;
	private final String lastName;
	private final String email;
	private final String phone;
	private final String passwordHash;
	private final UserStatus status;
	private final Set<String> roles;
	private final Set<String> permissions;
	private final Collection<? extends GrantedAuthority> authorities;

	public AppUserPrincipal(
			Long id,
			String firstName,
			String lastName,
			String email,
			String phone,
			String passwordHash,
			UserStatus status,
			Set<String> roles,
			Set<String> permissions,
			Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
		this.passwordHash = passwordHash;
		this.status = status;
		this.roles = roles;
		this.permissions = permissions;
		this.authorities = authorities;
	}

	public Long getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public UserStatus getStatus() {
		return status;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isEnabled() {
		return status == UserStatus.ACTIVE;
	}
}
