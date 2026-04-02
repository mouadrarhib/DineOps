package com.mouad.dineops.dineOps.inventory.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIngredientRequest(
		@NotBlank @Size(max = 150) String name,
		@NotBlank @Size(max = 30) String unit,
		@DecimalMin(value = "0.0", inclusive = false) BigDecimal minThreshold,
		Boolean active) {
}
