package com.mouad.dineops.dineOps.staff.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.staff.dto.AssignStaffRequest;
import com.mouad.dineops.dineOps.staff.dto.StaffAssignmentResponse;
import com.mouad.dineops.dineOps.staff.service.StaffAssignmentService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/staff-assignments")
public class StaffAssignmentController {

	private final StaffAssignmentService staffAssignmentService;

	public StaffAssignmentController(StaffAssignmentService staffAssignmentService) {
		this.staffAssignmentService = staffAssignmentService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<StaffAssignmentResponse> assignStaff(@Valid @RequestBody AssignStaffRequest request) {
		return ApiResponse.success(
				"Staff assigned to branch successfully",
				staffAssignmentService.assignStaffToBranch(request));
	}
}
