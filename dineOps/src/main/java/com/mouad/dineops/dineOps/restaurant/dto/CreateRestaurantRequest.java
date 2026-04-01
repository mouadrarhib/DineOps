package com.mouad.dineops.dineOps.restaurant.dto;

import com.mouad.dineops.dineOps.common.enums.RestaurantStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRestaurantRequest(
		@NotBlank @Size(max = 150) String name,
		@Size(max = 200) String legalName,
		@Size(max = 100) String taxId,
		@Email @Size(max = 150) String email,
		@Size(max = 30) String phone,
		RestaurantStatus status) {
}
