package com.mouad.dineops.dineOps.restaurant.dto;

import java.time.Instant;

import com.mouad.dineops.dineOps.common.enums.RestaurantStatus;

public record RestaurantResponse(
		Long id,
		String name,
		String legalName,
		String taxId,
		String email,
		String phone,
		RestaurantStatus status,
		Instant createdAt,
		Instant updatedAt) {
}
