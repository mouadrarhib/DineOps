package com.mouad.dineops.dineOps.restaurant.mapper;

import org.springframework.stereotype.Component;

import com.mouad.dineops.dineOps.common.enums.RestaurantStatus;
import com.mouad.dineops.dineOps.restaurant.dto.CreateRestaurantRequest;
import com.mouad.dineops.dineOps.restaurant.dto.RestaurantResponse;
import com.mouad.dineops.dineOps.restaurant.dto.UpdateRestaurantRequest;
import com.mouad.dineops.dineOps.restaurant.entity.Restaurant;

@Component
public class RestaurantMapper {

	public Restaurant toEntity(CreateRestaurantRequest request) {
		Restaurant restaurant = new Restaurant();
		restaurant.setName(request.name());
		restaurant.setLegalName(request.legalName());
		restaurant.setTaxId(request.taxId());
		restaurant.setEmail(request.email());
		restaurant.setPhone(request.phone());
		restaurant.setStatus(request.status() == null ? RestaurantStatus.ACTIVE : request.status());
		return restaurant;
	}

	public void updateEntity(Restaurant restaurant, UpdateRestaurantRequest request) {
		restaurant.setName(request.name());
		restaurant.setLegalName(request.legalName());
		restaurant.setTaxId(request.taxId());
		restaurant.setEmail(request.email());
		restaurant.setPhone(request.phone());
		restaurant.setStatus(request.status() == null ? RestaurantStatus.ACTIVE : request.status());
	}

	public RestaurantResponse toResponse(Restaurant restaurant) {
		return new RestaurantResponse(
				restaurant.getId(),
				restaurant.getName(),
				restaurant.getLegalName(),
				restaurant.getTaxId(),
				restaurant.getEmail(),
				restaurant.getPhone(),
				restaurant.getStatus(),
				restaurant.getCreatedAt(),
				restaurant.getUpdatedAt());
	}
}
