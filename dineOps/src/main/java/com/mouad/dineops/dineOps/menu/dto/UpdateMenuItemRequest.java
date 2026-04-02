package com.mouad.dineops.dineOps.menu.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateMenuItemRequest(
		@NotNull Long categoryId,
		@NotBlank @Size(max = 150) String name,
		String description,
		@NotNull @DecimalMin(value = "0.01") BigDecimal price,
		Boolean available,
		@Positive Integer preparationTimeMinutes,
		Boolean active) {
}
