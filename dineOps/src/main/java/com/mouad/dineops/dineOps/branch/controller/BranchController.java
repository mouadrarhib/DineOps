package com.mouad.dineops.dineOps.branch.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.branch.dto.BranchResponse;
import com.mouad.dineops.dineOps.branch.dto.CreateBranchRequest;
import com.mouad.dineops.dineOps.branch.dto.UpdateBranchRequest;
import com.mouad.dineops.dineOps.branch.service.BranchService;
import com.mouad.dineops.dineOps.common.response.ApiResponse;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/branches")
public class BranchController {

	private final BranchService branchService;

	public BranchController(BranchService branchService) {
		this.branchService = branchService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<BranchResponse> createBranch(@Valid @RequestBody CreateBranchRequest request) {
		return ApiResponse.success("Branch created successfully", branchService.createBranch(request));
	}

	@GetMapping
	public ApiResponse<List<BranchResponse>> listBranches(@RequestParam(required = false) Long restaurantId) {
		return ApiResponse.success("Branches fetched successfully", branchService.listBranches(restaurantId));
	}

	@GetMapping("/{branchId}")
	public ApiResponse<BranchResponse> getBranchById(@PathVariable Long branchId) {
		return ApiResponse.success("Branch fetched successfully", branchService.getBranchById(branchId));
	}

	@PutMapping("/{branchId}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<BranchResponse> updateBranch(
			@PathVariable Long branchId,
			@Valid @RequestBody UpdateBranchRequest request) {
		return ApiResponse.success("Branch updated successfully", branchService.updateBranch(branchId, request));
	}

	@PatchMapping("/{branchId}/activate")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<BranchResponse> activateBranch(@PathVariable Long branchId) {
		return ApiResponse.success("Branch activated successfully", branchService.activateBranch(branchId));
	}

	@PatchMapping("/{branchId}/deactivate")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<BranchResponse> deactivateBranch(@PathVariable Long branchId) {
		return ApiResponse.success("Branch deactivated successfully", branchService.deactivateBranch(branchId));
	}
}
