package com.mouad.dineops.dineOps.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mouad.dineops.dineOps.common.exception.NotFoundException;
import com.mouad.dineops.dineOps.restaurant.dto.CreateRestaurantRequest;
import com.mouad.dineops.dineOps.restaurant.dto.RestaurantResponse;
import com.mouad.dineops.dineOps.restaurant.dto.UpdateRestaurantRequest;
import com.mouad.dineops.dineOps.restaurant.entity.Restaurant;
import com.mouad.dineops.dineOps.restaurant.mapper.RestaurantMapper;
import com.mouad.dineops.dineOps.restaurant.repository.RestaurantRepository;

@Service
public class RestaurantService {

	private final RestaurantRepository restaurantRepository;
	private final RestaurantMapper restaurantMapper;

	public RestaurantService(RestaurantRepository restaurantRepository, RestaurantMapper restaurantMapper) {
		this.restaurantRepository = restaurantRepository;
		this.restaurantMapper = restaurantMapper;
	}

	@Transactional
	public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
		Restaurant restaurant = restaurantMapper.toEntity(request);
		Restaurant saved = restaurantRepository.save(restaurant);
		return restaurantMapper.toResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<RestaurantResponse> listRestaurants() {
		return restaurantRepository.findAll().stream().map(restaurantMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public RestaurantResponse getRestaurantById(Long restaurantId) {
		Restaurant restaurant = findRestaurant(restaurantId);
		return restaurantMapper.toResponse(restaurant);
	}

	@Transactional
	public RestaurantResponse updateRestaurant(Long restaurantId, UpdateRestaurantRequest request) {
		Restaurant restaurant = findRestaurant(restaurantId);
		restaurantMapper.updateEntity(restaurant, request);
		Restaurant saved = restaurantRepository.save(restaurant);
		return restaurantMapper.toResponse(saved);
	}

	private Restaurant findRestaurant(Long restaurantId) {
		return restaurantRepository.findById(restaurantId)
				.orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantId));
	}
}
