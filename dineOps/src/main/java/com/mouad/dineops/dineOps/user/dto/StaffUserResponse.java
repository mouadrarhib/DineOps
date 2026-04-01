package com.mouad.dineops.dineOps.user.dto;

import java.util.Set;

import com.mouad.dineops.dineOps.common.enums.UserStatus;

public record StaffUserResponse(
		Long id,
		String firstName,
		String lastName,
		String email,
		String phone,
		UserStatus status,
		Set<String> roles) {
}
