package com.mouad.dineops.dineOps.branch.dto;

import java.time.LocalTime;

import com.mouad.dineops.dineOps.common.enums.BranchStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBranchRequest(
		@NotNull Long restaurantId,
		@NotBlank @Size(max = 150) String name,
		@NotBlank @Size(max = 255) String address,
		@NotBlank @Size(max = 100) String city,
		@Size(max = 30) String phone,
		LocalTime openingTime,
		LocalTime closingTime,
		BranchStatus status) {
}
