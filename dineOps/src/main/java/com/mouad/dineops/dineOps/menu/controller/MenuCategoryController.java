package com.mouad.dineops.dineOps.menu.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.common.response.PagedResponse;
import com.mouad.dineops.dineOps.menu.dto.CreateMenuCategoryRequest;
import com.mouad.dineops.dineOps.menu.dto.MenuCategoryResponse;
import com.mouad.dineops.dineOps.menu.dto.UpdateMenuCategoryRequest;
import com.mouad.dineops.dineOps.menu.service.MenuCategoryService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/menu-categories")
public class MenuCategoryController {

	private final MenuCategoryService menuCategoryService;

	public MenuCategoryController(MenuCategoryService menuCategoryService) {
		this.menuCategoryService = menuCategoryService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<MenuCategoryResponse> createCategory(@Valid @RequestBody CreateMenuCategoryRequest request) {
		return ApiResponse.success("Menu category created successfully", menuCategoryService.createCategory(request));
	}

	@GetMapping
	public ApiResponse<PagedResponse<MenuCategoryResponse>> listByBranch(
			@RequestParam Long branchId,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ApiResponse.success(
				"Menu categories fetched successfully",
				menuCategoryService.listByBranch(branchId, active, search, page, size));
	}

	@PutMapping("/{categoryId}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<MenuCategoryResponse> updateCategory(
			@PathVariable Long categoryId,
			@Valid @RequestBody UpdateMenuCategoryRequest request) {
		return ApiResponse.success("Menu category updated successfully", menuCategoryService.updateCategory(categoryId, request));
	}

	@PatchMapping("/{categoryId}/activate")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<MenuCategoryResponse> activateCategory(@PathVariable Long categoryId) {
		return ApiResponse.success("Menu category activated successfully", menuCategoryService.activateCategory(categoryId));
	}

	@PatchMapping("/{categoryId}/deactivate")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<MenuCategoryResponse> deactivateCategory(@PathVariable Long categoryId) {
		return ApiResponse.success("Menu category deactivated successfully", menuCategoryService.deactivateCategory(categoryId));
	}
}
