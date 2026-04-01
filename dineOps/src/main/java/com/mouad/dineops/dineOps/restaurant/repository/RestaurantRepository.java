package com.mouad.dineops.dineOps.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.restaurant.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
