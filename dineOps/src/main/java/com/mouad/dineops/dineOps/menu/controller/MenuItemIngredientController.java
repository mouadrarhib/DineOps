package com.mouad.dineops.dineOps.menu.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.menu.dto.LinkMenuItemIngredientRequest;
import com.mouad.dineops.dineOps.menu.dto.MenuItemIngredientResponse;
import com.mouad.dineops.dineOps.menu.service.MenuItemIngredientService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/menu-item-ingredients")
public class MenuItemIngredientController {

	private final MenuItemIngredientService menuItemIngredientService;

	public MenuItemIngredientController(MenuItemIngredientService menuItemIngredientService) {
		this.menuItemIngredientService = menuItemIngredientService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<MenuItemIngredientResponse> linkIngredientToMenuItem(
			@Valid @RequestBody LinkMenuItemIngredientRequest request) {
		return ApiResponse.success(
				"Ingredient linked to menu item successfully",
				menuItemIngredientService.linkIngredientToMenuItem(request));
	}
}
