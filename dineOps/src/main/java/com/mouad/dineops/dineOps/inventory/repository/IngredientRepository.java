package com.mouad.dineops.dineOps.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mouad.dineops.dineOps.inventory.entity.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

	boolean existsByNameIgnoreCase(String name);

	List<Ingredient> findByActiveTrueOrderByNameAsc();
}
