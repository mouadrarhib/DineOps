package com.mouad.dineops.dineOps.staff.dto;

import jakarta.validation.constraints.NotNull;

public record AssignStaffRequest(
		@NotNull Long userId,
		@NotNull Long branchId,
		@NotNull Long roleId,
		Boolean active) {
}
