package com.mouad.dineops.dineOps.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMenuCategoryRequest(
		@NotNull Long branchId,
		@NotBlank @Size(max = 120) String name,
		@Size(max = 255) String description,
		Integer displayOrder,
		Boolean active) {
}
