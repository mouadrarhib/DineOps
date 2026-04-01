package com.mouad.dineops.dineOps.auth.dto;

import java.util.Set;

import com.mouad.dineops.dineOps.common.enums.UserStatus;

public record CurrentUserResponse(
		Long id,
		String firstName,
		String lastName,
		String email,
		String phone,
		UserStatus status,
		Set<String> roles,
		Set<String> permissions) {
}
