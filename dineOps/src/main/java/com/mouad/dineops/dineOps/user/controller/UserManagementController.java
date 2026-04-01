package com.mouad.dineops.dineOps.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.user.dto.CreateStaffUserRequest;
import com.mouad.dineops.dineOps.user.dto.StaffUserResponse;
import com.mouad.dineops.dineOps.user.service.UserManagementService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserManagementController {

	private final UserManagementService userManagementService;

	public UserManagementController(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	@PostMapping("/staff")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<StaffUserResponse> createStaff(@Valid @RequestBody CreateStaffUserRequest request) {
		return ApiResponse.success("Staff user created successfully", userManagementService.createStaffUser(request));
	}
}
