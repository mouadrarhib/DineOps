package com.mouad.dineops.dineOps.restaurant.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mouad.dineops.dineOps.common.response.ApiResponse;
import com.mouad.dineops.dineOps.restaurant.dto.CreateRestaurantRequest;
import com.mouad.dineops.dineOps.restaurant.dto.RestaurantResponse;
import com.mouad.dineops.dineOps.restaurant.dto.UpdateRestaurantRequest;
import com.mouad.dineops.dineOps.restaurant.service.RestaurantService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

	private final RestaurantService restaurantService;

	public RestaurantController(RestaurantService restaurantService) {
		this.restaurantService = restaurantService;
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<RestaurantResponse> createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
		return ApiResponse.success("Restaurant created successfully", restaurantService.createRestaurant(request));
	}

	@GetMapping
	public ApiResponse<List<RestaurantResponse>> listRestaurants() {
		return ApiResponse.success("Restaurants fetched successfully", restaurantService.listRestaurants());
	}

	@GetMapping("/{restaurantId}")
	public ApiResponse<RestaurantResponse> getRestaurantById(@PathVariable Long restaurantId) {
		return ApiResponse.success("Restaurant fetched successfully", restaurantService.getRestaurantById(restaurantId));
	}

	@PutMapping("/{restaurantId}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','RESTAURANT_OWNER')")
	public ApiResponse<RestaurantResponse> updateRestaurant(
			@PathVariable Long restaurantId,
			@Valid @RequestBody UpdateRestaurantRequest request) {
		return ApiResponse.success("Restaurant updated successfully", restaurantService.updateRestaurant(restaurantId, request));
	}
}
