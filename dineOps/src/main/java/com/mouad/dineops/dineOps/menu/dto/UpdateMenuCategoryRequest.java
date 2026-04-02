package com.mouad.dineops.dineOps.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMenuCategoryRequest(
		@NotBlank @Size(max = 120) String name,
		@Size(max = 255) String description,
		Integer displayOrder,
		Boolean active) {
}
