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
import com.mouad.dineops.dineOps.menu.dto.CreateMenuItemRequest;
import com.mouad.dineops.dineOps.menu.dto.MenuItemResponse;
import com.mouad.dineops.dineOps.menu.dto.UpdateMenuItemRequest;
import com.mouad.dineops.dineOps.menu.service.MenuItemService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

	private final MenuItemService menuItemService;

	public MenuItemController(MenuItemService menuItemService) {
		this.menuItemService = menuItemService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<MenuItemResponse> createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {
		return ApiResponse.success("Menu item created successfully", menuItemService.createMenuItem(request));
	}

	@GetMapping
	public ApiResponse<PagedResponse<MenuItemResponse>> listMenuItemsByBranch(
			@RequestParam Long branchId,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) Boolean available,
			@RequestParam(required = false) String search,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ApiResponse.success(
				"Menu items fetched successfully",
				menuItemService.listByBranch(branchId, active, available, search, page, size));
	}

	@GetMapping("/{menuItemId}")
	public ApiResponse<MenuItemResponse> getMenuItemDetails(@PathVariable Long menuItemId) {
		return ApiResponse.success("Menu item fetched successfully", menuItemService.getDetails(menuItemId));
	}

	@PutMapping("/{menuItemId}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<MenuItemResponse> updateMenuItem(
			@PathVariable Long menuItemId,
			@Valid @RequestBody UpdateMenuItemRequest request) {
		return ApiResponse.success("Menu item updated successfully", menuItemService.updateMenuItem(menuItemId, request));
	}

	@PatchMapping("/{menuItemId}/availability")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<MenuItemResponse> toggleAvailability(
			@PathVariable Long menuItemId,
			@RequestParam boolean available) {
		return ApiResponse.success(
				"Menu item availability updated successfully",
				menuItemService.toggleAvailability(menuItemId, available));
	}
}
