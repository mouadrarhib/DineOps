package com.mouad.dineops.dineOps.branch.dto;

import java.time.Instant;
import java.time.LocalTime;

import com.mouad.dineops.dineOps.common.enums.BranchStatus;

public record BranchResponse(
		Long id,
		Long restaurantId,
		String name,
		String address,
		String city,
		String phone,
		LocalTime openingTime,
		LocalTime closingTime,
		BranchStatus status,
		Instant createdAt,
		Instant updatedAt) {
}
