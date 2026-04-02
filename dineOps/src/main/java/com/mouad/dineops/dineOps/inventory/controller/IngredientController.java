package com.mouad.dineops.dineOps.inventory.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.inventory.dto.CreateIngredientRequest;
import com.mouad.dineops.dineOps.inventory.dto.IngredientResponse;
import com.mouad.dineops.dineOps.inventory.service.IngredientService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

	private final IngredientService ingredientService;

	public IngredientController(IngredientService ingredientService) {
		this.ingredientService = ingredientService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER','BRANCH_MANAGER')")
	public ApiResponse<IngredientResponse> createIngredient(@Valid @RequestBody CreateIngredientRequest request) {
		return ApiResponse.success("Ingredient created successfully", ingredientService.createIngredient(request));
	}

	@GetMapping
	public ApiResponse<List<IngredientResponse>> listIngredients(@RequestParam(defaultValue = "false") boolean activeOnly) {
		return ApiResponse.success("Ingredients fetched successfully", ingredientService.listIngredients(activeOnly));
	}
}
