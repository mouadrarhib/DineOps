package com.mouad.dineops.dineOps.user.dto;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateStaffUserRequest(
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@Email @NotBlank @Size(max = 150) String email,
		@Size(max = 30) String phone,
		@NotBlank @Size(min = 8, max = 100) String password,
		@NotEmpty Set<String> roles) {
}
