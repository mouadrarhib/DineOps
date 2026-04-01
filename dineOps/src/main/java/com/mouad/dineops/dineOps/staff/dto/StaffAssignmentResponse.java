package com.mouad.dineops.dineOps.staff.dto;

import java.time.Instant;

public record StaffAssignmentResponse(
		Long id,
		Long userId,
		String userEmail,
		Long branchId,
		String branchName,
		Long roleId,
		String roleName,
		boolean active,
		Instant createdAt) {
}
